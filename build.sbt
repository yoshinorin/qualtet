organization := "net.yoshinorin"
name := "qualtet"
version := "v2.21.0"
scalaVersion := "3.8.1"
val repository = "https://github.com/yoshinorin/qualtet"

scalacOptions ++= Seq(
  "-encoding",
  "utf8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xkind-projector",
  "-Wsafe-init",
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

val createOrUpdateAuthor = inputKey[Unit]("create an author. args must be three. They are 'name', 'displayName' and 'password'")
val dbMigration = inputKey[Unit]("Migrate database.")
val dbDestroy = inputKey[Unit]("Drop all database objects.")
val dbRecreation = inputKey[Unit]("Drop and recreate all database objects.")
lazy val root = (project in file("."))
  .settings(
    createOrUpdateAuthor := Def.inputTaskDyn {
      import sbt.Def.spaceDelimited
      val args = spaceDelimited("<args>").parsed
      val task = (Compile / runMain).toTask(s" net.yoshinorin.qualtet.tasks.CreateOrUpdateAuthor ${args.mkString(" ")}")
      task
    }.evaluated,
    dbMigration := Def.inputTaskDyn {
      import sbt.Def.spaceDelimited
      val task = (Compile / runMain).toTask(s" net.yoshinorin.qualtet.tasks.db.Migrate")
      task
    }.evaluated,
    dbDestroy := Def.inputTaskDyn {
      import sbt.Def.spaceDelimited
      val task = (Compile / runMain).toTask(s" net.yoshinorin.qualtet.tasks.db.Destroy")
      task
    }.evaluated,
    dbRecreation := Def.inputTaskDyn {
      import sbt.Def.spaceDelimited
      val task = (Compile / runMain).toTask(s" net.yoshinorin.qualtet.tasks.db.Recreate")
      task
    }.evaluated
  )
  .settings(
    libraryDependencies ++= Dependencies.dependencies
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
      "Copyright (c) 2025 @yoshinorin",
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

// Fork a new JVM process for 'sbt run' to enable javaOptions below
// This allows passing system properties (-D flags) to the application JVM
run / fork := true

// NOTE: This javaOptions setting only applies to 'sbt run'. When running the assembled jar,
// you must specify this JVM option manually:
//   java -Dcats.effect.trackFiberContext=true -jar qualtet-assembly-v<version>.jar
javaOptions += "-Dcats.effect.trackFiberContext=true"

reStart / mainClass := Some("net.yoshinorin.qualtet.BootStrap")

// https://github.com/scoverage/sbt-scoverage?tab=readme-ov-file#exclude-classes-and-packages-and-files
coverageExcludedFiles := ".*net.yoshinorin.qualtet.BootStrap;.*net.yoshinorin.qualtet.tasks.db.*;"

coverageExcludedPackages := "target.*;"
