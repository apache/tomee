/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.microprofile.openapi;

import io.smallrye.openapi.runtime.io.Format;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.microprofile.openapi.models.OpenAPI;

import java.io.IOException;
import java.util.Locale;
import java.util.stream.Stream;

import static io.smallrye.openapi.runtime.io.Format.JSON;
import static io.smallrye.openapi.runtime.io.Format.YAML;
import static io.smallrye.openapi.runtime.io.OpenApiSerializer.serialize;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This is not a JAXRS endpoint but a regular servlet because the Smallrye does the remaining job. We could do it as
 * a regular REST endpoint though but does not bring much more
 */
@WebServlet(name = "openapi-servlet", urlPatterns = "/openapi/*")
public class MicroProfileOpenApiEndpoint extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final ServletContext servletContext = request.getServletContext();
        final OpenAPI openAPI = MicroProfileOpenApiRegistration.getOpenApi(servletContext);
        final String format = request.getParameter("format");
        final Format formatOpenApi = getOpenApiFormat(request, format);
        response.setContentType(formatOpenApi.getMimeType());

        if (openAPI == null) {
            response.sendError(404, "No OpenAPI model available");
        } else {
            response.getOutputStream().write(serialize(openAPI, formatOpenApi).getBytes(UTF_8));
        }
    }

    private Format getOpenApiFormat(final HttpServletRequest request, final String format) {
        return Stream.of(Format.values())
                     .filter(f -> format != null && f.name().compareToIgnoreCase(format) == 0)
                     .findFirst()
                     .orElse(request.getHeader("Accept").toLowerCase(Locale.ENGLISH).contains("application/json") ? JSON : YAML);
    }
}
