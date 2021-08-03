package net.yoshinorin.qualtet.tasks

import net.yoshinorin.qualtet.domains.models.authors.{Author, DoobieAuthorRepository}
import net.yoshinorin.qualtet.domains.services.AuthorService
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext
import io.circe.syntax._
import org.slf4j.LoggerFactory

object CreateUser {

  private[this] val logger = LoggerFactory.getLogger(this.getClass)

  implicit val doobieContext: DoobieContext = new DoobieContext()
  val authorRepository = new DoobieAuthorRepository(doobieContext)
  val authorService: AuthorService = new AuthorService(authorRepository)

  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      logger.error("args must be two length.")
      logger.info("shutting down...")
      return
    }

    val author = for {
      _ <- authorService.create(Author(name = args(0), displayName = args(1)))
      a <- authorService.findByName(args(0))
    } yield a
    val createdAuthor = author.unsafeRunSync()
    val log = createdAuthor.asJson
    logger.info(s"user created: ${log}")
    logger.info("shutting down...")
  }

}
