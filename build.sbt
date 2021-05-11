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
  "org.mariadb.jdbc" % "mariadb-java-client" % "2.7.2",
  "org.flywaydb" % "flyway-core" % "7.8.2"
)
