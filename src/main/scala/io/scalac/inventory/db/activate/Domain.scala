package io.scalac.inventory.db.activate

import net.fwbrasil.activate.entity.Entity

object Domain {
  trait CanonicalForm[T] {
    def canonical: T
  }

  case class OfficeId(v: String) extends AnyRef with CanonicalForm[String] {
    override def canonical: String = v
  }

  sealed case class Office(val location: String, val address: String)
  class OfficeEntity(val location: String, val address: String) extends Entity with CanonicalForm[Office] {
    override def canonical: Office = Office(location, address)
  }

  case class ItemId(v: String) extends AnyRef with CanonicalForm[String] {
    override def canonical: String = v
  }

  sealed case class Item(val name: String, val code: Long, var office: Office)
  class ItemEntity(val name: String, val code: Long, var inOffice: OfficeEntity) extends Entity with CanonicalForm[Item] {
    def codeMustBeUnique = unique(_.code)

    override def canonical: Item = new Item(name, code, inOffice.canonical)
  }

  implicit def string2OfficeId(v: String) = OfficeId(v)
  implicit def string2ItemId(v: String) = ItemId(v)
}
