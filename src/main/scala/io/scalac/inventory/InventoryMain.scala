package io.scalac.inventory

object InventoryMain extends App {

  import io.scalac.inventory.db.activate.ActivateRepository

  override def main(args: Array[String]) = {
    val activate = new ActivateRepository()
    val TAG = "Activate!"

    val createdOffice = activate.createOffice("Gdańsk", "Gen. Bora-Komorowskiego")
    println(s"$TAG: created office $createdOffice")
    val readOffice = activate.readOffice(createdOffice.get)
    println(s"$TAG: read office $readOffice")


    val deletedOffice = activate.deleteOffice(createdOffice.get)
    println(s"$TAG: deleted office $deletedOffice")
    val readDeletedOffice = activate.readOffice(createdOffice.get)
    println(s"$TAG: read deleted office $readDeletedOffice")

    println()
    //////

    val secondOffice = activate.createOffice("Warszawa", "???")
    val createdItem = activate.createItem("Football table", 1L, secondOffice.get)
    println(s"$TAG: created item $createdItem")
    val readItem = activate.readItem(createdItem.get)
    println(s"$TAG: read item $readItem")

    val thirdOffice = activate.createOffice("Bratyslava", "???")
    val updatedItem = activate.updateItem(createdItem.get, thirdOffice.get)
    println(s"$TAG: updated item $updatedItem")
    val readItemAfterUpdate = activate.readItem(createdItem.get)
    println(s"$TAG: read item after update $readItemAfterUpdate")

    val deletedItem = activate.deleteItem(createdItem.get)
    println(s"$TAG: deleted item $deletedItem")
    val readDeletedItem = activate.readItem(createdItem.get)
    println(s"$TAG: read deleted item $readDeletedItem")

    println()
    //////

    activate.createItem("Hell Divers", 1L, secondOffice.get)
    //the second will fail due to invariant
    activate.createItem("Mortal Kombat", 1L, secondOffice.get)
    val allItems = activate.listAllItems()
    println(s"$TAG: Currently we have those $allItems items")
  }
}

object InventoryApi extends App {

  import akka.actor._
  import akka.stream.ActorFlowMaterializer
  import akka.http.scaladsl.model.HttpResponse
  import akka.http.scaladsl.server.Directives._
  import akka.http.scaladsl.Http
  import akka.http.scaladsl.model.{MediaTypes, HttpEntity}
  import scala.pickling.Defaults._
  import scala.pickling.json._
  import akka.http.scaladsl.server.StandardRoute
  import io.scalac.inventory.db.activate.ActivateRepository
  import io.scalac.inventory.db.activate.Domain._
  import io.scalac.inventory.db.{ReportRepository, ItemRepository, OfficeRepository}

  import scala.pickling.{Pickler, FastTypeTag}
  import scala.util.Try

  def routes = buildRoutesForRepo(new ActivateRepository, "activate")

  def completeWithPickling[T: Pickler: FastTypeTag](route: ⇒ Try[T]): StandardRoute = {
    route match {
      case scala.util.Success(v) =>
        println(s"200 (Pickling) with $v")
        val jsonVal = v.pickle
        val response = HttpResponse(entity = HttpEntity(MediaTypes.`application/json`, jsonVal.value))
        StandardRoute(_.complete(response))
      case scala.util.Failure(ex) if ex.isInstanceOf[MatchError] || ex.isInstanceOf[NoSuchElementException] =>
        println(s"Rejecting (Pickling)")
        StandardRoute(_.reject())
      case scala.util.Failure(ex) =>
        println(s"FAILING (Pickling)")
        StandardRoute(_.fail(ex))
    }
  }

  private def buildRoutesForRepo(repo: OfficeRepository with ItemRepository with ReportRepository,
                                 tag: String) =
    pathPrefix(tag) {
      path("office" / Rest) { id =>
        get {
          completeWithPickling {
            repo.readOffice(OfficeId(id)).map(_.canonical)
          }
        } ~
        post {
          completeWithPickling {
            repo.createOffice("location", "office address").map(_.canonical)
          }
        } ~
        delete {
          completeWithPickling {
            repo.deleteOffice(OfficeId(id)).map(_.canonical)
          }
        }
      }
    }

  override def main(args: Array[String]) = {
    println(s"API starting on 0.0.0.0:9000")

    implicit val system = ActorSystem()
    implicit val executor = system.dispatcher
    implicit val materializer = ActorFlowMaterializer()

    Http().bindAndHandle(routes, "0.0.0.0", 9000)
  }
}