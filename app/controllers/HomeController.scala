package controllers

import javax.inject._
import play.api.mvc._
import com.redis._

import scalaj.http.{Http, HttpOptions}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {

  def index = Action {
    val r = new RedisClientPool("127.0.0.1", 6379)
    val result = Http("http://example.com/url").postData("""{"id":"12","json":"data"}""")
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000)).asString

    println(result.headerSeq(""))
    //println(r.keys().get.flatten)
    Ok(views.html.index(""))
  }

}
