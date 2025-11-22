package net.yoshinorin.qualtet.domains.externalResources

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.ValueExtender
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.domains.errors.InvalidExternalResourceKind

opaque type ExternalResourceKind = String
object ExternalResourceKind extends ValueExtender[ExternalResourceKind] {
  given codecExternalResources: JsonValueCodec[ExternalResourceKind] = JsonCodecMaker.make

  val allowedKinds: List[String] = List("js", "css")
  def apply(value: String): Either[InvalidExternalResourceKind, ExternalResourceKind] = {
    if (!allowedKinds.contains(value)) {
      Left(InvalidExternalResourceKind(detail = "The field externalResource.kind allowed only js or css."))
    } else {
      Right(value)
    }
  }

  /**
   * Create an ExternalResourceKind from a trusted source (e.g., database) without validation.
   *
   * This method should ONLY be used in Repository layer when reading data from the database.
   * Database data is assumed to be already validated at write time, so we skip validation
   * for performance reasons.
   *
   * DO NOT use this method in:
   * - HTTP request handlers
   * - User input processing
   * - Any external data source
   *
   * @param value The raw string value from a trusted source
   * @return The ExternalResourceKind without validation
   */
  private[externalResources] def unsafe(value: String): ExternalResourceKind = value
}

final case class ExternalResource(
  contentId: ContentId,
  kind: ExternalResourceKind,
  name: String
)

final case class ExternalResources(
  kind: ExternalResourceKind,
  values: List[String]
)
object ExternalResources {
  given codecExternalResources: JsonValueCodec[ExternalResources] = JsonCodecMaker.make
}
