package net.yoshinorin.qualtet.domains.search

import cats.effect.IO
import cats.Monad
import doobie.util.transactor.Transactor.Aux
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.actions.Action._
import net.yoshinorin.qualtet.actions.{Action, Continue}
import net.yoshinorin.qualtet.config.SearchConfig
import net.yoshinorin.qualtet.domains.contentTypes.{ContentTypeId, ContentTypeService}
import net.yoshinorin.qualtet.message.Fail.NotFound
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import net.yoshinorin.qualtet.infrastructure.db.DataBaseContext
import net.yoshinorin.qualtet.message.Fail.UnprocessableEntity
import net.yoshinorin.qualtet.types.Points
import net.yoshinorin.qualtet.syntax._

import scala.util.Try
import scala.annotation.tailrec

class SearchService[M[_]: Monad](
  searchConfig: SearchConfig,
  searchRepository: SearchRepository[M]
)(dbContext: DataBaseContext[Aux[IO, Unit]]) {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  def actions(query: List[String]): Action[Seq[(Int, ResponseSearch)]] = {
    Continue(searchRepository.search(query), Action.done[Seq[(Int, ResponseSearch)]])
  }

  // TODO: move constant values
  private[search] def validateAndExtractQueryString(query: Map[String, List[String]]): List[String] = {
    val qs = query.getOrElse("q", List()).map(_.trim.toLower)
    val validator: (Boolean, String) => Unit = (b, s) => { if b then throw new UnprocessableEntity(s) else () }
    validator(qs.isEmpty, "SEARCH_QUERY_REQUIRED")
    validator(qs.sizeIs > searchConfig.maxWords, "TOO_MANY_SEARCH_WORDS")
    qs.map { q =>
      validator(q.hasIgnoreChars, "INVALID_CHARS_INCLUDED")
      validator(q.length < searchConfig.minWordLength, "SEARCH_CHAR_LENGTH_TOO_SHORT")
      validator(q.length > searchConfig.maxWordLength, "SEARCH_CHAR_LENGTH_TOO_LONG")
      q
    }
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
          case None if (acc == Nil) => calcSubStrRanges(t, Seq(h))
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
    for {
      qs <- IO(validateAndExtractQueryString(query))
      searchResult <- actions(qs).perform.andTransact(dbContext)
    } yield
      if (searchResult.nonEmpty) {
        val r = searchResult.map { x =>
          val stripedContent = x._2.content.stripHtmlTags.filterIgnoreChars.toLower
          x._2.copy(content = substrRecursively(stripedContent, calcSubStrRanges(positions(qs, stripedContent))))
        }
        ResponseSearchWithCount(searchResult.map(_._1).headOption.getOrElse(0), r)
      } else {
        ResponseSearchWithCount(0, Seq())
      }
  }

}
