import sbt.*

object Dependencies {

  val jsoniterVersion = "2.36.7"
  val doobieVersion = "1.0.0-RC10"
  val jwtScalaVersion = "11.0.2"
  val flywayVersion = "11.10.3"
  val http4sVersion = "1.0.0-M44"
  val log4catsVersion = "2.7.1"

  val dependencies = Seq(
    "com.typesafe" % "config" % "1.4.4",
    // NOTE: doobie 1.0.0-RC4 and http4s 1.0.0-M40 dependes on cats-effect 3.5.1
    "org.typelevel" %% "cats-effect" % "3.6.1",
    "org.typelevel" %% "log4cats-core" % log4catsVersion,
    "org.typelevel" %% "log4cats-slf4j" % log4catsVersion,
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-ember-client" % http4sVersion % Test,
    "com.github.jwt-scala" %% "jwt-core" % jwtScalaVersion,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % jsoniterVersion,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterVersion % "compile-internal",
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterVersion % "test-internal",
    "org.mariadb.jdbc" % "mariadb-java-client" % "3.5.4",
    "org.tpolecat" %% "doobie-core" % doobieVersion,
    "org.tpolecat" %% "doobie-hikari" % doobieVersion,
    // "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test,
    "org.wvlet.airframe" %% "airframe-ulid" % "2025.1.14",
    "com.github.ben-manes.caffeine" % "caffeine" % "3.2.2",
    "org.flywaydb" % "flyway-core" % flywayVersion,
    "org.flywaydb" % "flyway-mysql" % flywayVersion,
    "ch.qos.logback" % "logback-classic" % "1.5.18",
    "net.logstash.logback" % "logstash-logback-encoder" % "8.1",
    "org.codehaus.janino" % "janino" % "3.1.12",
    "org.springframework.security" % "spring-security-core" % "6.5.1",
    "org.slf4j" % "slf4j-api" % "2.0.17",
    "org.scalatest" %% "scalatest" % "3.2.19" % "test",
    "org.mockito" % "mockito-core" % "5.18.0" % "test"
  )
}
