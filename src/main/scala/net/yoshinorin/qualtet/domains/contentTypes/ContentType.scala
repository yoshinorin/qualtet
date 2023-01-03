package net.yoshinorin.qualtet.domains.contentTypes

import wvlet.airframe.ulid.ULID
import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._
import net.yoshinorin.qualtet.syntax._

final case class ContentTypeId(value: String = ULID.newULIDString.toLower) extends AnyVal
object ContentTypeId {
  implicit val codecContentTypeId: JsonValueCodec[ContentTypeId] = JsonCodecMaker.make

  def apply(value: String): ContentTypeId = {
    val _ = ULID.fromString(value)
    new ContentTypeId(value)
  }
}

final case class ContentType(
  id: ContentTypeId = new ContentTypeId,
  name: String
)

object ContentType {
  implicit val codecContentType: JsonValueCodec[ContentType] = JsonCodecMaker.make
  implicit val codecContentTypes: JsonValueCodec[Seq[ContentType]] = JsonCodecMaker.make
}
