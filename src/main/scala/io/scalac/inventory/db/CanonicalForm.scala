package io.scalac.inventory.db

trait CanonicalForm[T] {
  def canonical: T
}

sealed case class Office(val location: String, val address: String)