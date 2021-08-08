package net.yoshinorin.qualtet

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import net.yoshinorin.qualtet.auth.{AuthService, Jwt, KeyPair}
import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.domains.models.archives.DoobieArchiveRepository
import net.yoshinorin.qualtet.domains.models.articles.DoobieArticleRepository
import net.yoshinorin.qualtet.domains.models.authors.DoobieAuthorRepository
import net.yoshinorin.qualtet.domains.models.contentTypes.DoobieContentTypeRepository
import net.yoshinorin.qualtet.domains.models.contents.DoobieContentRepository
import net.yoshinorin.qualtet.domains.services.{ArchiveService, ArticleService, AuthorService, ContentService, ContentTypeService}
import net.yoshinorin.qualtet.http.routes.{ApiStatusRoute, ArchiveRoute, ArticleRoute, AuthRoute, AuthorRoute, ContentRoute, ContentTypeRoute, HomeRoute}
import net.yoshinorin.qualtet.http.HttpServer
import net.yoshinorin.qualtet.infrastructure.db.Migration
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext
import pdi.jwt.JwtAlgorithm

import java.security.SecureRandom
import scala.io.StdIn

object BootStrap extends App {

  Migration.migrate()

  implicit val actorSystem: ActorSystem = ActorSystem("qualtet")
  implicit val executionContextExecutor: ExecutionContextExecutor = actorSystem.dispatcher

  implicit val doobieContext: DoobieContext = new DoobieContext()

  // NOTE: for generate JWT. They are reset when re-boot application.
  val keyPair = new KeyPair("RSA", 2048, SecureRandom.getInstanceStrong)
  val message: Array[Byte] = SecureRandom.getInstanceStrong.toString.getBytes
  val signature = new net.yoshinorin.qualtet.auth.Signature("SHA256withRSA", message, keyPair)
  val jwtInstance = new Jwt(JwtAlgorithm.RS256, keyPair, signature)

  val authorRepository = new DoobieAuthorRepository(doobieContext)
  val authorService: AuthorService = new AuthorService(authorRepository)

  val authService = new AuthService(authorService, jwtInstance)

  val contentTypeRepository = new DoobieContentTypeRepository(doobieContext)
  val contentTypeService: ContentTypeService = new ContentTypeService(contentTypeRepository)

  val contentRepository = new DoobieContentRepository(doobieContext)
  val contentService: ContentService = new ContentService(contentRepository, authorService, contentTypeService)

  val articleRepository = new DoobieArticleRepository(doobieContext)
  val articleService: ArticleService = new ArticleService(articleRepository, contentTypeService)

  val archiveRepository = new DoobieArchiveRepository(doobieContext)
  val archiveService: ArchiveService = new ArchiveService(archiveRepository, contentTypeService)

  val homeRoute: HomeRoute = new HomeRoute()
  val apiStatusRoute: ApiStatusRoute = new ApiStatusRoute()
  val authRoute: AuthRoute = new AuthRoute(authService)
  val authorRoute: AuthorRoute = new AuthorRoute(authorService)
  val contentRoute: ContentRoute = new ContentRoute(contentService)
  val articleRoute: ArticleRoute = new ArticleRoute(articleService)
  val archiveRoute: ArchiveRoute = new ArchiveRoute(archiveService)
  val contentTypeRoute: ContentTypeRoute = new ContentTypeRoute(contentTypeService)

  val httpServer: HttpServer = new HttpServer(homeRoute, apiStatusRoute, authRoute, authorRoute, contentRoute, articleRoute, archiveRoute, contentTypeRoute)

  httpServer.start(Config.httpHost, Config.httpPort).onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      println(s"Server online at http://${address.getHostString}:${address.getPort}/")
      // TODO: delete comment out when create docker image
      StdIn.readLine()
      binding
        .unbind()
        .onComplete(_ => actorSystem.terminate())
    case Failure(ex) =>
      println("Failed to bind HTTP endpoint, terminating system", ex)
      actorSystem.terminate()
  }
}
