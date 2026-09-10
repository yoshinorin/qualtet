package net.yoshinorin.qualtet.domains.pagination

import cats.implicits.catsSyntaxEq

enum Order(val value: String) {
  case ASC extends Order("ASC")
  case DESC extends Order("DESC")
  case RANDOM extends Order("RANDOM")
}

opaque type Page = Int
object Page {
  def apply(value: Int): Page = value

  extension (a: Page) {
    def +(b: Page): Page = a + b
    def -(b: Page): Page = a - b
    def *(b: Page): Page = a * b
    def /(b: Page): Page = a / b
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
    def toInt: Int = a.toInt
  }
}

trait PaginationQueryParametersOps[T] {
  def make(p: PaginationQueryParametersModel): T

  def make(page: Option[Page], limit: Option[Limit], order: Option[Order] = None): T

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

final case class PaginationQueryParametersModel(
  page: Option[Page],
  limit: Option[Limit],
  order: Option[Order]
)

trait Pagination {
  def page: Page
  def limit: Limit
  def offset: Int
  def order: Order
}
