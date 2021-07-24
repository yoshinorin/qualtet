package net.yoshinorin.qualtet.http

final case class ArticlesQueryParamater(
  page: Int = 1,
  limit: Int = 10,
  offset: Int = 0
)

object ArticlesQueryParamater {
  def apply(
    page: Option[Int],
    limit: Option[Int]
  ): ArticlesQueryParamater = {
    new ArticlesQueryParamater(
      page.getOrElse(1) - 1,
      if (limit.getOrElse(10) > 10) 10 else limit.getOrElse(10),
      if (page.getOrElse(1) == 1) 0 else page.getOrElse(1) * 10
    )
  }

}

object QueryParamatersAliases {
  type SqlParams = ArticlesQueryParamater
}
