package api

import com.redis._

object DB extends RedisClient("127.0.0.1", 6379/*, secret= Some("")*/) {
//object DB extends RedisClient("ec2-184-73-200-54.compute-1.amazonaws.com", 13629, secret= Some("pcp334l9h13lh7dgv96rjik2adg")) {

  def save(record: Record) = {
    set(record.key, record.value)
  }
//pcp334l9h13lh7dgv96rjik2adg
  def getKeys = keys().get

  def getValues : List[String] = getKeys.flatten.map(getValue(_))

  def getValue(key : String) = {
    getType(key) match {
      case Some("string") => get(key).get
      case Some("hash") => "hash"
      case _ => ""
    }
  }

  def getPairs  = {
    val pairs = scala.collection.mutable.Map[String, String]()
    for(key <- getKeys.flatten) pairs += (key -> getValue(key))
    pairs
  }

}
