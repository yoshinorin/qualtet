package docker

import sbt._
import java.io.File
import scala.sys.process.Process
import scala.Console.{ BLACK_B, GREEN_B, WHITE, RESET }

trait Docker {
  def up_(dockerComposeFilePath: File) ={
    println(s"${RESET}${GREEN_B}${WHITE}\n ---- db container starting ${RESET}")
    val dockerCommand = Process(s"docker-compose -f ${dockerComposeFilePath.getAbsolutePath} up -d")
    dockerCommand.run

    // workaround
    Thread.sleep(20000)
    println(s"${RESET}${GREEN_B}${WHITE}\n ---- db container started ${RESET}")
  }

  def down_(dockerComposeFilePath: File) = {
    println(s"${RESET}${GREEN_B}${WHITE}\n ---- db container stopping ${RESET}")
    val dockerDownCommand = Process(s"docker-compose -f ${dockerComposeFilePath.getAbsolutePath} down")
    dockerDownCommand.run
    println(s"${RESET}${GREEN_B}${WHITE}\n ---- db container stopped ${RESET}")
  }
}

class Local extends Docker {
  val dockerComposeFilePath = new File("docker/docker-compose.local.yml")
  val upLocal = taskKey[Unit]("run db container for local development")
  val downLocal = taskKey[Unit]("shutdown db container for local development")

  val tasks = Seq(
    upLocal := up_(dockerComposeFilePath),
    downLocal := down_(dockerComposeFilePath)
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

class Testing extends Docker {
  val dockerComposeFilePath = new File("src/test/resources/docker-compose.yml")
  val upTesting = taskKey[Unit]("run db container for testing")
  val downTesting = taskKey[Unit]("shutdown db container for testing")

  val tasks = Seq(
    upTesting := up_(dockerComposeFilePath),
    downTesting := down_(dockerComposeFilePath)
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
