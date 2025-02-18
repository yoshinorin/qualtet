import sbt.*

object Dependencies {

  val jsoniterVersion = "2.33.0"
  val doobieVersion = "1.0.0-RC7"
  val jwtScalaVersion = "10.0.1"
  val flywayVersion = "11.2.0"
  val http4sVersion = "1.0.0-M44"
  val log4catsVersion = "2.7.0"

  val dependencies = Seq(
    "com.typesafe" % "config" % "1.4.3",
    // NOTE: doobie 1.0.0-RC4 and http4s 1.0.0-M40 dependes on cats-effect 3.5.1
    "org.typelevel" %% "cats-effect" % "3.5.7",
    "org.typelevel" %% "log4cats-core" % log4catsVersion,
    "org.typelevel" %% "log4cats-slf4j" % log4catsVersion,
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-ember-client" % http4sVersion % Test,
    "com.github.jwt-scala" %% "jwt-core" % jwtScalaVersion,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % jsoniterVersion,
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterVersion % "compile-internal",
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterVersion % "test-internal",
    "org.mariadb.jdbc" % "mariadb-java-client" % "3.5.1",
    "org.tpolecat" %% "doobie-core" % doobieVersion,
    "org.tpolecat" %% "doobie-hikari" % doobieVersion,
    // "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test,
    "org.wvlet.airframe" %% "airframe-ulid" % "2025.1.0",
    "com.github.ben-manes.caffeine" % "caffeine" % "3.2.0",
    "org.flywaydb" % "flyway-core" % flywayVersion,
    "org.flywaydb" % "flyway-mysql" % flywayVersion,
    "ch.qos.logback" % "logback-classic" % "1.5.16",
    "net.logstash.logback" % "logstash-logback-encoder" % "8.0",
    "org.codehaus.janino" % "janino" % "3.1.12",
    "org.springframework.security" % "spring-security-core" % "6.4.2",
    "org.slf4j" % "slf4j-api" % "2.0.16",
    "org.scalatest" %% "scalatest" % "3.2.19" % "test",
    "org.mockito" % "mockito-core" % "5.15.2" % "test"
  )
}
