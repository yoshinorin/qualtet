package net.yoshinorin.qualtet.domains.tags

object RepositoryRequests {
  final case class GetAll()
  final case class FindByName(data: TagName)
  final case class BulkUpsert(data: Option[List[Tag]])
}
