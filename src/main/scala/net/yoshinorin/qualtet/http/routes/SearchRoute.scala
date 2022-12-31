package net.yoshinorin.qualtet.http.routes

import cats.effect._
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.domains.search.SearchService
import net.yoshinorin.qualtet.syntax._

class SearchRoute(
  searchService: SearchService
) {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  def search(query: Map[String, List[String]]): IO[Response[IO]] = {
    logger.info(s"search query: ${query}")
    (for {
      searchResult <- searchService.search(query)
      response <- Ok(searchResult.asJson, `Content-Type`(MediaType.application.json))
    } yield response).handleErrorWith(_.logWithStackTrace.andResponse)
  }

}
