package api

import org.scalatest.{Matchers, WordSpec}
import org.specs2.mutable.Specification
import play.api.test.PlaySpecification


@RequiresDb
class test extends WordSpec with PlaySpecification with Matchers {

  "test with requeiresDb" in {
    true
  }

}