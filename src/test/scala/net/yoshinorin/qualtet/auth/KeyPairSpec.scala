package net.yoshinorin.qualtet.auth

import org.scalatest.wordspec.AnyWordSpec
import java.nio.file.{Files, Path, Paths}
import java.security.{KeyPairGenerator, SecureRandom, Signature as JavaSignature}
import java.util.Base64

// testOnly net.yoshinorin.qualtet.auth.KeyPairRepositorySpec
class KeyPairRepositorySpec extends AnyWordSpec {

  // NOTE: generate the test keypair at runtime so no private key is committed to the repo
  //       (avoids GitHub secret-scanning / gitleaks alerts). Generated once per suite.
  private def toPem(header: String, footer: String, der: Array[Byte]): String =
    s"$header\n${Base64.getMimeEncoder(64, "\n".getBytes("UTF-8")).encodeToString(der)}\n$footer"

  private val (publicKeyPem: String, privateKeyPem: String) = {
    val generator = KeyPairGenerator.getInstance("RSA")
    generator.initialize(2048)
    val keyPair = generator.generateKeyPair()
    (
      toPem("-----BEGIN PUBLIC KEY-----", "-----END PUBLIC KEY-----", keyPair.getPublic.getEncoded),
      toPem("-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----", keyPair.getPrivate.getEncoded)
    )
  }

  private val publicKeyPath: Path = {
    val p = Files.createTempFile("test-public", ".pem")
    Files.writeString(p, publicKeyPem)
    p.toFile.deleteOnExit()
    p
  }
  private val privateKeyPath: Path = {
    val p = Files.createTempFile("test-private", ".pem")
    Files.writeString(p, privateKeyPem)
    p.toFile.deleteOnExit()
    p
  }

  "InMemoryKeyPair given" should {

    "generate keypair" in {
      given InMemoryKeyPairConfig = InMemoryKeyPairConfig("RSA", 2048, SecureRandom.getInstanceStrong)
      val keyPair = summon[KeyPairRepository]
      assert(keyPair.publicKey.getAlgorithm === "RSA")
      assert(keyPair.publicKey.getFormat === "X.509")
      assert(keyPair.privateKey.getAlgorithm === "RSA")
      assert(keyPair.privateKey.getFormat === "PKCS#8")
    }
  }

  "FromFile given" should {

    "load keypair from PEM files" in {
      given FileKeyPairConfig = FileKeyPairConfig("RSA", publicKeyPath, privateKeyPath)
      val keyPair = summon[KeyPairRepository]
      assert(keyPair.publicKey.getAlgorithm === "RSA")
      assert(keyPair.publicKey.getFormat === "X.509")
      assert(keyPair.privateKey.getAlgorithm === "RSA")
      assert(keyPair.privateKey.getFormat === "PKCS#8")
    }

    "throw when file is missing" in {
      given FileKeyPairConfig = FileKeyPairConfig("RSA", Paths.get("/no-such-file-public.pem"), Paths.get("/no-such-file-private.pem"))
      assertThrows[java.io.IOException](summon[KeyPairRepository])
    }

    "throw when PEM content is invalid" in {
      val badPub = Files.createTempFile("bad-pub", ".pem")
      val badPriv = Files.createTempFile("bad-priv", ".pem")
      Files.writeString(badPub, "not a pem")
      Files.writeString(badPriv, "not a pem")
      try {
        given FileKeyPairConfig = FileKeyPairConfig("RSA", badPub, badPriv)
        // NOTE: parsing is eager, so construction itself fails fast.
        assertThrows[IllegalArgumentException](summon[KeyPairRepository])
      } finally {
        val _ = Files.deleteIfExists(badPub)
        val _ = Files.deleteIfExists(badPriv)
      }
    }
  }

  "FromPem given" should {

    "parse keypair from PEM strings" in {
      given PemKeyPairConfig = PemKeyPairConfig("RSA", publicKeyPem, privateKeyPem)
      val keyPair = summon[KeyPairRepository]
      assert(keyPair.publicKey.getAlgorithm === "RSA")
      assert(keyPair.publicKey.getFormat === "X.509")
      assert(keyPair.privateKey.getAlgorithm === "RSA")
      assert(keyPair.privateKey.getFormat === "PKCS#8")
    }

    "throw on empty string" in {
      given PemKeyPairConfig = PemKeyPairConfig("RSA", "", "")
      // NOTE: parsing is eager, so construction itself fails fast.
      assertThrows[IllegalArgumentException](summon[KeyPairRepository])
    }

    "throw on malformed PEM" in {
      given PemKeyPairConfig = PemKeyPairConfig("RSA", "garbage", "garbage")
      // NOTE: parsing is eager, so construction itself fails fast.
      assertThrows[IllegalArgumentException](summon[KeyPairRepository])
    }
  }

  "File <-> Pem" should {

    "produce identical key bytes" in {
      val fromFile = {
        given FileKeyPairConfig = FileKeyPairConfig("RSA", publicKeyPath, privateKeyPath)
        summon[KeyPairRepository]
      }
      val fromPem = {
        given PemKeyPairConfig = PemKeyPairConfig("RSA", publicKeyPem, privateKeyPem)
        summon[KeyPairRepository]
      }
      assert(fromFile.publicKey.getEncoded.sameElements(fromPem.publicKey.getEncoded))
      assert(fromFile.privateKey.getEncoded.sameElements(fromPem.privateKey.getEncoded))
    }
  }

  "sign / verify" should {

    def signAndVerify(keyPair: KeyPairRepository): org.scalatest.compatible.Assertion = {
      val message = "hello qualtet".getBytes("UTF-8")
      val signer = JavaSignature.getInstance("SHA256withRSA")
      signer.initSign(keyPair.privateKey)
      signer.update(message)
      val signed = signer.sign()
      val verifier = JavaSignature.getInstance("SHA256withRSA")
      verifier.initVerify(keyPair.publicKey)
      verifier.update(message)
      assert(verifier.verify(signed))
    }

    "round-trip with InMemory keyPair" in {
      given InMemoryKeyPairConfig = InMemoryKeyPairConfig("RSA", 2048, SecureRandom.getInstanceStrong)
      signAndVerify(summon[KeyPairRepository])
    }

    "round-trip with File keyPair" in {
      given FileKeyPairConfig = FileKeyPairConfig("RSA", publicKeyPath, privateKeyPath)
      signAndVerify(summon[KeyPairRepository])
    }

    "round-trip with Pem keyPair" in {
      given PemKeyPairConfig = PemKeyPairConfig("RSA", publicKeyPem, privateKeyPem)
      signAndVerify(summon[KeyPairRepository])
    }
  }
}
