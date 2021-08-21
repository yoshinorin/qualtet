package net.yoshinorin.qualtet.utils

import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.{DataKey, MutableDataSet}
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension

object Markdown {

  val parser: Parser = Parser.builder.build
  val options = new MutableDataSet
  options.set(
    Parser.EXTENSIONS.asInstanceOf[DataKey[Any]],
    java.util.Arrays.asList(FootnoteExtension.create())
  )

  val renderer: HtmlRenderer = HtmlRenderer.builder.build

  def renderHtml(markdown: String): String = {
    val document: Node = parser.parse(markdown)
    renderer.render(document)
  }

}
