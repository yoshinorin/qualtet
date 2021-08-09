package net.yoshinorin.qualtet.auth

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.domains.models.authors.Author
import org.slf4j.LoggerFactory
import pdi.jwt.algorithms.JwtAsymmetricAlgorithm
import pdi.jwt.{JwtCirce, JwtOptions}

import java.time.Instant
import java.util.UUID
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
      subject = Some(author.id),
      jwtId = Some(UUID.randomUUID().toString),
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
    Try {
      pdi.jwt.Jwt.validate(jwtString)
    }.toEither match {
      case Left(t) =>
        logger.error(t.getMessage)
        Left(t)
      case _ => // Nothing to do
    }

    for {
      jsonString <- JwtCirce.decodeJson(jwtString, keyPair.publicKey, JwtOptions(signature = true)).toEither
      maybeJwtClaim <- jsonString.as[JwtClaim] match {
        case Left(t) =>
          logger.error(t.getMessage)
          Left(t)
        case Right(x) => {
          // TODO: clean up
          if (x.aud != Config.jwtAud) {
            return Left(new Exception("TODO"))
          }
          if (x.iss != Config.jwtIss) {
            return Left(new Exception("TODO"))
          }
          if (Instant.now.getEpochSecond > x.exp) {
            return Left(new Exception("TODO"))
          }
          Right(x)
        }
      }
    } yield maybeJwtClaim
  }

}
