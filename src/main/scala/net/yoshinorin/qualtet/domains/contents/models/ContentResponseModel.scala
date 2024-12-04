package net.yoshinorin.qualtet.domains.contents

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.domains.externalResources.ExternalResources
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.tags.Tag

final case class ContentResponseModel(
  id: ContentId,
  title: String,
  robotsAttributes: Attributes,
  externalResources: List[ExternalResources] = List(),
  tags: List[Tag] = List(),
  description: String,
  content: String,
  length: Int,
  authorName: AuthorName,
  publishedAt: Long,
  updatedAt: Long
)

object ContentResponseModel {
  given codecResponseContent: JsonValueCodec[ContentResponseModel] = JsonCodecMaker.make(
    CodecMakerConfig
      .withRequireCollectionFields(true)
      .withTransientEmpty(false)
      .withSkipNestedOptionValues(false)
      .withSkipUnexpectedFields(false)
      .withTransientEmpty(false)
      .withTransientDefault(false)
  )
  given codecResponseContents: JsonValueCodec[Seq[ContentResponseModel]] = JsonCodecMaker.make
}
