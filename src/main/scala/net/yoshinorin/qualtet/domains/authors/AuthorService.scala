package net.yoshinorin.qualtet.domains.authors

import cats.Monad
import cats.implicits.*
import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}
import net.yoshinorin.qualtet.domains.errors.{DomainError, UnexpectedException}
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.syntax.*

import scala.annotation.nowarn

class AuthorService[G[_]: Monad, F[_]: Monad](
  authorRepositoryAdapter: AuthorRepositoryAdapter[G]
)(using executer: Executer[G, F], loggerFactory: Log4CatsLoggerFactory[F]) {

  private given logger: SelfAwareStructuredLogger[F] = loggerFactory.getLoggerFromClass(this.getClass)

  /**
   * create an authorName
   *
   * @param data Instance of Author
   * @return Instance of created Author
   */
  def create(data: Author): F[Either[DomainError, AuthorResponseModel]] = {
    executer.transact(authorRepositoryAdapter.upsert(data)) *>
      this.findByName(data.name).flatMap {
        case Some(author) => Monad[F].pure(Right(author))
        case None => Left(UnexpectedException("user not found")).logLeft[F](Error)
      }
  }

  /**
   * get all Authors
   *
   * @return Authors
   */
  def getAll: F[Seq[AuthorResponseModel]] = {
    executer.transact(authorRepositoryAdapter.fetch)
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findById(id: AuthorId): F[Option[AuthorResponseModel]] = {
    executer.transact(authorRepositoryAdapter.findById(id))
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findByIdWithPassword(id: AuthorId): F[Option[Author]] = {
    executer.transact(authorRepositoryAdapter.findByIdWithPassword(id))
  }

  /**
   * find an Author by name
   *
   * @param name authorName's name
   * @return Author
   */
  def findByName(name: AuthorName): F[Option[AuthorResponseModel]] = {
    executer.transact(authorRepositoryAdapter.findByName(name))
  }

}
