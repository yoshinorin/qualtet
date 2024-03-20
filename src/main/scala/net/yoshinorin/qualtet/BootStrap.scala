package net.yoshinorin.qualtet

import cats.data.Kleisli
import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.kernel.Resource
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.{LoggerFactory => Log4CatsLoggerFactory}
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import com.comcast.ip4s.*

import scala.concurrent.duration._

object BootStrap extends IOApp {

  import net.yoshinorin.qualtet.Modules.log4catsLogger

  val logger: SelfAwareStructuredLogger[IO] = Log4CatsLoggerFactory[IO].getLoggerFromClass(this.getClass)

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
    val host = Ipv4Address.fromString(Modules.config.http.host).getOrElse(ipv4"127.0.0.1")
    val port = Port.fromInt(Modules.config.http.port).getOrElse(port"9001")

    (for {
      _ <- logger.info(ApplicationInfo.asJson)
      _ <- IO(Modules.migrator.migrate(Modules.contentTypeService))
      routes <- Modules.router.withCors.map[Kleisli[IO, Request[IO], Response[IO]]](x => x.orNotFound)
      httpApp <- IO(new HttpAppBuilder(routes).build)
      server <- IO(
        server(host, port, httpApp)
          .use(_ => IO.never)
          .as(ExitCode.Success)
      )
    } yield server).flatMap(identity)

  }
}
