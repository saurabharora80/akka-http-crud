package com.lightbend.akka.http.sample

import org.scalatest.{Matchers, WordSpec}

import scala.util.{Failure, Success, Try}

class ValidateSpec extends WordSpec with Matchers {

  "Validate" should {
    "fail with Validation exception when some of the validation conditions are met" in {
      Try(Validate(
        Validation[String](_ => false, InvalidValueError("field1", "message-for-field1")),
        Validation[String](_ => true, InvalidValueError("field2", "message-for-field2")),
        Validation[String](_ => false, InvalidValueError("field3", "message-for-field3"))
      )("")) match {
        case Failure(ex: JsonValidationException) =>
          ex.errors should contain allElementsOf Seq(InvalidValueError("field1", "message-for-field1"), InvalidValueError("field3", "message-for-field3"))
        case Success(_) => fail("validations should have failed")
      }
    }
  }
}
