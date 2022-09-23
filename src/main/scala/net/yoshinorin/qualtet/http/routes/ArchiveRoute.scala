package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import net.yoshinorin.qualtet.domains.archives.ArchiveService
import net.yoshinorin.qualtet.domains.archives.ResponseArchive._
import net.yoshinorin.qualtet.http.ResponseHandler

import cats.effect.unsafe.implicits.global

class ArchiveRoute(
  archiveService: ArchiveService
) extends ResponseHandler {

  def route: Route = {
    pathPrefix("archives") {
      pathEndOrSingleSlash {
        get {
          onSuccess(archiveService.get.unsafeToFuture()) { result => httpResponseWithJsoniter(OK, result) }
        }
      }
    }
  }

}
