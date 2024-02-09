package net.yoshinorin.qualtet.domains.search

trait SearchRepository[F[_]] {
  def search(query: List[String]): F[Seq[(Int, ResponseSearch)]]
}
