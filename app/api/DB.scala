package api

import com.redis._

object DB extends RedisClient("ec2-184-73-200-54.compute-1.amazonaws.com", 13629) {

  def save(key: Key) = {
    set(key.key, key.value)
  }

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
