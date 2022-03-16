package net.yoshinorin.qualtet.auth

import cats.data.EitherT
import cats.effect.IO
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.domains.models.Fail.Unauthorized
import net.yoshinorin.qualtet.domains.models.authors.Author
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
          _ <- claimValidator(jc)(x => x.aud == Config.jwtAud)("invalid aud")
          _ <- claimValidator(jc)(x => x.iss == Config.jwtIss)("invalid iss")
          result <- claimValidator(jc)(x => x.exp > Instant.now.getEpochSecond)("token is expired")
        } yield result).value
      }
    }
  }

  /**
   * validate JWT claim
   *
   * @param jwtClaim JwtClaim case class
   * @param f function for validate condition
   * @param errorMsg error message for logging
   * @return validation result with EitherT
   *
   * TODO: move utility functions
   */
  def claimValidator(jwtClaim: JwtClaim)(f: JwtClaim => Boolean)(errorMsg: String): EitherT[IO, Throwable, JwtClaim] = {
    if (f(jwtClaim)) {
      EitherT.right(IO(jwtClaim))
    } else {
      // TODO: Maybe should not logging here
      logger.error(errorMsg)
      EitherT.left(IO(Unauthorized()))
    }
  }

}
