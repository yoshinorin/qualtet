package net.yoshinorin.qualtet.domains

trait Request[T] {
  // NOTE: JsonCodecMaker.make bypasses opaque type constructors during JSON deserialization.
  //       This means validation logic and transformations (e.g. adding leading slashes to paths,
  //       exception handling, sorting) defined in opaque type constructors are not executed.
  //       The postDecode method ensures these operations are performed after deserialization.
  // TODO: I want to delete this.
  def postDecode: T
}
