package io.scalac.inventory.db.slick

import io.scalac.inventory.db.IdWrappers.{OfficeId, ItemId}
import io.scalac.inventory.db._

import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession

import scala.util.Try

class SlickRepository extends OfficeRepository with ItemRepository with ReportRepository {

  SchemaMigration.migrate()

  ///OFFICE
  override def createOffice(location: String, address: String): Try[OfficeId] = Try {
    val id = Domain.officesQuery.map( c => (c.location, c.address) ).
      returning( Domain.officesQuery.map(_.id) ).
      insert( (location, address) )
    OfficeId(id.toString)
  }

  override def readOffice(_id: OfficeId): Try[Office] = Try {
    val toRead = _id.v.toLong
    val (_, l, a) = Domain.officesQuery.filter(_.id === toRead).first
    Office(l, a)
  }

  /// our office model is immutable, hence no update
  override def deleteOffice(_id: OfficeId): Try[OfficeId] = Try {
    val toDelete = _id.v.toLong
    Domain.officesQuery.filter(_.id === toDelete).delete
    _id
  }

  ///ITEM
  override def createItem(name: String, code: Long, officeId: OfficeId): Try[ItemId] = Try {
    val id = Domain.itemsQuery.map( c => (c.name, c.code, c.inOffice) ).
      returning( Domain.itemsQuery.map(_.id) ).
      insert( (name, code, officeId.v.toLong) )
    ItemId(id.toString)
  }

  override def readItem(_id: ItemId): Try[Item] = Try {
    val toRead = _id.v.toLong
    val (_, n, c, o) = Domain.itemsQuery.filter(_.id === toRead).first
    val office = readOffice(OfficeId(o.toString)).get
    Item(n, c, office)
  }

  override def deleteItem(_id: ItemId): Try[ItemId] = Try {
    val toDelete = _id.v.toLong
    Domain.itemsQuery.filter(_.id === toDelete).delete
    _id
  }

  override def updateItem(itemId: ItemId, officeId: OfficeId): Try[Item] = Try {
    val toRead = itemId.v.toLong
    Domain.itemsQuery.
      filter(_.id === toRead).
      map( c => (c.inOffice) ).
      update( (officeId.v.toLong) )
    readItem(itemId)
  }.flatten

  //REPORT
  override def listAllItems(): Try[List[Item]] = Try {
//    Domain.itemsQuery.list.map{
//      case (_, n, c, o) =>
//        val office = readOffice(OfficeId(o.toString)).get
//        Item(n, c, office)
//    }
    Domain.itemsQuery.join(Domain.officesQuery).on(_.inOffice === _.id).list.map {
      case ((_, name, code, _), (_, location, address)) =>
        Item(name, code, Office(location, address))
    }
  }
}
