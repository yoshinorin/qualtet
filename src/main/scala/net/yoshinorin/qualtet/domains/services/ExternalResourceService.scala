package net.yoshinorin.qualtet.domains.services

import cats.implicits.catsSyntaxEq
import net.yoshinorin.qualtet.domains.models.externalResources.{ExternalResourceKind, ExternalResources}

class ExternalResourceService {

  // TODO: to generics & move somewhere
  def toExternalResources(k: Option[String], v: Option[String]): Option[List[ExternalResources]] = {
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
    Option(keys.zip(values).groupBy(_._1).map(x => ExternalResources(ExternalResourceKind(x._1), x._2.map(_._2))).toList)
  }

}
