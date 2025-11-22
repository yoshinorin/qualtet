package net.yoshinorin.qualtet.domains.tags

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.{Request, UlidConvertible, ValueExtender}
import net.yoshinorin.qualtet.domains.errors.InvalidPath

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

opaque type TagPath = String
object TagPath extends ValueExtender[TagPath] {
  given codecTagPath: JsonValueCodec[TagPath] = JsonCodecMaker.make

  private lazy val unusableChars = "[:?#@!$&'()*+,;=<>\"\\\\^`{}|~]"
  private lazy val invalidPercentRegex = ".*%(?![0-9A-Fa-f]{2}).*"

  def apply(value: String): Either[InvalidPath, TagPath] = {
    if (unusableChars.r.findFirstMatchIn(value).isDefined) {
      return Left(InvalidPath(detail = s"Invalid character contains: ${value}"))
    }
    if (invalidPercentRegex.r.matches(value)) {
      return Left(InvalidPath(detail = s"Invalid percent encoding in path: ${value}"))
    }
    val normalized = if (!value.startsWith("/")) s"/${value}" else value
    Right(normalized)
  }

  /**
   * Create a TagPath from a trusted source (e.g., database) without validation.
   *
   * This method should ONLY be used in Repository layer when reading data from the database.
   * Database data is assumed to be already validated at write time, so we skip validation
   * for performance reasons while still applying normalization for consistency.
   *
   * DO NOT use this method in:
   * - HTTP request handlers
   * - User input processing
   * - Any external data source
   *
   * @param value The raw string value from a trusted source
   * @return The normalized TagPath without validation
   */
  private[tags] def unsafe(value: String): TagPath = {
    if (!value.startsWith("/")) s"/${value}" else value
  }
}

final case class Tag(
  id: TagId = TagId.apply(),
  name: TagName,
  path: TagPath
) extends Request[Tag] {
  def postDecode: Tag = {
    // TODO: Refactor to return Either instead of throwing
    val validatedPath = TagPath(path.value) match {
      case Right(p) => p
      case Left(error) => throw error
    }

    Tag(id, name, validatedPath)
  }
}
object Tag {
  given codecTag: JsonValueCodec[Tag] = JsonCodecMaker.make
  given codecTags: JsonValueCodec[Option[Seq[Tag]]] = JsonCodecMaker.make
}
