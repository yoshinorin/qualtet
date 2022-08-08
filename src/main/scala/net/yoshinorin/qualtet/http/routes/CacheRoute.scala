package net.yoshinorin.qualtet.http.routes

import cats.effect.IO
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import net.yoshinorin.qualtet.cache.CacheService
import net.yoshinorin.qualtet.auth.AuthService
import net.yoshinorin.qualtet.http.{Authentication, ResponseHandler}

class CacheRoute(
  authService: AuthService,
  cacheService: CacheService
) extends Authentication(authService)
    with ResponseHandler {

  def route: Route = {
    pathPrefix("caches") {
      pathEndOrSingleSlash {
        delete {
          authenticate { _ =>
            onSuccess(
              cacheService
                .invalidateAll()
                .handleErrorWith { e => IO.pure(e) }
                .unsafeToFuture()
            ) { _ => httpResponse(NoContent) }
          }
        }
      }
    }
  }

}
