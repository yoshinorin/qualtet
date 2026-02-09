package net.yoshinorin.qualtet.http.routes.v1

import cats.data.EitherT
import cats.effect.Concurrent
import cats.implicits.*
import cats.Monad
import org.http4s.headers.Allow
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.dsl.Http4sDsl
import net.yoshinorin.qualtet.domains.errors.DomainError
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.domains.PaginationRequestModel
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

import scala.annotation.nowarn

class ArticleRoute[F[_]: Concurrent, G[_]: Monad @nowarn](
  articleService: ArticleService[F, G]
)(using loggerFactory: Log4CatsLoggerFactory[F]) {

  private given dsl: Http4sDsl[F] = Http4sDsl[F]
  import dsl.*

  given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  private[http] def index: HttpRoutes[F] = HttpRoutes.of[F] { implicit r =>
    (r match {
      case request @ GET -> Root =>
        val p = request.uri.query.params.asPagination
        this.get(p)
      case request @ OPTIONS -> Root => NoContent()
      case request @ _ =>
        MethodNotAllowed(Allow(Set(GET)))
    }).handleErrorWith(_.logWithStackTrace[F].asResponse)
  }

  // articles?page=n&limit=m
  private[http] def get(p: PaginationRequestModel): Request[F] ?=> F[Response[F]] = {
    (for {
      maybeArticles <- EitherT(articleService.getWithCount(p))
    } yield maybeArticles).value.flatMap {
      case Right(articles) => articles.asResponse(Ok)
      case Left(error: DomainError) => error.asResponse
    }
  }

}
