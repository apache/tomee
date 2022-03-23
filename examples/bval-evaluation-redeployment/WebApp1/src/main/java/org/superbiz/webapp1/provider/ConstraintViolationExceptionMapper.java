/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.webapp1.provider;

import org.superbiz.webapp1.messages.ErrorList;
import org.superbiz.webapp1.messages.ErrorResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Context
    private HttpHeaders headers;

    @Override
    public Response toResponse(final ConstraintViolationException t) {
        final MediaType type = headers.getMediaType();
        final Locale locale = headers.getLanguage();

        final Object responsObject = getConstraintViolationErrors(t);
        return Response.status(Response.Status.NOT_ACCEPTABLE).type(type).language(locale).entity(responsObject).build();
    }

    private static Object getConstraintViolationErrors(final ConstraintViolationException ex) {
        final List<ErrorResponse> errors = new ArrayList<ErrorResponse>();
        for (final ConstraintViolation violation : ex.getConstraintViolations()) {
            final ErrorResponse error = new ErrorResponse();
            error.setMessage(violation.getMessage());
            errors.add(error);
        }
        return new ErrorList<ErrorResponse>(errors);
    }

}
