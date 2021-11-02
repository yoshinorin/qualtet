organization := "net.yoshinorin"
name := "qualtet"
version := "0.0.1"
scalaVersion := "2.13.7"

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
val circeVersion = "0.13.0"
val doobieVersion = "0.13.2"
val jwtScalaVersion = "9.0.0"
libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.4.1",
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "com.github.jwt-scala" %% "jwt-core" % jwtScalaVersion,
  "com.github.jwt-scala" %% "jwt-circe" % jwtScalaVersion,
  "org.mariadb.jdbc" % "mariadb-java-client" % "2.7.2",
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-quill" % doobieVersion,
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test,
  "org.wvlet.airframe" %% "airframe-ulid" % "21.8.1",
  "com.github.ben-manes.caffeine" % "caffeine" % "3.0.3",
  "org.flywaydb" % "flyway-core" % "7.8.2",
  "com.vladsch.flexmark" % "flexmark-all" % "0.62.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.springframework.security" % "spring-security-core" % "5.5.1",
  "org.slf4j" % "slf4j-api" % "1.7.30",
  "org.scalatest" %% "scalatest" % "3.2.8" % "test",
  "org.mockito" % "mockito-core" % "3.10.0" % "test"
)

val createAuthor = inputKey[Unit]("create an author. args must be three. They are 'name', 'displayName' and 'password'")
lazy val root = (project in file("."))
  .settings(
    createAuthor := Def.inputTaskDyn {
      import sbt.Def.spaceDelimited
      val args = spaceDelimited("<args>").parsed
      val task = (Compile / runMain).toTask(s" net.yoshinorin.qualtet.tasks.CreateAuthor ${args.mkString(" ")}")
      task
    }.evaluated
  )

reStart / mainClass := Some("net.yoshinorin.qualtet.BootStrap")

coverageExcludedPackages := "<empty>; net.yoshinorin.qualtet.BootStrap; net.yoshinorin.qualtet.infrastructure.db.Migration; net.yoshinorin.qualtet.http.HttpServer;"
//org.scoverage.coveralls.Imports.CoverallsKeys.coverallsGitRepoLocation := Some("..")
