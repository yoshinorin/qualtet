package net.yoshinorin.qualtet.auth

import org.slf4j.LoggerFactory

import java.security
import java.security.{KeyPairGenerator, PrivateKey, PublicKey, SecureRandom}

class KeyPair(algorithm: String, length: Int, secureRandom: SecureRandom) {

  // NOTE: DO NOT USE `log4cats`
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val keyPairGenerator = KeyPairGenerator.getInstance(algorithm)

  keyPairGenerator.initialize(length, secureRandom)

  logger.info("created: keyPair generator")

  // require entropy
  // https://stackoverflow.com/questions/4819359/dev-random-extremely-slow
  private lazy val keyPair: security.KeyPair = keyPairGenerator.generateKeyPair()
  lazy val publicKey: PublicKey = keyPair.getPublic
  lazy val privateKey: PrivateKey = keyPair.getPrivate

  logger.info(s"generated: keyPair - ${keyPair.getPublic.getAlgorithm}")

}
