package net.yoshinorin.qualtet.http

final case class ArticlesQueryParameter(
  page: Int = 1,
  limit: Int = 10,
  offset: Int = 0
)

object ArticlesQueryParameter {
  def apply(
    page: Option[Int],
    limit: Option[Int]
  ): ArticlesQueryParameter = {
    new ArticlesQueryParameter(
      page.getOrElse(1) - 1,
      if (limit.getOrElse(10) > 10) 10 else limit.getOrElse(10),
      if (page.getOrElse(1) == 1) 0 else page.getOrElse(1) * 10
    )
  }

}

object QueryParametersAliases {
  type SqlParams = ArticlesQueryParameter
}
