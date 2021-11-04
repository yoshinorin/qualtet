package net.yoshinorin.qualtet.domains.services

import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.services.ExternalResourceServiceSpec
class ExternalResourceServiceSpec extends AnyWordSpec {

  val externalResourceService = new ExternalResourceService

  "toExternalResources" should {
    "return list of ExternalResources" in {
      val maybeExternalResource = externalResourceService.toExternalResources(
        Option("js, css, js, js, css"),
        Option("js1, css1, js2, js3, css2")
      )

      assert(maybeExternalResource.get(0).kind.value == "css")
      assert(maybeExternalResource.get(1).kind.value == "js")
      assert(maybeExternalResource.get(0).values == List("css1", "css2"))
      assert(maybeExternalResource.get(1).values == List("js1", "js2", "js3"))

    }

    "return None if first arg is None" in {
      val maybeExternalResource = externalResourceService.toExternalResources(
        None,
        Option("js1, css1, js2, js3, css2")
      )
      assert(maybeExternalResource.isEmpty)
    }

    "return None if second arg is None" in {
      val maybeExternalResource = externalResourceService.toExternalResources(
        Option("js, css, js, js, css"),
        None
      )
      assert(maybeExternalResource.isEmpty)
    }

    "return None if two args list of string length are diff" in {
      val maybeExternalResource = externalResourceService.toExternalResources(
        Option("js, css, js, js, css"),
        Option("js1, css1, js2, js3")
      )
      assert(maybeExternalResource.isEmpty)
    }
  }

}
