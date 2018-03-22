package com.lightbend.akka.http.sample

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.util.Timeout
import com.lightbend.akka.http.sample.domain.User
import com.lightbend.akka.http.sample.repository.UserRepository
import spray.json.DefaultJsonProtocol

import scala.collection.immutable
import scala.concurrent.duration._

trait UserRoutes extends RequestJsonFormats with DefaultRejectionHandler {

  implicit def system: ActorSystem

  def userRepository: UserRepository

  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  import DefaultJsonProtocol._

  //@formatter:off
  lazy val userRoutes: Route = {
    pathPrefix("users") {
      pathEnd {
        get {
          onSuccess(userRepository.getUsers) { complete(_) }
        } ~
        (post & entity(as[User])) { user =>
          onSuccess(userRepository.insert(user)) { _ =>
            complete(HttpResponse(StatusCodes.Created, immutable.Seq(Location(Uri(s"/users/${user._id}")))))
          }
        }
      } ~
      path(Segment) { userId =>
        get {
          onSuccess(userRepository.getUser(userId)) { mayBeUser =>
            mayBeUser.map(complete(_)).getOrElse(complete(StatusCodes.NotFound))
          }
        } ~
        delete {
          onSuccess(userRepository.delete(userId)) { deleted =>
            if (deleted) complete(StatusCodes.NoContent) else complete(StatusCodes.NotFound)
          }
        } ~
        (put & entity(as[User])) { user =>
          onSuccess(userRepository.update(userId, user)) { updated =>
            if (updated) complete(StatusCodes.NoContent) else complete(StatusCodes.NotFound)
          }
        }
      }
    }
  }
  //@formatter:on
}

