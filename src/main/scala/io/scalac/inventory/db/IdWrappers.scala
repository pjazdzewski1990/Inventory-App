package io.scalac.inventory.db

object IdWrappers {
  case class OfficeId(v: String) extends AnyRef with CanonicalForm[String] {
    override def canonical: String = v
  }

  case class ItemId(v: String) extends AnyRef with CanonicalForm[String] {
    override def canonical: String = v
  }

  implicit def string2OfficeId(v: String) = OfficeId(v)
  implicit def string2ItemId(v: String) = ItemId(v)
}
