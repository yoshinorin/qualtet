package net.yoshinorin.qualtet.http

import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.{HttpRequest, RemoteAddress}
import akka.http.scaladsl.server.{Directive0, RouteResult}
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LogEntry, LoggingMagnet}
import net.yoshinorin.qualtet.syntax._

trait HttpLogger {

  private[this] def requestAndResponseLogging(loggingAdapter: LoggingAdapter, requestTimestamp: Long, ip: RemoteAddress)(
    request: HttpRequest
  )(routeResult: RouteResult): Unit = {
    val elapsedTime: Long = (System.nanoTime - requestTimestamp) / 1000000
    val hostAddress = ip.toOption.map(_.getHostAddress).getOrElse("unknown")
    routeResult match {
      case RouteResult.Complete(response) =>
        LogEntry(
          s"""${hostAddress} - ${request.method.name} - ${request.uri} - ${response.status} - ${elapsedTime}ms - ${request.headers.referer} - ${request.headers.userAgent}""",
          Logging.InfoLevel
        ).logTo(loggingAdapter)
      case RouteResult.Rejected(rejections) =>
        LogEntry(
          s"""${hostAddress} - ${request.method.name} - ${request.uri} - ${rejections
            .mkString(",")} - ${elapsedTime}ms - ${request.headers.referer} - ${request.headers.userAgent}""",
          Logging.ErrorLevel
        ).logTo(loggingAdapter)
    }
  }

  private[this] def loggingFunction(log: LoggingAdapter, ip: RemoteAddress): HttpRequest => RouteResult => Unit = {
    requestAndResponseLogging(log, System.nanoTime, ip)
  }

  def httpLogging(ip: RemoteAddress): Directive0 = DebuggingDirectives.logRequestResult(LoggingMagnet(loggingFunction(_, ip)))

}
