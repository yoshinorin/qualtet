package net.yoshinorin.qualtet.auth

import org.slf4j.LoggerFactory

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
}
