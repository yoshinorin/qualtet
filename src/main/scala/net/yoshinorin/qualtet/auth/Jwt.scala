package net.yoshinorin.qualtet.auth

import cats.effect.IO
import cats.implicits.catsSyntaxEq
import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._
import net.yoshinorin.qualtet.config.Config
import net.yoshinorin.qualtet.domains.authors.Author
import net.yoshinorin.qualtet.message.Fail.Unauthorized
import net.yoshinorin.qualtet.syntax._
import org.slf4j.LoggerFactory
import pdi.jwt.algorithms.JwtAsymmetricAlgorithm
import pdi.jwt.JwtOptions
import wvlet.airframe.ulid.ULID

import java.time.Instant
import java.util.Locale
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
  implicit val codecJwtClaim: JsonValueCodec[JwtClaim] = JsonCodecMaker.make
}

class Jwt(algorithm: JwtAsymmetricAlgorithm, keyPair: KeyPair, signature: Signature) {

  import JwtClaim._

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
      jwtId = Some(ULID.newULIDString.toLowerCase(Locale.ENGLISH)),
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
      maybeJwtClaim <- pdi.jwt.Jwt.decodeRaw(jwtString, keyPair.publicKey, JwtOptions(signature = true)).toEither
      jwt <- Try(maybeJwtClaim.decode).toEither
    } yield jwt) match {
      case Left(t) =>
        logger.error(t.getMessage)
        IO(Left(t))
      case Right(jc) => {
        (for {
          _ <- jc.toEitherIO(x => x.aud === Config.jwtAud)(Unauthorized())
          _ <- jc.toEitherIO(x => x.iss === Config.jwtIss)(Unauthorized())
          result <- jc.toEitherIO(x => x.exp > Instant.now.getEpochSecond)(Unauthorized())
        } yield result).value
      }
    }
  }

}
