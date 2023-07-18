package net.yoshinorin.qualtet.domains

import wvlet.airframe.ulid.ULID
import net.yoshinorin.qualtet.syntax.*

// TODO: rename and consider move somewhere
trait UlidConvertible[T >: String] {
  def apply(value: String = ULID.newULIDString.toLower): T = {
    val _ = ULID.fromString(value) // NOTE: for validation. Do not use return value.
    value.toLower
  }
}
