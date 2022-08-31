package net.yoshinorin.qualtet.tasks

import akka.actor.ActorSystem
import cats.implicits.catsSyntaxEq

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}
import io.circe.syntax._
import net.yoshinorin.qualtet.domains.authors.{Author, AuthorDisplayName, AuthorName, DoobieAuthorRepository, AuthorService, BCryptPassword}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import cats.effect.unsafe.implicits.global

object CreateAuthor {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  implicit val actorSystem: ActorSystem = ActorSystem("qualtet-task")
  implicit val executionContextExecutor: ExecutionContextExecutor = actorSystem.dispatcher

  implicit val doobieContext: DoobieContext = new DoobieContext()
  val authorRepository = new DoobieAuthorRepository()
  val authorService: AuthorService = new AuthorService(authorRepository)(doobieContext)

  def main(args: Array[String]): Unit = {
    if (args.length =!= 3) {
      throw new IllegalArgumentException("args must be three length.")
    }

    // https://docs.spring.io/spring-security/site/docs/current/reference/html5/#authentication-password-storage-bcrypt
    val bcryptPasswordEncoder = new BCryptPasswordEncoder()
    (for {
      _ <- authorService.create(
        Author(name = AuthorName(args(0)), displayName = AuthorDisplayName(args(1)), password = BCryptPassword(bcryptPasswordEncoder.encode(args(2))))
      )
      author <- authorService.findByName(AuthorName(args(0)))
    } yield author).unsafeToFuture().onComplete {
      case Success(author) =>
        logger.info(s"author created: ${author.asJson}")
        logger.info("shutting down...")
        actorSystem.terminate()
      case Failure(ex) =>
        logger.error(ex.getMessage)
        actorSystem.terminate()
    }
  }

}
