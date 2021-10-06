package net.yoshinorin.qualtet.domains.models.robots

final case class Attributes(value: String) extends AnyVal
object Attributes {

  // https://developers.google.com/search/docs/advanced/robots/robots_meta_tag
  // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/meta
  val allowedAttributes = List("all", "noindex", "nofollow", "none", "noarchive", "nosnippet", "notranslate", "noimageindex")

  def apply(value: String): Attributes = {
    if (value.endsWith(",")) {
      // TODO: declare exception
      throw new Exception("TODO")
    }

    val attributes = value.split(',').map(x => x.trim)
    if (attributes.diff(allowedAttributes).length > 0) {
      // TODO: declare exception
      throw new Exception("TODO")
    } else {
      new Attributes(attributes.mkString(", "))
    }
  }
}
