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

final case class ArticlesQueryParameter(
  page: Int = 1,
  limit: Int = 10,
  offset: Int = 0,
  order: Order = Order.DESC
)

final case class RequestQueryParamater(
  page: Option[Int],
  limit: Option[Int],
  order: Option[Order]
)

object ArticlesQueryParameter {
  def apply(
    page: Option[Int],
    limit: Option[Int],
    order: Option[Order]
  ): ArticlesQueryParameter = {
    new ArticlesQueryParameter(
      page.getOrElse(1) - 1,
      if (limit.getOrElse(10) > 10) 10 else limit.getOrElse(10),
      if (page.getOrElse(1) === 1) 0 else (page.getOrElse(1) - 1) * 10,
      order.getOrElse(Order.DESC)
    )
  }

}

object QueryParametersAliases {
  type SqlParams = ArticlesQueryParameter
}
