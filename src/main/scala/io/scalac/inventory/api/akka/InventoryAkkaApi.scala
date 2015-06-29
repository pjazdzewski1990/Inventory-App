package io.scalac.inventory.api.akka


import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, MediaTypes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.stream.ActorFlowMaterializer
import io.scalac.inventory.db.activate.ActivateRepository
import io.scalac.inventory.db.{ItemRepository, OfficeRepository, ReportRepository}
import io.scalac.inventory.db.IdWrappers.OfficeId
import io.scalac.inventory.db.slick.{InventoryContext, SlickRepository}

import scala.pickling.Defaults._
import scala.pickling.json._
import scala.pickling.{FastTypeTag, Pickler}
import scala.util.Try

object InventoryAkkaApi extends App {

//  implicit val session = InventoryContext.db.createSession()
//  val activate = new ActivateRepository
//  val slick = new SlickRepository

  def routes = buildRoutesForRepo(new ActivateRepository, "activate") // ~ buildRoutesForRepo(new SlickRepository, "slick")

  def completeWithTry[T: Pickler: FastTypeTag](route: ⇒ Try[T]): StandardRoute = {
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
        println(s"Failing (Pickling)")
        StandardRoute(_.fail(ex))
    }
  }

  private def buildRoutesForRepo(repo: OfficeRepository with ItemRepository with ReportRepository,
                                 tag: String) = {
    println(s"Building for $tag")

    pathPrefix(tag) {
      path("office" / Rest) { id =>
        get {
          println("GET: office " + id)
          completeWithTry {
            repo.readOffice(OfficeId(id))
          }
        } ~
          post {
            println("POST: office " + id)
            completeWithTry {
              repo.createOffice("location", "office address").map(_.canonical)
            }
          } ~
          delete {
            println("DELETE: office " + id)
            completeWithTry {
              repo.deleteOffice(OfficeId(id)).map(_.canonical)
            }
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
