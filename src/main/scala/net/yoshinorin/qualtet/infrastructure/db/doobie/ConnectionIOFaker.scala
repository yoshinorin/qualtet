package net.yoshinorin.qualtet.infrastructure.db.doobie

import cats.implicits.catsSyntaxApplicativeId
import doobie.ConnectionIO

trait ConnectionIOFaker {

  def ConnectionIOWithInt: ConnectionIO[Int] = 0.pure[ConnectionIO]

}
