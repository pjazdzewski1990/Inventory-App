package io.scalac.inventory.db.slick

import scala.slick.driver.H2Driver.simple._

object InventoryContext {
  lazy val db = Database.forURL(url = "jdbc:h2:file:~/inventory_app", driver = "org.h2.Driver")
}
