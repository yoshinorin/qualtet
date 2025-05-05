package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.*
import cats.Monad
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.search.SearchService
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

class SearchRoute[F[_]: Monad](
  searchService: SearchService[F]
)(using loggerFactory: Log4CatsLoggerFactory[IO]) {

  given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { implicit r =>
    (r match {
      case request @ GET -> _ => this.search(request.uri.query.multiParams)
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ => MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  private[http] def search(query: Map[String, List[String]]): IO[Response[IO]] = {
    (for {
      _ <- logger.info(s"search query: ${query}")
      searchResult <- searchService.search(query)
      response <- Ok(searchResult.asJson, `Content-Type`(MediaType.application.json))
    } yield response)
  }

}
