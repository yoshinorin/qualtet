package net.yoshinorin.qualtet.models.authors

trait AuthorRepository {

  /**
   * create a author
   *
   * @param date Instance of Author
   * @return created Author
   */
  def insert(date: Author): Author

}
