package com.lightbend.akka.http.sample.repository

import org.mongodb.scala.{ MongoClient, MongoDatabase }

trait MongoDB

object MongoDB {
  private lazy val database = MongoClient().getDatabase("akka-http-crud")

  def apply(): MongoDatabase = database
}
