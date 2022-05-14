package net.yoshinorin.qualtet.domains.authors

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.authors.RepositoryReqiests._

object AuthorRepository {

  /**
   * create a authorName
   *
   * @param request Upsert case class
   * @return created Author
   */
  def dispatch(request: Upsert): ConnectionIO[Int] = {
    AuthorQuery.upsert.run(request.data)
  }

  /**
   * get all Author
   *
   * @param request GetAll case class
   * @return Authors
   */
  def dispatch(request: GetAll): ConnectionIO[Seq[ResponseAuthor]] = {
    AuthorQuery.getAll.to[Seq]
  }

  /**
   * find a Author by id
   *
   * @param request FindById case class
   * @return Author
   */
  def dispatch(request: FindById): ConnectionIO[Option[ResponseAuthor]] = {
    AuthorQuery.findById(request.id).option
  }

  /**
   * find a Author by id
   *
   * @param request FindByIdWithPassword case class
   * @return Author
   */
  def dispatch(request: FindByIdWithPassword): ConnectionIO[Option[Author]] = {
    AuthorQuery.findByIdWithPassword(request.id).option
  }

  /**
   * find a Author by name
   *
   * @param request FindByName case class
   * @return Author
   */
  def dispatch(request: FindByName): ConnectionIO[Option[ResponseAuthor]] = {
    AuthorQuery.findByName(request.name).option
  }
}
