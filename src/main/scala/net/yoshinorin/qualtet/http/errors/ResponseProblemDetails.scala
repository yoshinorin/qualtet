package net.yoshinorin.qualtet.http.errors

import net.yoshinorin.qualtet.domains.errors.ProblemDetailsError

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

// https://datatracker.ietf.org/doc/html/rfc7807
final case class ResponseProblemDetails(
  `type`: String = "https://yoshinorin.github.io/qualtet/rest-api/",
  title: String,
  status: Int,
  detail: String,
  instance: String,
  errors: Option[Seq[ProblemDetailsError]] = None
)

object ResponseProblemDetails {
  given codecResponseProblemDetails: JsonValueCodec[ResponseProblemDetails] = JsonCodecMaker.make
}
