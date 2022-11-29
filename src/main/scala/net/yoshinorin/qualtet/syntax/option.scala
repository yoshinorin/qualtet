package net.yoshinorin.qualtet.syntax

trait option {

  implicit final class OptionOps[T](val s: Option[T]) {
    @SuppressWarnings(Array("org.wartremover.warts.ToString"))
    def stringify: String = s.getOrElse("").toString()

    def orThrow(t: Throwable): T = s match {
      case None => throw t
      case Some(t) => t
    }
  }

}
