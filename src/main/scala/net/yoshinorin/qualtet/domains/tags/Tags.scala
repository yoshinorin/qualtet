package net.yoshinorin.qualtet.domains.tags

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import wvlet.airframe.ulid.ULID
import net.yoshinorin.qualtet.syntax.*

final case class TagId(value: String = ULID.newULIDString.toLower) extends AnyVal
object TagId {
  given codecTagId: JsonValueCodec[TagId] = JsonCodecMaker.make
  def apply(value: String): TagId = {
    val _ = ULID.fromString(value)
    new TagId(value)
  }
}

final case class TagName(value: String) extends AnyVal
object TagName {
  given codecTagName: JsonValueCodec[TagName] = JsonCodecMaker.make
  given codecTagNames: JsonValueCodec[Seq[TagName]] = JsonCodecMaker.make
}

final case class Tag(
  id: TagId = new TagId,
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
