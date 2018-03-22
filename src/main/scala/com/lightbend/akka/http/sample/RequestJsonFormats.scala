package com.lightbend.akka.http.sample

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.lightbend.akka.http.sample.domain.{User, Users}
import spray.json.{DefaultJsonProtocol, JsNumber, JsObject, JsString, JsValue, RootJsonFormat}

trait RequestJsonFormats extends SprayJsonSupport {
  import DefaultJsonProtocol._
  implicit val userJsonFormat = new RootJsonFormat[User] {
    override def read(json: JsValue): User = json.asJsObject.getFields("name", "age", "countryOfResidence") match {
      case Seq(JsString(name), JsNumber(age), JsString(countryOfResidence)) => User(name, age.toInt, countryOfResidence)
    }

    override def write(u: User): JsValue = JsObject(
      "_id" -> JsString(u._id),
      "name" -> JsString(u.name),
      "age" -> JsNumber(u.age),
      "countryOfResidence" -> JsString(u.countryOfResidence)
    )
  }

  implicit val usersJsonFormat = jsonFormat1(Users)

}
