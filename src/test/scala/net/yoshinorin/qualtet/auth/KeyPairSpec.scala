package net.yoshinorin.qualtet.auth

import org.scalatest.wordspec.AnyWordSpec
import java.security.SecureRandom

// testOnly net.yoshinorin.qualtet.auth.KeyPairSpec
class KeyPairSpec extends AnyWordSpec {

  "KeyPair" should {

    "generate keypair" in {

      val keyPair = new KeyPair("RSA", 2048, SecureRandom.getInstanceStrong)
      assert(keyPair.publicKey.getAlgorithm == "RSA")
      assert(keyPair.publicKey.getFormat == "X.509")
      assert(keyPair.privateKey.getAlgorithm == "RSA")
      assert(keyPair.privateKey.getFormat == "PKCS#8")

    }
  }

}
