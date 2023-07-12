package net.yoshinorin.qualtet.domains.tags

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import wvlet.airframe.ulid.ULID
import net.yoshinorin.qualtet.syntax.*

opaque type TagId = String
object TagId {
  given codecTagId: JsonValueCodec[TagId] = JsonCodecMaker.make

  def apply(value: String = ULID.newULIDString.toLower): TagId = {
    val _ = ULID.fromString(value)
    value.toLower
  }

  extension (tagId: TagId) {
    def value: String = tagId
  }
}

opaque type TagName = String
object TagName {
  given codecTagName: JsonValueCodec[TagName] = JsonCodecMaker.make
  given codecTagNames: JsonValueCodec[Seq[TagName]] = JsonCodecMaker.make

  def apply(value: String): TagName = value

  extension (tagName: TagName) {
    def value: String = tagName
  }
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
  name: TagName
)
object ResponseTag {
  given codecResponseTag: JsonValueCodec[ResponseTag] = JsonCodecMaker.make
  given codecResponseTags: JsonValueCodec[Seq[ResponseTag]] = JsonCodecMaker.make
}
