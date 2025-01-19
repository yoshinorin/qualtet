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

object PaginationHelper {

  def calcPage(p: Option[Page]): Page = {
    p.getOrElse(Page(1)) - Page(1)
  }

  def calcLimit(l: Option[Limit]): Limit = {
    if (l.getOrElse(Limit(10)).toInt > 10) Limit(10) else l.getOrElse(Limit(10))
  }

  def calcOffset(p: Option[Page]): Int = {
    if (p.getOrElse(Page(1)).toInt === Page(1).toInt) 0 else (p.getOrElse(Page(1)).toInt - 1) * 10
  }

}

object ArticlesPagination {

  import PaginationHelper._

  def apply(
    page: Option[Page],
    limit: Option[Limit],
    order: Option[Order]
  ): ArticlesPagination = {
    new ArticlesPagination(
      page = calcPage(page),
      limit = calcLimit(limit),
      offset = calcOffset(page),
      order.getOrElse(Order.DESC)
    )
  }

}

object QueryParametersAliases {
  type SqlParams = ArticlesPagination
}
