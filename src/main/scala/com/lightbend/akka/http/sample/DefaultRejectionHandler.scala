package com.lightbend.akka.http.sample

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.directives.RouteDirectives._
import akka.http.scaladsl.server.{ MalformedRequestContentRejection, RejectionHandler, ValidationRejection }

trait DefaultRejectionHandler {
  implicit def rejectionHandler: RejectionHandler = {
    def pathAndReason(msg: String) = msg.replace("requirement failed:", "").trim.split("::")

    RejectionHandler.newBuilder()
      .handle {
        case ValidationRejection(msg, _) => complete(HttpResponse(
          StatusCodes.BadRequest,
          entity = HttpEntity(ContentTypes.`application/json`, s"""{"code":"invalid.request", "path": "${pathAndReason(msg)(0)}", "reason": "${pathAndReason(msg)(1)}"}""")
        ))
      }.result()
  }
}
