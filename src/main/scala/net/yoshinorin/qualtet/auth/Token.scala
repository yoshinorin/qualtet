package net.yoshinorin.qualtet.auth

import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._
import net.yoshinorin.qualtet.domains.authors.AuthorId
import net.yoshinorin.qualtet.domains.Request

final case class RequestToken(
  authorId: AuthorId,
  password: String
) extends Request {
  def postDecode: Unit = ()
}

object RequestToken {
  implicit val codecTokenRequest: JsonValueCodec[RequestToken] = JsonCodecMaker.make
}

final case class ResponseToken(
  token: String
)

object ResponseToken {
  implicit val codecTokenResponse: JsonValueCodec[ResponseToken] = JsonCodecMaker.make
}
