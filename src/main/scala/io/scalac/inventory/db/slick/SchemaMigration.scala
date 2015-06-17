package io.scalac.inventory.db.slick

import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import scala.slick.jdbc.meta.MTable

import io.scalac.inventory.db.slick.Domain._

object SchemaMigration {


  def migrationNeeded(name: String)(implicit s: Session) = {
    MTable.getTables.list.exists(table => {
      table.name.name.contains(name)
    }) == false
  }

  def migrate() = {
    InventoryContext.db.withDynSession {
      if (migrationNeeded("Office")) {
        println(s"Office is not present. Migrating both")
        (officesQuery.ddl ++ itemsQuery.ddl).create
      }
    }
  }
}
