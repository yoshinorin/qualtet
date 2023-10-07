import sbt.*

object Dependencies {

  val jsoniterVersion = "2.24.1"
  val doobieVersion = "1.0.0-RC4"
  val jwtScalaVersion = "9.4.3"
  val flywayVersion = "9.22.2"
  val http4sVersion = "1.0.0-M40"

  val dependencies = Seq(
    "com.typesafe" % "config" % "1.4.2",
    // NOTE: doobie 1.0.0-RC4 and http4s 1.0.0-M40 dependes on cats-effect 3.5.1
    "org.typelevel" %% "cats-effect" % "3.5.2",
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-ember-client" % http4sVersion % Test,
    "com.github.jwt-scala" %% "jwt-core" % jwtScalaVersion,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % jsoniterVersion,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterVersion % "compile-internal",
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterVersion % "test-internal",
    "org.mariadb.jdbc" % "mariadb-java-client" % "3.2.0",
    "org.tpolecat" %% "doobie-core" % doobieVersion,
    "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test,
    "org.wvlet.airframe" %% "airframe-ulid" % "23.9.3",
    "com.github.ben-manes.caffeine" % "caffeine" % "3.1.8",
    "org.flywaydb" % "flyway-core" % flywayVersion,
    "org.flywaydb" % "flyway-mysql" % flywayVersion,
    "ch.qos.logback" % "logback-classic" % "1.4.11",
    "net.logstash.logback" % "logstash-logback-encoder" % "7.4",
    "org.codehaus.janino" % "janino" % "3.1.10",
    "org.springframework.security" % "spring-security-core" % "6.1.4",
    "org.slf4j" % "slf4j-api" % "2.0.9",
    "org.scalatest" %% "scalatest" % "3.2.16" % "test",
    "org.mockito" % "mockito-core" % "5.6.0" % "test"
  )
}
