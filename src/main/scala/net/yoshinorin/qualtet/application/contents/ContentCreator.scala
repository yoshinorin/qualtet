package net.yoshinorin.qualtet.application.contents

import doobie.ConnectionIO
import net.yoshinorin.qualtet.domains.models.contents.{Content, ContentRepository}

class ContentCreator(contentRepository: ContentRepository) {

  def create(data: Content): ConnectionIO[Long] = {
    contentRepository.insert(data)
  }

}
