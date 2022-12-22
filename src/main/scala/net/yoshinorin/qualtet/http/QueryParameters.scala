package net.yoshinorin.qualtet.http

import cats.implicits.catsSyntaxEq
import org.http4s.dsl.io.OptionalQueryParamDecoderMatcher

/*
object PageQueryParam extends OptionalQueryParamDecoderMatcher[Int]("page") {
  override def unapply(params: Map[String, collection.Seq[String]]): Option[Option[Int]] = {
    super.unapply(params)
  }
}
object LimitQueryParam extends OptionalQueryParamDecoderMatcher[Int]("limit") {
  override def unapply(params: Map[String, collection.Seq[String]]): Option[Option[Int]] = {
    super.unapply(params)
  }
}
*/

final case class ArticlesQueryParameter(
  page: Int = 1,
  limit: Int = 10,
  offset: Int = 0
)

object ArticlesQueryParameter {
  def apply(
    page: Option[Int],
    limit: Option[Int]
  ): ArticlesQueryParameter = {
    new ArticlesQueryParameter(
      page.getOrElse(1) - 1,
      if (limit.getOrElse(10) > 10) 10 else limit.getOrElse(10),
      if (page.getOrElse(1) === 1) 0 else (page.getOrElse(1) - 1) * 10
    )
  }

}

object QueryParametersAliases {
  type SqlParams = ArticlesQueryParameter
}
