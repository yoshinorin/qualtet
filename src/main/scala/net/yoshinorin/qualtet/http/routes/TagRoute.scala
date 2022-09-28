package net.yoshinorin.qualtet.http.routes

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import net.yoshinorin.qualtet.auth.AuthService
import net.yoshinorin.qualtet.domains.articles.{ArticleService, ResponseArticleWithCount}
import net.yoshinorin.qualtet.domains.tags.{TagId, TagName, TagService}
import net.yoshinorin.qualtet.message.Fail
import net.yoshinorin.qualtet.http.{Authentication, ArticlesQueryParameter, ResponseHandler}
import net.yoshinorin.qualtet.syntax._

import cats.effect.unsafe.implicits.global

class TagRoute(
  authService: AuthService,
  tagService: TagService,
  articleService: ArticleService
) extends Authentication(authService)
    with ResponseHandler {

  def route: Route = {
    pathPrefix("tags") {
      pathEndOrSingleSlash {
        get {
          onSuccess(tagService.getAll.unsafeToFuture()) { result =>
            complete(HttpResponse(OK, entity = HttpEntity(ContentTypes.`application/json`, result.asJson)))
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
        // NOTE: Should not mix tag and id.
        pathPrefix(".+".r) { tagNameOrId =>
          get {
            parameters("page".as[Int].?, "limit".as[Int].?) { (page, limit) =>
              onSuccess(
                articleService
                  .getByTagNameWithCount(TagName(tagNameOrId), ArticlesQueryParameter(page, limit))
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
          } ~ {
            delete {
              authenticate { _ =>
                onSuccess(
                  tagService
                    .delete(TagId(tagNameOrId))
                    .handleErrorWith { e => IO.pure(e) }
                    .unsafeToFuture()
                ) {
                  case e: Exception => httpResponse(e)
                  case _ => httpResponse(NoContent)
                }
              }
            }
          }
        }
      }
    }
  }
}
