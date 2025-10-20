package net.yoshinorin.qualtet.infrastructure.telemetry

import cats.effect.IO
import doobie.util.log.LogHandler
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.trace.{SpanKind, Tracer}

object DoobieTracing {

  private def extractOperation(sql: String): String = {
    sql.trim.split("\\s+").headOption.getOrElse("UNKNOWN").toUpperCase
  }

  def logHandler(tracer: Tracer[IO]): LogHandler[IO] = {
    new LogHandler[IO] {
      def logSqlEvent(sql: String): IO[Unit] = {
        tracer.currentSpanContext.flatMap {
          case Some(_) =>
            tracer.span("query").use { span =>
              span.addAttribute(Attribute("db.statement", sql)) *>
                span.addAttribute(Attribute("db.operation", extractOperation(sql)))
            }
          case None => IO.unit
        }
      }

      override def run(logEvent: doobie.util.log.LogEvent): IO[Unit] = logEvent match {
        case doobie.util.log.Success(sql, _, _, _, _) => logSqlEvent(sql)
        case doobie.util.log.ProcessingFailure(sql, _, _, _, _, _) => logSqlEvent(sql)
        case doobie.util.log.ExecFailure(sql, _, _, _, _) => logSqlEvent(sql)
      }
    }
  }

  def traceTransaction[T](tracer: Tracer[IO])(io: IO[T]): IO[T] = {
    tracer
      .spanBuilder("db.transaction")
      .withSpanKind(SpanKind.Client)
      .build
      .surround(io)
  }

}
