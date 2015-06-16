package io.scalac.inventory.db.activate

import io.scalac.inventory.db.activate.Domain.{ItemEntity, OfficeEntity}
import io.scalac.inventory.db.activate.InventoryContext._
import net.fwbrasil.activate.migration.Migration

class SchemaMigration extends Migration {

  override def timestamp: Long = 1000L

  override def up: Unit = {
    table[OfficeEntity]
      .createTable(
        _.column[String]("location"),
        _.column[String]("address"))

    table[ItemEntity]
      .createTable(
        _.column[String]("name"),
        _.column[String]("code"),
        _.column[OfficeEntity]("inOffice"))
  }
}
