package net.yoshinorin.qualtet.domains

// TODO: rename and consider move somewhere
trait ValueExtender[T <: String] {
  extension (v: T) {
    def value: String = v
  }
}
