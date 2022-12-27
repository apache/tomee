/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.microprofile.opentracing;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

// See TOMEE-4133 for details
@Provider
public class MicroProfileOpenTracingExceptionMapper implements jakarta.ws.rs.ext.ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(final RuntimeException exception) {
        if (exception instanceof WebApplicationException) {
            final WebApplicationException o = (WebApplicationException) exception;
            return o.getResponse();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity(exception.getMessage())
                       .build();
    }

}