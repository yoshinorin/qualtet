package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import io.circe.syntax.EncoderOps
import net.yoshinorin.qualtet.domains.articles.{ArticleService, ResponseArticleWithCount}
import net.yoshinorin.qualtet.domains.models.Fail
import net.yoshinorin.qualtet.domains.tags.{TagName, TagService}
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
        /*
        NOTE:
          The Next.js can not pass custom argument with <Link> component.
          So, I want to belows, but can not...

          Front-end visible URL: https://example.com/tags/{tagName}
          API call (when transition with <Link>):  https://example.com/tags/{tagId}

          But, it can not. So, I have to find the tagging contents with tagName.

        pathPrefix(".+".r) { tagId =>
          get {
            parameters("page".as[Int].?, "limit".as[Int].?) { (page, limit) =>
              onSuccess(articleService.getByTagIdWithCount(TagId(tagId), ArticlesQueryParameter(page, limit)).unsafeToFuture()) { result =>
                httpResponse(OK, result)
              }
            }
          }
        }
         */
        pathPrefix(".+".r) { tagName =>
          get {
            parameters("page".as[Int].?, "limit".as[Int].?) { (page, limit) =>
              onSuccess(
                articleService
                  .getByTagNameWithCount(TagName(tagName), ArticlesQueryParameter(page, limit))
                  .handleErrorWith { e => IO.pure(e) }
                  .unsafeToFuture()
              ) {
                case r: ResponseArticleWithCount =>
                  httpResponse(OK, r)
                case e: Exception =>
                  httpResponse(e)
                case _ =>
                  httpResponse(Fail.InternalServerError("Internal server error"))
              }
            }
          }
        }
      }
    }
  }

}
