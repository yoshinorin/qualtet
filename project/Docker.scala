package docker

import sbt.*
import java.io.File
import scala.sys.process.Process
import console.{ Console => Console_ }

trait Docker {
  def up_(dockerComposeFilePath: File) ={
    Console_.info("db container starting")
    val dockerCommand = Process(s"docker compose -f ${dockerComposeFilePath.getAbsolutePath} up -d")
    dockerCommand.!

    // NOTE: workaround for starting up db container (Sometimes migrations fail due to connection errors when running tests on CI.)
    Thread.sleep(10000)
    Console_.info("db container started")
  }

  def down_(dockerComposeFilePath: File) = {
    Console_.info("db container stopping")
    val dockerDownCommand = Process(s"docker compose -f ${dockerComposeFilePath.getAbsolutePath} down")
    dockerDownCommand.!
    Console_.info("db container stopped")
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
