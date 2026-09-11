package net.yoshinorin.qualtet.domains.contents

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

final case class AdjacentContentResponseModel(
  previous: Option[AdjacentContentModel],
  next: Option[AdjacentContentModel]
)

object AdjacentContentResponseModel {
  given codecAdjacentContentResponseModel: JsonValueCodec[AdjacentContentResponseModel] = JsonCodecMaker.make
}
