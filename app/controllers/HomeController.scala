package controllers

import javax.inject._

import api.{DB, Key}
import play.api._
import play.api.mvc._
import com.redis._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {

  /**<::
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    val r = new RedisClient("127.0.0.1", 6379)
    val v = r.keys().get.flatten

    val q = for(e <- v) yield {
      //print(s"key:$e   ");
      r.getType(e) match {
          case Some("string") => r.get(e)//println(s"value:${r.get(e)}")
          case Some("hash") => "hash" //println("hash")
          case _ =>
      }
    }

    println("ASDADSA - " +DB.getPairs)

    //println(r.keys().get.flatten)
    Ok(views.html.index(""))
  }

  val keyForm  =  Form[Key]{
    mapping(
      "key" -> text,
      "value" -> text
    )(Key.apply)(Key.unapply)
  }

  def addKey = Action { implicit  request =>
    val key = keyForm.bindFromRequest.get
    DB.save(key)
    Redirect(routes.HomeController.index())
  }

  def getKeys = Action {
    val keys = DB.getKeys
    /*println(keys)
    println(keys.getClass)*/
    Ok(Json.toJson(keys))
  }

  def getValues = Action {
    val values = DB.getValues
    Ok(Json.toJson(values))
  }

  def getPairs = Action {
    Ok(Json.toJson(DB.getPairs))
  }

}
