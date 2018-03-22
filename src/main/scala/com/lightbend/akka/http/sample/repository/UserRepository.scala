package com.lightbend.akka.http.sample.repository

import com.lightbend.akka.http.sample.domain.User
import org.bson.codecs.configuration.CodecRegistries._
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.{Completed, MongoClient, MongoCollection, MongoDatabase, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait UserRepository {

  val usersCollection: MongoCollection[User]

  def update(userId: String, user: User): Future[Boolean] = {
    usersCollection.updateOne(Document("_id" -> userId), Document("name" -> user.name, "age" -> user.age, "countryOfResidence" -> user.countryOfResidence)).headOption().map {
      case Some(updateResult) => updateResult.wasAcknowledged()
      case None => false
    }
  }

  def delete(userId: String): Future[Boolean] = usersCollection.findOneAndDelete(Document("_id" -> userId)).headOption().map(_.isDefined)

  def getUser(userId: String): Future[Option[User]] = usersCollection.find(Document("_id" -> userId)).headOption()

  def insert(user: User): Future[Completed] = usersCollection.insertOne(user).head

  def getUsers: Future[Seq[User]] = usersCollection.find.toFuture()
}

object UserRepository extends UserRepository {
  private val database: MongoDatabase = MongoClient().getDatabase("akka-http-crud")
  lazy val codecRegistry = fromRegistries(fromProviders(classOf[User]), DEFAULT_CODEC_REGISTRY)
  override val usersCollection: MongoCollection[User] = database.getCollection[User]("users").withCodecRegistry(codecRegistry)
}

