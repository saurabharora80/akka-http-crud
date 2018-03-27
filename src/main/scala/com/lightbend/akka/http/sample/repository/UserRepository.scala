package com.lightbend.akka.http.sample.repository

import com.lightbend.akka.http.sample.domain.{ Seva, User, Users }
import com.mongodb.MongoWriteException
import org.bson.codecs.configuration.CodecRegistries._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model.{ IndexModel, IndexOptions, Indexes }
import org.mongodb.scala.{ Completed, Document, MongoCollection, MongoDatabase }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait UserRepository {

  def database: MongoDatabase

  lazy val collection: MongoCollection[User] = database.getCollection[User]("users")
    .withCodecRegistry(fromRegistries(fromProviders(classOf[User], classOf[Seva]), DEFAULT_CODEC_REGISTRY))

  collection.createIndexes(Seq(
    IndexModel(Indexes.ascending("name", "age", "countryOfResidence"), IndexOptions().background(false).unique(true))
  )).toFuture().map(println)

  def deleteSeva(userId: String, date: String): Future[Boolean] =
    collection.updateOne(Document("_id" -> userId), pullByFilter(Document("sevas" -> Document("date" -> date)))).headOption().map {
      case Some(updateResult) => updateResult.wasAcknowledged()
      case None => false
    }

  def addSeva(userId: String, seva: Seva): Future[Boolean] =
    collection.updateOne(Document("_id" -> userId), addToSet("sevas", seva)).headOption().map {
      case Some(updateResult) => updateResult.wasAcknowledged()
      case None => false
    }

  def update(userId: String, user: User): Future[Boolean] = {
    collection.updateOne(Document("_id" -> userId), combine(set("name", user.name), set("age", user.age),
      set("countryOfResidence", user.countryOfResidence))).headOption().map {
      case Some(updateResult) => updateResult.wasAcknowledged()
      case None => false
    }
  }

  def delete(userId: String): Future[Boolean] = collection.findOneAndDelete(Document("_id" -> userId)).headOption().map(_.isDefined)

  def getUser(userId: String): Future[Option[User]] = collection.find(Document("_id" -> userId)).headOption()

  def insert(user: User): Future[Completed] = collection.insertOne(user).head.recover {
    case ex: MongoWriteException if ex.getError.getCode == 11000 => throw DuplicateUserException(user)
  }

  def getUsers: Future[Users] = collection.find.toFuture().map(Users)
}

object UserRepository extends UserRepository {
  override def database: MongoDatabase = MongoDB()
}

case class DuplicateUserException(user: User) extends RuntimeException(s"Cannot insert duplicate user: $user")

