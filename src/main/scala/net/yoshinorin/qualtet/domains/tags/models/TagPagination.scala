package net.yoshinorin.qualtet.domains.tags

import net.yoshinorin.qualtet.domains.pagination.*

final case class TagsPagination(
  page: Page = Page(1),
  limit: Limit = Limit(10),
  offset: Int = 0,
  order: Order = Order.DESC
) extends Pagination

object TagsPagination {

  given TagsPagination: PaginationQueryParametersOps[TagsPagination] = {
    new PaginationQueryParametersOps[TagsPagination] {
      override def make(p: PaginationQueryParametersModel): TagsPagination = {
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

}
