package net.yoshinorin.qualtet.tasks

import cats.implicits.catsSyntaxEq
import cats.effect.{ExitCode, IO, IOApp}
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import net.yoshinorin.qualtet.domains.authors.{Author, AuthorDisplayName, AuthorName, BCryptPassword}
import net.yoshinorin.qualtet.Modules
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.domains.authors.ResponseAuthor

import cats.effect.unsafe.implicits.global

object CreateOrUpdateAuthor extends IOApp {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def run(args: List[String]): IO[ExitCode] = {
    if (args.length =!= 3) {
      throw new IllegalArgumentException("args must be three length.")
    }

    // https://docs.spring.io/spring-security/site/docs/current/reference/html5/#authentication-password-storage-bcrypt
    val bcryptPasswordEncoder = new BCryptPasswordEncoder()

    Modules.transactorResource.use { tx =>
      val modules = new Modules(tx)
      (for {
        author <- modules.authorService.create(
          Author(name = AuthorName(args(0)), displayName = AuthorDisplayName(args(1)), password = BCryptPassword(bcryptPasswordEncoder.encode(args(2))))
        )
        _ <- IO(logger.info(s"author created: ${author.asJson}"))
      } yield author)
        .handleErrorWith { e =>
          e match
            case e: Exception =>
              IO(logger.error(e.getMessage))
            case _ =>
              IO(logger.error("unknown error"))
        }
        .unsafeRunSync() // FIXME
      IO(ExitCode.Success)
    }
  }
}
