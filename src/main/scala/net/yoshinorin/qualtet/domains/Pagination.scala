package net.yoshinorin.qualtet.domains

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

trait PaginationOps[T] {
  def make(p: PaginationRequestModel): T

  def make(page: Option[Page], limit: Option[Limit], order: Option[Order]): T

  def make(page: Page, limit: Limit, order: Order): T = this.make(Option(page), Option(limit), Option(order))

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

final case class PaginationRequestModel(
  page: Option[Page],
  limit: Option[Limit],
  order: Option[Order]
)

sealed trait Pagination {
  def page: Page
  def limit: Limit
  def offset: Int
  def order: Order
}

final case class ArticlesPagination(
  page: Page = Page(1),
  limit: Limit = Limit(10),
  offset: Int = 0,
  order: Order = Order.DESC
) extends Pagination

final case class TagsPagination(
  page: Page = Page(1),
  limit: Limit = Limit(10),
  offset: Int = 0,
  order: Order = Order.DESC
) extends Pagination

final case class FeedsPagination(
  page: Page = Page(1),
  limit: Limit = Limit(5),
  offset: Int = 0,
  order: Order = Order.DESC
) extends Pagination

object Pagination {

  given ArticlesPagination: PaginationOps[ArticlesPagination] = {
    new PaginationOps[ArticlesPagination] {
      override def make(p: PaginationRequestModel): ArticlesPagination = {
        new ArticlesPagination(
          page = calcPage(p.page),
          limit = calcLimit(p.limit),
          offset = calcOffset(p.page),
          order = p.order.getOrElse(Order.DESC)
        )
      }

      override def make(page: Option[Page], limit: Option[Limit], order: Option[Order] = None): ArticlesPagination = {
        new ArticlesPagination(
          page = calcPage(page),
          limit = calcLimit(limit),
          offset = calcOffset(page),
          order = order.getOrElse(Order.DESC)
        )
      }

      override def make(page: Page, limit: Limit, order: Order): ArticlesPagination = this.make(Option(page), Option(limit), Option(order))
    }
  }

  given TagsPagination: PaginationOps[TagsPagination] = {
    new PaginationOps[TagsPagination] {
      override def make(p: PaginationRequestModel): TagsPagination = {
        new TagsPagination(
          page = calcPage(p.page),
          limit = calcLimit(p.limit),
          offset = calcOffset(p.page),
          order = p.order.getOrElse(Order.DESC)
        )
      }

      override def make(page: Option[Page], limit: Option[Limit], order: Option[Order] = None): TagsPagination = {
        new TagsPagination(
          page = calcPage(page),
          limit = calcLimit(limit),
          offset = calcOffset(page),
          order = order.getOrElse(Order.DESC)
        )
      }

      override def make(page: Page, limit: Limit, order: Order): TagsPagination = this.make(Option(page), Option(limit), Option(order))
    }
  }

  given FeedsPagination: PaginationOps[FeedsPagination] = {
    new PaginationOps[FeedsPagination] {
      override def make(p: PaginationRequestModel): FeedsPagination = {
        // TODO: Throw an exception instead of using a fixed value.
        new FeedsPagination(
          page = Page(1),
          limit = Limit(5),
          offset = 0,
          order = Order.DESC
        )
      }

      override def make(page: Option[Page], limit: Option[Limit], order: Option[Order] = None): FeedsPagination = {
        // TODO: Throw an exception instead of using a fixed value.
        new FeedsPagination(
          page = Page(1),
          limit = Limit(5),
          offset = 0,
          order = Order.DESC
        )
      }

      override def make(page: Page, limit: Limit, order: Order): FeedsPagination = this.make(Option(page), Option(limit), Option(order))
    }
  }

}
