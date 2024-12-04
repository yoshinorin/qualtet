package net.yoshinorin.qualtet.domains.authors

import cats.data.ContT
import cats.effect.IO
import cats.Monad
import cats.implicits.*
import net.yoshinorin.qualtet.domains.errors.UnexpectedException
import net.yoshinorin.qualtet.infrastructure.db.Executer
import net.yoshinorin.qualtet.syntax.*

class AuthorService[F[_]: Monad](
  authorRepository: AuthorRepository[F]
)(using executer: Executer[F, IO]) {

  def upsertCont(data: Author): ContT[F, Int, Int] = {
    ContT.apply[F, Int, Int] { next =>
      val w = AuthorWriteModel(
        id = data.id,
        name = data.name,
        displayName = data.displayName,
        password = data.password,
        createdAt = data.createdAt
      )
      authorRepository.upsert(w)
    }
  }

  def fetchCont: ContT[F, Seq[AuthorResponseModel], Seq[AuthorResponseModel]] = {
    ContT.apply[F, Seq[AuthorResponseModel], Seq[AuthorResponseModel]] { next =>
      authorRepository.getAll().map { authors =>
        authors.map { author =>
          AuthorResponseModel(
            id = author.id,
            name = author.name,
            displayName = author.displayName,
            createdAt = author.createdAt
          )
        }
      }
    }
  }

  def findByIdCont(id: AuthorId): ContT[F, Option[AuthorResponseModel], Option[AuthorResponseModel]] = {
    ContT.apply[F, Option[AuthorResponseModel], Option[AuthorResponseModel]] { next =>
      authorRepository.findById(id).map { author =>
        author.map { a =>
          AuthorResponseModel(
            id = a.id,
            name = a.name,
            displayName = a.displayName,
            createdAt = a.createdAt
          )
        }
      }
    }
  }

  def findByIdWithPasswordCont(id: AuthorId): ContT[F, Option[Author], Option[Author]] = {
    ContT.apply[F, Option[Author], Option[Author]] { next =>
      authorRepository.findByIdWithPassword(id).map { author =>
        author match {
          case Some(a) =>
            Some(
              Author(
                id = a.id,
                name = a.name,
                displayName = a.displayName,
                password = a.password,
                createdAt = a.createdAt
              )
            )
          case None => None
        }
      }
    }
  }

  def findByNameCont(name: AuthorName): ContT[F, Option[AuthorResponseModel], Option[AuthorResponseModel]] = {
    ContT.apply[F, Option[AuthorResponseModel], Option[AuthorResponseModel]] { next =>
      authorRepository.findByName(name).map { author =>
        author.map { a =>
          AuthorResponseModel(
            id = a.id,
            name = a.name,
            displayName = a.displayName,
            createdAt = a.createdAt
          )
        }
      }
    }
  }

  /**
   * create an authorName
   *
   * @param data Instance of Author
   * @return Instance of created Author with IO
   */
  def create(data: Author): IO[AuthorResponseModel] = {
    for {
      _ <- executer.transact(upsertCont(data))
      a <- this.findByName(data.name).throwIfNone(UnexpectedException("user not found"))
    } yield a
  }

  /**
   * get all Authors
   *
   * @return Authors
   */
  def getAll: IO[Seq[AuthorResponseModel]] = {
    executer.transact(fetchCont)
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findById(id: AuthorId): IO[Option[AuthorResponseModel]] = {
    executer.transact(findByIdCont(id))
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findByIdWithPassword(id: AuthorId): IO[Option[Author]] = {
    executer.transact(findByIdWithPasswordCont(id))
  }

  /**
   * find an Author by name
   *
   * @param name authorName's name
   * @return Author
   */
  def findByName(name: AuthorName): IO[Option[AuthorResponseModel]] = {
    executer.transact(findByNameCont(name))
  }

}
