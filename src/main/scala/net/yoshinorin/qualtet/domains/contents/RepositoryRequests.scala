package net.yoshinorin.qualtet.domains.contents

import net.yoshinorin.qualtet.domains.repository.requests._

trait ContentRepositoryRequest[T] extends RepositoryRequest[T]
final case class Upsert(data: Content) extends ContentRepositoryRequest[Int]
final case class FindByPath(path: Path) extends ContentRepositoryRequest[Option[Content]]
final case class FindByPathWithMeta(path: Path) extends ContentRepositoryRequest[Option[ResponseContentDbRow]]
