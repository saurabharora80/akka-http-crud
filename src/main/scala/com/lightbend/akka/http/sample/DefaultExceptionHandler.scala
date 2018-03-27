package com.lightbend.akka.http.sample

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.ExceptionHandler
import com.lightbend.akka.http.sample.repository.DuplicateUserException
import akka.http.scaladsl.server.Directives.complete

trait DefaultExceptionHandler {

  implicit def myExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case _: DuplicateUserException => complete(StatusCodes.Conflict)
    }
}
