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
    |;testOnly net.yoshinorin.qualtet.infrastructure.db.MigratorSpec
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
    |;testOnly net.yoshinorin.qualtet.infrastructure.db.MigratorSpec
    |;testOnly net.yoshinorin.qualtet.tasks.CreateAuthorSpec
    |""".stripMargin
}
addCommandAlias("testEnvUp", testEnvCommands)


val runLocalDbContainer = TaskKey[Unit]("runLocalDbContainer", "Run DB container for local development.")
val localdockerComposeFilePath = new File("docker/docker-compose.local.yml")
runLocalDbContainer := {
  println("\n ---- db container starting")
  val dockerCommand = Process(s"docker-compose -f ${localdockerComposeFilePath.getAbsolutePath} up -d")
  dockerCommand.run

  // workaround
  Thread.sleep(20000)
  println("\n ---- db container started")
}
val shutDownLocalDbContainer = TaskKey[Unit]("shutDownLocalDbContainer", "Shut down DB container for testing.")
shutDownLocalDbContainer := {
  println("\n ---- db container stopping")
  val dockerDownCommand = Process(s"docker-compose -f ${localdockerComposeFilePath.getAbsolutePath} down")
  dockerDownCommand.run
  println(" ---- db container stopped\n")
}
val runLocalDbCommands = {
  ";runLocalDbContainer"
}
val downLocalDbCommands = {
  ";shutDownLocalDbContainer"
}
addCommandAlias("localDbUp", runLocalDbCommands)
addCommandAlias("localDbDown", downLocalDbCommands)

val forceKillServer = TaskKey[Unit]("forceKillServer","force kill http server")
forceKillServer := {
  // NOTE: require global installed https://github.com/tiaanduplessis/kill-port
  ExecCommand("kill-port 9001 -y")
}
val killCommands = {
  ";forceKillServer"
}
addCommandAlias("kills", killCommands)

val startServerCommands = {
  """
    |;scalafmt
    |;Test / scalafmt
    |;forceKillServer
    |;~reStart
    |""".stripMargin
}
addCommandAlias("runs", startServerCommands)

coverageExcludedPackages := "<empty>; net.yoshinorin.qualtet.BootStrap; net.yoshinorin.qualtet.infrastructure.db.Migrator;"
//org.scoverage.coveralls.Imports.CoverallsKeys.coverallsGitRepoLocation := Some("..")
