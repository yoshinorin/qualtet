package net.yoshinorin.qualtet.domains.externalResources

import net.yoshinorin.qualtet.domains.contents.ContentId

trait ExternalResourceRepository[M[_]] {
  def bulkUpsert(data: List[ExternalResource]): M[Int]
  def delete(contentId: ContentId): M[Unit]
  def fakeRequest(): M[Int]
}
