package net.yoshinorin.qualtet.domains.authors

import cats.data.ContT
import cats.Monad
import cats.implicits.*

class AuthorRepositoryAdapter[F[_]: Monad](
  authorRepository: AuthorRepository[F]
) {

  private[domains] def upsert(data: Author): ContT[F, Int, Int] = {
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

  private[domains] def fetch: ContT[F, Seq[AuthorResponseModel], Seq[AuthorResponseModel]] = {
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

  private[domains] def findById(id: AuthorId): ContT[F, Option[AuthorResponseModel], Option[AuthorResponseModel]] = {
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

  private[domains] def findByIdWithPassword(id: AuthorId): ContT[F, Option[Author], Option[Author]] = {
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

  private[domains] def findByName(name: AuthorName): ContT[F, Option[AuthorResponseModel], Option[AuthorResponseModel]] = {
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

}
