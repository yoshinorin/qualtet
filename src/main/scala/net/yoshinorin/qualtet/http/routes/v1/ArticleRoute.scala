package net.yoshinorin.qualtet.http.routes.v1

import cats.data.EitherT
import cats.effect.IO
import cats.Monad
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{HttpRoutes, MediaType, Request, Response}
import org.http4s.dsl.io.*
import net.yoshinorin.qualtet.domains.errors.DomainError
import net.yoshinorin.qualtet.domains.articles.ArticleService
import net.yoshinorin.qualtet.domains.PaginationRequestModel
import net.yoshinorin.qualtet.syntax.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

class ArticleRoute[F[_]: Monad](
  articleService: ArticleService[F]
)(using loggerFactory: Log4CatsLoggerFactory[IO]) {

  given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

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
  private[http] def get(p: PaginationRequestModel): Request[IO] ?=> IO[Response[IO]] = {
    (for {
      maybeArticles <- EitherT(articleService.getWithCount(p))
    } yield maybeArticles).value.flatMap {
      case Right(articles) => Ok(articles.asJson, `Content-Type`(MediaType.application.json))
      case Left(error: DomainError) => error.asResponse
    }
  }

}
