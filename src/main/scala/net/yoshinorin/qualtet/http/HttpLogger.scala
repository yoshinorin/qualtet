package net.yoshinorin.qualtet.http

import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.{HttpRequest, RemoteAddress}
import akka.http.scaladsl.server.{Directive0, RouteResult}
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LogEntry, LoggingMagnet}

trait HttpLogger {

  private[this] def requestAndResponseLogging(loggingAdapter: LoggingAdapter, requestTimestamp: Long, ip: RemoteAddress)(
    request: HttpRequest
  )(response: RouteResult): Unit = {
    val responseTimestamp: Long = System.nanoTime
    val elapsedTime: Long = (responseTimestamp - requestTimestamp) / 1000000
    val entry = response match {
      case RouteResult.Complete(response) =>
        LogEntry(
          s"""${ip.toOption
            .map(_.getHostAddress)
            .getOrElse("unknown")} - ${request.method.name} - ${request.uri} - ${response.status} - ${elapsedTime}ms""",
          Logging.InfoLevel
        )
      case RouteResult.Rejected(rejections) =>
        LogEntry(
          s"""${ip.toOption
            .map(_.getHostAddress)
            .getOrElse("unknown")} - ${request.method.name} - ${request.uri} - ${rejections.mkString(",")} - ${elapsedTime}ms""",
          Logging.ErrorLevel
        )
    }
    entry.logTo(loggingAdapter)
  }

  private[this] def loggingFunction(log: LoggingAdapter, ip: RemoteAddress): HttpRequest => RouteResult => Unit = {
    val requestTimestamp = System.nanoTime
    requestAndResponseLogging(log, requestTimestamp, ip)
  }

  def httpLogging(ip: RemoteAddress): Directive0 = DebuggingDirectives.logRequestResult(LoggingMagnet(loggingFunction(_, ip)))

}
