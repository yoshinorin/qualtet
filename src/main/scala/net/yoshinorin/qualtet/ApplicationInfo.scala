package net.yoshinorin.qualtet

import net.yoshinorin.qualtet.buildinfo.BuildInfo
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

lazy val jvmVendor = System.getProperty("java.vendor")
lazy val runtimeVersion = System.getProperty("java.version")
lazy val commitHash = BuildInfo.commitHash.substring(0, 7)

final case class ApplicationInfo(
  name: String = BuildInfo.name,
  version: String = BuildInfo.version,
  scalaVersion: String = BuildInfo.scalaVersion,
  sbtVersion: String = BuildInfo.sbtVersion,
  commitHash: String = commitHash,
  runtime: String = "Java",
  jvmVendor: String = jvmVendor,
  runtimeVersion: String = runtimeVersion
)

object ApplicationInfo {
  given codecAuthor: JsonValueCodec[ApplicationInfo] = JsonCodecMaker.make(
    CodecMakerConfig
      .withRequireCollectionFields(true)
      .withTransientEmpty(false)
      .withSkipNestedOptionValues(false)
      .withSkipUnexpectedFields(false)
      .withTransientEmpty(false)
      .withTransientDefault(false)
  )
}
