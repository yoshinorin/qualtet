package net.yoshinorin.qualtet.auth

import cats.Monad
import cats.implicits.catsSyntaxEq
import net.yoshinorin.qualtet.config.JwtConfig
import net.yoshinorin.qualtet.domains.authors.Author
import net.yoshinorin.qualtet.domains.errors.Unauthorized
import net.yoshinorin.qualtet.syntax.*
import org.slf4j.LoggerFactory
import pdi.jwt.algorithms.JwtAsymmetricAlgorithm
import pdi.jwt.JwtOptions
import wvlet.airframe.ulid.ULID

import java.time.Instant
import scala.util.Try

class Jwt(config: JwtConfig, algorithm: JwtAsymmetricAlgorithm, keyPair: KeyPair, signature: Signature) {

  import JwtClaim.codecJwtClaim

  private val logger = LoggerFactory.getLogger(this.getClass)

  /**
   * create JWT with authorId. authorId uses claim names in JWT.
   *
   * @param author Author instance
   * @return String (JWT)
   */
  def encode(author: Author): String = {
    val claim = pdi.jwt
      .JwtClaim(
        issuer = Some(config.iss),
        audience = Some(Set(config.aud)),
        subject = Some(author.id.value),
        jwtId = Some(ULID.newULIDString.toLower),
        expiration = Some(Instant.now.plusSeconds(config.expiration).getEpochSecond),
        issuedAt = Some(Instant.now.getEpochSecond)
      )
    pdi.jwt.Jwt.encode(claim, signature.signedPrivateKey, algorithm)
  }

  /**
   * validate JWT. Throw exception if failed validate.
   *
   * @param jwtString String of JWT
   */
  private def verify(jwtString: String): Either[Throwable, Unit] = {
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
  def decode[F[_]: Monad](jwtString: String): F[Either[Throwable, JwtClaim]] = {
    (for {
      _ <- verify(jwtString)
      maybeJwtClaim <- pdi.jwt.Jwt.decodeRaw(jwtString, keyPair.publicKey, JwtOptions(signature = true)).toEither
      jwtClaim <- Try(maybeJwtClaim.decode).toEither
    } yield jwtClaim) match {
      case Left(t) =>
        logger.error(t.getMessage)
        Monad[F].pure(Left(t))
      case Right(jc) => {
        (for {
          _ <- jc.toEitherF(x => x.aud === config.aud)(Unauthorized())
          _ <- jc.toEitherF(x => x.iss === config.iss)(Unauthorized())
          result <- jc.toEitherF(x => x.exp > Instant.now.getEpochSecond)(Unauthorized())
        } yield result).value
      }
    }
  }

}
