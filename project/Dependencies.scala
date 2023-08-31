import sbt.*

object Dependencies {

  val jsoniterVersion = "2.23.2"
  val doobieVersion = "1.0.0-RC4"
  val jwtScalaVersion = "9.4.3"
  val flywayVersion = "9.21.1"
  val http4sVersion = "1.0.0-M40"

  val dependencies = Seq(
    "com.typesafe" % "config" % "1.4.2",
    // NOTE: doobie 1.0.0-RC4 and http4s 1.0.0-M40 dependes on cats-effect 3.5.1
    "org.typelevel" %% "cats-effect" % "3.5.1",
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
    "org.wvlet.airframe" %% "airframe-ulid" % "23.8.2",
    "com.github.ben-manes.caffeine" % "caffeine" % "3.1.8",
    "org.flywaydb" % "flyway-core" % flywayVersion,
    "org.flywaydb" % "flyway-mysql" % flywayVersion,
    "ch.qos.logback" % "logback-classic" % "1.4.11",
    "net.logstash.logback" % "logstash-logback-encoder" % "7.4",
    "org.codehaus.janino" % "janino" % "3.1.10",
    "org.springframework.security" % "spring-security-core" % "6.1.2",
    "org.slf4j" % "slf4j-api" % "2.0.7",
    "org.scalatest" %% "scalatest" % "3.2.16" % "test",
    "org.mockito" % "mockito-core" % "5.4.0" % "test"
  )
}
