package net.yoshinorin.qualtet.http.routes.v1

import cats.data.EitherT
import cats.effect.Concurrent
import cats.implicits.*
import cats.Monad
import org.http4s.headers.Allow
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.dsl.Http4sDsl
import net.yoshinorin.qualtet.domains.{Limit, Page, PaginationRequestModel}
import net.yoshinorin.qualtet.domains.errors.DomainError
import net.yoshinorin.qualtet.domains.feeds.FeedService
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

import scala.annotation.nowarn

class FeedRoute[F[_]: Concurrent, G[_]: Monad @nowarn](
  feedService: FeedService[F, G]
)(using loggerFactory: Log4CatsLoggerFactory[F]) {

  private given dsl: Http4sDsl[F] = Http4sDsl[F]
  import dsl.*

  given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[F] = HttpRoutes.of[F] { implicit r =>
    (r match {
      case request @ GET -> Root / name => this.get(name)
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ => MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[F].asResponse)
  }

  // TODO: `name`` parameter will be used in future implementation
  private[http] def get(@nowarn name: String): Request[F] ?=> F[Response[F]] = {
    (for {
      feeds <- EitherT(feedService.get(PaginationRequestModel(Option(Page(1)), Option(Limit(5)), None)))
    } yield feeds).value.flatMap {
      case Right(feeds) => feeds.asResponse(Ok)
      case Left(error: DomainError) => error.asResponse
    }
  }

}
