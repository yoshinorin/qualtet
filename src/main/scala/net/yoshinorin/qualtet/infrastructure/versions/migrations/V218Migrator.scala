package net.yoshinorin.qualtet.infrastructure.versions

object V218Migrator {

  import doobie.{Read, Write}
  import doobie.ConnectionIO
  import doobie.syntax.all.toSqlInterpolator
  import doobie.util.update.Update
  import net.yoshinorin.qualtet.domains.tags.{TagId, TagName, TagPath}
  import net.yoshinorin.qualtet.domains.series.{SeriesId, SeriesName, SeriesPath}

  final case class TagUnsafeV218(
    id: TagId = TagId.apply(),
    name: TagName,
    path: String
  )

  final case class SeriesUnsafeV218(
    id: SeriesId = SeriesId.apply(),
    name: SeriesName,
    path: String
  )

  trait TagRepositoryV217[F[_]] {
    def bulkUpsert(data: List[TagUnsafeV218]): F[Int]
    def getAll(): F[Seq[TagUnsafeV218]]
  }

  trait SeriesRepositoryV217[F[_]] {
    def bulkUpsert(data: List[SeriesUnsafeV218]): F[Int]
    def getAll(): F[Seq[SeriesUnsafeV218]]
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

      override def getAll(): ConnectionIO[Seq[TagUnsafeV218]] = {
        sql"""
          SELECT
            id,
            name,
            path
          FROM tags
        """
          .query[TagUnsafeV218]
          .to[Seq]
      }
    }
  }

  given SeriesRepositoryV217: SeriesRepositoryV217[ConnectionIO] = {
    new SeriesRepositoryV217[ConnectionIO] {

      given seriesRead: Read[SeriesUnsafeV218] =
        Read[(String, String, String)].map { case (id, name, path) => SeriesUnsafeV218(SeriesId(id), SeriesName(name), path) }

      given seriesWrite: Write[SeriesUnsafeV218] =
        Write[(String, String, String)].contramap(s => (s.id.value, s.name.value, s.path))

      override def bulkUpsert(data: List[SeriesUnsafeV218]): ConnectionIO[Int] = {
        val q = s"""
              INSERT INTO series (id, name, path, title, description)
                VALUES (?, ?, ?, '', NULL)
              ON DUPLICATE KEY UPDATE
                name = VALUES(name),
                path = VALUES(path)
            """
        Update[SeriesUnsafeV218](q).updateMany(data)
      }

      override def getAll(): ConnectionIO[Seq[SeriesUnsafeV218]] = {
        sql"""
          SELECT
            id,
            name,
            path
          FROM series
        """
          .query[SeriesUnsafeV218]
          .to[Seq]
      }
    }
  }

  import cats.effect.IO
  import net.yoshinorin.qualtet.infrastructure.db.Executer
  import org.typelevel.log4cats.{LoggerFactory as Log4CatsLoggerFactory, SelfAwareStructuredLogger}

  private[versions] def convertTags(tags: Seq[TagUnsafeV218]): Seq[(TagUnsafeV218, Boolean)] = {
    tags.map { tag =>
      val (convertedPath, isSuccess) =
        try {
          (TagPath(tag.path).value, true)
        } catch {
          case _: Exception => (tag.path, false)
        }
      (tag.copy(path = convertedPath), isSuccess)
    }
  }

  private[versions] def convertSeries(series: Seq[SeriesUnsafeV218]): Seq[(SeriesUnsafeV218, Boolean)] = {
    series.map { series =>
      val (convertedPath, isSuccess) =
        try {
          (SeriesPath(series.path).value, true)
        } catch {
          case _: Exception => (series.path, false)
        }
      (series.copy(path = convertedPath), isSuccess)
    }
  }

  private[versions] def aggregateConverted[T](converted: Seq[(T, Boolean)]): (List[T], Int, Int, List[T]) = {
    val items = converted.map(_._1).toList
    val successCnt = converted.count(_._2)
    val failureCnt = converted.count(!_._2)
    val failedItems = converted.filter(!_._2).map(_._1).toList
    (items, successCnt, failureCnt, failedItems)
  }

  private[versions] def runTagMigration(
    tagRepositoryV217: TagRepositoryV217[ConnectionIO],
    logger: SelfAwareStructuredLogger[IO],
    executer: Executer[ConnectionIO, IO]
  ): IO[Unit] = {
    for {
      currentTags <- executer.transact(tagRepositoryV217.getAll())
      convertedTagData = convertTags(currentTags)
      (convertedTags, tagSuccessCnt, tagFailureCnt, failedTags) = aggregateConverted(convertedTagData)
      _ <- Option
        .when(tagFailureCnt > 0)(
          for {
            _ <- logger.warn(s"TagPath validation failed for $tagFailureCnt tags, converted to original path")
            _ <- failedTags.foldLeft(IO.unit) { (acc, tag) =>
              acc *> logger.warn(s"Failed TagPath validation - id: ${tag.id.value}, name: ${tag.name.value}, path: ${tag.path}")
            }
          } yield ()
        )
        .getOrElse(IO.unit)
      _ <- executer.transact(tagRepositoryV217.bulkUpsert(convertedTags))
      _ <- logger.info(s"Tags table migration completed. success: $tagSuccessCnt, failed: $tagFailureCnt")
    } yield ()
  }

  private[versions] def runSeriesMigration(
    seriesRepositoryV217: SeriesRepositoryV217[ConnectionIO],
    logger: SelfAwareStructuredLogger[IO],
    executer: Executer[ConnectionIO, IO]
  ): IO[Unit] = {
    for {
      currentSeries <- executer.transact(seriesRepositoryV217.getAll())
      convertedSeriesData = convertSeries(currentSeries)
      (convertedSeries, seriesSuccessCnt, seriesFailureCnt, failedSeries) = aggregateConverted(convertedSeriesData)
      _ <- Option
        .when(seriesFailureCnt > 0)(
          for {
            _ <- logger.warn(s"SeriesPath validation failed for $seriesFailureCnt series, converted to original path")
            _ <- failedSeries.foldLeft(IO.unit) { (acc, series) =>
              acc *> logger.warn(s"Failed SeriesPath validation - id: ${series.id.value}, name: ${series.name.value}, path: ${series.path}")
            }
          } yield ()
        )
        .getOrElse(IO.unit)
      _ <- executer.transact(seriesRepositoryV217.bulkUpsert(convertedSeries))
      _ <- logger.info(s"Series table migration completed. success: $seriesSuccessCnt, failed: $seriesFailureCnt")
    } yield ()
  }

  given V218(using loggerFactory: Log4CatsLoggerFactory[IO]): VersionMigrator[ConnectionIO, IO] = {

    val tagRepositoryV217: TagRepositoryV217[ConnectionIO] = summon[TagRepositoryV217[ConnectionIO]]
    val seriesRepositoryV217: SeriesRepositoryV217[ConnectionIO] = summon[SeriesRepositoryV217[ConnectionIO]]
    val logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLoggerFromClass(classOf[V218Migrator.type])

    new VersionMigrator[ConnectionIO, IO](init = Version(version = VersionString("2.18.0"), migrationStatus = MigrationStatus.UNAPPLIED, deployedAt = 0)) {
      override def get(): IO[Version] = super.getInit()
      override def getInit(): IO[Version] = super.getInit()
      override def migrate()(using executer: Executer[ConnectionIO, IO]): IO[Unit] = {
        for {
          _ <- runTagMigration(tagRepositoryV217, logger, executer)
          _ <- runSeriesMigration(seriesRepositoryV217, logger, executer)
        } yield ()
      }
    }
  }

}
