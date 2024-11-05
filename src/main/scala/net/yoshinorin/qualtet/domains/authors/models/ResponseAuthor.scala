package net.yoshinorin.qualtet.domains.authors

import java.time.ZonedDateTime
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

final case class ResponseAuthor(
  id: AuthorId = AuthorId.apply(),
  name: AuthorName,
  displayName: AuthorDisplayName,
  createdAt: Long = ZonedDateTime.now.toEpochSecond
)

object ResponseAuthor {
  given codecAuthor: JsonValueCodec[ResponseAuthor] = JsonCodecMaker.make
  given codecAuthors: JsonValueCodec[Seq[ResponseAuthor]] = JsonCodecMaker.make
}
