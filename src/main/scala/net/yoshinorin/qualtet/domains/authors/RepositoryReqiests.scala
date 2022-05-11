package net.yoshinorin.qualtet.domains.authors

object RepositoryReqiests {
  final case class Upsert(data: Author)
  final case class GetAll()
  final case class FindById(id: AuthorId)
  final case class FindByIdWithPassword(id: AuthorId)
  final case class FindByName(name: AuthorName)
}
