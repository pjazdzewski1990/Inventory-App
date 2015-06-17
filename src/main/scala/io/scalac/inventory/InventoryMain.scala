package io.scalac.inventory

import io.scalac.inventory.db.{ReportRepository, ItemRepository, OfficeRepository}
import io.scalac.inventory.db.slick.{SlickRepository, InventoryContext}

object InventoryMain extends App {

  import io.scalac.inventory.db.activate.ActivateRepository

  override def main(args: Array[String]) = {
    testRepo(new ActivateRepository(), "Activate!")
    println("--==--")
    println("--==--")
    println("--==--")
    InventoryContext.db.withDynSession{
      testRepo(new SlickRepository(), "Slick")
    }
  }

  private def testRepo(repo: OfficeRepository with ItemRepository with ReportRepository, tag: String): Unit = {
    val createdOffice = repo.createOffice("Gdańsk", "Gen. Bora-Komorowskiego")
    println(s"$tag: created office $createdOffice")
    val readOffice = repo.readOffice(createdOffice.get)
    println(s"$tag: read office $readOffice")


    val deletedOffice = repo.deleteOffice(createdOffice.get)
    println(s"$tag: deleted office $deletedOffice")
    val readDeletedOffice = repo.readOffice(createdOffice.get)
    println(s"$tag: read deleted office $readDeletedOffice")

    println()
    //////

    val secondOffice = repo.createOffice("Warszawa", "???")
    val createdItem = repo.createItem("Football table", 1L, secondOffice.get)
    println(s"$tag: created item $createdItem")
    val readItem = repo.readItem(createdItem.get)
    println(s"$tag: read item $readItem")

    val thirdOffice = repo.createOffice("Bratyslava", "???")
    val updatedItem = repo.updateItem(createdItem.get, thirdOffice.get)
    println(s"$tag: updated item $updatedItem")
    val readItemAfterUpdate = repo.readItem(createdItem.get)
    println(s"$tag: read item after update $readItemAfterUpdate")

    val deletedItem = repo.deleteItem(createdItem.get)
    println(s"$tag: deleted item $deletedItem")
    val readDeletedItem = repo.readItem(createdItem.get)
    println(s"$tag: read deleted item $readDeletedItem")

    println()
    //////

    repo.createItem("Hell Divers", 1L, secondOffice.get)
    //the second will fail due to invariant
    repo.createItem("Mortal Kombat", 1L, secondOffice.get) // should fail, due to code uniqueness constraint
    val allItems = repo.listAllItems()
    println(s"$tag: Currently we have those $allItems items")
  }
}

