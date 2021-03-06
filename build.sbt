import java.io.File
import scala.sys.process.Process

organization := "net.yoshinorin"
name := "qualtet"
version := "v1.4.0"
scalaVersion := "2.13.8"

scalacOptions ++= Seq(
  "-Yrangepos",
  "-Ywarn-unused",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-encoding",
  "UTF-8"
)

// https://scalacenter.github.io/scalafix/docs/users/installation.html
ThisBuild / scalafixScalaBinaryVersion := "2.13"
// TODO: AggressiveMerge or Merge not working
// ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
inThisBuild(
  List(
    scalaVersion := scalaVersion.value,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

// https://www.wartremover.org/
Compile / compile / wartremoverWarnings ++= Warts.allBut(Wart.StringPlusAny)

val akkaVersion = "2.6.19"
val akkaHttpVersion = "10.2.9"
val circeVersion = "0.14.2"
val doobieVersion = "0.13.4"
val jwtScalaVersion = "9.0.5"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.4.2",
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "ch.megard" %% "akka-http-cors" % "1.1.3",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "com.github.jwt-scala" %% "jwt-core" % jwtScalaVersion,
  "com.github.jwt-scala" %% "jwt-circe" % jwtScalaVersion,
  "org.mariadb.jdbc" % "mariadb-java-client" % "3.0.5",
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test,
  "org.wvlet.airframe" %% "airframe-ulid" % "22.6.2",
  "com.github.ben-manes.caffeine" % "caffeine" % "3.1.1",
  // TODO: Upgrade higher than 8.2.2
  //       https://github.com/flyway/flyway/issues/3355
  "org.flywaydb" % "flyway-core" % "8.2.0",
  "ch.qos.logback" % "logback-classic" % "1.2.11",
  "org.springframework.security" % "spring-security-core" % "5.7.2",
  "org.slf4j" % "slf4j-api" % "1.7.36",
  "org.scalatest" %% "scalatest" % "3.2.12" % "test",
  "org.mockito" % "mockito-core" % "4.6.1" % "test"
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
    assembly / mainClass := Some("net.yoshinorin.qualtet.BootStrap")
    //assembly /assemblyJarName := "qualtet.jar"
  )

reStart / mainClass := Some("net.yoshinorin.qualtet.BootStrap")

// skip test when create assembly (because sometimes test fails)
assembly / test := {}

// https://github.com/sbt/sbt-assembly#merge-strategy
// https://github.com/sbt/sbt-assembly/issues/146#issuecomment-601134577
assembly / assemblyMergeStrategy := {
  //case PathList("spring-beans-5.3.14.jar", xs @ _*) => MergeStrategy.last
  //case PathList("spring-context-5.3.14.jar", xs @ _*) => MergeStrategy.last
  case x if Assembly.isConfigFile(x) =>
    MergeStrategy.concat
  case PathList(ps @ _*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
    MergeStrategy.rename
  case PathList("META-INF", xs @ _*) =>
    (xs map { _.toLowerCase }) match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
        MergeStrategy.discard
      case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
        MergeStrategy.discard
      case "plexus" :: xs =>
        MergeStrategy.discard
      case "services" :: xs =>
        MergeStrategy.filterDistinctLines
      case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
        MergeStrategy.filterDistinctLines

      case _ => MergeStrategy.last
    }
  case _ => MergeStrategy.first
}

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
