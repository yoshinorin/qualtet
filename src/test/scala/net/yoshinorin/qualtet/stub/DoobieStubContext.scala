// package net.yoshinorin.qualtet.stub

/*
import cats.Applicative
import cats.effect.{Blocker, IO, Resource}
import doobie.KleisliInterpreter
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux
import net.yoshinorin.qualtet.infrastructure.db.doobie.DoobieContext

class DoobieStubContext extends DoobieContext {

  implicit val applicative: Applicative[IO] = Applicative[IO]


  override val transactor: Aux[IO, Unit] = Transactor(
    (),
    (_: Unit) => Resource.pure(null)(applicative),
    KleisliInterpreter[IO](Blocker.liftExecutionContext(executionContexts)).ConnectionInterpreter,
    doobie.util.transactor.Strategy.void
  )

}
*/
