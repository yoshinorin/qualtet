package net.yoshinorin.qualtet.domains.externalResources

import net.yoshinorin.qualtet.domains.contents.ContentId

final case class ExternalResourceWriteModel(
  contentId: ContentId,
  kind: ExternalResourceKind,
  name: String
)
