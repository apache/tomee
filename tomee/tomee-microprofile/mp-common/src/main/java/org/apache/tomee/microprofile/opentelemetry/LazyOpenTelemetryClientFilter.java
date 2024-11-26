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
package org.apache.tomee.microprofile.opentelemetry;

import io.smallrye.opentelemetry.implementation.rest.OpenTelemetryClientFilter;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * OpenTelemetryClientFilter <b>requires</b> CDI constructor injection, however the way we register this filter makes it impossible to do that.
 * This is a small workaround to lazily fetch the correct instance from CDI when it is needed.
 */
public class LazyOpenTelemetryClientFilter implements ClientRequestFilter, ClientResponseFilter {
    private static final Logger LOGGER = Logger.getLogger(LazyOpenTelemetryClientFilter.class.getName());

    private OpenTelemetryClientFilter delegate;

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        delegate().filter(requestContext);
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        delegate().filter(requestContext, responseContext);
    }

    protected OpenTelemetryClientFilter delegate() {
        if (delegate == null) {
            try {
                delegate = CDI.current().select(OpenTelemetryClientFilter.class).get();
            } catch (final IllegalStateException ise) {
                LOGGER.warning("No CDI context available, falling back to NoOp");
                delegate = new NoOp();
            }
        }

        return delegate;
    }

    private static class NoOp extends OpenTelemetryClientFilter {
        @Override
        public void filter(ClientRequestContext requestContext)  {
            // no-op
        }

        @Override
        public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
            // no-op
        }
    }
}
