package net.yoshinorin.qualtet.syntax

import cats.implicits.*

trait tuple3 {

  private def getKeyValues(kv: (Option[String], Option[String], Option[String])): Option[List[(String, String, String)]] = {
    kv match {
      case (Some(a1), Some(b1), Some(c1)) => {
        val (a2, b2, c2) = (a1.split(",").map(_.trim).toList, b1.split(",").map(_.trim).toList, c1.split(",").map(_.trim).toList)
        if (a2.size =!= b2.size || b2.size =!= c2.size) {
          None
        } else {
          Option(a2.zip(b2).zip(c2).map { case ((a, b), c) => (a, b, c) })
        }
      }
      case _ => None
    }
  }

  extension (kv: (Option[String], Option[String], Option[String])) {

    // equally: def zip[A](k: Option[String], v: Option[String])(f: (String, String) => A)
    def zip[A](f: (String, String, String) => A): Option[List[A]] = {
      getKeyValues(kv) match {
        case None => None
        case Some(tuples) => Option(tuples.map { case (a, b, c) => f(a, b, c) })
      }
    }
  }

}
