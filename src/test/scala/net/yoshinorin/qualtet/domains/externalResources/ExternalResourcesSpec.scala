package net.yoshinorin.qualtet.domains.externalResources

import net.yoshinorin.qualtet.domains.externalResources.ExternalResourceKind
import net.yoshinorin.qualtet.domains.errors.InvalidExternalResourceKind
import net.yoshinorin.qualtet.syntax.*
import net.yoshinorin.qualtet.fixture.Fixture.*
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.externalResources.ExternalResourcesSpec
class ExternalResourcesSpec extends AnyWordSpec {

  "ExternalResources" should {
    "create instance" in {
      val externalResources = ExternalResources(
        ExternalResourceKind("js"),
        values = List("test", "foo", "bar")
      )
      assert(externalResources.kind.value === "js")
      assert(externalResources.values === List("test", "foo", "bar"))
    }

    "as JSON" in {
      val expectJson =
        """
          |{
          |  "kind" : "js",
          |  "values" : ["test", "foo", "bar"]
          |}
      """.stripMargin.replaceNewlineAndSpace

      val json =
        ExternalResources(
          ExternalResourceKind("js"),
          values = List("test", "foo", "bar")
        ).asJson.replaceNewlineAndSpace

      assert(json.contains(expectJson))
    }
  }

  "ExternalResourceKind" should {
    "create instance if specify js" in {
      val externalResourceKind = ExternalResourceKind("js")
      assert(externalResourceKind.value === "js")
    }

    "create instance if specify css" in {
      val externalResourceKind = ExternalResourceKind("css")
      assert(externalResourceKind.value === "css")
    }

    "can not create instance with invalid value" in {
      assertThrows[InvalidExternalResourceKind] {
        ExternalResourceKind("invalid-value")
      }
    }
  }

}
