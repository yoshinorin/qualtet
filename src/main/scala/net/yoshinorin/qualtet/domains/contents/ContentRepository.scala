package net.yoshinorin.qualtet.domains.contents

import doobie.ConnectionIO

object ContentRepository {

  def dispatch[T](request: ContentRepositoryRequest[T]): ConnectionIO[T] = request match {
    case Upsert(data) => ContentQuery.upsert.run(data)
    case FindByPath(path) => ContentQuery.findByPath(path).option
    case FindByPathWithMeta(path) => ContentQuery.findByPathWithMeta(path).unique
    // TODO: fix return type `Int` to `Unit`
    case Delete(id) => ContentQuery.delete(id).option.map(_ => 0)
  }

}
