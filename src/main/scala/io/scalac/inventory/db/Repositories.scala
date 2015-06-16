package io.scalac.inventory.db

import io.scalac.inventory.db.activate.Domain._

import scala.util.Try

trait OfficeRepository {
  def createOffice(location: String, address: String): Try[OfficeId]
  def readOffice(id: OfficeId): Try[OfficeEntity]
  /// our office model is immutable, hence no update
  def deleteOffice(id: OfficeId): Try[OfficeId]
}

trait ItemRepository {
  def createItem(name: String, code: Long, officeId: OfficeId): Try[ItemId]
  def readItem(id: ItemId): Try[ItemEntity]
  def updateItem(itemId: ItemId, officeId: OfficeId): Try[ItemEntity]
  def deleteItem(id: ItemId): Try[ItemId]
}

trait ReportRepository {
  def listAllItems(): Try[List[ItemEntity]]
}
