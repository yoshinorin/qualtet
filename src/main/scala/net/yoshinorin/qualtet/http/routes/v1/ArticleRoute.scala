package net.yoshinorin.qualtet.http.routes.v1

import cats.effect.IO
import cats.Monad
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.http.{ArticlesQueryParameter, MethodNotAllowedSupport}
import net.yoshinorin.qualtet.syntax.*

class ArticleRoute[F[_]: Monad](
  articleService: ArticleService[F]
) extends MethodNotAllowedSupport {

  private[http] def index: HttpRoutes[IO] = HttpRoutes.of[IO] { r =>
    implicit val x = r
    (r match {
      case request @ GET -> Root =>
        val q = request.uri.query.params.asRequestQueryParamater
        this.get(q.page, q.limit)
      case request @ OPTIONS -> Root => NoContent() // TODO: return `Allow Header`
      case request @ _ =>
        methodNotAllowed(request, Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  // articles?page=n&limit=m
  private[http] def get(page: Option[Int], limit: Option[Int]): IO[Response[IO]] = {
    (for {
      articles <- articleService.getWithCount(ArticlesQueryParameter(page, limit))
      response <- Ok(articles.asJson, `Content-Type`(MediaType.application.json))
    } yield response)
  }

}
