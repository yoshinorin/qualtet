package net.yoshinorin.qualtet.utils

import org.commonmark.node._
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

object CommonMark {

  val parser: Parser = Parser.builder.build
  val renderer: HtmlRenderer = HtmlRenderer.builder.build

  def renderHtml(markdown: String): String = {
    val document: Node = parser.parse(markdown)
    renderer.render(document)
  }

}
