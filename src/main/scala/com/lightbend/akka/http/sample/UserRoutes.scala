package com.lightbend.akka.http.sample

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.pattern.ask
import akka.util.Timeout
import com.lightbend.akka.http.sample.UserRegistryActor.{CreateUser, DeleteUser, GetUser, GetUsers}
import spray.json.DefaultJsonProtocol

import scala.concurrent.duration._

trait UserRoutes extends SprayJsonSupport with DefaultRejectionHandler {

  import DefaultJsonProtocol._
  implicit val userJsonFormat = jsonFormat3(User)
  implicit val usersJsonFormat = jsonFormat1(Users)

  implicit def system: ActorSystem

  def userRegistryActor: ActorRef

  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  lazy val userRoutes: Route = {
    pathPrefix("users") {
      pathEnd {
        get {
          onSuccess((userRegistryActor ? GetUsers).mapTo[Users]) { complete(_) }
        } ~
          (post & entity(as[User])) { user =>
            onSuccess(userRegistryActor ? CreateUser(user)) { _ => complete(StatusCodes.Created) }
          }
      } ~
        path(Segment) { userName =>
          get {
            onSuccess((userRegistryActor ? GetUser(userName)).mapTo[Option[User]]) { mayBeUser =>
              mayBeUser.map(complete(_)).getOrElse(complete(StatusCodes.NotFound))
            }
          } ~
            delete {
              onSuccess(userRegistryActor ? DeleteUser(userName)) { _ => complete(StatusCodes.NoContent) }
            }
        }
    }
  }
}

