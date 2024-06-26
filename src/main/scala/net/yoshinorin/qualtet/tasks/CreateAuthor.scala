package net.yoshinorin.qualtet.tasks

import cats.effect.IO
import cats.implicits.catsSyntaxEq
import doobie.ConnectionIO
import doobie.util.transactor.Transactor.Aux
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import net.yoshinorin.qualtet.domains.authors.{Author, AuthorDisplayName, AuthorName, AuthorRepository, AuthorService, BCryptPassword}
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieExecuter
import net.yoshinorin.qualtet.Modules.*
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor

import cats.effect.unsafe.implicits.global
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieTransactor

object CreateAuthor {

  private val logger = LoggerFactory.getLogger(this.getClass)

  val doobieTransactor: DoobieTransactor[Aux] = summon[DoobieTransactor[Aux]]

  given dbContext: DoobieExecuter = new DoobieExecuter(doobieTransactor.make(config.db))
  val authorRepository: AuthorRepository[ConnectionIO] = summon[AuthorRepository[ConnectionIO]]
  val authorService = new AuthorService(authorRepository)

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
    } yield author)
      .handleErrorWith { e =>
        IO.pure(e)
      }
      .unsafeRunSync() match {
      case Some(a: ResponseAuthor) =>
        logger.info(s"author created: ${a.asJson}")
        logger.info("shutting down...")
      case e: Exception =>
        logger.error(e.getMessage)
      case _ =>
        logger.error("unknown error")
    }
  }
}
