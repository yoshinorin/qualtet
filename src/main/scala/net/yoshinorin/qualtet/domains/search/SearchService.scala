package net.yoshinorin.qualtet.domains.search

import cats.effect.IO
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.actions.Action.*
import net.yoshinorin.qualtet.actions.{Action, Continue}
import net.yoshinorin.qualtet.config.SearchConfig
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.message.Fail.UnprocessableEntity
import net.yoshinorin.qualtet.http.{Error => Err}
import net.yoshinorin.qualtet.types.Points
import net.yoshinorin.qualtet.syntax.*

import scala.annotation.tailrec

class SearchService[F[_]: Monad](
  searchConfig: SearchConfig,
  searchRepository: SearchRepository[F]
)(using executer: Executer[F, IO]) {

  def actions(query: List[String]): Action[Seq[(Int, ResponseSearch)]] = {
    Continue(searchRepository.search(query), Action.done[Seq[(Int, ResponseSearch)]])
  }

  private[search] def extractQueryStringsFromQuery(query: Map[String, List[String]]): List[String] = query.getOrElse("q", List()).map(_.trim.toLower)

  // TODO: move constant values
  private[search] def accumurateQueryStringsErrors(queryStrings: List[String]): Seq[Err] = {
    val validator: (Boolean, Err) => Option[Err] = (cond, err) => { if cond then Some(err) else None }
    val isEmptyError = validator(
      queryStrings.isEmpty,
      Err(
        code = "SEARCH_QUERY_REQUIRED",
        message = "Search query required."
      )
    )

    val tooManySearchWordsError = validator(
      queryStrings.sizeIs > searchConfig.maxWords,
      Err(
        code = "TOO_MANY_SEARCH_WORDS",
        message = s"Search words must be less than ${searchConfig.maxWords}. You specified ${queryStrings.size}."
      )
    )

    val errorsByWords = queryStrings.map { q =>
      List(
        validator(
          q.hasIgnoreChars,
          Err(
            code = "INVALID_CHARS_INCLUDED",
            message = s"Contains unusable chars in ${q}"
          )
        ),
        validator(
          q.length < searchConfig.minWordLength,
          Err(
            code = "SEARCH_CHAR_LENGTH_TOO_SHORT",
            message = s"${q} is too short. You must be more than ${searchConfig.minWordLength} chars in one word."
          )
        ),
        validator(
          q.length > searchConfig.maxWordLength,
          Err(
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

  def search(query: Map[String, List[String]]): IO[ResponseSearchWithCount] = {
    val queryStrings = extractQueryStringsFromQuery(query)
    for {
      accErrors <- IO(accumurateQueryStringsErrors(queryStrings))
      _ <- IO(if (accErrors.nonEmpty) {
        throw new UnprocessableEntity(detail = "Invalid search conditions. Please see error details.", errors = Some(accErrors))
      })
      searchResult <- executer.transact(actions(queryStrings))
    } yield
      if (searchResult.nonEmpty) {
        val r = searchResult.map { x =>
          val stripedContent = x._2.content.stripHtmlTags.filterIgnoreChars.toLower
          x._2.copy(content = substrRecursively(stripedContent, calcSubStrRanges(positions(queryStrings, stripedContent))))
        }
        ResponseSearchWithCount(searchResult.map(_._1).headOption.getOrElse(0), r)
      } else {
        ResponseSearchWithCount(0, Seq())
      }
  }

}
