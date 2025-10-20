package docker

import sbt.*
import java.io.File
import scala.sys.process.Process
import console.{ Console => Console_ }

trait Docker {
  def up_(containerName: String, dockerComposeFilePath: File) ={
    Console_.info(s"${containerName} container starting")
    val dockerCommand = Process(s"docker compose -f ${dockerComposeFilePath.getAbsolutePath} up -d")
    dockerCommand.!

    // NOTE: workaround for starting up db container (Sometimes migrations fail due to connection errors when running tests on CI.)
    Thread.sleep(10000)
    Console_.info(s"${containerName} container started")
  }

  def down_(containerName: String, dockerComposeFilePath: File) = {
    Console_.info(s"${containerName} container stopping")
    val dockerDownCommand = Process(s"docker compose -f ${dockerComposeFilePath.getAbsolutePath} down")
    dockerDownCommand.!
    Console_.info(s"${containerName} container stopped")
  }
}

class LocalDb extends Docker {
  val dockerComposeFilePath = new File("docker/docker-compose.local.db.yml")
  val upLocalDb = taskKey[Unit]("run db container for local development")
  val downLocalDb = taskKey[Unit]("shutdown db container for local development")

  val tasks = Seq(
    upLocalDb := up_("db", dockerComposeFilePath),
    downLocalDb := down_("db", dockerComposeFilePath)
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

class LocalOtel extends Docker {
  val dockerComposeFilePath = new File("docker/docker-compose.local.otel.yml")
  val upLocalOtel = taskKey[Unit]("run otel container for local development")
  val downLocalOtel = taskKey[Unit]("shutdown otel container for local development")

  val tasks = Seq(
    upLocalOtel := up_("otel", dockerComposeFilePath),
    downLocalOtel := down_("otel", dockerComposeFilePath)
  )

  object Commands {
    val up = {
      ";runLocalOtelContainer"
    }
    val down = {
      ";shutDownLocalOtelContainer"
    }
  }
}
