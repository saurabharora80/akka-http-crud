package com.lightbend.akka.http.sample

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.directives.RouteDirectives._
import akka.http.scaladsl.server.{ MalformedRequestContentRejection, RejectionHandler, ValidationRejection }

trait DefaultRejectionHandler {
  implicit def rejectionHandler: RejectionHandler = {
    def offendingArg(msg: String) = "'(\\w+)'".r.findFirstIn(msg).map(_.replaceAll("'", "")).getOrElse("")
    RejectionHandler.newBuilder()
      .handle {
        case MalformedRequestContentRejection(msg, _) => complete(HttpResponse(
          StatusCodes.BadRequest,
          entity = HttpEntity(ContentTypes.`application/json`, s"""{"code":"invalid.request", "path": "${offendingArg(msg)}", "reason": "$msg"}""")
        ))
      }.result()
  }
}
