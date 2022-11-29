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
import scala.util.control.NonFatal
import scala.reflect.internal.FatalError
import net.yoshinorin.qualtet.syntax._

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

  val authUserHeader: Kleisli[IO, Request[IO], Either[String, ResponseAuthor]] =
    Kleisli({ request =>
      try {
        for {
          auth <- IO(request.headers.get[Authorization].orThrow(new RuntimeException("TODO: Authorization header is none")))
          _ = println(s"Authorization header is: ${auth}")
          _ = println(auth.credentials.renderString.replace("Bearer ", ""))
          // TODO: avoid using get
          author <- authService.findAuthorFromJwtString(auth.credentials.renderString.replace("Bearer ", ""))
          _ = println(author)
        } yield author match {
          case None => Left("TODO")
          case Some(value) => Right(value)
        }
      } catch {
        case NonFatal(nf) =>
          println(nf)
          IO.raiseError(nf)
        case _ => println("====fatalError")
          IO.raiseError(new RuntimeException("fatal error"))
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
