package net.yoshinorin.qualtet.infrastructure.versions

import cats.syntax.eq.*
import net.yoshinorin.qualtet.fixture.unsafe
import net.yoshinorin.qualtet.domains.errors.InvalidVersion
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.infrastructure.versions.VersionSpec
class VersionSpec extends AnyWordSpec {

  "Version" should {
    "default instance" in {
      val version = Version(
        version = VersionString("0.0.0").unsafe
      )

      assert(version.version === VersionString("0.0.0").unsafe)
      assert(version.migrationStatus === MigrationStatus.NOT_REQUIRED)
      assert(version.deployedAt === 0)
    }
  }

  "VersionString" should {

    "applicative" in {
      assert(VersionString("0.0").unsafe.value === "0.0")
      assert(VersionString("1.0.0").unsafe.value === "1.0.0")
      assert(VersionString("2.18.3").unsafe.value === "2.18.3")
      assert(VersionString("1").unsafe.value === "1")
      assert(VersionString("1.2.3.4").unsafe.value === "1.2.3.4")
    }

    "return Left for invalid formats" in {
      val result1 = VersionString("")
      assert(result1.isLeft)
      assert(result1.left.toOption.get.isInstanceOf[InvalidVersion])

      val result2 = VersionString("1.0.0a")
      assert(result2.isLeft)
      assert(result2.left.toOption.get.isInstanceOf[InvalidVersion])

      val result3 = VersionString(".1.0.0")
      assert(result3.isLeft)
      assert(result3.left.toOption.get.isInstanceOf[InvalidVersion])

      val result4 = VersionString("1.0.0.")
      assert(result4.isLeft)
      assert(result4.left.toOption.get.isInstanceOf[InvalidVersion])

      val result5 = VersionString("1..0.0")
      assert(result5.isLeft)
      assert(result5.left.toOption.get.isInstanceOf[InvalidVersion])

      val result6 = VersionString("1.0.0-beta")
      assert(result6.isLeft)
      assert(result6.left.toOption.get.isInstanceOf[InvalidVersion])

      val result7 = VersionString("1 0 0")
      assert(result7.isLeft)
      assert(result7.left.toOption.get.isInstanceOf[InvalidVersion])

      val result8 = VersionString("1.2.3.4.5")
      assert(result8.isLeft)
      assert(result8.left.toOption.get.isInstanceOf[InvalidVersion])
    }

    "Eq" in {
      assert(VersionString("0.0.1").unsafe === VersionString("0.0.1").unsafe)
    }

    "NotEq" in {
      assert(VersionString("0.0.1").unsafe =!= VersionString("0.0.2").unsafe)
    }

  }

  "VersionString.fromTrusted" should {
    "not modify the input for valid version" in {
      val version = VersionString.fromTrusted("1.2.3")
      assert(version.value === "1.2.3")
    }

    "skip validation for invalid version" in {
      val version = VersionString.fromTrusted("invalid")
      assert(version.value === "invalid")
    }

    "skip validation for empty string" in {
      val version = VersionString.fromTrusted("")
      assert(version.value === "")
    }
  }

}
