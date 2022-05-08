package net.yoshinorin.qualtet.domains.contentTypes

object RepositoryRequests {
  final case class GetAll()
  final case class FindByName(name: String)
  final case class Upsert(data: ContentType)
}
