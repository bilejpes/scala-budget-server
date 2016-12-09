package controllers

import javax.inject.Inject

import api.{DB, Record}
import play.api.libs.json.Json
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._

import scala.io.Source

class ApiController @Inject() extends Controller {

  def getKeys = Action {
    Ok(Json.toJson(DB.getKeys))
  }

  def getValues = Action {
    Ok(Json.toJson(DB.getValues))
  }

  def getPairs = Action {
    Ok(Json.toJson(DB.getPairs))
  }

  val recForm  =  Form[Record]{
    mapping(
      "key" -> text,
      "value" -> text
    )(Record.apply)(Record.unapply)
  }

  def addRecord = Action { implicit request =>
//    println(recForm.bind(getFileText(request.body)))
//    println(getFileText(request.body))
//    println(getData(request.body))
//    println(recForm.bind(getData(request.body)))
    DB.save(recForm.bind(getFileText(request.body)).get)
    Redirect(routes.HomeController.index())
  }

  def getData(body: AnyContent): Map[String, String] = {
    body.asMultipartFormData.map(x => x.asFormUrlEncoded) match {
      case Some(s) => {
        s map {
          case (k, v) => (k, v match {case x : Seq[String] => x(0)})
        }
      }
      case None => {
        println("beee")
        Map()
      }
    }
  }

  def getFileText(body: AnyContent) : Map[String, String] = {
    /*body.asMultipartFormData.get.files map {
      case Vector(x) =>  Map(x.filename -> Source.fromFile(x.ref.file).mkString)
      case _  => Map()
    }*/
    body.asMultipartFormData.get.files map { x =>
      (x.filename, Source.fromFile(x.ref.file).mkString)
    } match {
      case Vector((k, v)) => Map[String, String]("key" -> k, "value" -> v)
    }
  }
}
