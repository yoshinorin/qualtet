package net.yoshinorin.qualtet.auth

import org.scalatest.wordspec.AnyWordSpec
import java.security.SecureRandom

// testOnly net.yoshinorin.qualtet.auth.SignatureSpec
class SignatureSpec extends AnyWordSpec {

  "Signature" should {

    val keyPair = new KeyPair("RSA", 2048, SecureRandom.getInstanceStrong)
    val message = SecureRandom.getInstanceStrong.toString.getBytes
    val signature = new net.yoshinorin.qualtet.auth.Signature("SHA256withRSA", message, keyPair)

    "verify - success" in {
      assert(signature.verify(message))
    }

    "verify - failure" in {
      assert(!signature.verify("wrong".getBytes))
    }

  }

}
