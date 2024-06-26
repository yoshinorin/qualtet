package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.*
import cats.Monad
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.domains.search.SearchService
import net.yoshinorin.qualtet.syntax.*

class SearchRoute[F[_]: Monad](
  searchService: SearchService[F]
) {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { implicit r =>
    (r match {
      case request @ GET -> _ => this.search(request.uri.query.multiParams)
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ => MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  private[http] def search(query: Map[String, List[String]]): IO[Response[IO]] = {
    logger.info(s"search query: ${query}")
    (for {
      searchResult <- searchService.search(query)
      response <- Ok(searchResult.asJson, `Content-Type`(MediaType.application.json))
    } yield response)
  }

}
