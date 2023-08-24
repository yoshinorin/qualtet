package net.yoshinorin.qualtet.domains.search

trait SearchRepository[M[_]] {
  def search(query: List[String]): M[Seq[(Int, ResponseSearch)]]
}
