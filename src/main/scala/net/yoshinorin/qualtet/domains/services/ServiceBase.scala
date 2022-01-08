package net.yoshinorin.qualtet.domains.services

import cats.effect.IO

trait ServiceBase {

  def findBy[A, B](data: A, message: Throwable)(f: A => IO[Option[B]]): IO[B] = {
    f(data).flatMap {
      case None => IO.raiseError(message)
      case Some(x) => IO(x)
    }
  }

}
