package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import cats.Monad
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.http.request.query.{ArticlesPagination, Limit, Order, Page}
import net.yoshinorin.qualtet.syntax.*

class ArticleRoute[F[_]: Monad](
  articleService: ArticleService[F]
) {

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { implicit r =>
    (r match {
      case request @ GET -> Root =>
        val q = request.uri.query.params.asPagination
        this.get(q.page, q.limit, q.order)
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ =>
        MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  // articles?page=n&limit=m
  private[http] def get(page: Option[Page], limit: Option[Limit], order: Option[Order]): IO[Response[IO]] = {
    (for {
      articles <- articleService.getWithCount(ArticlesPagination(page, limit, order))
      response <- Ok(articles.asJson, `Content-Type`(MediaType.application.json))
    } yield response)
  }

}
