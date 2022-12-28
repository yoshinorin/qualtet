package net.yoshinorin.qualtet.infrastructure.db

import cats.effect.IO

trait DataBaseContext[T] {
  val transactor: T
}
