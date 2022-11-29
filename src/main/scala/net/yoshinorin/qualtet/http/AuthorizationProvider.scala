package net.yoshinorin.qualtet.http

import cats.effect.IO
import cats.implicits._
import cats.data.Kleisli
import cats.data.OptionT
import org.http4s.server._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.dsl.impl.Auth
import org.http4s.headers.Authorization
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.auth.AuthService
import scala.util.control.NonFatal
import scala.reflect.internal.FatalError
import net.yoshinorin.qualtet.syntax._

class AuthorizationProvider(
  authService: AuthService
) {

  def authUser: Kleisli[IO, Request[IO], Either[String, ResponseAuthor]] =
    Kleisli({ request =>
      try {
        for {
          auth <- IO(request.headers.get[Authorization].orThrow(new RuntimeException("TODO: Authorization header is none")))
          author <- authService.findAuthorFromJwtString(auth.credentials.renderString.replace("Bearer ", ""))
        } yield author match {
          // TODO: logging
          case None => Left("TODO")
          case Some(value) => Right(value)
        }
      } catch {
        // TODO: error handling
        case NonFatal(nf) =>
          println(nf)
          IO.raiseError(nf)
        case _ =>
          println("====fatalError")
          IO.raiseError(new RuntimeException("fatal error"))
      }

    })

  def onFailure: AuthedRoutes[String, IO] = Kleisli(req => OptionT.liftF(Forbidden(req.context)))
  def authenticate: AuthMiddleware[IO, ResponseAuthor] = AuthMiddleware(authUser, onFailure)
}
