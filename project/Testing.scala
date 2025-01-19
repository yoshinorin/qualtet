import sbt.*
import java.io.File
import scala.sys.process.Process
import console.{ Console => Console_ }
import docker.Docker

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
        |;Test / compile
        |;runTestDbContainer
        |;testOnly net.yoshinorin.qualtet.infrastructure.db.MigratorSpec
        |;testOnly net.yoshinorin.qualtet.tasks.CreateOrUpdateAuthorSpec
        |;test
        |;shutDownTestDbContainer
        |""".stripMargin
    }
    val upDbAndCreateMinData = {
      """;runTestDbContainer
        |;testOnly net.yoshinorin.qualtet.infrastructure.db.MigratorSpec
        |;testOnly net.yoshinorin.qualtet.tasks.CreateOrUpdateAuthorSpec
        |""".stripMargin
    }
  }
}
