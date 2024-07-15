package net.yoshinorin.qualtet.domains.authors

import cats.data.ContT
import cats.effect.IO
import cats.Monad
import net.yoshinorin.qualtet.domains.errors.InternalServerError
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.syntax.*

class AuthorService[F[_]: Monad](
  authorRepository: AuthorRepository[F]
)(using executer: Executer[F, IO]) {

  def upsertActions(data: Author): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { next =>
      authorRepository.upsert(data)
    }
  }

  def fetchActions: ContT[F, Seq[ResponseAuthor], Seq[ResponseAuthor]] = {
    ContT.apply[F, Seq[ResponseAuthor], Seq[ResponseAuthor]] { next =>
      authorRepository.getAll()
    }
  }

  def findByIdActions(id: AuthorId): ContT[F, Option[ResponseAuthor], Option[ResponseAuthor]] = {
    ContT.apply[F, Option[ResponseAuthor], Option[ResponseAuthor]] { next =>
      authorRepository.findById(id)
    }
  }

  def findByIdWithPasswordActions(id: AuthorId): ContT[F, Option[Author], Option[Author]] = {
    ContT.apply[F, Option[Author], Option[Author]] { next =>
      authorRepository.findByIdWithPassword(id)
    }
  }

  def findByNameActions(name: AuthorName): ContT[F, Option[ResponseAuthor], Option[ResponseAuthor]] = {
    ContT.apply[F, Option[ResponseAuthor], Option[ResponseAuthor]] { next =>
      authorRepository.findByName(name)
    }
  }

  /**
   * create an authorName
   *
   * @param data Instance of Author
   * @return Instance of created Author with IO
   */
  def create(data: Author): IO[ResponseAuthor] = {
    for {
      _ <- executer.transact(upsertActions(data))
      a <- this.findByName(data.name).throwIfNone(InternalServerError("user not found"))
    } yield a
  }

  /**
   * get all Authors
   *
   * @return Authors
   */
  def getAll: IO[Seq[ResponseAuthor]] = {
    executer.transact(fetchActions)
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findById(id: AuthorId): IO[Option[ResponseAuthor]] = {
    executer.transact(findByIdActions(id))
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findByIdWithPassword(id: AuthorId): IO[Option[Author]] = {
    executer.transact(findByIdWithPasswordActions(id))
  }

  /**
   * find an Author by name
   *
   * @param name authorName's name
   * @return Author
   */
  def findByName(name: AuthorName): IO[Option[ResponseAuthor]] = {
    executer.transact(findByNameActions(name))
  }

}
