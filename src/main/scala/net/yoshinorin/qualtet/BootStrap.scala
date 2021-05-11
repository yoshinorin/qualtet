package net.yoshinorin.qualtet

import net.yoshinorin.qualtet.infrastructure.db.Migration

object BootStrap extends App {

  Migration.migrate()

}
