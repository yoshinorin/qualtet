package net.yoshinorin.qualtet.http

import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.{HttpRequest, RemoteAddress}
import akka.http.scaladsl.server.{Directive0, RouteResult}
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LogEntry, LoggingMagnet}
import net.yoshinorin.qualtet.syntax._

trait HttpLogger {

  private[http] def makeLogString(request: HttpRequest, ip: String, elapsedTime: Long, reason: String): String = {
    List[String](
      ip,
      request.method.name,
      request.uri.path.toString(),
      reason,
      s"${elapsedTime}ms",
      request.headers.referer.stringify,
      request.headers.userAgent.stringify
    ).map(x => s""""${x}"""").mkString(" - ")
  }

  private[this] def write(loggingAdapter: LoggingAdapter, requestTimestamp: Long, ip: RemoteAddress)(request: HttpRequest)(routeResult: RouteResult): Unit = {
    val elapsedTime: Long = (System.nanoTime - requestTimestamp) / 1000000
    val stringifyIp = ip.toOption.map(_.getHostAddress).getOrElse("unknown")
    routeResult match {
      case RouteResult.Complete(response) =>
        LogEntry(makeLogString(request, stringifyIp, elapsedTime, response.status.toString()), Logging.InfoLevel).logTo(loggingAdapter)
      case RouteResult.Rejected(rejections) =>
        LogEntry(makeLogString(request, stringifyIp, elapsedTime, rejections.mkString(",")), Logging.ErrorLevel).logTo(loggingAdapter)
    }
  }

  private[this] def loggingFunction(loggingAdapter: LoggingAdapter, ip: RemoteAddress): HttpRequest => RouteResult => Unit = {
    write(loggingAdapter, System.nanoTime, ip)
  }

  def httpLogging(ip: RemoteAddress): Directive0 = DebuggingDirectives.logRequestResult(LoggingMagnet(loggingFunction(_, ip)))

}
