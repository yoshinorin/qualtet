package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import cats.Monad
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.domains.PaginationRequestModel
import net.yoshinorin.qualtet.syntax.*

class ArticleRoute[F[_]: Monad](
  articleService: ArticleService[F]
) {

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { implicit r =>
    (r match {
      case request @ GET -> Root =>
        val p = request.uri.query.params.asPagination
        this.get(p)
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ =>
        MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  // articles?page=n&limit=m
  private[http] def get(p: PaginationRequestModel): IO[Response[IO]] = {
    (for {
      articles <- articleService.getWithCount(p)
      response <- Ok(articles.asJson, `Content-Type`(MediaType.application.json))
    } yield response)
  }

}
