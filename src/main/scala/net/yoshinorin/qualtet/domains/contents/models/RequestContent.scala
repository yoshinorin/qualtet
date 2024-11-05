package net.yoshinorin.qualtet.domains.contents

import java.time.ZonedDateTime
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import net.yoshinorin.qualtet.domains.Request
import net.yoshinorin.qualtet.domains.externalResources.ExternalResources
import net.yoshinorin.qualtet.domains.robots.Attributes
import net.yoshinorin.qualtet.domains.series.SeriesName
import net.yoshinorin.qualtet.domains.errors.BadRequest
import net.yoshinorin.qualtet.syntax.*

final case class RequestContent(
  contentType: String,
  robotsAttributes: Attributes, // TODO: Consider to use `Option[Attributes]`
  externalResources: List[ExternalResources] = List(),
  tags: List[String] = List(),
  series: Option[SeriesName] = None,
  path: Path,
  title: String,
  rawContent: String,
  htmlContent: String,
  publishedAt: Long = ZonedDateTime.now.toEpochSecond,
  updatedAt: Long = ZonedDateTime.now.toEpochSecond
) extends Request[RequestContent] {
  // NOTE: see `net.yoshinorin.qualtet.domains.Request` comment.
  def postDecode: RequestContent = {
    new RequestContent(
      contentType = contentType,
      robotsAttributes = this.robotsAttributes.sort,
      externalResources = externalResources,
      tags = tags,
      series = series,
      path = path,
      title = title.trimOrThrow(BadRequest(detail = "title required.")),
      rawContent = rawContent.trimOrThrow(BadRequest(detail = "rawContent required.")),
      htmlContent = htmlContent.trimOrThrow(BadRequest(detail = "htmlContent required.")),
      publishedAt = publishedAt,
      updatedAt = updatedAt
    )
  }
}

object RequestContent {
  given codecRequestContent: JsonValueCodec[RequestContent] = JsonCodecMaker.make
  given codecRequestContents: JsonValueCodec[List[RequestContent]] = JsonCodecMaker.make

  def apply(
    contentType: String,
    robotsAttributes: Attributes,
    externalResources: List[ExternalResources] = List(),
    tags: List[String] = List(),
    series: Option[SeriesName] = None,
    path: Path,
    title: String,
    rawContent: String,
    htmlContent: String,
    publishedAt: Long = ZonedDateTime.now.toEpochSecond,
    updatedAt: Long = ZonedDateTime.now.toEpochSecond
  ): RequestContent = {
    new RequestContent(
      contentType = contentType,
      robotsAttributes = robotsAttributes,
      externalResources = externalResources,
      tags = tags,
      series = series,
      path = path,
      title = title.trimOrThrow(BadRequest(detail = "title required.")),
      rawContent = rawContent.trimOrThrow(BadRequest(detail = "rawContent required.")),
      htmlContent = htmlContent.trimOrThrow(BadRequest(detail = "htmlContent required.")),
      publishedAt = publishedAt,
      updatedAt = updatedAt
    )
  }
}
