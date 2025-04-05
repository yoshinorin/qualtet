package net.yoshinorin.qualtet.auth

import org.slf4j.LoggerFactory
import java.security.{PrivateKey, Signature => JavaSignature}

class Signature(algorithm: String, message: Array[Byte], key: KeyPair) {

  // NOTE: DO NOT USE `log4cats`
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val signature = JavaSignature.getInstance(algorithm)

  signature.initSign(key.privateKey)
  signature.update(message)

  private val sign: Array[Byte] = signature.sign()

  logger.info(s"signed: ${signature.getAlgorithm}")

  def signedPrivateKey: PrivateKey = {
    key.privateKey
  }

  def verify(s: Array[Byte]): Boolean = {
    signature.initVerify(key.publicKey)
    signature.update(s)
    signature.verify(sign)
  }
}
