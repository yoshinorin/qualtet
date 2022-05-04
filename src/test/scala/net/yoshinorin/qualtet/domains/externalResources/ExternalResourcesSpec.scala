package net.yoshinorin.qualtet.domains.externalResources

import io.circe.syntax._
import net.yoshinorin.qualtet.domains.externalResources.{ExternalResourceKind, ExternalResources}
import net.yoshinorin.qualtet.error.Fail.UnprocessableEntity
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.models.externalResources.ExternalResourcesSpec
class ExternalResourcesSpec extends AnyWordSpec {

  "ExternalResources" should {
    "create instance" in {
      val externalResources = ExternalResources(
        ExternalResourceKind("js"),
        values = List("test", "foo", "bar")
      )
      assert(externalResources.kind.value == "js")
      assert(externalResources.values == List("test", "foo", "bar"))
    }

    "as JSON" in {
      val expectJson =
        """
          |{
          |  "kind" : "js",
          |  "values" : ["test", "foo", "bar"]
          |}
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      val json = ExternalResources(
        ExternalResourceKind("js"),
        values = List("test", "foo", "bar")
      ).asJson.toString.replaceAll("\n", "").replaceAll(" ", "")

      assert(json.contains(expectJson))
    }
  }

  "ExternalResourceKind" should {
    "create instance if specify js" in {
      val externalResourceKind = ExternalResourceKind("js")
      assert(externalResourceKind.value == "js")
    }

    "create instance if specify css" in {
      val externalResourceKind = ExternalResourceKind("css")
      assert(externalResourceKind.value == "css")
    }

    "can not create instance with invalid value" in {
      assertThrows[UnprocessableEntity] {
        ExternalResourceKind("invalid-value")
      }
    }
  }

}
