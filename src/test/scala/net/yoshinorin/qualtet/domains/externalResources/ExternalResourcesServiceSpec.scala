package net.yoshinorin.qualtet.domains.externalResources

import cats.effect.IO
import net.yoshinorin.qualtet.domains.Path
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieExecuter
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.ExternalResourcesServiceSpec
class ExternalResourcesServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  given doobieExecuterContext: DoobieExecuter = new DoobieExecuter(fixtureTx)

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createContentRequestModels(
      1,
      "externalResourcesService",
      externalResources = List(
        ExternalResources(ExternalResourceKind("js"), values = List("a.js", "b.js", "c.js")),
        ExternalResources(ExternalResourceKind("css"), values = List("a.css", "b.css", "c.css"))
      )
    ).unsafeCreateConternt()

    createContentRequestModels(
      1,
      "externalResourcesServiceDel",
      externalResources = List(
        ExternalResources(ExternalResourceKind("js"), values = List("d.js", "e.js", "f.js")),
        ExternalResources(ExternalResourceKind("css"), values = List("d.css", "e.css", "f.css"))
      )
    ).unsafeCreateConternt()
  }

  "ExternalResourcesService" should {

    "findByContentId" in {
      val r: Seq[ExternalResource] = (for {
        c <- contentService.findByPath(Path("/test/externalResourcesService-0"))
        e <- doobieExecuterContext.transact(externalResourceService.findByContentIdCont(c.get.id))
      } yield e).unsafeRunSync()

      assert(r.size === 6)
      assert(r.count(_.kind == ExternalResourceKind("js")) === 3)
      assert(r.count(_.kind == ExternalResourceKind("css")) === 3)
      assert(r.exists(e => e.kind == ExternalResourceKind("js") && e.name == "a.js"))
      assert(r.exists(e => e.kind == ExternalResourceKind("js") && e.name == "b.js"))
      assert(r.exists(e => e.kind == ExternalResourceKind("js") && e.name == "c.js"))
      assert(r.exists(e => e.kind == ExternalResourceKind("css") && e.name == "a.css"))
      assert(r.exists(e => e.kind == ExternalResourceKind("css") && e.name == "b.css"))
      assert(r.exists(e => e.kind == ExternalResourceKind("css") && e.name == "c.css"))
    }

    "bulkDelete" in {
      val remainingResources: Seq[ExternalResource] = (for {
        content <- contentService.findByPath(Path("/test/externalResourcesServiceDel-0"))
        shouldDeleteModels <- IO(
          List(
            ExternalResourceDeleteModel(content.get.id, ExternalResourceKind("js"), "d.js"),
            ExternalResourceDeleteModel(content.get.id, ExternalResourceKind("css"), "d.css")
          )
        )
        _ <- doobieExecuterContext.transact(externalResourceService.bulkDeleteCont(shouldDeleteModels))
        e <- doobieExecuterContext.transact(externalResourceService.findByContentIdCont(content.get.id))
      } yield e).unsafeRunSync()

      assert(remainingResources.size === 4)
      assert(!remainingResources.exists(e => e.kind == ExternalResourceKind("js") && e.name == "d.js"))
      assert(!remainingResources.exists(e => e.kind == ExternalResourceKind("css") && e.name == "d.css"))
    }
  }

}
