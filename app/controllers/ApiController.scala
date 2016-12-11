package controllers

import java.nio.file.{Files, Paths}
import javax.inject.Inject

import api._
import api.Status._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.libs.Files.TemporaryFile


class ApiController @Inject() extends Controller {

  /**
    * Fetch all keys from the database
    * @return Ok Action with keys in JSON format
    */
  def getKeys = Action {
    Ok(Json.toJson(Server.getKeys))
  }

  /**
    * Fetch all values from the database
    * @return Ok Action with values in JSON format
    */
  def getValues = Action {
    Ok(Json.toJson(Server.getValues))
  }

  /**
    * Fetch all key:value from the database
    * @return Ok Action with key:value in JSON format
    */
  def getPairs = Action {
    Ok(Json.toJson(Server.getPairs))
  }

  /**
    * TODO
    * @param key
    * @return
    */
  def getRecord(key: String) = Action { implicit request =>
    Server.getValue(key) match {
      case Some(value) => Ok(value).withHeaders("Connection" -> "close")
      case None => NotFound
    }
  }

  /**
    * Adds record to the database, header("name" -> "filename") needed
    * @return Created - if there was no record with specified key
    *         NoContent - changed content of the record in database, returns no body with request
    *         NotFound - in case of any error
    */
  def addRecord = Action { implicit request =>
    request.headers.get("name") match {
      case Some(_) =>
        getRecord(request) match {
          case Some(record) =>
            Server.addRecord(record) match {
              case ADD_CREATE => Created
              case ADD_CHANGE => NoContent
              case _ => NotFound
            } // end of Server.addRecord
          case None => NotFound
        }// end of getRecord
      case None => NotFound
    }
  }

  /**
    * Deletes record with specified key
    * @param key delete record with this key
    * @return Ok if successful
    *         NotFound if no such record
    */
  def deleteRecord(key: String) = Action {
    Server.deleteRecord(key) match {
      case DELETE_OK => Ok
      case _ => NotFound
    }
  }

  /**
    * Filter for incoming requests. Accepts only AnyContentAsRaw or AnyContentAsMultipartFormData
    * and header with name
    * @param request
    * @return Record with "name:String, file:Array[Byte]" where name takes from header
    *         (in case of AnyContentAsRaw takes name from header("name"),
    *          in case of AnyContentAsMultipartFormData takes name from the file itself)
    */
  def getRecord(request: Request[AnyContent]) : Option[Record] = {
    request.body match {
      case x : AnyContentAsRaw => Some(Record(request.headers("name"), getRawData(x.asRaw.get)))
      case y : AnyContentAsMultipartFormData =>
        getMultiPartFormData(y.asMultipartFormData.get) match {
          case Some(data) => Some(Record(request.headers("name"), data))
          case _ => None
      }
      case _ => None
    }
  }

  /**
    * @param parts
    * @return Option[Array[Byte]]
    */
  def getMultiPartFormData(parts: MultipartFormData[TemporaryFile]) =
    parts.files.headOption match {
      case Some(head) => Some(Files.readAllBytes(head.ref.file.toPath))
      case _ => None
    }

  /**
    * @param rawBuffer
    * @return Array[Byte] from rawBuffer
    */
  def getRawData(rawBuffer: RawBuffer) =  rawBuffer.asBytes().get.toArray
}
