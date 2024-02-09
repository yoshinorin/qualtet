package net.yoshinorin.qualtet.http.routes.v1

import cats.Monad
import cats.implicits.*
import cats.effect.IO
import org.http4s.headers.{Allow, `Content-Type`}
import org.http4s.{AuthedRoutes, HttpRoutes, MediaType, Response}
import org.http4s.dsl.io.*
import org.http4s.{ContextRequest, Request}
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor
import net.yoshinorin.qualtet.domains.contents.{ContentId, ContentService, Path, RequestContent}
import net.yoshinorin.qualtet.http.{AuthProvider, RequestDecoder}
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.http.MethodNotAllowedSupport

class ContentRoute[F[_]: Monad](
  authProvider: AuthProvider[F],
  contentService: ContentService[F]
) extends RequestDecoder
    with MethodNotAllowedSupport {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  // NOTE: must be compose `auth route` after `Non auth route`.
  private[http] def index: HttpRoutes[IO] =
    (contentWithoutAuth <+>
      authProvider.authenticate(contentWithAuthed))

  private[http] def contentWithoutAuth: HttpRoutes[IO] = HttpRoutes.of[IO] {
    // need slash on the prefix and suffix.
    // example: /yyyy/mm/dd/content-name/
    /* compile error
    case GET -> Root /: path =>
      contentRoute.get(path)
     */
    case request @ GET -> _ =>
      this
        .get(request.uri.path.toString().replace("/v1/contents/", ""))(request)
        .handleErrorWith(_.logWithStackTrace[IO].andResponse(request))
  }

  private[http] def contentWithAuthed: AuthedRoutes[(ResponseAuthor, String), IO] = AuthedRoutes.of { ctxRequest =>
    implicit val x = ctxRequest.req
    (ctxRequest match {
      case ContextRequest(_, r) =>
        r match {
          case request @ POST -> Root => this.post(ctxRequest.context)
          case request @ DELETE -> Root / id => this.delete(id)
          case request @ _ =>
            methodNotAllowed(r, Allow(Set(GET, POST, DELETE)))
        }
    }).handleErrorWith(_.logWithStackTrace[IO].andResponse)
  }

  private[http] def post(payload: (ResponseAuthor, String)): IO[Response[IO]] = {
    val maybeContent = for {
      maybeContent <- IO(decode[RequestContent](payload._2))
    } yield maybeContent

    maybeContent.flatMap { c =>
      c match {
        case Left(f) => throw f
        case Right(c) =>
          contentService.createContentFromRequest(payload._1.name, c).flatMap { createdContent =>
            Created(createdContent.asJson, `Content-Type`(MediaType.application.json))
          }
      }
    }
  }

  private[http] def delete(id: String): IO[Response[IO]] = {
    (for {
      _ <- contentService.delete(ContentId(id))
      _ = logger.info(s"deleted content: ${id}")
      response <- NoContent()
    } yield response)
  }

  def get(path: String)(implicit r: Request[IO]): IO[Response[IO]] = {
    (for {
      // TODO: should be configurlize for append suffix or prefix
      maybeContent <- contentService.findByPathWithMeta(Path(s"/${path}"))
    } yield maybeContent)
      .flatMap(_.asResponse)
  }

}
