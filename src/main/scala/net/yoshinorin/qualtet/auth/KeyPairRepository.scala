package net.yoshinorin.qualtet.auth

import org.slf4j.LoggerFactory

import java.nio.file.*
import java.security
import java.security.{KeyPairGenerator, PrivateKey, PublicKey, SecureRandom}

trait KeyPairRepository {
  def publicKey: PublicKey
  def privateKey: PrivateKey
}

final case class InMemoryKeyPairConfig(
  algorithm: String,
  length: Int,
  secureRandom: SecureRandom
)

final case class FileKeyPairConfig(
  algorithm: String,
  publicKeyPath: Path,
  privateKeyPath: Path
)

final case class PemKeyPairConfig(
  algorithm: String,
  publicKeyPem: String,
  privateKeyPem: String
)

object KeyPairRepository {

  // NOTE: DO NOT USE `log4cats`
  private val logger = LoggerFactory.getLogger(this.getClass)

  given InMemoryKeyPair(using config: InMemoryKeyPairConfig): KeyPairRepository = {
    new KeyPairRepository {
      val keyPairGenerator = KeyPairGenerator.getInstance(config.algorithm)
      // require entropy
      // https://stackoverflow.com/questions/4819359/dev-random-extremely-slow
      private lazy val keyPair: security.KeyPair = keyPairGenerator.generateKeyPair()

      logger.info("created: keyPair generator")
      keyPairGenerator.initialize(config.length, config.secureRandom)
      logger.info(s"generated: keyPair - ${keyPair.getPublic.getAlgorithm}")

      override def privateKey: PrivateKey = keyPair.getPrivate
      override def publicKey: PublicKey = keyPair.getPublic

    }
  }

  given FromFile(using config: FileKeyPairConfig): KeyPairRepository = {
    val publicPem = Files.readString(config.publicKeyPath)
    val privatePem = Files.readString(config.privateKeyPath)

    logger.info(s"loaded: keyPair from files - public=${config.publicKeyPath}, private=${config.privateKeyPath}")
    fromPemStrings(config.algorithm, publicPem, privatePem)
  }

  given FromPem(using config: PemKeyPairConfig): KeyPairRepository = {
    logger.info(s"loaded: keyPair from PEM strings - algorithm=${config.algorithm}")
    fromPemStrings(config.algorithm, config.publicKeyPem, config.privateKeyPem)
  }

  private def fromPemStrings(algorithm: String, publicKeyPem: String, privateKeyPem: String): KeyPairRepository = {
    // NOTE: parse eagerly so malformed keys (both public AND private) fail at boot, not on first use.
    val parsedPublic: PublicKey = PemParser.parsePublicKey(algorithm, publicKeyPem)
    val parsedPrivate: PrivateKey = PemParser.parsePrivateKey(algorithm, privateKeyPem)

    new KeyPairRepository {
      override def publicKey: PublicKey = parsedPublic
      override def privateKey: PrivateKey = parsedPrivate
    }
  }
}
