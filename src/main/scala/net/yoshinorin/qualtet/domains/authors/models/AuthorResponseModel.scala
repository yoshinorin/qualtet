package net.yoshinorin.qualtet.domains.authors

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

final case class AuthorResponseModel(
  id: AuthorId,
  name: AuthorName,
  displayName: AuthorDisplayName,
  createdAt: Long
)

object AuthorResponseModel {
  given codecAuthor: JsonValueCodec[AuthorResponseModel] = JsonCodecMaker.make
  given codecAuthors: JsonValueCodec[Seq[AuthorResponseModel]] = JsonCodecMaker.make
}
