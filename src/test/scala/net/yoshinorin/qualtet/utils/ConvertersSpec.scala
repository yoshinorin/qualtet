package net.yoshinorin.qualtet.utils

import net.yoshinorin.qualtet.domains.models.externalResources.{ExternalResourceKind, ExternalResources}
import net.yoshinorin.qualtet.domains.models.tags.{Tag, TagId, TagName}
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.utils.ConvertersSpec
class ConvertersSpec extends AnyWordSpec {

  "zipFromStringPair" should {
    "return list of [A]" in {
      val maybeTags = Converters.zipFromSeparatedComma(
        Option("A, B, C"),
        Option("Z, X, Y")
      )((x, y) => new Tag(new TagId(x), new TagName(y)))

      assert(maybeTags.get(0).id.value == "A")
      assert(maybeTags.get(0).name.value == "Z")
      assert(maybeTags.get(1).id.value == "B")
      assert(maybeTags.get(1).name.value == "X")
      assert(maybeTags.get(2).id.value == "C")
      assert(maybeTags.get(2).name.value == "Y")
    }

    "return None if first arg is None" in {
      val maybeTags = Converters.zipFromSeparatedComma(
        None,
        Option("Z, X, Y")
      )((x, y) => new Tag(new TagId(x), new TagName(y)))
      assert(maybeTags.isEmpty)
    }

    "return None if second arg is None" in {
      val maybeTags = Converters.zipFromSeparatedComma(
        Option("A, B, C"),
        None
      )((x, y) => new Tag(new TagId(x), new TagName(y)))
      assert(maybeTags.isEmpty)
    }

    "return None if two args list of string length are diff" in {
      val maybeTags = Converters.zipFromSeparatedComma(
        Option("A, B, C, D"),
        Option("Z, X, Y")
      )((x, y) => new Tag(new TagId(x), new TagName(y)))
      assert(maybeTags.isEmpty)
    }
  }

  "zipAndGroupByFromSeparatedComma" should {
    "return list of [A]" in {
      val maybeExternalResource = Converters.zipWithGroupByFromSeparatedComma(
        Option("js, css, js, js, css"),
        Option("js1, css1, js2, js3, css2")
      )((x, y) => ExternalResources(ExternalResourceKind(x), y.map(_._2)))

      assert(maybeExternalResource.get(0).kind.value == "css")
      assert(maybeExternalResource.get(1).kind.value == "js")
      assert(maybeExternalResource.get(0).values == List("css1", "css2"))
      assert(maybeExternalResource.get(1).values == List("js1", "js2", "js3"))

    }

    "return None if first arg is None" in {
      val maybeExternalResource = Converters.zipWithGroupByFromSeparatedComma(
        None,
        Option("js1, css1, js2, js3, css2")
      )((x, y) => ExternalResources(ExternalResourceKind(x), y.map(_._2)))
      assert(maybeExternalResource.isEmpty)
    }

    "return None if second arg is None" in {
      val maybeExternalResource = Converters.zipWithGroupByFromSeparatedComma(
        Option("js, css, js, js, css"),
        None
      )((x, y) => ExternalResources(ExternalResourceKind(x), y.map(_._2)))
      assert(maybeExternalResource.isEmpty)
    }

    "return None if two args list of string length are diff" in {
      val maybeExternalResource = Converters.zipWithGroupByFromSeparatedComma(
        Option("js, css, js, js, css"),
        Option("js1, css1, js2, js3")
      )((x, y) => ExternalResources(ExternalResourceKind(x), y.map(_._2)))
      assert(maybeExternalResource.isEmpty)
    }
  }

}
