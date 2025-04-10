package net.yoshinorin.qualtet.syntax

import cats.implicits.*

trait tuple2 {

  private def getKeyValues(kv: (Option[String], Option[String])): Option[(List[String], List[String])] = {
    kv match {
      case (None, _) => None
      case (_, None) => None
      case (Some(k), Some(v)) =>
        val (x, y) = (k.split(",").map(_.trim).toList, v.split(",").map(_.trim).toList)
        if (x.size =!= y.size) {
          None
        } else {
          Option(x, y)
        }
    }
  }

  extension (kv: (Option[String], Option[String])) {

    // equally: def zip[A](k: Option[String], v: Option[String])(f: (String, String) => A)
    def zip[A](f: (String, String) => A): Option[List[A]] = {
      getKeyValues(kv) match {
        case None => None
        case Some(kv) => Option(kv._1.zip(kv._2).map(x => f(x._1, x._2)))
      }
    }

    def zipWithGroupBy[A](f: (String, List[(String, String)]) => A): Option[List[A]] = {
      getKeyValues(kv) match {
        case None => None
        case Some(kv) => Option(kv._1.zip(kv._2).groupBy(_._1).map(x => f(x._1, x._2)).toList)
      }
    }
  }

}
