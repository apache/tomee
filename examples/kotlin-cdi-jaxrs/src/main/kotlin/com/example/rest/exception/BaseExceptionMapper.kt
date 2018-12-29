package com.example.rest.exception

import com.example.rest.ApiError
import java.util.logging.Logger.getLogger
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR
import javax.ws.rs.core.Response.status
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class BaseExceptionMapper : ExceptionMapper<Exception> {
  private val logger = getLogger(BaseExceptionMapper::class.java.simpleName)

  override fun toResponse(e: Exception): Response = status(INTERNAL_SERVER_ERROR)
    .entity(ApiError(100, "Generic Error"))
    .build()
    .also { logger.warning(e.message ?: "No exception message") }
}
