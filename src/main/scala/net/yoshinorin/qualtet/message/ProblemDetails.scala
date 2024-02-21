package net.yoshinorin.qualtet.message

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

// https://datatracker.ietf.org/doc/html/rfc7807
final case class ProblemDetails(
  type_ : String = "https://yoshinorin.github.io/qualtet/rest-api/",
  title: String,
  status: Int,
  detail: String,
  instance: String,
  errors: Option[Seq[Error]] = None
)

object ProblemDetails {
  given codecProblemDetails: JsonValueCodec[ProblemDetails] = JsonCodecMaker.make(
    CodecMakerConfig
      .withFieldNameMapper { case "type_" =>
        "type"
      }
      .withTransientEmpty(false)
      .withSkipNestedOptionValues(false)
      .withSkipUnexpectedFields(false)
      .withTransientEmpty(false)
      .withTransientDefault(false)
  )
}

final case class Error(
  code: String,
  message: String
)

object Error {
  given codecError: JsonValueCodec[Error] = JsonCodecMaker.make
  given codecErrors: JsonValueCodec[Option[Seq[Error]]] = JsonCodecMaker.make
}
