package net.yoshinorin.qualtet.domains.feeds

import net.yoshinorin.qualtet.domains.pagination.*

final case class FeedsPagination(
  page: Page = Page(1),
  limit: Limit = Limit(5),
  offset: Int = 0,
  order: Order = Order.DESC
) extends Pagination

object FeedsPagination {

  given FeedsPagination: PaginationQueryParametersOps[FeedsPagination] = {
    new PaginationQueryParametersOps[FeedsPagination] {
      override def make(p: PaginationQueryParametersModel): FeedsPagination = {
        new FeedsPagination(
          page = Page(1),
          limit = p.limit.getOrElse(Limit(5)),
          offset = 0,
          order = Order.DESC
        )
      }

      override def make(page: Option[Page], limit: Option[Limit], order: Option[Order] = None): FeedsPagination = {
        new FeedsPagination(
          page = Page(1),
          limit = limit.getOrElse(Limit(5)),
          offset = 0,
          order = Order.DESC
        )
      }

      override def make(page: Page, limit: Limit, order: Order): FeedsPagination = this.make(Option(page), Option(limit), Option(order))
    }
  }

}
