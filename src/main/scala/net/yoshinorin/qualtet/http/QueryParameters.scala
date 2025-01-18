package net.yoshinorin.qualtet.http

import cats.implicits.catsSyntaxEq
// import org.http4s.dsl.io.OptionalQueryParamDecoderMatcher

/*

NOTE: GitHub Actions failued with VerifyError if I use them.

```
[info]   java.lang.VerifyError: Constructor must call super() or this() before return
[info] Exception Details:
[info]   Location:
[info]     net/yoshinorin/qualtet/http/PageQueryParam$.<init>()V @0: return
[info]   Reason:
[info]     Error exists in the bytecode
[info]   Bytecode:
[info]     0000000: b1
```

object PageQueryParam extends OptionalQueryParamDecoderMatcher[Int]("page")
object LimitQueryParam extends OptionalQueryParamDecoderMatcher[Int]("limit")
 */

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

final case class ArticlesQueryParameter(
  page: Page = Page(1),
  limit: Limit = Limit(10),
  offset: Int = 0,
  order: Order = Order.DESC
)

final case class RequestQueryParamater(
  page: Option[Page],
  limit: Option[Limit],
  order: Option[Order]
)

object ArticlesQueryParameter {
  def apply(
    page: Option[Page],
    limit: Option[Limit],
    order: Option[Order]
  ): ArticlesQueryParameter = {
    new ArticlesQueryParameter(
      page.getOrElse(Page(1)) - Page(1),
      if (limit.getOrElse(Limit(10)).toInt > 10) Limit(10) else limit.getOrElse(Limit(10)),
      if (page.getOrElse(Page(1)).toInt === Page(1).toInt) 0 else (page.getOrElse(Page(1)).toInt - 1) * 10,
      order.getOrElse(Order.DESC)
    )
  }

}

object QueryParametersAliases {
  type SqlParams = ArticlesQueryParameter
}
