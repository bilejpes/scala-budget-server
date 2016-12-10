package controllers

import javax.inject.Inject

import api._
import api.Status._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.libs.Files.TemporaryFile

import scala.io.Source

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
      case Some(value) => Ok(value)
      case None => NotFound
    }
  }

  /**
    * Adds record to the database
    * @return Created - if there was no record with specified key
    *         NoContent - changed content of the record in database, returns no body with request
    *         NotFound - in case of any error
    */
  def addRecord = Action { implicit request =>
    getFileText(request) match {
      case Some(record) => Server.addRecord(record) match {
        case ADD_CREATE => Created
        case ADD_CHANGE => NoContent
        case _ => NotFound
      }
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
    * Filter for incoming requests. Accepts only AnyContentAsRaw and header with name
    * or AnyContentAsMultipartFormData
    * @param request
    * @return Some(Record) with "name:String, file:Array[Byte]" where name depends on the incoming request format
    *         (in case of AnyContentAsRaw takes name from header("name"),
    *          in case of AnyContentAsMultipartFormData takes name from the file itself)
    *         None in case of any error
    */
  def getFileText(request: Request[AnyContent]) : Option[Record] = {
    request.body match {
      case x : AnyContentAsRaw => {
        request.headers.get("name") match {
          case Some(name) => Some(Record(name, getRawData(x.asRaw.get)))
          case None => None
        }
      }
      case y : AnyContentAsMultipartFormData => getMultiPartFormData(y.asMultipartFormData.get)
      case _ => None
    }
  }

  /**
    * @param parts
    * @return Some(record)
    */
  def getMultiPartFormData(parts: MultipartFormData[TemporaryFile]) : Option[Record] = {
    parts.files.headOption match {
      case Some(head) =>
        Some(Record(head.filename, Source.fromFile(head.ref.file).mkString.getBytes))
      case None => None
    }
  }

  /**
    * @param rawBuffer
    * @return Array[Byte] from rawBuffer
    */
  def getRawData(rawBuffer: RawBuffer) = {
    rawBuffer.asBytes().get.toArray
  }
}
