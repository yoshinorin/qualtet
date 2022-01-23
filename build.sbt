import java.io.File
import scala.sys.process.Process

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

val akkaVersion = "2.6.18"
val akkaHttpVersion = "10.2.7"
val circeVersion = "0.14.1"
val doobieVersion = "0.13.4"
val jwtScalaVersion = "9.0.2"

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
  "org.mariadb.jdbc" % "mariadb-java-client" % "2.7.4",
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test,
  "org.wvlet.airframe" %% "airframe-ulid" % "21.12.1",
  "com.github.ben-manes.caffeine" % "caffeine" % "3.0.5",
  "org.flywaydb" % "flyway-core" % "7.8.2",
  "com.vladsch.flexmark" % "flexmark-all" % "0.62.2",
  "ch.qos.logback" % "logback-classic" % "1.2.10",
  "org.springframework.security" % "spring-security-core" % "5.6.1" % "provided",
  "org.slf4j" % "slf4j-api" % "1.7.32",
  "org.scalatest" %% "scalatest" % "3.2.10" % "test",
  "org.mockito" % "mockito-core" % "4.2.0" % "test"
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
  .settings(
    assembly / mainClass := Some("net.yoshinorin.qualtet.BootStrap"),
    assembly /assemblyJarName := "qualtet.jar"
  )

reStart / mainClass := Some("net.yoshinorin.qualtet.BootStrap")

// skip test when create assembly (because sometimes test fails)
test in assembly := {}

// NOTE: testcontiners does not works well...
// https://stackoverflow.com/questions/22321500/how-to-run-task-before-all-tests-from-all-modules-in-sbt
val runTestDbContainer = TaskKey[Unit]("runTestDbContainer", "Run DB container for testing.")
val dockerComposeFilePath = new File("src/test/resources/docker-compose.yml")
runTestDbContainer := {
  println("=====starting db container")
  val dockerCommand = Process(s"docker-compose -f ${dockerComposeFilePath.getAbsolutePath} up -d")
  dockerCommand.run

  // workaround
  Thread.sleep(20000)
  println("=====started db container")
}

val shutDownTestDbContainer = TaskKey[Unit]("shutDownTestDbContainer", "Shut down DB container for testing.")
shutDownTestDbContainer := {
  println("=====stopping db container")
  val dockerDownCommand = Process(s"docker-compose -f ${dockerComposeFilePath.getAbsolutePath} down")
  dockerDownCommand.run
  println("=====stopped db container")
}
// TODO: The DB container does not seems to shutdown if the tests are fails.
val testCommands = {
  """;runTestDbContainer
    |;testOnly net.yoshinorin.qualtet.infrastructure.db.MigrationSpec
    |;testOnly net.yoshinorin.qualtet.tasks.CreateAuthorSpec
    |;test
    |;shutDownTestDbContainer
    |""".stripMargin
}
addCommandAlias("testWithDb", testCommands)
addCommandAlias("testWithDB", testCommands)

// NOTE: Sometimes I want to run testOnly xyz manually.
val testEnvCommands = {
  """;runTestDbContainer
    |;testOnly net.yoshinorin.qualtet.infrastructure.db.MigrationSpec
    |;testOnly net.yoshinorin.qualtet.tasks.CreateAuthorSpec
    |""".stripMargin
}
addCommandAlias("testEnvUp", testEnvCommands)

coverageExcludedPackages := "<empty>; net.yoshinorin.qualtet.BootStrap; net.yoshinorin.qualtet.infrastructure.db.Migration; net.yoshinorin.qualtet.http.HttpServer;"
//org.scoverage.coveralls.Imports.CoverallsKeys.coverallsGitRepoLocation := Some("..")
