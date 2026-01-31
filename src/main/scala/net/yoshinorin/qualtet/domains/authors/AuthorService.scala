package net.yoshinorin.qualtet.domains.authors

import cats.effect.IO
import cats.Monad
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}
import net.yoshinorin.qualtet.domains.errors.{DomainError, UnexpectedException}
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.syntax.*

class AuthorService[F[_]: Monad](
  authorRepositoryAdapter: AuthorRepositoryAdapter[F]
)(using executer: Executer[F, IO], loggerFactory: Log4CatsLoggerFactory[IO]) {

  private given logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(this.getClass)

  /**
   * create an authorName
   *
   * @param data Instance of Author
   * @return Instance of created Author with IO
   */
  def create(data: Author): IO[Either[DomainError, AuthorResponseModel]] = {
    executer.transact(authorRepositoryAdapter.upsert(data)) *>
      this.findByName(data.name).flatMap {
        case Some(author) => IO.pure(Right(author))
        case None => Left(UnexpectedException("user not found")).logLeft[IO](Error)
      }
  }

  /**
   * get all Authors
   *
   * @return Authors
   */
  def getAll: IO[Seq[AuthorResponseModel]] = {
    executer.transact(authorRepositoryAdapter.fetch)
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findById(id: AuthorId): IO[Option[AuthorResponseModel]] = {
    executer.transact(authorRepositoryAdapter.findById(id))
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findByIdWithPassword(id: AuthorId): IO[Option[Author]] = {
    executer.transact(authorRepositoryAdapter.findByIdWithPassword(id))
  }

  /**
   * find an Author by name
   *
   * @param name authorName's name
   * @return Author
   */
  def findByName(name: AuthorName): IO[Option[AuthorResponseModel]] = {
    executer.transact(authorRepositoryAdapter.findByName(name))
  }

}
