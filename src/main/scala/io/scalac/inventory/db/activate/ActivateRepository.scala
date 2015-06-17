package io.scalac.inventory.db.activate

import io.scalac.inventory.db.IdWrappers._
import io.scalac.inventory.db.activate.Domain.{Item, ItemEntity, OfficeEntity}
import io.scalac.inventory.db.{Office, ReportRepository, ItemRepository, OfficeRepository}

import io.scalac.inventory.db.activate.InventoryContext._
import net.fwbrasil.activate.statement.StatementSelectValue

import scala.util.Try

class ActivateRepository extends OfficeRepository with ItemRepository with ReportRepository {

  private def genericFinder[T <: Entity : Manifest](_id: String)(implicit tval1: (=> T) => StatementSelectValue) = Try {
    transactional {
      query {
        (o: T) => where(o.id :== _id).select(o)(tval1)
      }.head
    }
  }
  
  ///OFFICE
  override def createOffice(location: String, address: String): Try[OfficeId] = Try {
    transactional {
      val office = new OfficeEntity(location, address)
      /// although some dbs use numbers for ids, it's not always the case (like in Mongo) so in Activate id is a String
      office.id
    }
  }

  override def readOffice(_id: OfficeId): Try[Office] = genericFinder[OfficeEntity](_id.v).map(_.canonical)

  override def deleteOffice(_id: OfficeId): Try[OfficeId] = Try {
    transactional {
      val deleteResults = query {
        (o: OfficeEntity) => where(o.id :== _id.v).select(o)
      }.map(_.delete)

      deleteResults.headOption.map(x => _id).get /// throws on empty list, but this is what we want
    }
  }
  
  ///ITEM
  override def createItem(name: String, code: Long, officeId: OfficeId): Try[ItemId] = Try {
    transactional {
      val item = new ItemEntity(name, code, genericFinder[OfficeEntity](officeId.v).get)
      item.id
    }
  }

  override def readItem(_id: ItemId): Try[Item] = genericFinder[ItemEntity](_id.v).map(_.canonical)

  override def updateItem(itemId: ItemId, officeId: OfficeId): Try[Item] = Try {
    transactional {
      val original = genericFinder[ItemEntity](itemId.v)
      val newOffice = genericFinder[OfficeEntity](officeId.v)
      original.map(toUpdate => {
        toUpdate.inOffice = newOffice.get
        toUpdate
      })
    }.map(_.canonical)
  }.flatten

  override def deleteItem(_id: ItemId): Try[ItemId] = Try {
    transactional {
      val deleteResults = query {
        (o: ItemEntity) => where(o.id :== _id.v).select(o)
      }.map(_.delete)

      deleteResults.headOption.map(x => _id).get /// throws on empty list, but this is what we want
    }
  }

  //REPORT
  override def listAllItems(): Try[List[Item]] = Try {
    transactional {
      all[ItemEntity]
    }.map(_.canonical)
  }
}
