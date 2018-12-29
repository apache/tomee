package com.example.rest.exception

import com.example.rest.ApiError
import java.util.logging.Logger
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR
import javax.ws.rs.core.Response.status
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class NumberFormatExceptionMapper : ExceptionMapper<NumberFormatException> {
  private val logger = Logger.getLogger(NumberFormatExceptionMapper::class.java.simpleName)

  override fun toResponse(e: NumberFormatException): Response = status(INTERNAL_SERVER_ERROR).entity(
    ApiError(111, "A number format error occurred"))
    .build()
    .also { logger.warning(e.message ?: "No exception message") }
}
