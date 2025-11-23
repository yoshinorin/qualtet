package net.yoshinorin.qualtet.domains

// TODO: rename and consider move somewhere
trait ValueExtender[T <: String] {
  extension (v: T) {
    def value: String = v
  }

  def fromTrusted(value: String)(using source: FromTrustedSource[T]): T = {
    source.fromTrusted(value)
  }
}
