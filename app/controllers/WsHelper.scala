package controllers


import java.net.ConnectException

import com.google.inject.Inject
import exceptions.{CommunicationException, InvalidRequestException}
import play.api.Logger
import play.api.http.Status.{CREATED, OK}
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.mvc.Results

import scala.concurrent._
import scala.concurrent.duration._


trait WsHelper {

  val Token = "5lX2X73Os18K74CplmH3217gcL69L768wG263159PmNi8qu73D312183Da9G7080Ad4T5D1py73T3C3K"

  val Timeout = Duration(10, SECONDS)

  @Inject val ws: WSClient = null

  import scala.concurrent.ExecutionContext.Implicits.global

  val apiUrl = "https://ipnpb.sandbox.paypal.com/cgi-bin/webscr"

  object Response {
    val Success = Response(true)
    val Failure = Response(false)
  }
  case class Response(success: Boolean)

  private def recoverable[P](future: Future[P], request: WSRequest) = {
    future recover {
      case t: TimeoutException =>
        Logger.error(s"Timeout when calling to API = ${request.url}. Exception = ${t.getMessage}")
        throw new CommunicationException(s"Timeout when calling to API = ${request.url}. Exception = ${t.getMessage}", t)
      case c: ConnectException =>
        Logger.error(s"Unable to connect to API = ${request.url}")
        throw new CommunicationException(s"Unable to connect to API = ${request.url}", c)
      case c: InvalidRequestException =>
        throw c
      case e =>
        Logger.error(s"Unknown error when calling API = ${request.url}")
        throw new CommunicationException(s"Unknown error when calling API = ${request.url} ${e.getMessage}", e)
    }
  }


  def get(uri: String): Future[JsValue] = {
    val request = ws.url(s"$apiUrl$uri")
    recoverable(
      request
        .withRequestTimeout(Timeout)
        .get().map { response =>
        response.status match {
          case OK => response.json
          case _ => throw new InvalidRequestException(response.json.toString())
        }
      },
      request
    )
  }


  def post(uri: String, data: JsValue): Future[JsValue] = {
    val request = ws.url(s"$apiUrl$uri")
    recoverable(
      request
        .withRequestTimeout(Timeout)
        .post(data).map { response =>
        response.status match {
          case CREATED => response.json
          case OK => response.json
          case _ => throw new InvalidRequestException(response.json.toString())
        }

      },
      request
    )
  }


  def postEmpty(uri: String): Future[Unit] = {
    val request = ws.url(s"$apiUrl$uri")
    recoverable(
      request
        .withRequestTimeout(Timeout)
        .post(Results.EmptyContent()).map { response =>
        response.status match {
          case CREATED => Unit
          case OK => Unit
          case _ =>
            throw new InvalidRequestException(response.json.toString())
        }

      },
      request
    )
  }


  def delete(uri: String): Future[Response] = {
    val request = ws.url(s"$apiUrl$uri")
    recoverable(
      request
        .withRequestTimeout(Timeout)
        .delete().map { response =>
        response.status match {
          case OK => Response.Success
          case _ => throw new InvalidRequestException()
        }

      },
      request
    )
  }
}
