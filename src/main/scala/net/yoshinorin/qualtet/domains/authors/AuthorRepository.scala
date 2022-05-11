package net.yoshinorin.qualtet.domains.authors

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.authors.RepositoryReqiests._

trait AuthorRepository {

  /**
   * create a authorName
   *
   * @param request Upsert case class
   * @return dummy long id (Doobie return Int)
   */
  def dispatch(request: Upsert): ConnectionIO[Int]

  /**
   * get all Author
   *
   * @param request GetAll case class
   * @return Authors
   */
  def dispatch(request: GetAll): ConnectionIO[Seq[ResponseAuthor]]

  /**
   * find a Author by id
   *
   * @param request FindById case class
   * @return Author
   */
  def dispatch(request: FindById): ConnectionIO[Option[ResponseAuthor]]

  /**
   * find a Author by id
   *
   * @param request FindByIdWithPassword case class
   * @return Author
   */
  def dispatch(request: FindByIdWithPassword): ConnectionIO[Option[Author]]

  /**
   * find a Author by name
   *
   * @param request FindByName case class
   * @return Author
   */
  def dispatch(request: FindByName): ConnectionIO[Option[ResponseAuthor]]
}
