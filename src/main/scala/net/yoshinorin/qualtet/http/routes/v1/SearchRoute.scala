package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.Concurrent
import cats.implicits.*
import cats.Monad
import org.http4s.headers.Allow
import org.http4s.{HttpRoutes, Response}
import org.http4s.dsl.Http4sDsl
import net.yoshinorin.qualtet.domains.search.SearchService
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

import scala.annotation.nowarn

class SearchRoute[F[_]: Concurrent, G[_]: Monad @nowarn](
  searchService: SearchService[F, G]
)(using loggerFactory: Log4CatsLoggerFactory[F]) {

  private given dsl: Http4sDsl[F] = Http4sDsl[F]
  import dsl.*

  given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[F] = HttpRoutes.of[F] { implicit r =>
    (r match {
      case request @ GET -> _ => this.search(request.uri.query.multiParams)
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ => MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[F].asResponse)
  }

  private[http] def search(query: Map[String, List[String]]): F[Response[F]] = {
    (for {
      _ <- logger.info(s"search query: ${query}")
      searchResult <- searchService.search(query)
      response <- searchResult.asResponse(Ok)
    } yield response)
  }

}
