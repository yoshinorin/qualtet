package net.yoshinorin.qualtet.syntax

import net.yoshinorin.qualtet.types.Points

trait points {

  implicit class PointsOps(t: Points) {
    def expand(s: Int, e: Int, limit: Int): Points = {
      val r1 = if (t._1 - s) > 0 then (t._1 - s) else 0
      val r2 = if (t._2 + e) > limit then limit else (t._2 + e)
      (r1, r2)
    }
  }

}
