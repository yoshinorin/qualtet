package net.yoshinorin.qualtet.application.contents

import cats.effect.IO
import net.yoshinorin.qualtet.domains.models.contents.{Content, ContentRepository}

class ContentCreator(contentRepository: ContentRepository) {

  def create(data: Content): IO[Content] = {
    contentRepository.insert(data)
  }

}
