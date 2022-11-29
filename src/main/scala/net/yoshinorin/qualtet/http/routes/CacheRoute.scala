package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import cats.implicits._
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
import org.http4s.dsl.impl.Auth

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

  // とりあえず動く（Unautorizedにはなる）コード
  val authUserHeader: Kleisli[IO, Request[IO], Either[String, ResponseAuthor]] =
    Kleisli({ request =>
      val c = for {
        t <- request.headers.get[Authorization].toRight("todo1")
      } yield t
      for {
        // TODO: ここからBearerだけ抜き出す
        a <- authService.findAuthorFromJwtString(c.toString())
      } yield a match {
        case None => Left("todo2")
        case Some(v) => Right(v)
      }
    })

  val onFailure: AuthedRoutes[String, IO] = Kleisli(req => OptionT.liftF(Forbidden(req.context)))
  val authMiddleware: AuthMiddleware[IO, ResponseAuthor] = AuthMiddleware(authUserHeader, onFailure)
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
