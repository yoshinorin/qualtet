package net.yoshinorin.qualtet.http

import akka.event.Logging
import akka.http.scaladsl.model.{HttpRequest, RemoteAddress}
import akka.http.scaladsl.server.{Directive0, RouteResult}
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LogEntry}

trait HttpLogger {

  private[this] def requestAndResponseLogging(request: HttpRequest, ip: RemoteAddress): RouteResult => Option[LogEntry] = {
    case RouteResult.Complete(response) =>
      Some(
        LogEntry(
          ip.toOption.map(_.getHostAddress).getOrElse("unknown") + " - " + request.method.name + " - " + request.uri + " - " + response.status,
          Logging.InfoLevel
        )
      )
    case RouteResult.Rejected(rejections) =>
      Some(
        LogEntry(
          ip.toOption.map(_.getHostAddress).getOrElse("unknown") + " - " + request.method.name + " - " + request.uri + " - " + rejections,
          Logging.ErrorLevel
        )
      )
  }

  def httpLogging(ip: RemoteAddress): Directive0 = DebuggingDirectives.logRequestResult(requestAndResponseLogging(_, ip))

}
