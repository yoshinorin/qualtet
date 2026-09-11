package net.yoshinorin.qualtet.domains.tags

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

final case class TagResponseModel(
  id: TagId,
  name: TagName,
  path: TagPath,
  count: Int
)
object TagResponseModel {
  given codecResponseTag: JsonValueCodec[TagResponseModel] = JsonCodecMaker.make
  given codecResponseTags: JsonValueCodec[Seq[TagResponseModel]] = JsonCodecMaker.make
}
