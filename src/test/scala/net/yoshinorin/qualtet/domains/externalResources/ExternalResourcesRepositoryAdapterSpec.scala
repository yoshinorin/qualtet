package net.yoshinorin.qualtet.domains.externalResources

import net.yoshinorin.qualtet.fixture.unsafe
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
        ExternalResources(ExternalResourceKind("js").unsafe, values = List("a.js", "b.js", "c.js")),
        ExternalResources(ExternalResourceKind("css").unsafe, values = List("a.css", "b.css", "c.css"))
      )
    ).unsafeCreateConternt()

    createContentRequestModels(
      1,
      "ExternalResourcesRADel",
      externalResources = List(
        ExternalResources(ExternalResourceKind("js").unsafe, values = List("d.js", "e.js", "f.js")),
        ExternalResources(ExternalResourceKind("css").unsafe, values = List("d.css", "e.css", "f.css"))
      )
    ).unsafeCreateConternt()
  }

  "ExternalResourcesRepositoryAdapter" should {

    "findByContentId" in {
      (for {
        maybeContent <- contentService.findByPath(ContentPath("/test/ExternalResourcesRA-0").unsafe)
        externalResources <- doobieExecuterContext.transact(externalResourceRepositoryAdapter.findByContentId(maybeContent.get.id))
      } yield {
        assert(externalResources.size === 6)
        assert(externalResources.count(_.kind === ExternalResourceKind("js").unsafe) === 3)
        assert(externalResources.count(_.kind === ExternalResourceKind("css").unsafe) === 3)
        assert(externalResources.exists(e => e.kind === ExternalResourceKind("js").unsafe && e.name === "a.js"))
        assert(externalResources.exists(e => e.kind === ExternalResourceKind("js").unsafe && e.name === "b.js"))
        assert(externalResources.exists(e => e.kind === ExternalResourceKind("js").unsafe && e.name === "c.js"))
        assert(externalResources.exists(e => e.kind === ExternalResourceKind("css").unsafe && e.name === "a.css"))
        assert(externalResources.exists(e => e.kind === ExternalResourceKind("css").unsafe && e.name === "b.css"))
        assert(externalResources.exists(e => e.kind === ExternalResourceKind("css").unsafe && e.name === "c.css"))
      }).unsafeRunSync()
    }

    "bulkDelete" in {
      (for {
        maybeContent <- contentService.findByPath(ContentPath("/test/ExternalResourcesRADel-0").unsafe)
        shouldDeleteModels <- IO(
          List(
            ExternalResourceDeleteModel(maybeContent.get.id, ExternalResourceKind("js").unsafe, "d.js"),
            ExternalResourceDeleteModel(maybeContent.get.id, ExternalResourceKind("css").unsafe, "d.css")
          )
        )
        _ <- doobieExecuterContext.transact(externalResourceRepositoryAdapter.bulkDelete(shouldDeleteModels))
        remainingExternalResources <- doobieExecuterContext.transact(externalResourceRepositoryAdapter.findByContentId(maybeContent.get.id))
      } yield {
        assert(remainingExternalResources.size === 4)
        assert(!remainingExternalResources.exists(e => e.kind == ExternalResourceKind("js").unsafe && e.name == "d.js"))
        assert(!remainingExternalResources.exists(e => e.kind == ExternalResourceKind("css").unsafe && e.name == "d.css"))
      }).unsafeRunSync()
    }
  }

}
