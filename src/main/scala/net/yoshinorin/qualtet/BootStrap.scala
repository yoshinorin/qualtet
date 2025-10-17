package net.yoshinorin.qualtet

import cats.data.Kleisli
import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.kernel.Resource
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import com.comcast.ip4s.*

import scala.concurrent.duration.*

object BootStrap extends IOApp {

  import Modules.log4catsLogger
  import org.typelevel.otel4s.trace.Tracer
  import org.typelevel.otel4s.oteljava.OtelJava

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
    val runApp: (Option[Tracer[IO]], Option[OtelJava[IO]]) => IO[ExitCode] = (maybeTracer, maybeOtelJava) =>
      Modules.transactorResource(maybeOtelJava).use { tx =>
        val modules = new Modules(tx, maybeTracer)
        val host = Ipv4Address.fromString(modules.config.http.host).getOrElse(ipv4"127.0.0.1")
        val port = Port.fromInt(modules.config.http.port).getOrElse(port"9001")

        for {
          _ <- logger.info(ApplicationInfo.asJson)
          _ <- IO(modules.flywayMigrator.migrate())
          _ <- modules.migrator.migrate(modules.contentTypeService)
          _ <- modules.versionService.migrate(Some(modules.v218Migrator))
          routes <- modules.router.withCors.map[Kleisli[IO, Request[IO], Response[IO]]](_.orNotFound)
          httpApp <- IO(new HttpAppBuilder(routes, maybeTracer).build)
          _ <- server(host, port, httpApp).use(_ => IO.never)
        } yield ExitCode.Success
        // TODO: should flush otel telemetries
      }

    Modules.makeOtel(runtime).fold(runApp(None, None)) { resource =>
      resource.use { case (otelJava, tracer) =>
        runApp(Some(tracer), Some(otelJava))
      }
    }
  }
}
