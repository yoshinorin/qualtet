package net.yoshinorin.qualtet.domains.models.authors

trait AuthorRepository {

  /**
   * create a author
   *
   * @param date Instance of Author
   * @return created Author
   */
  def insert(date: Author): Author

}
