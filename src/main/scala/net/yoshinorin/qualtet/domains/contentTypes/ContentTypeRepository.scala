package net.yoshinorin.qualtet.domains.contentTypes

import doobie.ConnectionIO

object ContentTypeRepository {

  def dispatch[T](request: ContentTypeRepositoryRequest[T]): ConnectionIO[T] = request match {
    case GetAll() => ContentTypeQuery.getAll.to[Seq]
    case Upsert(data) => ContentTypeQuery.upsert.run(data)
    case FindByName(name) => ContentTypeQuery.findByName(name).option
  }

}
