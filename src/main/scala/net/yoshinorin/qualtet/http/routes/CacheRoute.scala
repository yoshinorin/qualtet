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

class CacheRoute(
  authService: AuthService,
  cacheService: CacheService
) {

  /* No-Auth: Works well.
  def route: HttpRoutes[IO] = HttpRoutes.of[IO] {
    { case DELETE -> Root =>
        for {
          _ <- cacheService.invalidateAll()
          response <- NoContent()
        } yield response
    }
  }
  */


  val onFailure: AuthedRoutes[String, IO] =
    Kleisli(req => OptionT.liftF(Forbidden(req.context)))

  val authMiddleware = AuthMiddleware(authUserHeader, onFailure)

  val authUserHeader: Kleisli[IO, Request[IO], Either[String, ResponseAuthor]] = Kleisli({ request =>
    val h = request.headers.get[Authorization]
    // https://http4s.org/v1/docs/auth.html#authorization-header
    for {
      author <- authService.findAuthorFromJwtString(h.toString())
    } yield author match {
      case None => Left("Unauthorized")
      case Some(a) => Right(a)
    }
  })

  def route: HttpRoutes[IO] = authMiddleware(authedRoutes)
  // caches
  val authedRoutes: AuthedRoutes[ResponseAuthor, IO] =
    AuthedRoutes.of { case DELETE -> Root as author =>
      for {
        _ <- cacheService.invalidateAll()
        response <- NoContent()
      } yield response
    }
}
