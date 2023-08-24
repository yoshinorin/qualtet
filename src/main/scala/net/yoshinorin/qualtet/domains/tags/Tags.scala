package net.yoshinorin.qualtet.domains.tags

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.{ValueExtender, UlidConvertible}

opaque type TagId = String
object TagId extends ValueExtender[TagId] with UlidConvertible[TagId] {
  given codecTagId: JsonValueCodec[TagId] = JsonCodecMaker.make
}

opaque type TagName = String
object TagName extends ValueExtender[TagName] {
  given codecTagName: JsonValueCodec[TagName] = JsonCodecMaker.make
  given codecTagNames: JsonValueCodec[Seq[TagName]] = JsonCodecMaker.make

  def apply(value: String): TagName = value
}

final case class Tag(
  id: TagId = TagId.apply(),
  name: TagName
)
object Tag {
  given codecTag: JsonValueCodec[Tag] = JsonCodecMaker.make
  given codecTags: JsonValueCodec[Option[Seq[Tag]]] = JsonCodecMaker.make
}

final case class ResponseTag(
  id: TagId,
  name: TagName,
  count: Int
)
object ResponseTag {
  given codecResponseTag: JsonValueCodec[ResponseTag] = JsonCodecMaker.make
  given codecResponseTags: JsonValueCodec[Seq[ResponseTag]] = JsonCodecMaker.make
}
