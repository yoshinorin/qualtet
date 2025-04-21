package net.yoshinorin.qualtet.syntax

import net.yoshinorin.qualtet.domains.tags.{Tag, TagId, TagName, TagPath}
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.syntax.Tuple3Spec
class Tuple3Spec extends AnyWordSpec {

  "zipFromStringTriple" should {
    "return list of [A]" in {
      val maybeTags = (
        Option("01h08d6m9p5say793h288n0rsc, 01h08d6pazabydf3eneghthp84, 01h08d6pkag4p7y6xebzyn9bkf"),
        Option("B1, B2, B3"),
        Option("C1, C2, C3")
      ).zip((id, name, path) => Tag(TagId(id), TagName(name), TagPath(path)))

      assert(maybeTags.get(0).id.value === "01h08d6m9p5say793h288n0rsc")
      assert(maybeTags.get(0).name.value === "B1")
      assert(maybeTags.get(1).id.value === "01h08d6pazabydf3eneghthp84")
      assert(maybeTags.get(1).name.value === "B2")
      assert(maybeTags.get(2).id.value === "01h08d6pkag4p7y6xebzyn9bkf")
      assert(maybeTags.get(2).name.value === "B3")
    }

    "return None if first arg is None" in {
      val maybeTags = (
        None,
        Option("B1, B2, B3"),
        Option("C1, C2, C3")
      ).zip((id, name, path) => Tag(TagId(id), TagName(name), TagPath(path)))
      assert(maybeTags.isEmpty)
    }

    "return None if second arg is None" in {
      val maybeTags = (
        Option("01h08d6m9p5say793h288n0rsc, 01h08d6pazabydf3eneghthp84, 01h08d6pkag4p7y6xebzyn9bkf"),
        None,
        Option("C1, C2, C3")
      ).zip((id, name, path) => Tag(TagId(id), TagName(name), TagPath(path)))
      assert(maybeTags.isEmpty)
    }

    "return None if third arg is None" in {
      val maybeTags = (
        Option("01h08d6m9p5say793h288n0rsc, 01h08d6pazabydf3eneghthp84, 01h08d6pkag4p7y6xebzyn9bkf"),
        Option("B1, B2, B3"),
        None
      ).zip((id, name, path) => Tag(TagId(id), TagName(name), TagPath(path)))
      assert(maybeTags.isEmpty)
    }

    "return None if length of args are different" in {
      val maybeTags = (
        Option("01h08d6m9p5say793h288n0rsc, 01h08d6pazabydf3eneghthp84, 01h08d6pkag4p7y6xebzyn9bkf, 01h08d6pkag4p7y6xeb09k4bkf"),
        Option("B1, B2, B3"),
        Option("C1, C2, C3")
      ).zip((id, name, path) => Tag(TagId(id), TagName(name), TagPath(path)))
      assert(maybeTags.isEmpty)

      val maybeTags2 = (
        Option("01h08d6m9p5say793h288n0rsc, 01h08d6pazabydf3eneghthp84, 01h08d6pkag4p7y6xebzyn9bkf"),
        Option("B1, B2"),
        Option("C1, C2, C3")
      ).zip((id, name, path) => Tag(TagId(id), TagName(name), TagPath(path)))
      assert(maybeTags2.isEmpty)

      val maybeTags3 = (
        Option("01h08d6m9p5say793h288n0rsc, 01h08d6pazabydf3eneghthp84, 01h08d6pkag4p7y6xebzyn9bkf"),
        Option("B1, B2, B3"),
        Option("C1, C2")
      ).zip((id, name, path) => Tag(TagId(id), TagName(name), TagPath(path)))
      assert(maybeTags3.isEmpty)
    }
  }
}
