package com.lightbend.akka.http.sample

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.lightbend.akka.http.sample.domain.User
import com.lightbend.akka.http.sample.repository.UserRepository
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class UserRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
    with UserRoutes {
  override def userRepository: UserRepository = UserRepository
  private lazy val routes = userRoutes

  "UserRoutes" should {
    "return no users if no present (GET /users)" in {
      val request = HttpRequest(uri = "/users")

      request ~> routes ~> check {
        status should === (StatusCodes.OK)

        contentType should === (ContentTypes.`application/json`)

        entityAs[String] should ===("""{"users":[]}""")
      }
    }

    "be able to add users (POST /users)" in {
      val userEntity = Marshal(User("Kapi", 42, "jp")).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      val request = Post("/users").withEntity(userEntity)

      request ~> routes ~> check {
        status should === (StatusCodes.Created)
      }
    }

    "be able to remove users (DELETE /users)" in {
      val request = Delete(uri = "/users/Kapi")

      request ~> routes ~> check {
        status should ===(StatusCodes.NoContent)
      }
    }
  }


}
