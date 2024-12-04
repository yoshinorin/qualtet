package net.yoshinorin.qualtet.domains.contents

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.authors.AuthorName
import net.yoshinorin.qualtet.domains.externalResources.ExternalResources
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.tags.Tag

final case class ContentDetailResponseModel(
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

object ContentDetailResponseModel {
  given codecResponseContent: JsonValueCodec[ContentDetailResponseModel] = JsonCodecMaker.make(
    CodecMakerConfig
      .withRequireCollectionFields(true)
      .withTransientEmpty(false)
      .withSkipNestedOptionValues(false)
      .withSkipUnexpectedFields(false)
      .withTransientEmpty(false)
      .withTransientDefault(false)
  )
  given codecResponseContents: JsonValueCodec[Seq[ContentDetailResponseModel]] = JsonCodecMaker.make
}
