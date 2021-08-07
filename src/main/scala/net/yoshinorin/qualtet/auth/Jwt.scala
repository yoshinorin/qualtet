package net.yoshinorin.qualtet.auth

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import net.yoshinorin.qualtet.domains.models.authors.Author
import pdi.jwt.algorithms.JwtAsymmetricAlgorithm
import pdi.jwt.{JwtCirce, JwtOptions}

import java.time.Instant

final case class JwtClaim(
  exp: Int,
  iat: Int,
  authorId: String
)

object JwtClaim {

  implicit val decodeJwtClaim: Decoder[JwtClaim] = deriveDecoder[JwtClaim]
  implicit val decodeJwtClaims: Decoder[List[JwtClaim]] = Decoder.decodeList[JwtClaim]

}

class Jwt(algorithm: JwtAsymmetricAlgorithm, keyPair: KeyPair, signature: Signature) {

  /**
   * create JWT with authorId. authorId uses claim names in JWT.
   *
   * @param authorId authorId
   * @return String (JWT)
   */
  def encode(author: Author): String = {
    // TODO: add claim names
    val claim = pdi.jwt.JwtClaim(
      s"""{"authorId":${author.id}}""",
      expiration = Some(Instant.now.plusSeconds(3600).getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond)
    )
    pdi.jwt.Jwt.encode(claim, signature.signedPrivateKey, algorithm)
  }

  /**
   * validate JWT. Throw exception if failed validate.
   *
   * @param jwtString String of JWT
   */
  def verify(jwtString: String): Unit = {
    // TODO: verify claim names
    pdi.jwt.Jwt.validate(jwtString)
  }

  /**
   * decode JWT from string
   *
   * @param jwtString String of JWT
   * @return JwtClaim
   * TODO: consider return type of left
   */
  def decode(jwtString: String): Either[Throwable, JwtClaim] = {
    // TODO: logging
    for {
      jsonString <- JwtCirce.decodeJson(jwtString, keyPair.publicKey, JwtOptions(signature = true)).toEither
      maybeJwtClaim <- jsonString.as[JwtClaim] match {
        case Right(x) => Right(x)
        case Left(t) => Left(t)
      }
    } yield maybeJwtClaim
  }

}
