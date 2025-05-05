package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import cats.Monad
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.{Limit, Page, PaginationRequestModel}
import net.yoshinorin.qualtet.domains.feeds.FeedService
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

class FeedRoute[F[_]: Monad](
  feedService: FeedService[F]
)(using loggerFactory: Log4CatsLoggerFactory[IO]) {

  given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { implicit r =>
    (r match {
      case request @ GET -> Root / name => this.get(name)
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ => MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  private[http] def get(name: String): IO[Response[IO]] = {
    for {
      feeds <- feedService.get(PaginationRequestModel(Option(Page(1)), Option(Limit(5)), None))
      response <- Ok(feeds.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }

}
