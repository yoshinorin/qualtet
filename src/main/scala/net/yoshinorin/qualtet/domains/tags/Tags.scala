package net.yoshinorin.qualtet.domains.tags

import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._
import wvlet.airframe.ulid.ULID
import java.util.Locale

final case class TagId(value: String = ULID.newULIDString.toLowerCase(Locale.ROOT)) extends AnyVal
object TagId {
  implicit val codecTagId: JsonValueCodec[TagId] = JsonCodecMaker.make
  def apply(value: String): TagId = {
    val _ = ULID.fromString(value)
    new TagId(value)
  }
}

final case class TagName(value: String) extends AnyVal
object TagName {
  implicit val codecTagName: JsonValueCodec[TagName] = JsonCodecMaker.make
  implicit val codecTagNames: JsonValueCodec[Seq[TagName]] = JsonCodecMaker.make
}

final case class Tag(
  id: TagId = new TagId,
  name: TagName
)
object Tag {
  implicit val codecTag: JsonValueCodec[Tag] = JsonCodecMaker.make
  implicit val codecTags: JsonValueCodec[Option[Seq[Tag]]] = JsonCodecMaker.make
}

final case class ResponseTag(
  id: TagId,
  name: TagName
)
object ResponseTag {
  implicit val codecResponseTag: JsonValueCodec[ResponseTag] = JsonCodecMaker.make
  implicit val codecResponseTags: JsonValueCodec[Seq[ResponseTag]] = JsonCodecMaker.make
}
