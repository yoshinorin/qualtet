import sbt._
import sbt.protocol.ExecCommand
import java.io.File
import scala.sys.process.Process

object Docker {

  def up_(dockerComposeFilePath: File) = {
    println("\n ---- db container starting")
    val dockerCommand = Process(s"docker-compose -f ${dockerComposeFilePath.getAbsolutePath} up -d")
    dockerCommand.run

    // workaround
    Thread.sleep(20000)
    println("\n ---- db container started")
  }

  def down_(dockerComposeFilePath: File) = {
    println("\n ---- db container stopping")
    val dockerDownCommand = Process(s"docker-compose -f ${dockerComposeFilePath.getAbsolutePath} down")
    dockerDownCommand.run
    println(" ---- db container stopped\n")
  }

  object Local {
    lazy val dockerComposeFilePath = new File("docker/docker-compose.local.yml")
    lazy val up = taskKey[Unit]("run db container for local development")
    lazy val down = taskKey[Unit]("shutdown db container for local development")

    val tasks = Seq(
      up := up_(dockerComposeFilePath),
      down := down_(dockerComposeFilePath)
    )

    object Commands {
      val up = {
        ";runLocalDbContainer"
      }
      val down = {
        ";shutDownLocalDbContainer"
      }
    }
  }

  object Testing {
    lazy val dockerComposeFilePath = new File("src/test/resources/docker-compose.yml")
    lazy val up = taskKey[Unit]("run db container for testing")
    lazy val down = taskKey[Unit]("shutdown db container for testing")

    val tasks = Seq(
      up := up_(dockerComposeFilePath),
      down := down_(dockerComposeFilePath)
    )

    object Commands {
      val runAll = {
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
      val upDbAndCreateMinData = {
        """;runTestDbContainer
          |;testOnly net.yoshinorin.qualtet.infrastructure.db.MigratorSpec
          |;testOnly net.yoshinorin.qualtet.tasks.CreateAuthorSpec
          |""".stripMargin
      }
    }
  }

}
