package net.yoshinorin.qualtet.domains.authors

import doobie.ConnectionIO

object AuthorRepository {

  def dispatch[T](request: AuthorRepositoryRequest[T]): ConnectionIO[T] = request match {
    case GetAll() => AuthorQuery.getAll.to[Seq]
    case Upsert(data) => AuthorQuery.upsert.run(data)
    case FindById(id) => AuthorQuery.findById(id).option
    case FindByIdWithPassword(id) => AuthorQuery.findByIdWithPassword(id).option
    case FindByName(name) => AuthorQuery.findByName(name).option
  }

}
