package net.yoshinorin.qualtet.domains.contents

object RepositoryReqiests {
  final case class Upsert(data: Content)
  final case class FindByPath(path: Path)
  final case class FindByPathWithMeta(path: Path)
}
