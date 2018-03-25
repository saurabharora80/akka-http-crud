package com.lightbend.akka.http.sample.domain

import com.lightbend.akka.http.sample.{ InvalidValueError, Validate, Validation }
import org.mongodb.scala.bson.BsonObjectId

case class Seva(date: String, activity: String)

final case class User(name: String, age: Int, countryOfResidence: String, sevas: List[Seva] = List.empty, _id: String = BsonObjectId().getValue.toString) {
  Validate(
    Validation[User](!_.name.isEmpty, InvalidValueError("name", "name must be provided")),
    Validation[User](_.age >= 18, InvalidValueError("age", "age must be greater than equal to 18")),
    Validation[User](user => Seq("UK", "US").contains(user.countryOfResidence.toUpperCase), InvalidValueError("countryOfResidence", "countryOfResidence must be either UK or US"))
  )(this)

  def sevaForDay(date: String): Option[Seva] = sevas.find(_.date == date)
}

