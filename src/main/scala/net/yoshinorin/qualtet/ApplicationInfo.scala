package net.yoshinorin.qualtet

import net.yoshinorin.qualtet.buildinfo.BuildInfo
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.buildinfo.BuildInfo.repository

lazy val jvmVendor = System.getProperty("java.vendor")
lazy val runtimeVersion = System.getProperty("java.version")
lazy val commitHash = BuildInfo.commitHash match {
  case Some(c) if c.length() >= 7 => c.substring(0, 7)
  case _ => "N/A"
}
lazy val runtime = Runtime()
lazy val build = Build()

private final case class Runtime(
  name: String = "Java",
  vendor: String = jvmVendor,
  version: String = runtimeVersion
)

private final case class Build(
  commit: String = commitHash,
  url: String = s"${BuildInfo.repository}/commit/${commitHash}",
  scalaVersion: String = BuildInfo.scalaVersion,
  sbtVersion: String = BuildInfo.sbtVersion
)

private final case class ApplicationInfo(
  name: String = BuildInfo.name,
  version: String = BuildInfo.version,
  repository: String = BuildInfo.repository,
  runtime: Runtime = runtime,
  build: Build = build
)

object ApplicationInfo {

  import net.yoshinorin.qualtet.syntax.*

  lazy val asJson = ApplicationInfo().asJson

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
