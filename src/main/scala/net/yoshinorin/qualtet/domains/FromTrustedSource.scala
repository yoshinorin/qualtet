package net.yoshinorin.qualtet.domains

/**
 * Type class for creating values from trusted sources (e.g., database).
 *
 * This type class should ONLY be implemented in the Repository layer.
 * Database data is assumed to be already validated at write time, so implementations
 * skip validation for performance reasons while still applying normalization for consistency.
 *
 * DO NOT implement this type class in:
 * - HTTP request handlers
 * - User input processing
 * - Any external data source
 *
 * @tparam T The target type to create from a trusted source
 */
trait FromTrustedSource[T] {
  def fromTrusted(value: String): T
}
