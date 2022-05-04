package net.yoshinorin.qualtet.auth

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import net.yoshinorin.qualtet.domains.authors.AuthorId

final case class RequestToken(
  authorId: AuthorId,
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
  implicit val encodeTokenResponse: Encoder[ResponseToken] = deriveEncoder[ResponseToken]
  implicit val decodeTokenResponse: Decoder[ResponseToken] = deriveDecoder[ResponseToken]
}
