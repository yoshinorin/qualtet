package net.yoshinorin.qualtet.utils

import org.scalatest.wordspec.AnyWordSpec

// testOnly net.yoshinorin.qualtet.utils.MarkdownSpec
class MarkdownSpec extends AnyWordSpec {

  "Markdown" should {
    "render" in {
      val markdown =
        """
          |# h1
          |
          |## h2
          |
          |### h3
          |
          |this is a test[^1]
          |
          |* hoge
          |   * fuga
          |   * fuga
          |
          |![](example.com/test/image.jpg)
        """.stripMargin
      val result = Markdown.renderHtml(markdown).replaceAll("\n", "").replaceAll(" ", "")
      val expectedHtml =
        """
          |<h1>h1</h1>
          |<h2>h2</h2>
          |<h3>h3</h3>
          |<p>this is a test[^1]</p>
          |<ul>
          |<li>hoge
          |<ul>
          |<li>fuga</li>
          |<li>fuga</li>
          |</ul>
          |</li>
          |</ul>
          |<p><img src="example.com/test/image.jpg" alt="" /></p>
        """.stripMargin.replaceAll("\n", "").replaceAll(" ", "")

      assert(result == expectedHtml)
    }

    // TODO: more test
  }

}
