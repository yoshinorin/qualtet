package net.yoshinorin.qualtet.domains.contentTaggings

import cats.effect.IO
import cats.Monad
import net.yoshinorin.qualtet.domains.tags.TagId
import net.yoshinorin.qualtet.infrastructure.db.Executer

class ContentTaggingService[F[_]: Monad](
  contentTaggingRepositoryAdapter: ContentTaggingRepositoryAdapter[F]
)(using executer: Executer[F, IO]) {

  def findByTagId(id: TagId): IO[Seq[ContentTagging]] = {
    executer.transact(contentTaggingRepositoryAdapter.findByTagId(id))
  }
}
