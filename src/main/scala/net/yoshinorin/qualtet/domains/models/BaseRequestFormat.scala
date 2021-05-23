package net.yoshinorin.qualtet.domains.models

sealed abstract class BaseRequestFormat[T](value: String) {

  protected def validate: Either[Message, T]

}
