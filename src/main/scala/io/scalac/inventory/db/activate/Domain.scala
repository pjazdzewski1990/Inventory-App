package io.scalac.inventory.db.activate

import io.scalac.inventory.db.{Item, Office, CanonicalForm}
import net.fwbrasil.activate.entity.Entity

object Domain {

  class OfficeEntity(val location: String, val address: String) extends Entity with CanonicalForm[Office] {
    override def canonical: Office = Office(location, address)
  }

  class ItemEntity(val name: String, val code: Long, var inOffice: OfficeEntity) extends Entity with CanonicalForm[Item] {
    def codeMustBeUnique = unique(_.code)

    override def canonical: Item = new Item(name, code, inOffice.canonical)
  }
}
