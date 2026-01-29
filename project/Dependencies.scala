import sbt.*

object Dependencies {

  val jsoniterVersion = "2.38.8"
  val doobieVersion = "1.0.0-RC11"
  val jwtScalaVersion = "11.0.3"
  val flywayVersion = "12.0.0"
  val http4sVersion = "1.0.0-M45"
  val log4catsVersion = "2.7.1"
  val otel = "1.58.0"
  val otelInstrumentation = "2.24.0-alpha"

  val dependencies = Seq(
    "com.typesafe" % "config" % "1.4.5",
    // NOTE: doobie 1.0.0-RC4 and http4s 1.0.0-M40 dependes on cats-effect 3.5.1
    "org.typelevel" %% "cats-effect" % "3.6.3",
    "org.typelevel" %% "log4cats-core" % log4catsVersion,
    "org.typelevel" %% "log4cats-slf4j" % log4catsVersion,
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-ember-client" % http4sVersion % Test,
    "org.typelevel" %% "otel4s-oteljava" % "0.15.0",
    "org.typelevel" %% "otel4s-instrumentation-metrics" % "0.15.0",
    "io.opentelemetry.instrumentation" % "opentelemetry-runtime-telemetry-java17" % otelInstrumentation,
    "io.opentelemetry.instrumentation" % "opentelemetry-logback-appender-1.0" % otelInstrumentation,
    "io.opentelemetry" % "opentelemetry-exporter-otlp" % otel,
    "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % otel,
    "com.github.jwt-scala" %% "jwt-core" % jwtScalaVersion,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % jsoniterVersion,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterVersion % "compile-internal",
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterVersion % "test-internal",
    "org.mariadb.jdbc" % "mariadb-java-client" % "3.5.7",
    "org.tpolecat" %% "doobie-core" % doobieVersion,
    "org.tpolecat" %% "doobie-hikari" % doobieVersion,
    // "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test,
    "org.wvlet.airframe" %% "airframe-ulid" % "2025.1.27",
    "com.github.ben-manes.caffeine" % "caffeine" % "3.2.3",
    "org.flywaydb" % "flyway-core" % flywayVersion,
    "org.flywaydb" % "flyway-mysql" % flywayVersion,
    "ch.qos.logback" % "logback-classic" % "1.5.26",
    "net.logstash.logback" % "logstash-logback-encoder" % "9.0",
    "org.codehaus.janino" % "janino" % "3.1.12",
    "org.springframework.security" % "spring-security-core" % "7.0.2",
    "org.slf4j" % "slf4j-api" % "2.0.17",
    "org.scalatest" %% "scalatest" % "3.2.19" % "test",
    "org.mockito" % "mockito-core" % "5.21.0" % "test"
  )
}
