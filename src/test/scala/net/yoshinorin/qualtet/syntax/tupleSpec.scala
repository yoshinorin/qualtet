package net.yoshinorin.qualtet.syntax

import net.yoshinorin.qualtet.domains.externalResources.{ExternalResourceKind, ExternalResources}
import net.yoshinorin.qualtet.domains.tags.{Tag, TagId, TagName}
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.syntax.TupleSpec
class TupleSpec extends AnyWordSpec {

  "zipFromStringPair" should {
    "return list of [A]" in {
      val maybeTags = (
        Option("01h08d6m9p5say793h288n0rsc, 01h08d6pazabydf3eneghthp84, 01h08d6pkag4p7y6xebzyn9bkf"),
        Option("Z, X, Y")
      ).zip((x, y) => new Tag(TagId(x), TagName(y)))

      assert(maybeTags.get(0).id.value === "01h08d6m9p5say793h288n0rsc")
      assert(maybeTags.get(0).name.value === "Z")
      assert(maybeTags.get(1).id.value === "01h08d6pazabydf3eneghthp84")
      assert(maybeTags.get(1).name.value === "X")
      assert(maybeTags.get(2).id.value === "01h08d6pkag4p7y6xebzyn9bkf")
      assert(maybeTags.get(2).name.value === "Y")
    }

    "return None if first arg is None" in {
      val maybeTags = (
        None,
        Option("Z, X, Y")
      ).zip((x, y) => new Tag(TagId(x), TagName(y)))
      assert(maybeTags.isEmpty)
    }

    "return None if second arg is None" in {
      val maybeTags = (
        Option("A, B, C"),
        None
      ).zip((x, y) => new Tag(TagId(x), TagName(y)))
      assert(maybeTags.isEmpty)
    }

    "return None if two args list of string length are diff" in {
      val maybeTags = (
        Option("A, B, C, D"),
        Option("Z, X, Y")
      ).zip((x, y) => new Tag(TagId(x), TagName(y)))
      assert(maybeTags.isEmpty)
    }
  }

  "zipAndGroupByFromSeparatedComma" should {
    "return list of [A]" in {
      val maybeExternalResource = (
        Option("js, css, js, js, css"),
        Option("js1, css1, js2, js3, css2")
      ).zipWithGroupBy((x, y) => ExternalResources(ExternalResourceKind(x), y.map(_._2)))

      assert(maybeExternalResource.get(0).kind.value === "css")
      assert(maybeExternalResource.get(1).kind.value === "js")
      assert(maybeExternalResource.get(0).values === List("css1", "css2"))
      assert(maybeExternalResource.get(1).values === List("js1", "js2", "js3"))

    }

    "return None if first arg is None" in {
      val maybeExternalResource = (
        None,
        Option("js1, css1, js2, js3, css2")
      ).zipWithGroupBy((x, y) => ExternalResources(ExternalResourceKind(x), y.map(_._2)))
      assert(maybeExternalResource.isEmpty)
    }

    "return None if second arg is None" in {
      val maybeExternalResource = (
        Option("js, css, js, js, css"),
        None
      ).zipWithGroupBy((x, y) => ExternalResources(ExternalResourceKind(x), y.map(_._2)))
      assert(maybeExternalResource.isEmpty)
    }

    "return None if two args list of string length are diff" in {
      val maybeExternalResource = (
        Option("js, css, js, js, css"),
        Option("js1, css1, js2, js3")
      ).zipWithGroupBy((x, y) => ExternalResources(ExternalResourceKind(x), y.map(_._2)))
      assert(maybeExternalResource.isEmpty)
    }
  }

}
