package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.data.Kleisli
import cats.data.OptionT
import org.http4s.HttpRoutes
import org.http4s.server._
import org.http4s._
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import org.http4s.headers.Authorization
import org.http4s.dsl.io._
import net.yoshinorin.qualtet.cache.CacheService
import net.yoshinorin.qualtet.auth.AuthService
import net.yoshinorin.qualtet.http.{Authentication, ResponseHandler}

class CacheRoute(
  authService: AuthService,
  cacheService: CacheService
) extends Authentication(authService)
    with ResponseHandler {

  val onFailure: AuthedRoutes[String, IO] =
    Kleisli(req => OptionT.liftF(Forbidden(req.context)))

  val authMiddleware = AuthMiddleware(authUserHeader, onFailure)

  val authUserHeader: Kleisli[IO, Request[IO], Either[String, ResponseAuthor]] = Kleisli({ request =>
    val h = request.headers.get[Authorization]
    for {
      author <- authService.findAuthorFromJwtString(h.toString())
    } yield author match {
      case None => Left("Unauthorized")
      case Some(a) => Right(a)
    }
  })

  def route: HttpRoutes[IO] = authMiddleware(authedRoutes)

  val authedRoutes: AuthedRoutes[ResponseAuthor, IO] =
    AuthedRoutes.of {
      // TODO: DRY
      case DELETE -> Root / "caches" as author =>
        for {
          _ <- cacheService.invalidateAll()
          response <- NoContent()
        } yield response
      case DELETE -> Root / "caches" / "" as author =>
        for {
          _ <- cacheService.invalidateAll()
          response <- NoContent()
        } yield response
    }

}
