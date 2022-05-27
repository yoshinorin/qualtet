package net.yoshinorin.qualtet.domains

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.repository.requests.RepositoryRequest
import net.yoshinorin.qualtet.domains.authors.AuthorRepositoryRequest
import net.yoshinorin.qualtet.domains.authors.AuthorRepository
import net.yoshinorin.qualtet.domains.archives.ArchiveRepository
import net.yoshinorin.qualtet.domains.archives.ArchiveRepositoryRequest
import net.yoshinorin.qualtet.domains.articles.ArticleRepositoryRequest
import net.yoshinorin.qualtet.domains.articles.ArticleRepository
import net.yoshinorin.qualtet.domains.contents.ContentRepositoryRequest
import net.yoshinorin.qualtet.domains.contents.ContentRepository
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeRepositoryRequest
import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeRepository
import net.yoshinorin.qualtet.domains.externalResources.ExternalResourceRepository
import net.yoshinorin.qualtet.domains.externalResources.ExternalResourceRepositoryRequest
import net.yoshinorin.qualtet.domains.robots.RobotsRepositoryRequest
import net.yoshinorin.qualtet.domains.robots.RobotsRepository
import net.yoshinorin.qualtet.domains.sitemaps.SitemapRepositoryRequest
import net.yoshinorin.qualtet.domains.sitemaps.SitemapsRepository
import net.yoshinorin.qualtet.domains.tags.TagRepositoryRequest
import net.yoshinorin.qualtet.domains.tags.TagRepository

package repository {

  object Repository {
    def dispatch[T](request: RepositoryRequest[T]): ConnectionIO[T] = request match {
      case archiveRepositoryRequest: ArchiveRepositoryRequest[T] => ArchiveRepository.dispatch(archiveRepositoryRequest)
      case articleRepositoryRequest: ArticleRepositoryRequest[T] => ArticleRepository.dispatch(articleRepositoryRequest)
      case authorRepositoryRequest: AuthorRepositoryRequest[T] => AuthorRepository.dispatch(authorRepositoryRequest)
      case contentRepositoryRequest: ContentRepositoryRequest[T] => ContentRepository.dispatch(contentRepositoryRequest)
      case contentTypeRepositoryRequest: ContentTypeRepositoryRequest[T] => ContentTypeRepository.dispatch(contentTypeRepositoryRequest)
      case externalResourceRepositoryRequest: ExternalResourceRepositoryRequest[T] => ExternalResourceRepository.dispatch(externalResourceRepositoryRequest)
      case robotsRepositoryRequest: RobotsRepositoryRequest[T] => RobotsRepository.dispatch(robotsRepositoryRequest)
      case sitemapsRepositoryRequest: SitemapRepositoryRequest[T] => SitemapsRepository.dispatch(sitemapsRepositoryRequest)
      case tagRepositoryRequest: TagRepositoryRequest[T] => TagRepository.dispatch(tagRepositoryRequest)
    }
  }

  package requests {
    abstract class RepositoryRequest[T]
  }
}
