organization := "net.yoshinorin"
name := "qualtet"
version := "0.0.1"
scalaVersion := "2.13.5"

scalacOptions ++= Seq(
  "-Yrangepos",
  "-Ywarn-unused",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-encoding",
  "UTF-8"
)

val akkaVersion = "2.6.14"
val akkaHttpVersion = "10.2.4"
libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.4.1",
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.mariadb.jdbc" % "mariadb-java-client" % "2.7.2",
  "org.flywaydb" % "flyway-core" % "7.8.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.slf4j" % "slf4j-api" % "1.7.30",
  "org.scalatest" %% "scalatest" % "3.2.8" % "test",
  "org.mockito" % "mockito-core" % "3.10.0" % "test"
)
