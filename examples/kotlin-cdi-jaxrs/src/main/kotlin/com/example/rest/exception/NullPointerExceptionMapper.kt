package com.example.rest.exception

import com.example.rest.ApiError
import java.util.logging.Logger
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR
import javax.ws.rs.core.Response.status
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class NullPointerExceptionMapper: ExceptionMapper<NullPointerException> {
  private val logger = Logger.getLogger(NullPointerExceptionMapper::class.java.simpleName)

  override fun toResponse(e: NullPointerException): Response = status(INTERNAL_SERVER_ERROR)
    .entity(ApiError(105, "A null pointer error occurred"))
    .build()
    .also { logger.warning(e.message ?: "No exception message") }
}
