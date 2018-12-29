package com.example.rest.exception

import com.example.rest.ApiError
import java.util.logging.Logger
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.status
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class WebApplicationExceptionMapper  : ExceptionMapper<WebApplicationException> {
  private val logger = Logger.getLogger(WebApplicationExceptionMapper::class.java.simpleName)

  override fun toResponse(e: WebApplicationException): Response = status(BAD_REQUEST)
    .entity(ApiError(117, "A web application error occurred"))
    .build()
    .also { logger.warning(e.message ?: "No exception message") }
}
