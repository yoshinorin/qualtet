package net.yoshinorin.qualtet.domains.articles

import net.yoshinorin.qualtet.domains.pagination.*

final case class ArticlesPagination(
  page: Page = Page(1),
  limit: Limit = Limit(10),
  offset: Int = 0,
  order: Order = Order.DESC
) extends Pagination

object ArticlesPagination {

  given ArticlesPagination: PaginationQueryParametersOps[ArticlesPagination] = {
    new PaginationQueryParametersOps[ArticlesPagination] {
      override def make(p: PaginationQueryParametersModel): ArticlesPagination = {
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

}
