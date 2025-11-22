package net.yoshinorin.qualtet.syntax

import net.yoshinorin.qualtet.fixture.unsafe
import net.yoshinorin.qualtet.domains.externalResources.{ExternalResourceKind, ExternalResources}
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.syntax.Tuple2Spec
class Tuple2Spec extends AnyWordSpec {

  case class TestObject(s1: String, s2: String)

  "zipFromStringPair" should {
    "return list of [A]" in {
      val zippedResult = (
        Option("01h08d6m9p5say793h288n0rsc, 01h08d6pazabydf3eneghthp84, 01h08d6pkag4p7y6xebzyn9bkf"),
        Option("Z, X, Y")
      ).zip((x, y) => TestObject(x, y))

      assert(zippedResult.get(0)._1 === "01h08d6m9p5say793h288n0rsc")
      assert(zippedResult.get(0)._2 === "Z")
      assert(zippedResult.get(1)._1 === "01h08d6pazabydf3eneghthp84")
      assert(zippedResult.get(1)._2 === "X")
      assert(zippedResult.get(2)._1 === "01h08d6pkag4p7y6xebzyn9bkf")
      assert(zippedResult.get(2)._2 === "Y")
    }

    "return None if first arg is None" in {
      val zippedResult = (
        None,
        Option("Z, X, Y")
      ).zip((x, y) => TestObject(x, y))
      assert(zippedResult.isEmpty)
    }

    "return None if second arg is None" in {
      val zippedResult = (
        Option("A, B, C"),
        None
      ).zip((x, y) => TestObject(x, y))
      assert(zippedResult.isEmpty)
    }

    "return None if two args list of string length are diff" in {
      val zippedResult = (
        Option("A, B, C, D"),
        Option("Z, X, Y")
      ).zip((x, y) => TestObject(x, y))
      assert(zippedResult.isEmpty)
    }
  }

  "zipAndGroupByFromSeparatedComma" should {
    "return list of [A]" in {
      val maybeExternalResource = (
        Option("js, css, js, js, css"),
        Option("js1, css1, js2, js3, css2")
      ).zipWithGroupBy((x, y) => ExternalResources(ExternalResourceKind(x).unsafe, y.map(_._2)))

      assert(maybeExternalResource.get(0).kind.value === "css")
      assert(maybeExternalResource.get(1).kind.value === "js")
      assert(maybeExternalResource.get(0).values === List("css1", "css2"))
      assert(maybeExternalResource.get(1).values === List("js1", "js2", "js3"))

    }

    "return None if first arg is None" in {
      val maybeExternalResource = (
        None,
        Option("js1, css1, js2, js3, css2")
      ).zipWithGroupBy((x, y) => ExternalResources(ExternalResourceKind(x).unsafe, y.map(_._2)))
      assert(maybeExternalResource.isEmpty)
    }

    "return None if second arg is None" in {
      val maybeExternalResource = (
        Option("js, css, js, js, css"),
        None
      ).zipWithGroupBy((x, y) => ExternalResources(ExternalResourceKind(x).unsafe, y.map(_._2)))
      assert(maybeExternalResource.isEmpty)
    }

    "return None if two args list of string length are diff" in {
      val maybeExternalResource = (
        Option("js, css, js, js, css"),
        Option("js1, css1, js2, js3")
      ).zipWithGroupBy((x, y) => ExternalResources(ExternalResourceKind(x).unsafe, y.map(_._2)))
      assert(maybeExternalResource.isEmpty)
    }
  }

}
