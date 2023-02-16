import Docker._
import LocalProcesses._
import sbt.protocol.ExecCommand
import java.io.File
import scala.sys.process.Process

organization := "net.yoshinorin"
name := "qualtet"
version := "v2.3.0"
scalaVersion := "3.2.1"

scalacOptions ++= Seq(
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
ThisBuild / scalafixScalaBinaryVersion := "3.2.1"
// TODO: AggressiveMerge or Merge not working
// ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
inThisBuild(
  List(
    scalaVersion := scalaVersion.value,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
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
      "-project", "Qualtet",
      "-siteroot", "docs",
      "-social-links:github::https://github.com/yoshinorin/qualtet",
      "-author",
      "-project-version", version.value,
      "-project-footer", "Copyright (c) 2022 @yoshinorin",
      "-groups",
      "-default-template", "static-site-main",
      "-revision", "master"
    )
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

Docker.Testing.tasks
runTestDbContainer := Def.sequential(Docker.Testing.up).value
shutDownTestDbContainer := Def.sequential(Docker.Testing.down).value
addCommandAlias("testWithDb", Docker.Testing.Commands.runAll)
addCommandAlias("testWithDB", Docker.Testing.Commands.runAll)
addCommandAlias("testDbUp", Docker.Testing.Commands.upDbAndCreateMinData)
addCommandAlias("testDBUp", Docker.Testing.Commands.upDbAndCreateMinData)


// Register Task and its Commands for run local db with container.
val runLocalDbContainer = TaskKey[Unit]("runLocalDbContainer", "Run DB container for local development.")
val shutDownLocalDbContainer = TaskKey[Unit]("shutDownLocalDbContainer", "Shut down DB container for local development.")

Docker.Local.tasks
runLocalDbContainer := Def.sequential(Docker.Local.up).value
shutDownLocalDbContainer := Def.sequential(Docker.Local.down).value
addCommandAlias("localDbUp", Docker.Local.Commands.up)
addCommandAlias("localDBUp", Docker.Local.Commands.up)
addCommandAlias("localDbDown", Docker.Local.Commands.down)
addCommandAlias("localDBDown", Docker.Local.Commands.down)


// Register Task and its Commands for kill server and run server locally.
val forceKillServer = TaskKey[Unit]("forceKillServer","force kill http server")

LocalProcesses.tasks
forceKillServer := Def.sequential(LocalProcesses.kill).value
addCommandAlias("kills", LocalProcesses.Commands.kill)
addCommandAlias("runs", LocalProcesses.Commands.startLocalServer)

coverageExcludedPackages := "<empty>; net.yoshinorin.qualtet.BootStrap; net.yoshinorin.qualtet.infrastructure.db.Migrator;"
//org.scoverage.coveralls.Imports.CoverallsKeys.coverallsGitRepoLocation := Some("..")
