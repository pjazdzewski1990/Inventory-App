package io.scalac.inventory

import io.scalac.inventory.db.IdWrappers.OfficeId

object InventoryApi extends App {

  import akka.actor._
  import akka.http.scaladsl.Http
  import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes}
  import akka.http.scaladsl.server.Directives._
  import akka.http.scaladsl.server.StandardRoute
  import akka.stream.ActorFlowMaterializer
  import io.scalac.inventory.db.activate.ActivateRepository
  import io.scalac.inventory.db.activate.Domain._
  import io.scalac.inventory.db.{ItemRepository, OfficeRepository, ReportRepository}

  import scala.pickling.Defaults._
  import scala.pickling.json._
  import scala.pickling.{FastTypeTag, Pickler}
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
            repo.readOffice(OfficeId(id))
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
