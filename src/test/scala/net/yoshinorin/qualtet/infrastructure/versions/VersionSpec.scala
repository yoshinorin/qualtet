package net.yoshinorin.qualtet.infrastructure.versions

import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.infrastructure.versions.VersionSpec
class VersionSpec extends AnyWordSpec {

  "Version" should {
    "default instance" in {
      val version = Version(
        version = "0.0.0"
      )

      assert(version.version === "0.0.0")
      assert(version.migrationStatus === MigrationStatus.NOT_REQUIRED)
      assert(version.deployedAt === 0)
    }
  }

}
