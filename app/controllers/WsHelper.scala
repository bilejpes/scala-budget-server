package controllers


import com.google.inject.Inject
import models.IPNResult.{IPNResult, Invalid}
import models.IPNResult
import play.api.http.Status.OK
import play.api.libs.ws.WSClient

import scala.concurrent._
import scala.concurrent.duration._


trait WsHelper {

  val Token = "5lX2X73Os18K74CplmH3217gcL69L768wG263159PmNi8qu73D312183Da9G7080Ad4T5D1py73T3C3K"

  val Timeout = Duration(10, SECONDS)

  @Inject val ws: WSClient = null

  import scala.concurrent.ExecutionContext.Implicits.global

  val apiUrl = " https://ipnpb.sandbox.paypal.com/cgi-bin/webscr"

  object Response {
    val Success = Response(true)
    val Failure = Response(false)
  }
  case class Response(success: Boolean)

  def post(data: Map[String, Seq[String]]): Future[IPNResult] = {
    val request = ws.url(s"$apiUrl")
      request
        .withHeaders(("Content-Type", "x-www-form-urlencoded"))
        .post(data).map { response =>
        response.status match {
          case OK =>
            printf(s"\n\nOk :)\n\n")
            printf(s"\n\n${response.body}\n\n")
            IPNResult.findByName(response.body) match {
              case Some(result) => result
              case _ => Invalid
            }
          case _ =>
            Invalid
        }

      }
  }
}
