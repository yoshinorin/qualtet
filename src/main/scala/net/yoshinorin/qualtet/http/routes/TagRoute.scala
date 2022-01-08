package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.circe.syntax.EncoderOps
import net.yoshinorin.qualtet.domains.models.tags.{ResponseTag, TagId}
import net.yoshinorin.qualtet.domains.services.{ArticleService, TagService}
import net.yoshinorin.qualtet.http.{ArticlesQueryParameter, ResponseHandler}

class TagRoute(
  tagService: TagService,
  articleService: ArticleService
) extends ResponseHandler {

  def route: Route = {
    pathPrefix("tags") {
      pathEndOrSingleSlash {
        get {
          onSuccess(tagService.getAll.unsafeToFuture()) { result =>
            complete(HttpResponse(OK, entity = HttpEntity(ContentTypes.`application/json`, s"${result.asJson}")))
          }
        }
      } ~ {
        pathPrefix(".+".r) { tagId =>
          get {
            parameters("page".as[Int].?, "limit".as[Int].?) { (page, limit) =>
              onSuccess(articleService.getByTagIdWithCount(TagId(tagId), ArticlesQueryParameter(page, limit)).unsafeToFuture()) { result =>
                httpResponse(OK, result)
              }
            }
          }
        }
      }
    }
  }

}