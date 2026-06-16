package net.yoshinorin.qualtet.auth

import java.security.{KeyFactory, PrivateKey, PublicKey}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.util.Base64

object PemParser {

  private val publicKeyHeader = "-----BEGIN PUBLIC KEY-----"
  private val publicKeyFooter = "-----END PUBLIC KEY-----"
  private val privateKeyHeader = "-----BEGIN PRIVATE KEY-----"
  private val privateKeyFooter = "-----END PRIVATE KEY-----"

  def parsePublicKey(algorithm: String, pem: String): PublicKey = {
    val bytes = decodePem(pem, publicKeyHeader, publicKeyFooter)
    KeyFactory.getInstance(algorithm).generatePublic(new X509EncodedKeySpec(bytes))
  }

  def parsePrivateKey(algorithm: String, pem: String): PrivateKey = {
    val bytes = decodePem(pem, privateKeyHeader, privateKeyFooter)
    KeyFactory.getInstance(algorithm).generatePrivate(new PKCS8EncodedKeySpec(bytes))
  }

  private def decodePem(pem: String, header: String, footer: String): Array[Byte] = {
    val trimmed = pem.trim
    val headerIdx = trimmed.indexOf(header)
    val footerIdx = trimmed.indexOf(footer)
    if (headerIdx < 0 || footerIdx < 0 || footerIdx <= headerIdx) {
      throw new IllegalArgumentException(s"PEM is missing expected boundaries: $header / $footer")
    }
    val body = trimmed.substring(headerIdx + header.length, footerIdx).replaceAll("\\s", "")
    Base64.getDecoder.decode(body)
  }
}
