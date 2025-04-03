package net.yoshinorin.qualtet

import cats.data.Kleisli
import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.kernel.Resource
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import com.comcast.ip4s.*

import scala.concurrent.duration._

object BootStrap extends IOApp {

  import Modules.log4catsLogger

  val logger: SelfAwareStructuredLogger[IO] = log4catsLogger.getLoggerFromClass(this.getClass)

  private def server(host: Ipv4Address, port: Port, httpApp: HttpApp[IO]): Resource[IO, Server] = {
    EmberServerBuilder
      .default[IO]
      .withHost(host)
      .withPort(port)
      .withHttpApp(httpApp)
      .withLogger(logger)
      .withShutdownTimeout(1.second)
      .build
  }

  def run(args: List[String]): IO[ExitCode] = {
    Modules.transactorResource.use { tx =>
      val modules = new Modules(tx)
      val host = Ipv4Address.fromString(modules.config.http.host).getOrElse(ipv4"127.0.0.1")
      val port = Port.fromInt(modules.config.http.port).getOrElse(port"9001")
      (for {
        _ <- logger.info(ApplicationInfo.asJson)
        _ <- IO(modules.migrator.migrate(modules.contentTypeService))
        routes <- modules.router.withCors.map[Kleisli[IO, Request[IO], Response[IO]]](x => x.orNotFound)
        httpApp <- IO(new HttpAppBuilder(routes).build)
        server <- IO(
          server(host, port, httpApp)
            .use(_ => IO.never)
            .as(ExitCode.Success)
        )
      } yield server).flatMap(identity)
    }

  }
}
