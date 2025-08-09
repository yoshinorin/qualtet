package net.yoshinorin.qualtet.domains.contents

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

final case class AdjacentContentResponseModel(
  previous: Option[AdjacentContentModel],
  next: Option[AdjacentContentModel]
)

object AdjacentContentResponseModel {
  given codecAdjacentContentResponseModel: JsonValueCodec[AdjacentContentResponseModel] = JsonCodecMaker.make
}
