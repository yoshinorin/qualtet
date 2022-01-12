package net.yoshinorin.qualtet.fixture

import net.yoshinorin.qualtet.domains.models.archives.ResponseArchive
import net.yoshinorin.qualtet.domains.models.articles.ResponseArticle
import net.yoshinorin.qualtet.domains.models.authors._
import net.yoshinorin.qualtet.domains.models.contentTypes.{ContentType, ContentTypeId}
import net.yoshinorin.qualtet.domains.models.contents.{ContentId, Path}
import net.yoshinorin.qualtet.domains.models.robots.Attributes

// Just test data
object Fixture {

  val authorId: AuthorId = AuthorId("01febb8az5t42m2h68xj8c754a")
  val authorId2: AuthorId = AuthorId("01febb8az5t42m2h68xj8c754b")
  val validBCryptPassword: BCryptPassword = BCryptPassword("$2a$10$XmRiVEV8yV9u8BnsIfSTTuzUvH/.6jutH6QvIX6zRoTcqkuKsxE0O")

  val author: Author = Author(
    id = authorId,
    name = AuthorName("JhonDue"),
    displayName = AuthorDisplayName("JD"),
    password = validBCryptPassword
  )

  val contentId: ContentId = ContentId("01febb1333pd3431q1a1e00fbt")
  val contentTypeId: ContentTypeId = ContentTypeId("01febb1333pd3431q1a1e01fbc")
  val articleContentType: ContentType = ContentType(contentTypeId, "articles")

  val tagId: TagId = TagId("01frdbe1g83533h92rkhy8ctkw")

  val fullRobotsAttributes: Attributes = Attributes("all, noindex, nofollow, none, noarchive, nosnippet, notranslate, noimageindex")

  val responseArchive: ResponseArchive = ResponseArchive(
    path = Path("/test"),
    title = "title",
    publishedAt = 1567814290
  )

  val responseArchive2: ResponseArchive =
    ResponseArchive(
      path = Path("/test/path1"),
      title = "title1",
      publishedAt = 1567814290
    )

  val responseArchive3: ResponseArchive =
    ResponseArchive(
      path = Path("/test/path2"),
      title = "title2",
      publishedAt = 1567814391
    )

  val responseArticle1: ResponseArticle = ResponseArticle(
    path = Path("/test"),
    title = "title",
    content = "this is a content",
    publishedAt = 0,
    updatedAt = 0
  )

  val responseArticle2: ResponseArticle = ResponseArticle(
    path = Path("/test/path2"),
    title = "title2",
    content = "this is a content2",
    publishedAt = 1,
    updatedAt = 2
  )

  val responseArticleWithLongLengthContent: ResponseArticle = ResponseArticle(
    path = Path("/test"),
    title = "title",
    content = "a" * 101,
    publishedAt = 0,
    updatedAt = 0
  )
}
