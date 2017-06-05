package controllers

import java.nio.file.Files
import javax.inject.Inject

import api.Status._
import api._
import models.IPNResult.{Invalid, Verified}
import models._
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.ExecutionContext


class ApiController @Inject()(implicit ec: ExecutionContext) extends Controller with WsHelper {

  import play.api.data.Forms._
  import play.api.data._



  val userForm = Form(
    mapping(
      "" -> mapping(
        "receiver_email" -> optional(text),
        "receiver_id" -> optional(text),
        "residence_country" -> optional(text)
      )(AboutSeller.apply)(AboutSeller.unapply),
      "" -> mapping(
        "test_ipn" -> optional(text),
        "transaction_subject" -> optional(text),
        "txn_id" -> optional(text),
        "txn_type" -> optional(text)
      )(AboutTransaction.apply)(AboutTransaction.unapply),
      "" -> mapping(
        "payer_email" -> optional(text),
        "payer_id" -> optional(text),
        "payer_status" -> optional(text),
        "first_name" -> optional(text),
        "last_name" -> optional(text),
        "address_city" -> optional(text),
        "address_country" -> optional(text),
        "address_state" -> optional(text),
        "address_status" -> optional(text),
        "address_country_code" -> optional(text),
        "address_name" -> optional(text),
        "address_street" -> optional(text),
        "address_zip" -> optional(text)
      )(AboutBuyer.apply)(AboutBuyer.unapply),
      "" -> mapping(
        "custom" -> optional(text),
        "handling_amount" -> optional(text),
        "item_name" -> optional(text),
        "item_number" -> optional(text),
        "mc_currency" -> optional(text),
        "mc_fee" -> optional(text),
        "mc_gross" -> optional(text),
        "payment_date" -> optional(text),
        "payment_fee" -> optional(text),
        "payment_gross" -> optional(text),
        "payment_status" -> optional(text),
        "payment_type" -> optional(text),
        "protection_eligibility" -> optional(text),
        "quantity" -> optional(text),
        "shipping" -> optional(text),
        "tax" -> optional(text)
      )(AboutPayment.apply)(AboutPayment.unapply),
      "" -> mapping(
        "notify_version" -> optional(text),
        "charset" -> optional(text),
        "verify_sign" -> optional(text)
      )(Other.apply)(Other.unapply)
    )(IPN.apply)(IPN.unapply)
  )

  val receiverEmail = "pes@bb.com"
  val paymentStatus = "completed"
  val handlingAmount = "50"


  implicit val aboutSellerFormat: Format[AboutSeller] = Json.format[AboutSeller]
  implicit val aboutTransactionFormat: Format[AboutTransaction] = Json.format[AboutTransaction]
  implicit val aboutBuyerFormat: Format[AboutBuyer] = Json.format[AboutBuyer]
  implicit val aboutPaymentFormat: Format[AboutPayment] = Json.format[AboutPayment]
  implicit val otherFormat: Format[Other] = Json.format[Other]
  implicit val ipnFormat: Format[IPN] = Json.format[IPN]

  def ipnPost = Action { implicit request =>
    val body: Map[String, Seq[String]] = request.body.asFormUrlEncoded.get
    val upd:  List[(String, Seq[String])] = List("cmd" -> Seq("_notify-validate"))
    userForm.bindFromRequest.fold(
      formWithErrors => {
        val data = formWithErrors.data.mkString(",  ")
        BadRequest(data)
      },
      ipn => {
        println(s"$ipn")
        post((upd ++ body.toList).toMap) map {
          case Verified =>
            println("Verified callback from PayPal")
            if(checkRequiredFields(ipn)) {
              println("validated fields - continue")
            }
            else {
              println("some fraud is here")
            }
          case Invalid =>
            println("Invalid callback from PayPal")
            if(checkRequiredFields(ipn)) {
              println("validated fields - continue")
            }
            else {
              println("some fraud is here")
            }

        }
        Ok(Json.toJson(ipn))
      }
    )
  }


  private def checkRequiredFields(ipn: IPN): Boolean = {
      ipn.aboutSeller.receiver_email.getOrElse("") == receiverEmail &&
      ipn.aboutPayment.payment_status.getOrElse("") == paymentStatus &&
      ipn.aboutPayment.handling_amount.getOrElse("") == handlingAmount
  }



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
      case x : AnyContentAsRaw =>Some(Record(request.headers("name"), getRawData(x.asRaw.get)))
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
    * @return Option[Array[Byte]] from rawBuffer
    */
  def getRawData(rawBuffer: RawBuffer) : Array[Byte] = {
    rawBuffer.asBytes() match {
      case Some(bytes) => bytes.toArray
      case None => Files.readAllBytes(rawBuffer.asFile.toPath)
    }
  }
}
