package net.yoshinorin.qualtet.domains.models.archives

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

final case class ResponseArchive(
  path: String,
  title: String,
  publishedAt: Long
)

object ResponseArchive {
  implicit val encodeContent: Encoder[ResponseArchive] = deriveEncoder[ResponseArchive]
  implicit val encodeContents: Encoder[List[ResponseArchive]] = Encoder.encodeList[ResponseArchive]
  implicit val decodeContent: Decoder[ResponseArchive] = deriveDecoder[ResponseArchive]
  implicit val decodeContents: Decoder[List[ResponseArchive]] = Decoder.decodeList[ResponseArchive]
}
