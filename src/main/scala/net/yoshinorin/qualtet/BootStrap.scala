package net.yoshinorin.qualtet

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import akka.actor.ActorSystem
import net.yoshinorin.qualtet.application.contents.ContentFinder
import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.domains.services.ContentService
import net.yoshinorin.qualtet.http.routes.{ApiStatusRoute, ContentRoute, HomeRoute}
import net.yoshinorin.qualtet.http.HttpServer
import net.yoshinorin.qualtet.infrastructure.db.Migration
import net.yoshinorin.qualtet.infrastructure.db.doobie.{DoobieContentRepository, DoobieContext}

import scala.io.StdIn

object BootStrap extends App {

  Migration.migrate()

  implicit val actorSystem: ActorSystem = ActorSystem("qualtet")
  implicit val executionContextExecutor: ExecutionContextExecutor = actorSystem.dispatcher

  val doobieContext: DoobieContext = new DoobieContext()
  val contentRepository = new DoobieContentRepository(doobieContext)

  val contentFinder: ContentFinder = new ContentFinder(contentRepository)
  val contentService: ContentService = new ContentService(contentFinder)

  val homeRoute: HomeRoute = new HomeRoute()
  val apiStatusRoute: ApiStatusRoute = new ApiStatusRoute()
  val contentRoute: ContentRoute = new ContentRoute(contentService)

  val httpServer: HttpServer = new HttpServer(homeRoute, apiStatusRoute, contentRoute)

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
