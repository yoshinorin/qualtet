package net.yoshinorin.qualtet.domains.externalResources

import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._
import net.yoshinorin.qualtet.domains.contents.ContentId
import net.yoshinorin.qualtet.message.Fail.UnprocessableEntity

final case class ExternalResourceKind(value: String) extends AnyVal
object ExternalResourceKind {
  implicit val codecExternalResources: JsonValueCodec[ExternalResourceKind] = JsonCodecMaker.make

  val allowedKinds: List[String] = List("js", "css")
  def apply(value: String): ExternalResourceKind = {
    if (!allowedKinds.contains(value)) {
      throw UnprocessableEntity("The field externalResource.kind allowed only js or css.")
    }
    new ExternalResourceKind(value)
  }
}

final case class ExternalResource(
  contentId: ContentId,
  kind: ExternalResourceKind,
  name: String // TODO: consider naming
)

final case class ExternalResources(
  kind: ExternalResourceKind,
  values: List[String]
)
object ExternalResources {
  implicit val codecExternalResources: JsonValueCodec[ExternalResources] = JsonCodecMaker.make
}
