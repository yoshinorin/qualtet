package net.yoshinorin.qualtet.tasks

import cats.implicits.*
import cats.effect.{ExitCode, IO, IOApp}
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import net.yoshinorin.qualtet.domains.authors.{AuthorDisplayName, AuthorName, BCryptPassword}
import net.yoshinorin.qualtet.domains.authors.Author
import net.yoshinorin.qualtet.Modules
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.domains.authors.AuthorResponseModel

import cats.effect.unsafe.implicits.global

object CreateOrUpdateAuthor extends IOApp {

  // NOTE: NO-NEED TO USE `log4cats`
  private val logger = LoggerFactory.getLogger(this.getClass)

  def run(args: List[String]): IO[ExitCode] = {
    if (args.length =!= 3) {
      throw new IllegalArgumentException("args must be three length.")
    }

    // https://docs.spring.io/spring-security/site/docs/current/reference/html5/#authentication-password-storage-bcrypt
    val bcryptPasswordEncoder = new BCryptPasswordEncoder()

    Modules.transactorResource(None).use { tx =>
      val modules = new Modules(tx)
      (for {
        authorName <- AuthorName(args(0)).liftTo[IO]
        displayName <- AuthorDisplayName(args(1)).liftTo[IO]
        author <- modules.authorService.create(
          Author(name = authorName, displayName = displayName, password = BCryptPassword(bcryptPasswordEncoder.encode(args(2))))
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
