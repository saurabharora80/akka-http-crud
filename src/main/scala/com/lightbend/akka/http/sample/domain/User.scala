package com.lightbend.akka.http.sample.domain

import org.mongodb.scala.bson.BsonObjectId


final case class User(name: String, age: Int, countryOfResidence: String, _id: String = BsonObjectId().getValue.toString) {
  require(!name.isEmpty, "name::name must be provided")
  require(age >= 18, "age::age must be greater than equal to 18")
  require(Seq("uk", "us").contains(countryOfResidence), "countryOfResidence::countryOfResidence must be either UK or US")
}
