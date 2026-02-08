package net.yoshinorin.qualtet.domains.search

import cats.data.ContT
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.config.SearchConfig
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.domains.errors.InvalidSearchConditions
import net.yoshinorin.qualtet.domains.errors.ProblemDetailsError
import net.yoshinorin.qualtet.types.Points
import net.yoshinorin.qualtet.syntax.*

import scala.annotation.tailrec

class SearchService[G[_]: Monad, F[_]: Monad](
  searchConfig: SearchConfig,
  searchRepository: SearchRepository[G]
)(using executer: Executer[G, F]) {

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

  // TODO: move constant values
  private[search] def accumurateQueryStringsErrors(queryStrings: List[String]): Seq[ProblemDetailsError] = {
    val validator: (Boolean, ProblemDetailsError) => Option[ProblemDetailsError] = (cond, err) => { if cond then Some(err) else None }
    val isEmptyError = validator(
      queryStrings.isEmpty,
      ProblemDetailsError(
        code = "SEARCH_QUERY_REQUIRED",
        message = "Search query required."
      )
    )

    val tooManySearchWordsError = validator(
      queryStrings.sizeIs > searchConfig.maxWords,
      ProblemDetailsError(
        code = "TOO_MANY_SEARCH_WORDS",
        message = s"Search words must be less than ${searchConfig.maxWords}. You specified ${queryStrings.size}."
      )
    )

    val errorsByWords = queryStrings.map { q =>
      List(
        validator(
          q.hasIgnoreChars,
          ProblemDetailsError(
            code = "INVALID_CHARS_INCLUDED",
            message = s"Contains unusable chars in ${q}"
          )
        ),
        validator(
          q.length < searchConfig.minWordLength,
          ProblemDetailsError(
            code = "SEARCH_CHAR_LENGTH_TOO_SHORT",
            message = s"${q} is too short. You must be more than ${searchConfig.minWordLength} chars in one word."
          )
        ),
        validator(
          q.length > searchConfig.maxWordLength,
          ProblemDetailsError(
            code = "SEARCH_CHAR_LENGTH_TOO_LONG",
            message = s"${q} is too long. You must be less than ${searchConfig.maxWordLength} chars in one word."
          )
        )
      )
    }
    (errorsByWords.flatten :+ isEmptyError :+ tooManySearchWordsError).collect { case Some(x) => x }
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
      accErrors <- Monad[F].pure(accumurateQueryStringsErrors(queryStrings))
      _ <- Monad[F].pure(if (accErrors.nonEmpty) {
        throw new InvalidSearchConditions(detail = "Invalid search conditions. Please see error details.", errors = Some(accErrors))
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
