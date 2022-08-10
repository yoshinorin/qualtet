package net.yoshinorin.qualtet.syntax

trait option {

  implicit final class OptionOps[T](val s: Option[T]) {
    @SuppressWarnings(Array("org.wartremover.warts.ToString"))
    def stringify: String = s.getOrElse("").toString()
  }

}
