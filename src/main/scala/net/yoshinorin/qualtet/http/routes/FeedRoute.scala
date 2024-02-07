package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.Monad
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import net.yoshinorin.qualtet.domains.feeds.FeedService
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.http.MethodNotAllowedSupport

class FeedRoute[F[_]: Monad](
  feedService: FeedService[F]
) extends MethodNotAllowedSupport {

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { r =>
    (r match {
      case request @ GET -> Root / name => this.get(name)
      case request @ OPTIONS -> Root => NoContent() // TODO: return `Allow Header`
      case request @ _ =>
        methodNotAllowed(request, Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  private[http] def get(name: String): IO[Response[IO]] = {
    for {
      feeds <- feedService.get(ArticlesQueryParameter(1, 5))
      response <- Ok(feeds.asJson, `Content-Type`(MediaType.application.json))
    } yield response
  }

}
