package net.yoshinorin.qualtet.syntax

trait option {

  extension [T](s: Option[T]) {
    def stringify: String = s.getOrElse("").toString()

    def orThrow(t: Throwable): T = s match {
      case None => throw t
      case Some(t) => t
    }

    def asEither[T2](t: T2): Either[T2, T] = s match {
      case None => Left(t)
      case Some(s) => Right(s)
    }
  }

}
