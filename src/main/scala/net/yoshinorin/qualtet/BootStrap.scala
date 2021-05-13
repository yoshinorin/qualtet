package net.yoshinorin.qualtet

import akka.actor.ActorSystem
import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.http.routes.{ApiStatusRoute, HomeRoute}
import net.yoshinorin.qualtet.http.HttpServer

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import net.yoshinorin.qualtet.infrastructure.db.Migration

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object BootStrap extends App {

  Migration.migrate()

  implicit val actorSystem: ActorSystem = ActorSystem("qualtet")
  implicit val executionContextExecutor: ExecutionContextExecutor = actorSystem.dispatcher

  val homeRoute: HomeRoute = new HomeRoute()
  val apiStatusRoute: ApiStatusRoute = new ApiStatusRoute()

  val httpServer: HttpServer = new HttpServer(homeRoute, apiStatusRoute)

  httpServer.start(Config.httpHost, Config.httpPort).onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      println(s"Server online at http://${address.getHostString}:${address.getPort}/")
    case Failure(ex) =>
      println("Failed to bind HTTP endpoint, terminating system", ex)
      actorSystem.terminate()
  }

}