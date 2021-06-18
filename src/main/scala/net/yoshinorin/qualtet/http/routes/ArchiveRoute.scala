package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import io.circe.syntax._
import net.yoshinorin.qualtet.domains.services.ArchiveService
import net.yoshinorin.qualtet.http.RequestDecoder

class ArchiveRoute(
  archiveService: ArchiveService
) extends RequestDecoder {

  def route: Route = {
    pathPrefix("archives") {
      pathEndOrSingleSlash {
        get {
          onSuccess(archiveService.get.unsafeToFuture()) { result =>
            complete(HttpResponse(OK, entity = HttpEntity(ContentTypes.`application/json`, s"${result.asJson}")))
          }
        }
      }
    }
  }

}
