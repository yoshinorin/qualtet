package net.yoshinorin.qualtet.domains.archives

import net.yoshinorin.qualtet.domains.contentTypes.ContentTypeId
import net.yoshinorin.qualtet.domains.period.Period

import org.typelevel.doobie.Fragments.whereAndOpt
import org.typelevel.doobie.Read
import org.typelevel.doobie.syntax.all.*
import org.typelevel.doobie.util.fragment.Fragment
import org.typelevel.doobie.util.query.Query0

object ArchiveQuery {

  private def toFragments(period: Period): List[Fragment] = {
    val column = period match {
      case _: Period.Published => Fragment.const("published_at")
      case _: Period.Updated => Fragment.const("updated_at")
    }

    List(
      period.from.map(f => column ++ fr">= ${f.toLong}"),
      period.to.map(t => column ++ fr"<= ${t.toLong}")
    ).flatten
  }

  // TODO: Consider create index for `published_at`, `updated_at`
  def get(contentTypeId: ContentTypeId, periods: Seq[Period]): Read[ArchiveReadModel] ?=> Query0[ArchiveReadModel] = {
    val where = whereAndOpt(fr"content_type_id = ${contentTypeId.value}" :: periods.flatMap(toFragments).toList)
    sql"""
      SELECT path, title, published_at
      FROM contents
        ${where}
        ORDER BY published_at desc
    """
      .query[ArchiveReadModel]
  }

}
