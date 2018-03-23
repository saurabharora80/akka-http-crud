package com.lightbend.akka.http.sample

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.lightbend.akka.http.sample.domain.User
import com.lightbend.akka.http.sample.repository.UserRepository
import org.mongodb.scala.bson.collection.immutable.Document
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class UserRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
    with UserRoutes with BeforeAndAfterEach {
  override def userRepository: UserRepository = UserRepository
  private lazy val routes = userRoutes

  override def beforeEach(): Unit = {
    Await.result(userRepository.usersCollection.deleteMany(Document()).toFuture(), Duration(10, TimeUnit.SECONDS))
  }

  "UserRoutes" should {
    "return no users if no present (GET /users)" in {
      Get("/users") ~> routes ~> check {
        status shouldBe StatusCodes.OK

        contentType shouldBe ContentTypes.`application/json`

        entityAs[String] shouldBe """{"users":[]}"""
      }
    }

    "be able to add users (POST /users)" in {
      val userEntity = Marshal(User("saurabh", 38, "UK")).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      Post("/users").withEntity(userEntity) ~> routes ~> check {
        status shouldBe StatusCodes.Created
        header("Location").map(_.toString()).get should include regex "/users/.*"
      }
    }

    "be able to remove users (DELETE /users)" ignore {
      val userEntity = Marshal(User("saurabh", 38, "UK")).to[MessageEntity].futureValue // futureValue is from ScalaFutures
      var newUserLocation: String = ""

      Post("/users").withEntity(userEntity) ~> routes ~> check {
        status shouldBe StatusCodes.Created
        newUserLocation = header("Location").map(_.toString()).get
      }

      Delete("/users/123") ~> routes ~> check {
        status shouldBe StatusCodes.NoContent
      }

      Get("/users/123") ~> routes ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }
  }


}
