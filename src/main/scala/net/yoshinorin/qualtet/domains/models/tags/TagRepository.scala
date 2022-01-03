package net.yoshinorin.qualtet.domains.models.tags

import doobie.ConnectionIO

trait TagRepository {

  /**
   * find a Tag by Name
   *
   * @param data tag name
   * @return Instance of Tag
   */
  def findByName(data: TagName): ConnectionIO[Option[Tag]]

  /**
   * create a Tag
   *
   * @param data Instance of ExternalResource
   * @return dummy long id (Doobie return Long)
   */
  def upsert(data: Tag): ConnectionIO[Long]

  /**
   * create a Tag
   *
   * @param data List of Tag
   * @return dummy long id (Doobie return Int)
   *
   * TODO: remove Option
   * TODO: return ConnectionIO[Long]
   */
  def bulkUpsert(data: Option[List[Tag]]): ConnectionIO[Int]

}
