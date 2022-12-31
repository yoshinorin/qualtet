package net.yoshinorin.qualtet.domains.search

import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.http.QueryParametersAliases.SqlParams

trait SearchRepository[M[_]] {
  def search(query: List[String]): M[Seq[(Int, ResponseSearch)]]
}
