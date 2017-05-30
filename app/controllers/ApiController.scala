package controllers

import java.nio.file.{Files, Paths}
import javax.inject.Inject

import api._
import api.Status._
import org.joda.time.DateTime
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._
import play.api.libs.Files.TemporaryFile


class ApiController @Inject() extends Controller with WsHelper {


/*

  val voucherForm = Form(
    mapping(
      "voucherName" -> nonEmptyText,
      "voucherCode" -> optional(text(minLength = 6).verifying(pattern("""[a-zA-Z0-9]+""".r, error = "Only alphanumeric allowed"))),
      "genCode" -> boolean,
      "vouchersCount" -> optional(number(min = 1)),
      "planId" -> nonEmptyText,
      "totalCount" -> default(number(min = 1), 1),
      "durationInDays" -> optional(number(min = 1)),
      "durationAsDate" -> optional(jodaDate("yyyy-MM-dd", timeZone = DateTimeZone.UTC)),
      "validUntil" -> jodaDate("yyyy-MM-dd", timeZone = DateTimeZone.UTC),
      "email" -> optional(email),
      "applyVoucher" -> boolean
    )(VoucherForm.apply)(VoucherForm.unapply).verifying(durationCheckConstraint)
  )


  def index = Action {
    Redirect(routes.UserController.getProfileByEmailOrId(None))
  }


  def vouchers = Action.async { implicit request =>
    planDao.getVoucherable.map { plans =>
      Ok(views.html.vouchers.index(voucherForm, plans.map(p => p.id -> p.id)))
    }
  }


  def vouchersPost = Action.async { implicit request =>
    voucherForm.bindFromRequest.fold(
      formWithErrors => {
        planDao.getVoucherable.map( plans =>
          BadRequest(views.html.vouchers.index(formWithErrors, plans.map(p => p.id -> p.id)))
        )
      },
      voucherData => {
        val redirect: Result = Redirect(routes.Application.vouchers)
        voucherDao.generateVoucher(voucherData.copy(validUntil = voucherData.validUntil.plusDays(1))).map { vouchers =>
          if (voucherData.applyVoucher) {
            Redirect(routes.UserController.getProfileByEmailOrId(voucherData.email))
          } else {
            redirect.flashing("success" -> Messages("voucher.form.success", vouchers.map(_.voucherCode).mkString(",")))
          }
        } recover {
          case e: InvalidRequestException => redirect.flashing("failure" -> Messages("voucher.form.failure", e.getMessage))
        }
      }
    )
  }*/
  import play.api.data._
  import play.api.data.Forms._

  case class AboutYou(receiver_email: Option[String],  receiver_id: Option[String],  residence_country: Option[String])
  case class AboutTransaction(test_ipn: Option[String], transaction_subject: Option[String], txn_id: Option[String], txn_type: Option[String])
  case class AboutBuyer(payer_email: Option[String] = None, payer_id: Option[String] = None, payer_status: Option[String] = None, first_name: Option[String] = None, last_name: Option[String] = None, address_city: Option[String] = None, address_country: Option[String] = None, address_state: Option[String] = None, address_status: Option[String] = None, address_country_code: Option[String] = None, address_name: Option[String] = None, address_street: Option[String] = None, address_zip: Option[String] = None)
  case class AboutPayment(custom: Option[String] = None, handling_amount: Option[String] = None, item_name: Option[String] = None, item_number: Option[String] = None, mc_currency: Option[String] = None, mc_fee: Option[String] = None, mc_gross: Option[String] = None, payment_date: Option[String] = None, payment_fee: Option[String] = None, payment_gross: Option[String] = None, payment_status: Option[String] = None, payment_type: Option[String] = None, protection_eligibility: Option[String] = None, quantity: Option[String] = None, shipping: Option[String] = None, tax: Option[String] = None)
  case class Other(notify_version: Option[String] = None, charset: Option[String] = None, verify_sign: Option[String] = None)

  case class IPN(
                  aboutYou: AboutYou,
                  aboutTransaction: AboutTransaction,
                  aboutBuyer: AboutBuyer,
                  aboutPayment: AboutPayment,
                  other: Other
                )


  val userForm = Form(
    mapping(
      "" -> mapping(
        "receiver_email" -> optional(text),
        "receiver_id" -> optional(text),
        "residence_country" -> optional(text)
      )(AboutYou.apply)(AboutYou.unapply),
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

  def ipnPost = Action { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => {
        val data = formWithErrors.data.mkString(",  ")
        BadRequest(data)
      },
      ipn => {
        println(s"$ipn")
        post("", Json.toJson("ahoj"))
        Ok(ipn.aboutYou.receiver_email.getOrElse(""))
      }
    )
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
