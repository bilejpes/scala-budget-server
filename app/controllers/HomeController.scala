package controllers

import java.io.File
import javax.inject._

import play.api.mvc._

import scala.io.Source

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {

  def index = Action {
    val readme : Iterator[String] = Source.fromFile(new File("README")).getLines()
    Ok(views.html.index(readme))
  }

}
