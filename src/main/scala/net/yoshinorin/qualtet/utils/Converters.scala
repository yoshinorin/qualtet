package net.yoshinorin.qualtet.utils

import cats.implicits._

object Converters {

  def zipFromSeparatedComma[A](k: Option[String], v: Option[String])(f: (String, String) => A): Option[List[A]] = {
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
    Option(keys.zip(values).map(x => f(x._1, x._2)))
  }

  def zipWithGroupByFromSeparatedComma[A](k: Option[String], v: Option[String])(f: (String, List[(String, String)]) => A): Option[List[A]] = {
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
    Option(keys.zip(values).groupBy(_._1).map(x => f(x._1, x._2)).toList)
  }

}
