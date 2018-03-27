package com.lightbend.akka.http.sample.domain

import com.lightbend.akka.http.sample.{ JsonValidationException, ValidationError }
import org.scalatest.{ Matchers, WordSpec }

import scala.util.{ Failure, Success, Try }

class UserSpec extends WordSpec with Matchers {

  "user" should {
    "have a name" in {
      Try(User("", 18, "US")) match {
        case Failure(ex: JsonValidationException) => ex.errors should contain allElementsOf Seq(
          ValidationError("invalid.value", "name", "name must be provided")
        )
        case _ => fail("should fail with Validation exception")
      }
    }

    "be older than 17" in {
      Try(User("saurabh", 17, "US")) match {
        case Failure(ex: JsonValidationException) => ex.errors should contain allElementsOf Seq(
          ValidationError("invalid.value", "age", "age must be greater than equal to 18")
        )
        case _ => fail("should fail with Validation exception")
      }
    }

    "be living in UK or US" in {
      Try(User("saurabh", 18, "IN")) match {
        case Failure(ex: JsonValidationException) => ex.errors should contain allElementsOf Seq(
          ValidationError("invalid.value", "countryOfResidence", "countryOfResidence must be either UK or US")
        )
        case _ => fail("should fail with Validation exception")
      }
    }

    "have a name, older than 17 and be living in UK or US" in {
      Try(User("", 17, "IN")) match {
        case Failure(ex: JsonValidationException) => ex.errors should contain allElementsOf Seq(
          ValidationError("invalid.value", "name", "name must be provided"),
          ValidationError("invalid.value", "age", "age must be greater than equal to 18"),
          ValidationError("invalid.value", "countryOfResidence", "countryOfResidence must be either UK or US")
        )
        case _ => fail("should fail with Validation exception")
      }
    }
  }
}
