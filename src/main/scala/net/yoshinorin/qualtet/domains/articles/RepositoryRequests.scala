package net.yoshinorin.qualtet.domains.articles

import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.tags.TagName
import net.yoshinorin.qualtet.http.QueryParametersAliases.SqlParams

object RepositoryReqiests {
  final case class GetWithCount(contentTypeId: ContentTypeId, none: Unit, sqlParams: SqlParams)
  final case class FindByTagNameWithCount(contentTypeId: ContentTypeId, tagName: TagName, sqlParams: SqlParams)
}
