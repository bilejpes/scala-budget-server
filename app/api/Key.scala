package api

import play.api.libs.json.Json

case class Key(key: String, value: String)
/*
object Key {
  implicit val keyForm = Json.format[Key]
}*/
