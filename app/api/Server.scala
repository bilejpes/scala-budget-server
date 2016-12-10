package api

import com.redis._
import Status._

//object Server extends RedisClient("127.0.0.1", 6379/*, secret= Some("")*/) {
object DB extends RedisClient("ec2-184-73-200-54.compute-1.amazonaws.com", 13629, secret= Some("pcp334l9h13lh7dgv96rjik2adg")) {

  def getKeys = keys().get

  def getValues : List[String] = getKeys.flatten.map(getValue(_))

  def getValue(key: String) = {
    getType(key) match {
      case Some("string") => get(key).get
      case Some("hash") => "hash"
      case _ => ""
    }
  }

  def getPairs  = {
    getKeys.flatten.map { x =>
      x -> getValue(x)
    }.toMap
  }

  def deleteRecord(key: String) = {
    del(key) match {
      case Some(0) => DELETE_ERROR
      case _ => DELETE_OK
    }
  }

  def addRecord(record: Record) = {
    val created = !exists(record.key)

    set(record.key, record.value) match {
      case true if created => ADD_CREATE
      case true => ADD_CHANGE
      case _ => ADD_ERROR
    }
  }
}
