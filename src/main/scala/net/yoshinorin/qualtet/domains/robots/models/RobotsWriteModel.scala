package net.yoshinorin.qualtet.domains.robots

import net.yoshinorin.qualtet.domains.contents.ContentId

final case class RobotsWriteModel(
  contentId: ContentId,
  attributes: Attributes
)
