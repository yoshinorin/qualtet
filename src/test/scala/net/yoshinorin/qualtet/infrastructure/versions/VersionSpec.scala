package net.yoshinorin.qualtet.infrastructure.versions

import cats.syntax.eq.*
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.infrastructure.versions.VersionSpec
class VersionSpec extends AnyWordSpec {

  "Version" should {
    "default instance" in {
      val version = Version(
        version = VersionString("0.0.0")
      )

      assert(version.version === VersionString("0.0.0"))
      assert(version.migrationStatus === MigrationStatus.NOT_REQUIRED)
      assert(version.deployedAt === 0)
    }
  }

  "VersionString" should {

    "applicative" in {
      assert(VersionString("0.0").value === "0.0")
      assert(VersionString("1.0.0").value === "1.0.0")
      assert(VersionString("2.18.3").value === "2.18.3")
      assert(VersionString("1").value === "1")
      assert(VersionString("1.2.3.4").value === "1.2.3.4")
    }

    "throw IllegalArgumentException for invalid formats" in {
      assertThrows[IllegalArgumentException] {
        VersionString("")
      }

      assertThrows[IllegalArgumentException] {
        VersionString("1.0.0a")
      }

      assertThrows[IllegalArgumentException] {
        VersionString(".1.0.0")
      }

      assertThrows[IllegalArgumentException] {
        VersionString("1.0.0.")
      }

      assertThrows[IllegalArgumentException] {
        VersionString("1..0.0")
      }

      assertThrows[IllegalArgumentException] {
        VersionString("1.0.0-beta")
      }

      assertThrows[IllegalArgumentException] {
        VersionString("1 0 0")
      }

      assertThrows[IllegalArgumentException] {
        VersionString("1.0.0a")
      }

      assertThrows[IllegalArgumentException] {
        VersionString("1.2.3.4.5")
      }
    }

    "Eq" in {
      assert(VersionString("0.0.1") === VersionString("0.0.1"))
    }

    "NotEq" in {
      assert(VersionString("0.0.1") =!= VersionString("0.0.2"))
    }

  }

}
