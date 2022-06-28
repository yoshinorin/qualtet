package net.yoshinorin.qualtet.domains

import doobie.ConnectionIO

package repository {
  package requests {
    trait RepositoryRequest[T] {
      def dispatch: ConnectionIO[T]
    }
  }
}
