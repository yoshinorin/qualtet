package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import net.yoshinorin.qualtet.domains.archives.ArchiveService
import net.yoshinorin.qualtet.http.{RequestDecoder, ResponseHandler}

class ArchiveRoute(
  archiveService: ArchiveService
) extends RequestDecoder
    with ResponseHandler {

  def route: Route = {
    pathPrefix("archives") {
      pathEndOrSingleSlash {
        get {
          onSuccess(archiveService.get.unsafeToFuture()) { result => httpResponse(OK, result) }
        }
      }
    }
  }

}
