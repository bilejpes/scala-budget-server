package api

object Status extends Enumeration {
  type Status = Value
  val ADD_CREATE, ADD_CHANGE, ADD_ERROR, DELETE_OK, DELETE_ERROR = Value
}
