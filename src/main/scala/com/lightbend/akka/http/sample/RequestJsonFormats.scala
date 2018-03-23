package com.lightbend.akka.http.sample

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.lightbend.akka.http.sample.domain.{ Seva, User, Users }
import spray.json.{ DefaultJsonProtocol, JsArray, JsNumber, JsObject, JsString, JsValue, RootJsonFormat }

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
      "countryOfResidence" -> JsString(u.countryOfResidence),
      "sevas" -> JsArray(u.sevas.toVector.map(s => JsObject("date" -> JsString(s.date), "activity" -> JsString(s.activity))))
    )
  }

  implicit val usersJsonFormat = jsonFormat1(Users)

  implicit val sevaFormat = jsonFormat2(Seva)

}
