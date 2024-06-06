import sbt.*

object Dependencies {

  val jsoniterVersion = "2.30.1"
  val doobieVersion = "1.0.0-RC5"
  val jwtScalaVersion = "10.0.1"
  val flywayVersion = "10.14.0"
  val http4sVersion = "1.0.0-M41"
  val log4catsVersion = "2.7.0"

  val dependencies = Seq(
    "com.typesafe" % "config" % "1.4.3",
    // NOTE: doobie 1.0.0-RC4 and http4s 1.0.0-M40 dependes on cats-effect 3.5.1
    "org.typelevel" %% "cats-effect" % "3.5.4",
    "org.typelevel" %% "log4cats-core" % log4catsVersion,
    "org.typelevel" %% "log4cats-slf4j" % log4catsVersion,
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-ember-client" % http4sVersion % Test,
    "com.github.jwt-scala" %% "jwt-core" % jwtScalaVersion,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % jsoniterVersion,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterVersion % "compile-internal",
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterVersion % "test-internal",
    "org.mariadb.jdbc" % "mariadb-java-client" % "3.3.3",
    "org.tpolecat" %% "doobie-core" % doobieVersion,
    "org.tpolecat" %% "doobie-hikari" % doobieVersion,
    // "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test,
    "org.wvlet.airframe" %% "airframe-ulid" % "24.5.2",
    "com.github.ben-manes.caffeine" % "caffeine" % "3.1.8",
    "org.flywaydb" % "flyway-core" % flywayVersion,
    "org.flywaydb" % "flyway-mysql" % flywayVersion,
    "ch.qos.logback" % "logback-classic" % "1.5.6",
    "net.logstash.logback" % "logstash-logback-encoder" % "7.4",
    "org.codehaus.janino" % "janino" % "3.1.12",
    "org.springframework.security" % "spring-security-core" % "6.3.0",
    "org.slf4j" % "slf4j-api" % "2.0.13",
    "org.scalatest" %% "scalatest" % "3.2.18" % "test",
    "org.mockito" % "mockito-core" % "5.12.0" % "test"
  )
}
