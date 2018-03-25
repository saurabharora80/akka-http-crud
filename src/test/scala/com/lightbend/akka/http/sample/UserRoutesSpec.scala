package com.lightbend.akka.http.sample

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.lightbend.akka.http.sample.repository.UserRepository
import org.mongodb.scala.bson.collection.immutable.Document
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class UserRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
    with UserRoutes with BeforeAndAfterEach {
  override def userRepository: UserRepository = UserRepository
  private lazy val routes = userRoutes

  implicit val errorFormat = jsonFormat3(ValidationError)

  override def beforeEach(): Unit = {
    Await.result(userRepository.usersCollection.deleteMany(Document()).toFuture(), Duration(10, TimeUnit.SECONDS))
  }

  val httpEntity: (String) => HttpEntity.Strict = (str: String) => HttpEntity(ContentTypes.`application/json`, str)

  "UserRoutes" should {

    val validUser = """{"name": "saurabh", "age": 38, "countryOfResidence": "UK"}""".stripMargin

    "return no users if no present (GET /users)" in {
      Get("/users") ~> routes ~> check {
        status shouldBe StatusCodes.OK

        contentType shouldBe ContentTypes.`application/json`

        entityAs[String] shouldBe """{"users":[]}"""
      }
    }

    "be able to add users (POST /users)" in {
      Post("/users").withEntity(httpEntity(validUser)) ~> routes ~> check {
        status shouldBe StatusCodes.Created
        header("Location").map(_.toString()).get should include regex "/users/.*"
      }
    }

    "not be able to add invalid users (POST /users)" in {
      val inValidUser = """{"name": "", "age": 10, "countryOfResidence": "IN"}""".stripMargin

      Post("/users").withEntity(httpEntity(inValidUser)) ~> Route.seal(routes) ~> check {
        status shouldBe StatusCodes.BadRequest
        contentType shouldBe ContentTypes.`application/json`
        responseAs[Seq[ValidationError]] should contain allElementsOf Seq(
          ValidationError("invalid.value", "name", "name must be provided"),
          ValidationError("invalid.value", "age", "age must be greater than equal to 18"),
          ValidationError("invalid.value", "countryOfResidence", "countryOfResidence must be either UK or US")
        )
      }
    }

    "be able to remove users (DELETE /users)" in {
      Post("/users").withEntity(httpEntity(validUser)) ~> routes ~> check {
        status shouldBe StatusCodes.Created

        val locationHeader = header("Location").map(_.value()).get

        Delete(locationHeader) ~> routes ~> check {
          status shouldBe StatusCodes.NoContent
        }

        Get(locationHeader) ~> routes ~> check {
          status shouldBe StatusCodes.NotFound
        }
      }
    }
  }

}



