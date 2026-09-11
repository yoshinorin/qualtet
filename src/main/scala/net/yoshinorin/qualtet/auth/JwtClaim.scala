package net.yoshinorin.qualtet.auth

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

final case class JwtClaim(
  iss: String,
  aud: String,
  sub: String,
  jti: String,
  exp: Long,
  iat: Long
)

object JwtClaim {
  given codecJwtClaim: JsonValueCodec[JwtClaim] = JsonCodecMaker.make
}
