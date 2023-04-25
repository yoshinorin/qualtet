package net.yoshinorin.qualtet.http.routes

import cats.effect._
import cats.Monad
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.dsl.io._
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.domains.series.SeriesService
import net.yoshinorin.qualtet.domains.contents.Path
import net.yoshinorin.qualtet.http.RequestDecoder
import net.yoshinorin.qualtet.syntax._

class SeriesRoute[M[_]: Monad](
  seriesService: SeriesService[M]
) extends RequestDecoder {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  def get(path: String): IO[Response[IO]] = {
    (for {
      // TODO: should be configurlize for append suffix or prefix
      maybeSeries <- seriesService.findByPath(Path(s"/${path}"))
    } yield maybeSeries)
      .flatMap(_.asResponse)
      .handleErrorWith(_.logWithStackTrace.andResponse)
  }

}
