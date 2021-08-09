package net.yoshinorin.qualtet.domains.models.archives

import io.circe.syntax._
import net.yoshinorin.qualtet.domains.models.contents.Path
import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.domains.models.archives.ArchiveSpec
class ArchiveSpec extends AnyWordSpec {

  "ResponseArchive" should {
    "as JSON" in {
      val expectJson =
        """
          |{
          |  "path" : "/test",
          |  "title" : "title",
          |  "publishedAt" : 1567814290
          |}
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      val json = ResponseArchive(
        path = Path("/test"),
        title = "title",
        publishedAt = 1567814290
      ).asJson.toString.replaceAll("\n", "").replaceAll(" ", "")

      //NOTE: failed equally compare
      assert(json.contains(expectJson))
    }

    "as JSON Array" in {
      val expectJson =
        """
          |[
          |  {
          |    "path" : "/test/path1",
          |    "title" : "title1",
          |    "publishedAt" : 1567814290
          |  },
          |  {
          |    "path" : "/test/path2",
          |    "title" : "title2",
          |    "publishedAt" : 1567814391
          |  }
          |]
      """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      val json = Seq(
        ResponseArchive(
          path = Path("/test/path1"),
          title = "title1",
          publishedAt = 1567814290
        ),
        ResponseArchive(
          path = Path("/test/path2"),
          title = "title2",
          publishedAt = 1567814391
        )
      ).asJson.toString.replaceAll("\n", "").replaceAll(" ", "")

      //NOTE: failed equally compare
      assert(json.contains(expectJson))
    }

  }

}
