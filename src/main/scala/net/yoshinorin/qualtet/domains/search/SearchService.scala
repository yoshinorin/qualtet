package net.yoshinorin.qualtet.domains.search

import net.yoshinorin.qualtet.config.SearchConfig
import net.yoshinorin.qualtet.domains.errors.{InvalidSearchConditions, ProblemDetailsError}
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.types.Points

import cats.Monad
import cats.data.{ContT, Validated, ValidatedNec}
import cats.implicits.*
import scala.annotation.tailrec

class SearchService[F[_]: Monad, G[_]: Monad](
  searchConfig: SearchConfig,
  searchRepository: SearchRepository[G]
)(using executer: Executer[F, G]) {

  def cont(query: List[String]): ContT[G, Seq[(Int, SearchResponseModel)], Seq[(Int, SearchResponseModel)]] = {
    ContT.apply[G, Seq[(Int, SearchResponseModel)], Seq[(Int, SearchResponseModel)]] { _ =>
      searchRepository.search(query).map { x =>
        x.map { case (count, search) =>
          (count, SearchResponseModel(search.path, search.title, search.content, search.publishedAt, search.updatedAt))
        }
      }
    }
  }

  private[search] def extractQueryStringsFromQuery(query: Map[String, List[String]]): List[String] = query.getOrElse("q", List()).map(_.trim.toLower)

  private def validateWord(q: String): ValidatedNec[ProblemDetailsError, String] = {
    val noIgnoreChars = Validated.condNec(
      !q.hasIgnoreChars,
      q,
      ProblemDetailsError(
        code = "INVALID_CHARS_INCLUDED",
        message = s"Contains unusable chars in ${q}"
      )
    )

    val notTooShort = Validated.condNec(
      q.length >= searchConfig.minWordLength,
      q,
      ProblemDetailsError(
        code = "SEARCH_CHAR_LENGTH_TOO_SHORT",
        message = s"${q} is too short. You must be more than ${searchConfig.minWordLength} chars in one word."
      )
    )

    val notTooLong = Validated.condNec(
      q.length <= searchConfig.maxWordLength,
      q,
      ProblemDetailsError(
        code = "SEARCH_CHAR_LENGTH_TOO_LONG",
        message = s"${q} is too long. You must be less than ${searchConfig.maxWordLength} chars in one word."
      )
    )

    (noIgnoreChars, notTooShort, notTooLong).mapN((_, _, _) => q)
  }

  // TODO: move constant values
  private[search] def accumurateQueryStringsErrors(queryStrings: List[String]): ValidatedNec[ProblemDetailsError, List[String]] = {
    val notEmpty = Validated.condNec(
      queryStrings.nonEmpty,
      queryStrings,
      ProblemDetailsError(
        code = "SEARCH_QUERY_REQUIRED",
        message = "Search query required."
      )
    )

    val notTooManySearchWords = Validated.condNec(
      queryStrings.sizeIs <= searchConfig.maxWords,
      queryStrings,
      ProblemDetailsError(
        code = "TOO_MANY_SEARCH_WORDS",
        message = s"Search words must be less than ${searchConfig.maxWords}. You specified ${queryStrings.size}."
      )
    )

    (queryStrings.traverse(validateWord), notEmpty, notTooManySearchWords).mapN((qs, _, _) => qs)
  }

  private[search] def positions(words: List[String], sentence: String): Seq[Points] = {
    words.flatMap(w => sentence.position(w).map(x => x.expand(8, 24, sentence.length)))
  }

  @tailrec
  private def calcSubStrRanges(idxes: Seq[Points], acc: Seq[Points] = Nil): Seq[Points] = {
    idxes.headOption match {
      case None => acc
      case Some(h) =>
        val t = idxes.tail
        idxes.lift(1) match {
          case Some(n) if (n._1 > h._2) => calcSubStrRanges(t, Seq(h, n))
          case Some(n) if (acc.sizeIs > 1 && acc.tail.head._1 == h._1) => calcSubStrRanges(t, acc.dropRight(1) :+ (h._1, n._2))
          case Some(n) => calcSubStrRanges(t, acc :+ (h._1, n._2))
          case None if (acc === Nil) => calcSubStrRanges(t, Seq(h))
          case _ => acc
        }
    }
  }

  @tailrec
  private def substrRecursively(sentence: String, idxes: Seq[Points], current: Int = 0, acc: String = ""): String = {
    if (idxes.sizeIs > current) {
      val currentIdx = idxes(current)
      substrRecursively(
        sentence,
        idxes,
        current + 1,
        acc + sentence.slice(currentIdx._1, currentIdx._2) + "...    "
      )
    } else {
      acc
    }
  }

  def search(query: Map[String, List[String]]): F[SearchWithCountResponseModel] = {
    val queryStrings = extractQueryStringsFromQuery(query)
    for {
      validated <- Monad[F].pure(accumurateQueryStringsErrors(queryStrings))
      _ <- Monad[F].pure(validated match {
        case Validated.Invalid(errors) =>
          throw new InvalidSearchConditions(detail = "Invalid search conditions. Please see error details.", errors = Some(errors.toList))
        case Validated.Valid(_) => ()
      })
      searchResult <- executer.transact(cont(queryStrings))
    } yield
      if (searchResult.nonEmpty) {
        val r = searchResult.map { x =>
          val strippedContent = x._2.content.stripHtmlTags.filterIgnoreChars.toLower
          x._2.copy(content = substrRecursively(strippedContent, calcSubStrRanges(positions(queryStrings, strippedContent))))
        }
        SearchWithCountResponseModel(searchResult.map(_._1).headOption.getOrElse(0), r)
      } else {
        SearchWithCountResponseModel(0, Seq())
      }
  }

}
