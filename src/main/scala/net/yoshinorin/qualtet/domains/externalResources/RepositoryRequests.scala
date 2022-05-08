package net.yoshinorin.qualtet.domains.externalResources

object RepositoryRequests {
  final case class BulkUpsert(data: Option[List[ExternalResource]])
}
