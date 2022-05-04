package net.yoshinorin.qualtet.auth

import cats.effect.IO
import cats.implicits.catsSyntaxEq
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.domains.authors.Author
import net.yoshinorin.qualtet.error.Fail.Unauthorized
import net.yoshinorin.qualtet.validator.Validator
import org.slf4j.LoggerFactory
import pdi.jwt.algorithms.JwtAsymmetricAlgorithm
import pdi.jwt.{JwtCirce, JwtOptions}
import wvlet.airframe.ulid.ULID

import java.time.Instant
import scala.util.Try

final case class JwtClaim(
  iss: String,
  aud: String,
  sub: String,
  jti: String,
  exp: Long,
  iat: Long
)

object JwtClaim {

  implicit val decodeJwtClaim: Decoder[JwtClaim] = deriveDecoder[JwtClaim]
  implicit val decodeJwtClaims: Decoder[List[JwtClaim]] = Decoder.decodeList[JwtClaim]

}

class Jwt(algorithm: JwtAsymmetricAlgorithm, keyPair: KeyPair, signature: Signature) {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  /**
   * create JWT with authorId. authorId uses claim names in JWT.
   *
   * @param author Author instance
   * @return String (JWT)
   */
  def encode(author: Author): String = {
    val claim = pdi.jwt.JwtClaim(
      issuer = Some(Config.jwtIss),
      audience = Some(Set(Config.jwtAud)),
      subject = Some(author.id.value),
      jwtId = Some(ULID.newULIDString.toLowerCase),
      expiration = Some(Instant.now.plusSeconds(Config.jwtExpiration).getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond)
    )
    pdi.jwt.Jwt.encode(claim, signature.signedPrivateKey, algorithm)
  }

  /**
   * validate JWT. Throw exception if failed validate.
   *
   * @param jwtString String of JWT
   */
  def verify(jwtString: String): Either[Throwable, Unit] = {
    Try {
      pdi.jwt.Jwt.validate(jwtString, keyPair.publicKey)
    }.toEither
  }

  /**
   * decode JWT from string
   *
   * @param jwtString String of JWT
   * @return JwtClaim
   */
  def decode(jwtString: String): IO[Either[Throwable, JwtClaim]] = {
    (for {
      _ <- verify(jwtString)
      jsonString <- JwtCirce.decodeJson(jwtString, keyPair.publicKey, JwtOptions(signature = true)).toEither
      maybeJwtClaim <- jsonString.as[JwtClaim]
    } yield maybeJwtClaim) match {
      case Left(t) =>
        logger.error(t.getMessage)
        IO(Left(t))
      case Right(jc) => {
        (for {
          _ <- Validator.validate(jc)(x => x.aud === Config.jwtAud)(Unauthorized())
          _ <- Validator.validate(jc)(x => x.iss === Config.jwtIss)(Unauthorized())
          result <- Validator.validate(jc)(x => x.exp > Instant.now.getEpochSecond)(Unauthorized())
        } yield result).value
      }
    }
  }

}
