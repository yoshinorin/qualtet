package net.yoshinorin.qualtet.http.request.query

import cats.implicits.catsSyntaxEq

enum Order(val value: String) {
  case ASC extends Order("ASC")
  case DESC extends Order("DESC")
}

opaque type Page = Int
object Page {
  def apply(value: Int): Page = value

  extension (a: Page) {
    def +(b: Page): Page = a + b
    def -(b: Page): Page = a - b
    def *(b: Page): Page = a * b
    def /(b: Page): Page = a / b
    def toString: String = a.toString
    def toInt: Int = a.toInt
  }
}

opaque type Limit = Int
object Limit {
  def apply(value: Int): Limit = value

  extension (a: Limit) {
    def +(b: Limit): Limit = a + b
    def -(b: Limit): Limit = a - b
    def *(b: Limit): Limit = a * b
    def /(b: Limit): Limit = a / b
    def toString: String = a.toString
    def toInt: Int = a.toInt
  }
}

final case class ArticlesPagination(
  page: Page = Page(1),
  limit: Limit = Limit(10),
  offset: Int = 0,
  order: Order = Order.DESC
)

final case class Pagination(
  page: Option[Page],
  limit: Option[Limit],
  order: Option[Order]
)

object ArticlesPagination {
  def apply(
    page: Option[Page],
    limit: Option[Limit],
    order: Option[Order]
  ): ArticlesPagination = {
    new ArticlesPagination(
      page.getOrElse(Page(1)) - Page(1),
      if (limit.getOrElse(Limit(10)).toInt > 10) Limit(10) else limit.getOrElse(Limit(10)),
      if (page.getOrElse(Page(1)).toInt === Page(1).toInt) 0 else (page.getOrElse(Page(1)).toInt - 1) * 10,
      order.getOrElse(Order.DESC)
    )
  }

}

object QueryParametersAliases {
  type SqlParams = ArticlesPagination
}
