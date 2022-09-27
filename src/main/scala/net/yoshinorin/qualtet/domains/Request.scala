package net.yoshinorin.qualtet.domains

trait Request[T] {
  // NOTE: If use `jsoniter_scala`, a `case class` doesn't raise an exception when decode.
  //       It means can't assert each field and return an HTTP status code when decoded.
  //       So, this `def` is deal with them when decode.
  // TODO: I want to delete this.
  def postDecode: T
}
