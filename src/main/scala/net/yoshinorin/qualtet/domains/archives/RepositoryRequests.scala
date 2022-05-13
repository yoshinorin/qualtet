package net.yoshinorin.qualtet.domains.archives

import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId

object RepositoryReqiests {
  final case class GetByContentTypeId(contentTypeId: ContentTypeId)
}
