package io.scalac.inventory.db.activate

import io.scalac.inventory.db.{ReportRepository, ItemRepository, OfficeRepository}
import io.scalac.inventory.db.activate.Domain._

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

  override def readOffice(_id: OfficeId): Try[OfficeEntity] = genericFinder[OfficeEntity](_id.v)

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
      val item = new ItemEntity(name, code, readOffice(officeId).get)
      item.id
    }
  }

  override def readItem(_id: ItemId): Try[ItemEntity] = genericFinder[ItemEntity](_id.v)

  override def updateItem(itemId: ItemId, officeId: OfficeId): Try[ItemEntity] = Try {
    transactional {
      val original = readItem(itemId)
      val newOffice = readOffice(officeId)
      original.map(toUpdate => {
        toUpdate.inOffice = newOffice.get
        toUpdate
      }).get
    }
  }

  override def deleteItem(_id: ItemId): Try[ItemId] = Try {
    transactional {
      val deleteResults = query {
        (o: ItemEntity) => where(o.id :== _id.v).select(o)
      }.map(_.delete)

      deleteResults.headOption.map(x => _id).get /// throws on empty list, but this is what we want
    }
  }

  //REPORT
  override def listAllItems(): Try[List[ItemEntity]] = Try {
    transactional {
      all[ItemEntity]
    }
  }
}
