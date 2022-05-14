package net.yoshinorin.qualtet.domains.authors

import cats.effect.IO
import doobie.implicits._
import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.ServiceBase
import net.yoshinorin.qualtet.message.Fail.InternalServerError
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContextBase
import net.yoshinorin.qualtet.domains.authors.RepositoryReqiests._

class AuthorService(implicit doobieContext: DoobieContextBase) extends ServiceBase {

  /**
   * create an authorName
   *
   * @param data Instance of Author
   * @return Instance of created Author with IO
   */
  def create(data: Author): IO[ResponseAuthor] = {

    def makeRequest(data: Author): (Upsert, ConnectionIO[Int] => ConnectionIO[Int]) = {
      val request = Upsert(data)
      val resultHandler: ConnectionIO[Int] => ConnectionIO[Int] = (connectionIO: ConnectionIO[Int]) => { connectionIO }
      (request, resultHandler)
    }

    def run(data: Author): IO[Int] = {
      val (request, _) = makeRequest(data)
      AuthorRepository.dispatch(request).transact(doobieContext.transactor)
    }

    for {
      _ <- run(data)
      a <- findBy(data.name, InternalServerError("user not found"))(this.findByName)
    } yield a
  }

  /**
   * get all Authors
   *
   * @return Authors
   */
  def getAll: IO[Seq[ResponseAuthor]] = {

    def makeRequest(): (GetAll, ConnectionIO[Seq[ResponseAuthor]] => ConnectionIO[Seq[ResponseAuthor]]) = {
      val request = GetAll()
      val resultHandler: ConnectionIO[Seq[ResponseAuthor]] => ConnectionIO[Seq[ResponseAuthor]] = (connectionIO: ConnectionIO[Seq[ResponseAuthor]]) => {
        connectionIO
      }
      (request, resultHandler)
    }

    def run(): IO[Seq[ResponseAuthor]] = {
      val (request, _) = makeRequest()
      AuthorRepository.dispatch(request).transact(doobieContext.transactor)
    }

    run()
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findById(id: AuthorId): IO[Option[ResponseAuthor]] = {

    def makeRequest(id: AuthorId): (FindById, ConnectionIO[Option[ResponseAuthor]] => ConnectionIO[Option[ResponseAuthor]]) = {
      val request = FindById(id)
      val resultHandler: ConnectionIO[Option[ResponseAuthor]] => ConnectionIO[Option[ResponseAuthor]] = (connectionIO: ConnectionIO[Option[ResponseAuthor]]) =>
        {
          connectionIO
        }
      (request, resultHandler)
    }

    def run(id: AuthorId): IO[Option[ResponseAuthor]] = {
      val (request, _) = makeRequest(id)
      AuthorRepository.dispatch(request).transact(doobieContext.transactor)
    }

    run(id)
  }

  /**
   * find an Author by id
   *
   * @param id authorName's id
   * @return Author
   */
  def findByIdWithPassword(id: AuthorId): IO[Option[Author]] = {

    def makeRequest(id: AuthorId): (FindByIdWithPassword, ConnectionIO[Option[Author]] => ConnectionIO[Option[Author]]) = {
      val request = FindByIdWithPassword(id)
      val resultHandler: ConnectionIO[Option[Author]] => ConnectionIO[Option[Author]] = (connectionIO: ConnectionIO[Option[Author]]) => {
        connectionIO
      }
      (request, resultHandler)
    }

    def run(id: AuthorId): IO[Option[Author]] = {
      val (request, _) = makeRequest(id)
      AuthorRepository.dispatch(request).transact(doobieContext.transactor)
    }

    run(id)
  }

  /**
   * find an Author by name
   *
   * @param name authorName's name
   * @return Author
   */
  def findByName(name: AuthorName): IO[Option[ResponseAuthor]] = {

    def makeRequest(name: AuthorName): (FindByName, ConnectionIO[Option[ResponseAuthor]] => ConnectionIO[Option[ResponseAuthor]]) = {
      val request = FindByName(name)
      val resultHandler: ConnectionIO[Option[ResponseAuthor]] => ConnectionIO[Option[ResponseAuthor]] = (connectionIO: ConnectionIO[Option[ResponseAuthor]]) =>
        {
          connectionIO
        }
      (request, resultHandler)
    }

    def run(name: AuthorName): IO[Option[ResponseAuthor]] = {
      val (request, _) = makeRequest(name)
      AuthorRepository.dispatch(request).transact(doobieContext.transactor)
    }

    run(name)
  }

}
