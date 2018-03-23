package com.lightbend.akka.http.sample.repository

import com.lightbend.akka.http.sample.domain.{Seva, User, Users}
import org.mongodb.scala.bson.collection.immutable.Document
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class UserRepositorySpec extends WordSpec with Matchers with ScalaFutures with IntegrationPatience with BeforeAndAfterEach {

  val userRepository: UserRepository = UserRepository

  override def beforeEach(): Unit = {
    Await.result(userRepository.usersCollection.deleteMany(Document()).toFuture(), 10.seconds)
  }

  "repository" should {
    val user = User("saurabh", 38, "UK")

    "return None for a non existent user" in {
      whenReady(userRepository.getUser("123")) { user =>
        user shouldBe None
      }
    }

    "find a existing user" in {
      val eventualMaybeUser: Future[Option[User]] = for {
        _ <- userRepository.insert(user)
        user <- userRepository.getUser(user._id)
      } yield user

      whenReady(eventualMaybeUser) { actualUser =>
        actualUser shouldBe Some(user)
      }
    }

    "find all existing users" in {
      val userFoo = User("foo", 38, "UK")
      val eventualMaybeUsers: Future[Users] = for {
        _ <- userRepository.insert(user)
        _ <- userRepository.insert(userFoo)
        users <- userRepository.getUsers
      } yield users

      whenReady(eventualMaybeUsers) { actualUsers =>
        actualUsers shouldBe Users(Seq(user, userFoo))
      }
    }

    "update a user" in {
      val eventualMaybeUser: Future[Option[User]] = for {
        _ <- userRepository.insert(user)
        _ <- userRepository.update(user._id, User("saurabh", 18, "US"))
        user <- userRepository.getUser(user._id)
      } yield user

      whenReady(eventualMaybeUser) { actualUser =>
        actualUser shouldBe Some(user.copy(age = 18, countryOfResidence = "US"))
      }
    }

    "delete a user" in {
      val eventualMaybeUser: Future[Option[User]] = for {
        _ <- userRepository.insert(user)
        _ <- userRepository.delete(user._id)
        user <- userRepository.getUser(user._id)
      } yield user

      whenReady(eventualMaybeUser) { actualUser =>
        actualUser shouldBe None
      }
    }

    "add seva to a user" in {
      val eventualMaybeUser: Future[Option[User]] = for {
        _ <- userRepository.insert(user)
        _ <- userRepository.addSeva(user._id, Seva("2010-10-01", "general"))
        _ <- userRepository.addSeva(user._id, Seva("2010-10-02", "housekeeping"))
        user <- userRepository.getUser(user._id)
      } yield user

      whenReady(eventualMaybeUser) {
        case Some(actualUser) => actualUser.sevas should contain theSameElementsAs Seq(Seva("2010-10-01", "general"), Seva("2010-10-02", "housekeeping"))
        case None => fail("couldn't find user for the given id")
      }
    }

    "delete a seva from user" in {
      val eventualMaybeUser: Future[(Boolean, Option[User])] = for {
        _ <- userRepository.insert(user)
        _ <- userRepository.addSeva(user._id, Seva("2010-10-01", "general"))
        _ <- userRepository.addSeva(user._id, Seva("2010-10-02", "housekeeping"))
        deleteSeva <- userRepository.deleteSeva(user._id, "2010-10-01")
        user <- userRepository.getUser(user._id)
      } yield (deleteSeva, user)

      whenReady(eventualMaybeUser) {
        case (true, Some(actualUser)) => actualUser.sevas should contain theSameElementsAs Seq(Seva("2010-10-02", "housekeeping"))
        case (false, _) => fail("couldn't remove seva")
      }
    }

  }
}
