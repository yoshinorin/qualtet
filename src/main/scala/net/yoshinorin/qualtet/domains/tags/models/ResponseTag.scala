package net.yoshinorin.qualtet.domains.tags

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

final case class ResponseTag(
  id: TagId,
  name: TagName,
  count: Int
)
object ResponseTag {
  given codecResponseTag: JsonValueCodec[ResponseTag] = JsonCodecMaker.make
  given codecResponseTags: JsonValueCodec[Seq[ResponseTag]] = JsonCodecMaker.make
}
