package net.yoshinorin.qualtet.domains.period

import java.time.OffsetDateTime

opaque type From = Long
object From {
  def apply(value: Long): From = {
    if (OffsetDateTime.MIN.toZonedDateTime().toEpochSecond() > value) {
      // NOTE: Throws an exception instead of returning an Either.
      //       Because From and To are only used to convert query parameters and no error response is returned to the client.
      throw new IllegalArgumentException()
    }
    value
  }

  extension (a: From) {
    def toLong: Long = a
  }
}

opaque type To = Long
object To {
  def apply(value: Long): To = {
    if (OffsetDateTime.MIN.toZonedDateTime().toEpochSecond() > value) {
      // NOTE: Throws an exception instead of returning an Either.
      //       Because From and To are only used to convert query parameters and no error response is returned to the client.
      throw new IllegalArgumentException()
    }
    value
  }

  extension (a: To) {
    def toLong: Long = a
  }
}

enum Period(val from: Option[From], val to: Option[To]) {
  case Published(f: Option[From], t: Option[To]) extends Period(f, t)
  case Updated(f: Option[From], t: Option[To]) extends Period(f, t)
}
