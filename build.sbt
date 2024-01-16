import docker.*
import testing.*
import LocalProcesses.*

organization := "net.yoshinorin"
name := "qualtet"
version := "v2.11.0"
scalaVersion := "3.3.1"
val repository = "https://github.com/yoshinorin/qualtet"

scalacOptions ++= Seq(
  "-encoding",
  "utf8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Ykind-projector",
  "-Wvalue-discard",
  "-Wunused:implicits",
  "-Wunused:explicits",
  "-Wunused:imports",
  "-Wunused:locals",
  "-Wunused:params",
  "-Wunused:privates"
)

// https://github.com/rtimush/sbt-updates
dependencyAllowPreRelease := true

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
    libraryDependencies ++= Dependencies.dependencies
  )
  .settings(
    Compile / compile / wartremoverWarnings ++= Wartremover.rules
  )
  .settings(
    assembly / mainClass := Some("net.yoshinorin.qualtet.BootStrap")
  )
  .settings(
    // for Scaladoc3
    Compile / doc / target := file("./docs/dist"),
    Compile / doc / scalacOptions ++= Seq(
      "-project",
      "Qualtet",
      "-siteroot",
      "docs",
      "-social-links:github::https://github.com/yoshinorin/qualtet",
      "-author",
      "-project-version",
      version.value,
      "-project-footer",
      "Copyright (c) 2024 @yoshinorin",
      "-groups",
      "-default-template",
      "static-site-main",
      "-revision",
      "master"
    )
  )
  .enablePlugins(GitVersioning)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion,
      "commitHash" -> git.gitHeadCommit.value,
      "repository" -> repository
    ),
    //buildInfoPackage := s"${organization}.${name}.buildinfo"
    buildInfoPackage := "net.yoshinorin.qualtet.buildinfo"
  )

reStart / mainClass := Some("net.yoshinorin.qualtet.BootStrap")

// skip test when create assembly (because sometimes test fails)
assembly / test := {}

// https://github.com/sbt/sbt-assembly#merge-strategy
// https://github.com/sbt/sbt-assembly/issues/146#issuecomment-601134577
assembly / assemblyMergeStrategy := {
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

// Register Task and its Commands for testing with container.
val runTestDbContainer = TaskKey[Unit]("runTestDbContainer", "Run DB container for testing.")
val shutDownTestDbContainer = TaskKey[Unit]("shutDownTestDbContainer", "Shut down DB container for testing.")
val testingDocker = new Testing()

testingDocker.tasks
runTestDbContainer := Def.sequential(testingDocker.upTesting).value
shutDownTestDbContainer := Def.sequential(testingDocker.downTesting).value
addCommandAlias("testWithDb", testingDocker.Commands.runAll)
addCommandAlias("testWithDB", testingDocker.Commands.runAll)
addCommandAlias("testDbUp", testingDocker.Commands.upDbAndCreateMinData)
addCommandAlias("testDBUp", testingDocker.Commands.upDbAndCreateMinData)

// Register Task and its Commands for run local db with container.
val runLocalDbContainer = TaskKey[Unit]("runLocalDbContainer", "Run DB container for local development.")
val shutDownLocalDbContainer = TaskKey[Unit]("shutDownLocalDbContainer", "Shut down DB container for local development.")
val localDocker = new Local()

localDocker.tasks
runLocalDbContainer := Def.sequential(localDocker.upLocal).value
shutDownLocalDbContainer := Def.sequential(localDocker.downLocal).value
addCommandAlias("localDbUp", localDocker.Commands.up)
addCommandAlias("localDBUp", localDocker.Commands.up)
addCommandAlias("localDbDown", localDocker.Commands.down)
addCommandAlias("localDBDown", localDocker.Commands.down)

// Register Task and its Commands for kill server and run server locally.
val forceKillServer = TaskKey[Unit]("forceKillServer", "force kill http server")

LocalProcesses.tasks
forceKillServer := Def.sequential(LocalProcesses.kill).value
addCommandAlias("kills", LocalProcesses.Commands.kill)
addCommandAlias("runs", LocalProcesses.Commands.startLocalServer)

// TODO: maybe ignored need update
coverageExcludedPackages := "<empty>; net.yoshinorin.qualtet.BootStrap; net.yoshinorin.qualtet.infrastructure.db.Migrator;"
