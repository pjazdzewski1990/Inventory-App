package io.scalac.inventory.db.slick

import scala.slick.lifted.{TableQuery, Tag}
import scala.slick.driver.H2Driver.simple._

object Domain {

  class OfficeTable(tag: Tag) extends Table[(Long, String, String)](tag, "Office") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def location = column[String]("location")
    def address = column[String]("address")
    def * = (id, location, address)
  }
  val officesQuery = TableQuery[OfficeTable]

  class ItemTable(tag: Tag) extends Table[(Long, String, Long, Long)](tag, "Item") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def code = column[Long]("code")
    def inOffice = column[Long]("inOffice")

    // can't define constraints in slick :(
    // the only way we could achieve it through index
//    def uniqueCode = index("IDX_Code", code, unique = true)

    def office_pk = foreignKey("office_pk", inOffice, officesQuery)(_.id)
    def * = (id, name, code, inOffice)
  }
  val itemsQuery = TableQuery[ItemTable]
}
