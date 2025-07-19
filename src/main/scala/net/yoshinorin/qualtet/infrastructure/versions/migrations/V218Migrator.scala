package net.yoshinorin.qualtet.infrastructure.versions

object V218Migrator {

  import doobie.{Read, Write}
  import doobie.ConnectionIO
  import doobie.syntax.all.toSqlInterpolator
  import doobie.util.update.Update
  import net.yoshinorin.qualtet.domains.tags.{TagId, TagName, TagPath}

  final case class TagUnsafeV218(
    id: TagId = TagId.apply(),
    name: TagName,
    path: String
  )

  trait TagRepositoryV217[F[_]] {
    def bulkUpsert(data: List[TagUnsafeV218]): F[Int]
    def getAll(): F[Seq[(Int, TagUnsafeV218)]]
  }

  given TagRepositoryV217: TagRepositoryV217[ConnectionIO] = {
    new TagRepositoryV217[ConnectionIO] {

      given tagRead: Read[TagUnsafeV218] =
        Read[(String, String, String)].map { case (id, name, path) => TagUnsafeV218(TagId(id), TagName(name), path) }

      given tagWrite: Write[TagUnsafeV218] =
        Write[(String, String, String)].contramap(p => (p.id.value, p.name.value, p.path))

      override def bulkUpsert(data: List[TagUnsafeV218]): ConnectionIO[Int] = {
        val q = s"""
              INSERT INTO tags (id, name, path)
                VALUES (?, ?, ?)
              ON DUPLICATE KEY UPDATE
                name = VALUES(name)
            """
        Update[TagUnsafeV218](q).updateMany(data)
      }

      override def getAll(): ConnectionIO[Seq[(Int, TagUnsafeV218)]] = {
        sql"""
          SELECT
            COUNT(*) AS count,
            tags.id,
            tags.name,
            tags.path
          FROM tags
          INNER JOIN contents_tagging
            ON contents_tagging.tag_id = tags.id
          INNER JOIN contents
            ON contents_tagging.content_id = contents.id
          GROUP BY
            tags.id
          ORDER BY
            tags.name
        """
          .query[(Int, TagUnsafeV218)]
          .to[Seq]
      }
    }
  }

  import cats.effect.IO
  import net.yoshinorin.qualtet.infrastructure.db.Executer
  import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

  private[versions] def convert(tags: Seq[(Int, TagUnsafeV218)]): Seq[(TagUnsafeV218, Boolean)] = {
    tags.map { case (count, tag) =>
      val (convertedPath, isSuccess) =
        try {
          (TagPath(tag.path).value, true)
        } catch {
          case _: Exception => (tag.path, false)
        }
      (tag.copy(path = convertedPath), isSuccess)
    }
  }

  private[versions] def aggregateConverted(converted: Seq[(TagUnsafeV218, Boolean)]): (List[TagUnsafeV218], Int, Int, List[TagUnsafeV218]) = {
    val tags = converted.map(_._1).toList
    val successCnt = converted.count(_._2)
    val failureCnt = converted.count(!_._2)
    val failedTags = converted.filter(!_._2).map(_._1).toList
    (tags, successCnt, failureCnt, failedTags)
  }

  given V218(using loggerFactory: Log4CatsLoggerFactory[IO]): VersionMigrator[ConnectionIO, IO] = {

    val tagRepositoryV217: TagRepositoryV217[ConnectionIO] = summon[TagRepositoryV217[ConnectionIO]]
    val logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(classOf[V218Migrator.type])

    new VersionMigrator[ConnectionIO, IO](default = Version(version = VersionString("2.18.0"), migrationStatus = MigrationStatus.UNAPPLIED, deployedAt = 0)) {
      override def get(): IO[Version] = super.getDefault()
      override def getDefault(): IO[Version] = super.getDefault()
      override def migrate()(using executer: Executer[ConnectionIO, IO]): IO[Unit] = {
        for {
          currentTags <- executer.transact(tagRepositoryV217.getAll())
          convertedData = convert(currentTags)
          (convertedTags, successCnt, failureCnt, failedTags) = aggregateConverted(convertedData)
          _ <- Option
            .when(failureCnt > 0)(
              for {
                _ <- logger.warn(s"TagPath validation failed for $failureCnt tags, converted to original path")
                _ <- failedTags.foldLeft(IO.unit) { (acc, tag) =>
                  acc *> logger.warn(s"Failed TagPath validation - id: ${tag.id.value}, name: ${tag.name.value}, path: ${tag.path}")
                }
              } yield ()
            )
            .getOrElse(IO.unit)
          _ <- executer.transact(tagRepositoryV217.bulkUpsert(convertedTags))
          _ <- logger.info(s"Tags table migration completed. success: $successCnt, failed: $failureCnt")
        } yield ()
      }
    }
  }

}
