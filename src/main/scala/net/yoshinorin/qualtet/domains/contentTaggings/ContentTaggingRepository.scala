package net.yoshinorin.qualtet.domains.contentTaggings

import doobie.ConnectionIO
import net.yoshinorin.qualtet.infrastructure.db.doobie.ConnectionIOFaker

object ContentTaggingRepository extends ConnectionIOFaker {

  def dispatch[T](request: ContentTaggingRepositoryRequest[T]): ConnectionIO[T] = request match {
    case BulkUpsert(data) => ContentTaggingQuery.bulkUpsert.updateMany(data)
    case FindByTagId(id) => ContentTaggingQuery.findByTagId(id).to[Seq]
    case DeleteByContentId(id) => ContentTaggingQuery.deleteByContentId(id).option.map(_ => 0)
    case DeleteByTagId(id) => ContentTaggingQuery.deleteByTagId(id).option.map(_ => 0)
    case FakeRequest() => ConnectionIOWithInt
  }

}
