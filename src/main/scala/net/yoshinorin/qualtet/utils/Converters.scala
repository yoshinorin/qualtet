package net.yoshinorin.qualtet.utils

import cats.implicits._

object Converters {

  private def getKeyValueOrNone(k: Option[String], v: Option[String]): Option[(List[String], List[String])] = {
    val keys = k match {
      case None => return None
      case Some(x) => x.split(",").map(_.trim).toList
    }
    val values = v match {
      case None => return None
      case Some(x) => x.split(",").map(_.trim).toList
    }
    if (keys.size =!= values.size) {
      return None
    }
    Option(keys, values)
  }

  implicit class KeyValueCommaSeparatedString(kv: (Option[String], Option[String])) {

    // equally: def zip[A](k: Option[String], v: Option[String])(f: (String, String) => A)
    def zip[A](f: (String, String) => A): Option[List[A]] = {
      getKeyValueOrNone(kv._1, kv._2) match {
        case None => None
        case Some(kv) => Option(kv._1.zip(kv._2).map(x => f(x._1, x._2)))
      }
    }

    def zipWithGroupBy[A](f: (String, List[(String, String)]) => A): Option[List[A]] = {
      getKeyValueOrNone(kv._1, kv._2) match {
        case None => None
        case Some(kv) => Option(kv._1.zip(kv._2).groupBy(_._1).map(x => f(x._1, x._2)).toList)
      }
    }

  }

}
