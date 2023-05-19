import sbt._

object Dependencies {

  val jsoniterVersion = "2.23.0"
  val doobieVersion = "1.0.0-RC2"
  val jwtScalaVersion = "9.3.0"
  val flywayVersion = "9.18.0"
  val http4sVersion = "1.0.0-M39"

  val dependencies = Seq(
    "com.typesafe" % "config" % "1.4.2",
    // NOTE: doobie 1.0.0-RC2 dependes on cats-effect 3.3.4, but cats-effect keep fully compatibility with 3.x
    "org.typelevel" %% "cats-effect" % "3.4.10",
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-ember-client" % http4sVersion % Test,
    "com.github.jwt-scala" %% "jwt-core" % jwtScalaVersion,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % jsoniterVersion,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterVersion % "compile-internal",
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterVersion % "test-internal",
    "org.mariadb.jdbc" % "mariadb-java-client" % "3.1.4",
    "org.tpolecat" %% "doobie-core" % doobieVersion,
    "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test,
    "org.wvlet.airframe" %% "airframe-ulid" % "23.5.4",
    "com.github.ben-manes.caffeine" % "caffeine" % "3.1.6",
    "org.flywaydb" % "flyway-core" % flywayVersion,
    "org.flywaydb" % "flyway-mysql" % flywayVersion,
    "ch.qos.logback" % "logback-classic" % "1.4.7",
    "net.logstash.logback" % "logstash-logback-encoder" % "7.3",
    "org.codehaus.janino" % "janino" % "3.1.9",
    "org.springframework.security" % "spring-security-core" % "6.1.0",
    "org.slf4j" % "slf4j-api" % "2.0.7",
    "org.scalatest" %% "scalatest" % "3.2.15" % "test",
    "org.mockito" % "mockito-core" % "5.3.1" % "test"
  )
}
