package net.yoshinorin.qualtet.domains.authors

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.authors.RepositoryReqiests._

class DoobieAuthorRepository extends AuthorRepository {

  /**
   * create a authorName
   *
   * @param request Upsert case class
   * @return created Author
   */
  override def dispatch(request: Upsert): ConnectionIO[Int] = {
    DoobieAuthorQuery.upsert.run(request.data)
  }

  /**
   * get all Author
   *
   * @param request GetAll case class
   * @return Authors
   */
  override def dispatch(request: GetAll): ConnectionIO[Seq[ResponseAuthor]] = {
    DoobieAuthorQuery.getAll.to[Seq]
  }

  /**
   * find a Author by id
   *
   * @param request FindById case class
   * @return Author
   */
  override def dispatch(request: FindById): ConnectionIO[Option[ResponseAuthor]] = {
    DoobieAuthorQuery.findById(request.id).option
  }

  /**
   * find a Author by id
   *
   * @param request FindByIdWithPassword case class
   * @return Author
   */
  override def dispatch(request: FindByIdWithPassword): ConnectionIO[Option[Author]] = {
    DoobieAuthorQuery.findByIdWithPassword(request.id).option
  }

  /**
   * find a Author by name
   *
   * @param request FindByName case class
   * @return Author
   */
  override def dispatch(request: FindByName): ConnectionIO[Option[ResponseAuthor]] = {
    DoobieAuthorQuery.findByName(request.name).option
  }
}
