package net.yoshinorin.qualtet.domains.externalResources

import net.yoshinorin.qualtet.domains.contents.ContentId

trait ExternalResourceRepository[F[_]] {
  def bulkUpsert(data: List[ExternalResource]): F[Int]
  def delete(contentId: ContentId): F[Unit]
  def fakeRequest(): F[Int]
}
