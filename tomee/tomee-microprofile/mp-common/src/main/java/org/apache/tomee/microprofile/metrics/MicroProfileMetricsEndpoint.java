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
package org.apache.tomee.microprofile.metrics;

import io.smallrye.metrics.MetricsRequestHandler;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * This is not a JAXRS endpoint but a regular servlet because the Smallrye handler does the remaining job
 */
@WebServlet(name = "metrics-servlet", urlPatterns = "/metrics/*")
public class MicroProfileMetricsEndpoint extends HttpServlet {

    @Inject
    private MetricsRequestHandler metricsHandler;

    @Override
    protected void doOptions(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        doGet(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String requestPath = request.getContextPath().length() > 1
                                   ? request.getRequestURI().substring(request.getContextPath().length())
                                   : request.getRequestURI();
        final String method = request.getMethod();
        final Stream<String> acceptHeaders = Collections.list(request.getHeaders("Accept")).stream();

        metricsHandler.handleRequest(requestPath, method, acceptHeaders, (status, message, headers) -> {
            headers.forEach(response::addHeader);
            response.setStatus(status);
            response.getWriter().write(message);
        });
    }
}
