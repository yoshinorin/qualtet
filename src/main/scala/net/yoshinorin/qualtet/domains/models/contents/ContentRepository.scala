package net.yoshinorin.qualtet.domains.models.contents

trait ContentRepository {

  /**
   * create a content
   *
   * @param data Instance of Content
   * @return created Content
   */
  def insert(data: Content): Content

  def find = ???

  def update = ???
}
