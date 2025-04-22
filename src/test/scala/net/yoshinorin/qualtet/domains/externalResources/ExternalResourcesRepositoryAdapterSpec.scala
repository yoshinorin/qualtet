package net.yoshinorin.qualtet.domains.externalResources

import cats.effect.IO
import net.yoshinorin.qualtet.domains.contents.ContentPath
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieExecuter
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.externalResources.ExternalResourcesRepositoryAdapterSpec
class ExternalResourcesRepositoryAdapterSpec extends AnyWordSpec with BeforeAndAfterAll {

  given doobieExecuterContext: DoobieExecuter = new DoobieExecuter(fixtureTx)

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createContentRequestModels(
      1,
      "ExternalResourcesRA",
      externalResources = List(
        ExternalResources(ExternalResourceKind("js"), values = List("a.js", "b.js", "c.js")),
        ExternalResources(ExternalResourceKind("css"), values = List("a.css", "b.css", "c.css"))
      )
    ).unsafeCreateConternt()

    createContentRequestModels(
      1,
      "ExternalResourcesRADel",
      externalResources = List(
        ExternalResources(ExternalResourceKind("js"), values = List("d.js", "e.js", "f.js")),
        ExternalResources(ExternalResourceKind("css"), values = List("d.css", "e.css", "f.css"))
      )
    ).unsafeCreateConternt()
  }

  "ExternalResourcesRepositoryAdapter" should {

    "findByContentId" in {
      (for {
        maybeContent <- contentService.findByPath(ContentPath("/test/ExternalResourcesRA-0"))
        externalResources <- doobieExecuterContext.transact(externalResourceRepositoryAdapter.findByContentId(maybeContent.get.id))
      } yield {
        assert(externalResources.size === 6)
        assert(externalResources.count(_.kind === ExternalResourceKind("js")) === 3)
        assert(externalResources.count(_.kind === ExternalResourceKind("css")) === 3)
        assert(externalResources.exists(e => e.kind === ExternalResourceKind("js") && e.name === "a.js"))
        assert(externalResources.exists(e => e.kind === ExternalResourceKind("js") && e.name === "b.js"))
        assert(externalResources.exists(e => e.kind === ExternalResourceKind("js") && e.name === "c.js"))
        assert(externalResources.exists(e => e.kind === ExternalResourceKind("css") && e.name === "a.css"))
        assert(externalResources.exists(e => e.kind === ExternalResourceKind("css") && e.name === "b.css"))
        assert(externalResources.exists(e => e.kind === ExternalResourceKind("css") && e.name === "c.css"))
      }).unsafeRunSync()
    }

    "bulkDelete" in {
      (for {
        maybeContent <- contentService.findByPath(ContentPath("/test/ExternalResourcesRADel-0"))
        shouldDeleteModels <- IO(
          List(
            ExternalResourceDeleteModel(maybeContent.get.id, ExternalResourceKind("js"), "d.js"),
            ExternalResourceDeleteModel(maybeContent.get.id, ExternalResourceKind("css"), "d.css")
          )
        )
        _ <- doobieExecuterContext.transact(externalResourceRepositoryAdapter.bulkDelete(shouldDeleteModels))
        remainingExternalResources <- doobieExecuterContext.transact(externalResourceRepositoryAdapter.findByContentId(maybeContent.get.id))
      } yield {
        assert(remainingExternalResources.size === 4)
        assert(!remainingExternalResources.exists(e => e.kind == ExternalResourceKind("js") && e.name == "d.js"))
        assert(!remainingExternalResources.exists(e => e.kind == ExternalResourceKind("css") && e.name == "d.css"))
      }).unsafeRunSync()
    }
  }

}
