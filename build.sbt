import java.io.File
import scala.sys.process.Process

organization := "net.yoshinorin"
name := "qualtet"
version := "v1.13.0"
scalaVersion := "2.13.10"

scalacOptions ++= Seq(
  "-Yrangepos",
  "-Ywarn-unused",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-encoding",
  "UTF-8"
)

initialize := {
  val _ = initialize.value
  // Process("git config --local core.hooksPath .githooks").run()
  // println("set the git pre-commit hooks.")
}

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
Compile / compile / wartremoverWarnings ++= Warts.allBut(
  Wart.StringPlusAny,
  Wart.Throw,
  Wart.DefaultArguments,
  Wart.Overloading,
  Wart.Nothing
)

// NOTE: do not bump 2.7.x for a LICENSE reasonse
// https://www.lightbend.com/blog/why-we-are-changing-the-license-for-akka
val akkaVersion = "2.6.20"
val akkaHttpVersion = "10.2.10"
val jsoniterVersion = "2.17.9"
val doobieVersion = "1.0.0-RC2"
val jwtScalaVersion = "9.1.2"
val flywayVersion = "9.8.2"
val http4sVersion = "1.0.0-M37"

// https://github.com/rtimush/sbt-updates
dependencyAllowPreRelease := true
libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.4.2",
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  // NOTE: doobie 1.0.0-RC2 dependes on cats-effect 3.3.4, but cats-effect keep fully compatibility with 3.x
  "org.typelevel" %% "cats-effect" % "3.4.1",
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-ember-client" % http4sVersion % Test,
  "ch.megard" %% "akka-http-cors" % "1.1.3",
  "com.github.jwt-scala" %% "jwt-core" % jwtScalaVersion,
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % jsoniterVersion,
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterVersion % "compile-internal",
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % jsoniterVersion % Test,
  "org.mariadb.jdbc" % "mariadb-java-client" % "3.1.0",
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test,
  "org.wvlet.airframe" %% "airframe-ulid" % "22.11.1",
  "com.github.ben-manes.caffeine" % "caffeine" % "3.1.1",
  "org.flywaydb" % "flyway-core" % flywayVersion,
  // NOTE: workaround: https://github.com/flyway/flyway/issues/3355
  "org.flywaydb" % "flyway-mysql" % flywayVersion,
  "ch.qos.logback" % "logback-classic" % "1.4.5",
  "net.logstash.logback" % "logstash-logback-encoder" % "7.2",
  "org.codehaus.janino" % "janino" % "3.1.9",
  "org.springframework.security" % "spring-security-core" % "5.7.5",
  "org.slf4j" % "slf4j-api" % "2.0.4",
  "org.scalatest" %% "scalatest" % "3.2.14" % "test",
  "org.mockito" % "mockito-core" % "4.9.0" % "test"
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
    // assembly /assemblyJarName := "qualtet.jar"
  )

reStart / mainClass := Some("net.yoshinorin.qualtet.BootStrap")

// skip test when create assembly (because sometimes test fails)
assembly / test := {}

// https://github.com/sbt/sbt-assembly#merge-strategy
// https://github.com/sbt/sbt-assembly/issues/146#issuecomment-601134577
assembly / assemblyMergeStrategy := {
  // case PathList("spring-beans-5.3.14.jar", xs @ _*) => MergeStrategy.last
  // case PathList("spring-context-5.3.14.jar", xs @ _*) => MergeStrategy.last
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
  println("\n ---- db container starting")
  val dockerCommand = Process(s"docker-compose -f ${dockerComposeFilePath.getAbsolutePath} up -d")
  dockerCommand.run

  // workaround
  Thread.sleep(20000)
  println("\n ---- db container started")
}

val shutDownTestDbContainer = TaskKey[Unit]("shutDownTestDbContainer", "Shut down DB container for testing.")
shutDownTestDbContainer := {
  println("\n ---- db container stopping")
  val dockerDownCommand = Process(s"docker-compose -f ${dockerComposeFilePath.getAbsolutePath} down")
  dockerDownCommand.run
  println(" ---- db container stopped\n")
}
// TODO: The DB container does not seems to shutdown if the tests are fails.
val testCommands = {
  """
    |;scalafmt
    |;Test / scalafmt
    |;runTestDbContainer
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

coverageExcludedPackages := "<empty>; net.yoshinorin.qualtet.BootStrap; net.yoshinorin.qualtet.infrastructure.db.Migration;"
//org.scoverage.coveralls.Imports.CoverallsKeys.coverallsGitRepoLocation := Some("..")
