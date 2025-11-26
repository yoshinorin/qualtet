package net.yoshinorin.qualtet.auth

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.authors.AuthorId
import net.yoshinorin.qualtet.domains.Request
import net.yoshinorin.qualtet.domains.errors.DomainError

final case class RequestToken(
  authorId: AuthorId,
  password: String
) extends Request[RequestToken] {
  def postDecode: Either[DomainError, RequestToken] = Right(this) // NOTE: nothing to validate
}

object RequestToken {
  given codecTokenRequest: JsonValueCodec[RequestToken] = JsonCodecMaker.make
}

final case class ResponseToken(
  token: String
)

object ResponseToken {
  given codecTokenResponse: JsonValueCodec[ResponseToken] = JsonCodecMaker.make
}
