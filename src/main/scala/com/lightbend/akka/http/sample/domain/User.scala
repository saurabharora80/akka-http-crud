package com.lightbend.akka.http.sample.domain

import org.mongodb.scala.bson.BsonObjectId

case class Seva(date: String, activity: String)

final case class User(name: String, age: Int, countryOfResidence: String, sevas: List[Seva] = List.empty, _id: String = BsonObjectId().getValue.toString) {
  def sevaForDay(date: String): Option[Seva] = sevas.find(_.date == date).headOption

  require(!name.isEmpty, "name::name must be provided")
  require(age >= 18, "age::age must be greater than equal to 18")
  require(Seq("UK", "US").contains(countryOfResidence.toUpperCase), "countryOfResidence::countryOfResidence must be either UK or US")
}

