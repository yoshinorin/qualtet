package net.yoshinorin.qualtet.tasks

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import io.circe.syntax._
import org.slf4j.LoggerFactory
import net.yoshinorin.qualtet.domains.models.authors.{Author, AuthorDisplayName, AuthorName, DoobieAuthorRepository}
import net.yoshinorin.qualtet.domains.services.AuthorService
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

object CreateUser {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  implicit val actorSystem: ActorSystem = ActorSystem("qualtet-task")
  implicit val executionContextExecutor: ExecutionContextExecutor = actorSystem.dispatcher

  implicit val doobieContext: DoobieContext = new DoobieContext()
  val authorRepository = new DoobieAuthorRepository(doobieContext)
  val authorService: AuthorService = new AuthorService(authorRepository)

  def main(args: Array[String]): Unit = {
    if (args.length != 3) {
      logger.error("args must be three length.")
      logger.info("shutting down...")
      return
    }

    // https://docs.spring.io/spring-security/site/docs/current/reference/html5/#authentication-password-storage-bcrypt
    val bcryptPasswordEncoder = new BCryptPasswordEncoder()
    val author = for {
      _ <- authorService.create(Author(name = AuthorName(args(0)), displayName = AuthorDisplayName(args(1)), password = bcryptPasswordEncoder.encode(args(2))))
      a <- authorService.findByName(AuthorName(args(0)))
    } yield a
    author.unsafeToFuture().onComplete {
      case Success(author) =>
        logger.info(s"user created: ${author.asJson}")
        logger.info("shutting down...")
        actorSystem.terminate()
      case Failure(ex) =>
        logger.error(ex.getMessage)
        actorSystem.terminate()
    }
  }

}
