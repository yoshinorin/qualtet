package net.yoshinorin.qualtet.auth

import org.slf4j.LoggerFactory

import java.security
import java.security.{KeyPairGenerator, PrivateKey, PublicKey, SecureRandom}

class KeyPair(algorithm: String, length: Int, secureRandom: SecureRandom) {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)
  private[this] val keyPairGenerator = KeyPairGenerator.getInstance(algorithm)

  keyPairGenerator.initialize(length, secureRandom)

  lazy val keyPair: security.KeyPair = keyPairGenerator.generateKeyPair()
  lazy val publicKey: PublicKey = keyPair.getPublic
  lazy val privateKey: PrivateKey = keyPair.getPrivate

  logger.info(s"key pair generated: ${keyPair.getPublic.getAlgorithm}")

}
