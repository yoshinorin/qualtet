package net.yoshinorin.qualtet.domains.models.externalResources

import io.circe.syntax._
import net.yoshinorin.qualtet.domains.models.contents.ContentId
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.models.externalResources.ExternalResourcesSpec
class ExternalResourcesSpec extends AnyWordSpec {

  "ExternalResources" should {
    "create instance" in {
      val externalResources = ExternalResources(
        ContentId("01febb1333pd3431q1a1e00fbt"),
        ExternalResourceKind("js"),
        names = List("test", "foo", "bar")
      )
      assert(externalResources.contentId.value == "01febb1333pd3431q1a1e00fbt")
      assert(externalResources.kind.value == "js")
      assert(externalResources.names == List("test", "foo", "bar"))
    }

    "as JSON" in {
      val expectJson =
        """
          |{
          |  "contentId" : "01febb1333pd3431q1a1e00fbt",
          |  "kind" : "js",
          |  "names" : ["test", "foo", "bar"]
          |}
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      val json = ExternalResources(
        ContentId("01febb1333pd3431q1a1e00fbt"),
        ExternalResourceKind("js"),
        names = List("test", "foo", "bar")
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
      // TODO: declare exception
      assertThrows[Exception] {
        ExternalResourceKind("invalid-value")
      }
    }
  }

}
