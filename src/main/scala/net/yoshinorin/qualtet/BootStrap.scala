package net.yoshinorin.qualtet

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import net.yoshinorin.qualtet.application.authors.AuthorFinder
import net.yoshinorin.qualtet.application.contents.{ContentCreator, ContentFinder}
import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.domains.models.authors.DoobieAuthorRepository
import net.yoshinorin.qualtet.domains.models.contents.DoobieContentRepository
import net.yoshinorin.qualtet.domains.services.{AuthoreService, ContentService}
import net.yoshinorin.qualtet.http.routes.{ApiStatusRoute, AuthorRoute, ContentRoute, HomeRoute}
import net.yoshinorin.qualtet.http.HttpServer
import net.yoshinorin.qualtet.infrastructure.db.Migration
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

import scala.io.StdIn

object BootStrap extends App {

  Migration.migrate()

  implicit val actorSystem: ActorSystem = ActorSystem("qualtet")
  implicit val executionContextExecutor: ExecutionContextExecutor = actorSystem.dispatcher

  implicit val doobieContext: DoobieContext = new DoobieContext()
  val contentRepository = new DoobieContentRepository(doobieContext)
  val authorRepository = new DoobieAuthorRepository(doobieContext)

  val contentFinder: ContentFinder = new ContentFinder(contentRepository)
  val contentCreator: ContentCreator = new ContentCreator(contentRepository)
  val contentService: ContentService = new ContentService(contentFinder, contentCreator)

  val authorFinder: AuthorFinder = new AuthorFinder(authorRepository)
  val authoreService: AuthoreService = new AuthoreService(authorFinder)

  val homeRoute: HomeRoute = new HomeRoute()
  val apiStatusRoute: ApiStatusRoute = new ApiStatusRoute()
  val authorRoute: AuthorRoute = new AuthorRoute(authoreService)
  val contentRoute: ContentRoute = new ContentRoute(contentService)

  val httpServer: HttpServer = new HttpServer(homeRoute, apiStatusRoute, authorRoute, contentRoute)

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
