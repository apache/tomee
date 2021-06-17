package com.example.rest.exception

import com.example.rest.ApiError
import java.util.logging.Logger
import javax.validation.ConstraintViolationException
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.status
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class ConstraintViolationExceptionMapper : ExceptionMapper<ConstraintViolationException> {
  private val logger = Logger.getLogger(ConstraintViolationExceptionMapper::class.java.simpleName)

  /**
   * This code allows a bean validation message to be a code which will be looked up in the api codes map,
   * or it can be a custom developer message.
   */
  override fun toResponse(e: ConstraintViolationException): Response {
    val message = e.constraintViolations.first().message ?: "100008"

    return try {
      val code = message.toInt()
      status(BAD_REQUEST)
        .entity(ApiError(code, "A validation error occurred"))
        .build()
        .also { logger.warning(e.message ?: "No exception message") }
    } catch (nfe: NumberFormatException) {
      status(BAD_REQUEST).entity(ApiError(100, message)).build()
    }
  }
}
