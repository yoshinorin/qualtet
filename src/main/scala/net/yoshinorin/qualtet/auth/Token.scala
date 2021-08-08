package net.yoshinorin.qualtet.auth

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

final case class RequestToken(
  authorId: String,
  password: String
)

object RequestToken {
  implicit val encodeTokenRequest: Encoder[RequestToken] = deriveEncoder[RequestToken]
  implicit val decodeTokenRequest: Decoder[RequestToken] = deriveDecoder[RequestToken]
}

final case class ResponseToken(
  token: String
)

object ResponseToken {
  implicit val encodeTokenReponse: Encoder[ResponseToken] = deriveEncoder[ResponseToken]
  implicit val decodeTokenReponse: Decoder[ResponseToken] = deriveDecoder[ResponseToken]
}
