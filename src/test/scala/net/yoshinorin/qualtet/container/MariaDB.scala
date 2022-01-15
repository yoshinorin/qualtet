package net.yoshinorin.qualtet.container

/* NOTE:
This object is debris of using testcontainers (I can't implement & use it for testing)
Finally I implemented startup-and-shutdown container in sbt (build.sbt)

import doobie.implicits._
import net.yoshinorin.qualtet.domains.models.articles.DoobieArticleRepository
import net.yoshinorin.qualtet.domains.models.contentTypes.{ContentType, DoobieContentTypeRepository}
import net.yoshinorin.qualtet.domains.services.ContentTypeService
import net.yoshinorin.qualtet.fixture.Fixture.{contentTypeId, doobieContext}
import net.yoshinorin.qualtet.http.ArticlesQueryParameter
import net.yoshinorin.qualtet.infrastructure.db.Migration
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase
import net.yoshinorin.qualtet.utils.Cache
import org.scalatest.wordspec.AnyWordSpec

import java.util.concurrent.TimeUnit
import com.github.benmanes.caffeine.cache.{Caffeine, Cache => CaffeineCache}


object MariaDBSpec {

  val dockerComposeFilePath = new File("src/test/resources/docker-compose.yml")

  def run(): Unit = {
    println("=====starting db container")
    val dockerCommand = Process(s"docker-compose -f ${dockerComposeFilePath.getAbsolutePath} up -d")
    dockerCommand.run

    // workaround
    Thread.sleep(25000)
    println("=====started db container")
  }

  def shutDown(): Unit = {
    println("=====stopping db container")
    val dockerDownCommand = s"docker-compose -f ${dockerComposeFilePath.getAbsolutePath} down"
    dockerDownCommand.!!
    println("=====stopped db container")
  }

  /*
  override def beforeAll(): Unit = {
    println("=====starting db container")
    val dockerCommand = Process(s"docker-compose -f ${dockerComposeFilePath.getAbsolutePath} up -d")
    dockerCommand.run

    // workaround
    Thread.sleep(25000)
    println("=====started db container")
  }

  override def afterAll(): Unit = {
    println("=====stopping db container")
    val dockerDownCommand = s"docker-compose -f ${dockerComposeFilePath.getAbsolutePath} down"
    dockerDownCommand.!!
    println("=====stopped db container")
  }
 */

  /*
  override val container: DockerComposeContainer = DockerComposeContainer(
    new File("src/test/resources/docker-compose.yml"),
    exposedServices = Seq(ExposedService("qualtet_test_db", 33066, Wait.forListeningPort)),
    waitingFor = Option(WaitingForService("qualtet_test_db", Wait.forListeningPort()))
  )

 */

  /*
    import org.testcontainers.utility.DockerImageName

    val myImage: DockerImageName = DockerImageName.parse("yoshinorin/docker-mariadb:10.6.3").asCompatibleSubstituteFor("mariadb")

    val container: MariaDBContainer = new MariaDBContainer(
      dockerImageName = myImage,
      dbName = "qualtet_testing",
      dbUsername = Config.dbUser,
      dbPassword = Config.dbPassword
    )
 */
}
 */
