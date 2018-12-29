package com.example.rest.exception

import com.example.rest.ApiError
import java.util.logging.Logger
import javax.ws.rs.ClientErrorException
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.status
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class ClientErrorExceptionMapper : ExceptionMapper<ClientErrorException> {
  private val logger = Logger.getLogger(ClientErrorExceptionMapper::class.java.simpleName)

  override fun toResponse(e: ClientErrorException): Response = status(BAD_REQUEST)
    .entity(ApiError(101, "A client error occurred"))
    .build()
    .also { logger.warning(e.message ?: "No exception message") }
}
