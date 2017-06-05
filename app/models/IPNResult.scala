package models

object IPNResult {


  sealed abstract class IPNResult(val name: String)

  case object Invalid extends IPNResult(name = "Invalid")
  case object Verified extends IPNResult(name = "Verified")


  val values = Seq(
    Invalid,
    Verified
  )

  def findByName(name: String) = values.find(_.name.toLowerCase == name.toLowerCase)
}