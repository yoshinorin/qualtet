package net.yoshinorin.qualtet.domains.models.externalResources

import doobie.ConnectionIO

trait ExternalResourceRepository {

  /**
   * create a externalResource (for meta)
   *
   * @param data Instance of ExternalResource
   * @return dummy long id (Doobie return Long)
   */
  def upsert(data: ExternalResource): ConnectionIO[Long]

  /**
   * create a externalResources (for meta)
   *
   * @param data List of ExternalResources
   * @return dummy long id (Doobie return Int)
   *
   * TODO: remove Option
   * TODO: return ConnectionIO[Long]
   */
  def bulkUpsert(data: Option[List[ExternalResource]]): ConnectionIO[Int]

}
