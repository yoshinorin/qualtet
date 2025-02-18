package net.yoshinorin.qualtet.domains.externalResources

import net.yoshinorin.qualtet.domains.Path
import net.yoshinorin.qualtet.domains.errors.TagNotFound
import net.yoshinorin.qualtet.fixture.Fixture.*
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieExecuter
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.BeforeAndAfterAll

import cats.effect.unsafe.implicits.global

// testOnly net.yoshinorin.qualtet.domains.ExternalResourcesServiceSpec copy
class ExternalResourcesServiceSpec extends AnyWordSpec with BeforeAndAfterAll {

  given doobieExecuterContext: DoobieExecuter = new DoobieExecuter(fixtureTx)

  override protected def beforeAll(): Unit = {
    // NOTE: create content and related data for test
    createContentRequestModels(
      1,
      "externalResourcesService",
      externalResources = List(ExternalResources(ExternalResourceKind("js"), values = List("a.js", "b.js")))
    ).unsafeCreateConternt()
  }

  "ExternalResourcesService" should {

    "findByContentId" in {
      val r = (for {
        c <- contentService.findByPath(Path("/test/externalResourcesService-0"))
        e <- doobieExecuterContext.transact(externalResourceService.findByContentIdCont(c.get.id))
      } yield e).unsafeRunSync()

      assert(r.size === 2)
      assert(r.head.kind === ExternalResourceKind("js"))
      assert(r.head.name === "a.js")
      assert(r.last.name === "b.js")
    }

  }

}
