package com.example.rest.exception

import com.example.rest.ApiError
import java.util.logging.Logger
import javax.json.stream.JsonParsingException
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR
import javax.ws.rs.core.Response.status
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Provider
class JsonParsingExceptionMapper : ExceptionMapper<JsonParsingException> {
  private val logger = Logger.getLogger(JsonParsingExceptionMapper::class.java.simpleName)

  override fun toResponse(e: JsonParsingException): Response = status(INTERNAL_SERVER_ERROR)
    .entity(ApiError(110, "A JSON parsing error occurred"))
    .build()
    .also { logger.warning(e.message ?: "No exception message") }
}
