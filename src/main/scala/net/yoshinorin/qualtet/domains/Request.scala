package net.yoshinorin.qualtet.domains

trait Request[T] {
  // NOTE: If use `jsoniter_scala`, a `case class` smart-constructor will be ignored.
  //       It means any constructor operation will be ignored (e.g. raise an exception, sorting... etc...) when decode.
  //       So, this `def postDecode` is deal with them when decode.
  // TODO: I want to delete this.
  def postDecode: T
}
