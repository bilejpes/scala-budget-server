package api

import com.redis._
import com.redis.serialization.Parse.Implicits.parseByteArray
import Status._

object Server extends RedisClient("ec2-184-73-200-54.compute-1.amazonaws.com", 13629, secret= Some("pcp334l9h13lh7dgv96rjik2adg")) {

  /**
    * @return List[String] all keys from the database
    */
  def getKeys = keys[String]().get.flatten

  /**
    * @return List[Array[Byte]] all values from the database
    */
  def getValues : List[Array[Byte]] = getKeys.map(getValue(_).get)

  /**
    * @return Option[Array[Byte]]
    */
  def getValue(key: String) = get[Array[Byte]](key)

  /**
    * @return Map(key:String -> file:Array[Byte])
    */
  def getPairs  = {
    getKeys.map { x =>
      x -> new String(getValue(x).get)
    }.toMap
  }

  /**
    * Deletes record with specified key
    * @param key delete record with this key
    * @return DELETE_OK if successful
    *         DELETE_ERROR if no such record
    */
  def deleteRecord(key: String) = {
    del(key) match {
      case Some(0) => DELETE_ERROR
      case _ => DELETE_OK
    }
  }

  /**
    * Adds record to the database
    * @return ADD_CREATE - if there was no record with specified key
    *         ADD_CHANGE - changed content of the record in database, returns no body with request
    *         ADD_ERROR - in case of any error
    */
  def addRecord(record: Record) = {
    val created = !exists(record.key)

    set(record.key, record.value) match {
      case true if created => ADD_CREATE
      case true => ADD_CHANGE
      case _ => ADD_ERROR
    }
  }
}
