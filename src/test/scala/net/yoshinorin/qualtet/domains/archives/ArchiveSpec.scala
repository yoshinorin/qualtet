package net.yoshinorin.qualtet.domains.archives

import net.yoshinorin.qualtet.syntax.*
import org.scalatest.wordspec.AnyWordSpec
import net.yoshinorin.qualtet.domains.Path

// testOnly net.yoshinorin.qualtet.domains.archives.ArchiveSpec
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

      val json = ArchiveResponseModel(
        path = Path("/test"),
        title = "title",
        publishedAt = 1567814290
      ).asJson.replaceAll("\n", "").replaceAll(" ", "")

      // NOTE: failed equally compare
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
        ArchiveResponseModel(
          path = Path("/test/path1"),
          title = "title1",
          publishedAt = 1567814290
        ),
        ArchiveResponseModel(
          path = Path("/test/path2"),
          title = "title2",
          publishedAt = 1567814391
        )
      ).asJson.replaceAll("\n", "").replaceAll(" ", "")

      // NOTE: failed equally compare
      assert(json.contains(expectJson))
    }

  }

}
