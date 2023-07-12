package net.yoshinorin.qualtet.domains.externalResources

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.message.Fail.UnprocessableEntity

opaque type ExternalResourceKind = String
object ExternalResourceKind {
  given codecExternalResources: JsonValueCodec[ExternalResourceKind] = JsonCodecMaker.make

  val allowedKinds: List[String] = List("js", "css")
  def apply(value: String): ExternalResourceKind = {
    if (!allowedKinds.contains(value)) {
      throw UnprocessableEntity("The field externalResource.kind allowed only js or css.")
    }
    value
  }

  extension (externalResourceKind: ExternalResourceKind) {
    def value: String = externalResourceKind
  }
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
