package net.yoshinorin.qualtet.domains.authors

import java.time.ZonedDateTime
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

final case class AuthorResponseModel(
  id: AuthorId = AuthorId.apply(),
  name: AuthorName,
  displayName: AuthorDisplayName,
  createdAt: Long = ZonedDateTime.now.toEpochSecond
)

object AuthorResponseModel {
  given codecAuthor: JsonValueCodec[AuthorResponseModel] = JsonCodecMaker.make
  given codecAuthors: JsonValueCodec[Seq[AuthorResponseModel]] = JsonCodecMaker.make
}
