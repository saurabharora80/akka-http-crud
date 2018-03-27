package com.lightbend.akka.http.sample

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.lightbend.akka.http.sample.domain.User
import com.lightbend.akka.http.sample.repository.UserRepository
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.bson.collection.mutable.Document
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ BeforeAndAfterEach, Matchers }
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Await
import scala.concurrent.duration._

class UserRoutesSpec extends MongoDBSpec with Matchers with ScalaFutures with ScalatestRouteTest
    with UserRoutes with BeforeAndAfterEach {

  lazy val userRepository: UserRepository = new UserRepository {
    override def database: MongoDatabase = testMongoDB
  }

  private lazy val routes = userRoutes

  implicit val errorFormat = jsonFormat3(ValidationError)

  override def beforeEach(): Unit = {
    Await.result(userRepository.collection.deleteMany(Document()).toFuture(), 10.seconds)
  }

  val httpEntity: (String) => HttpEntity.Strict = (str: String) => HttpEntity(ContentTypes.`application/json`, str)

  "UserRoutes" should {

    val validUser = """{"name": "saurabh", "age": 38, "countryOfResidence": "UK"}""".stripMargin

    //GET /users
    "return no users if no present (GET /users)" in {
      Get("/users") ~> routes ~> check {
        status shouldBe StatusCodes.OK

        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe """{"users":[]}"""
      }
    }

    //POST /users
    "be able to add users (POST /users)" in {
      Post("/users").withEntity(httpEntity(validUser)) ~> routes ~> check {
        status shouldBe StatusCodes.Created
        header("Location").map(_.toString()).get should include regex "/users/.*"
      }
    }

    "not be able to add invalid user (POST /users)" in {
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

    "not be able to add a duplicate user (POST /users)" in {
      Post("/users").withEntity(httpEntity(validUser)) ~> Route.seal(routes) ~> check {
        status shouldBe StatusCodes.Created
        Post("/users").withEntity(httpEntity(validUser)) ~> Route.seal(routes) ~> check {
          status shouldBe StatusCodes.Conflict
        }
      }
    }

    //GET /users/:id
    "be able to Get User (GET /users/:id)" in {
      Post("/users").withEntity(httpEntity(validUser)) ~> routes ~> check {
        Get(header("Location").map(_.value()).get) ~> routes ~> check {
          status shouldBe StatusCodes.OK
          val user = entityAs[User]
          user.name shouldBe "saurabh"
          user.age shouldBe 38
          user.countryOfResidence shouldBe "UK"
        }
      }
    }

    "not be able to Get a existent User (GET /users/:id)" in {
      Get("/users/non-existent-user") ~> routes ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }

    //Delete /users/:id
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

