package com.lightbend.akka.http.sample

case class JsonValidationException(errors: Seq[ValidationError]) extends RuntimeException

case class Validation[T](apply: T => Boolean, error: ValidationError)
case class ValidationError(code: String, path: String, reason: String)

object InvalidValueError {
  def apply(fieldName: String, errorMessage: String) = ValidationError("invalid.value", fieldName, errorMessage)
}

object Validate {
  def apply[T](validations: Validation[T]*)(entity: T): Unit = {
    val errors = validations.foldLeft(List.empty[ValidationError]) { (errors, validation) =>
      if (!validation.apply(entity)) validation.error :: errors else errors
    }
    if (errors.nonEmpty) throw JsonValidationException(errors)
  }
}