package net.yoshinorin.qualtet.auth

import org.slf4j.LoggerFactory
import java.security.{PrivateKey, Signature}

class Signature(algorithm: String, message: Array[Byte], key: KeyPair) {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)
  private[this] val signature = Signature.getInstance(algorithm)

  signature.initSign(key.privateKey)
  signature.update(message)

  val sign: Array[Byte] = signature.sign()

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
