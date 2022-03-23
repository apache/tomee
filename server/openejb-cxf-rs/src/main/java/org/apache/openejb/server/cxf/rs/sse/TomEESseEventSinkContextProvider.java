/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.cxf.rs.sse;

import org.apache.cxf.jaxrs.ext.ContextProvider;
import org.apache.cxf.jaxrs.sse.SseEventSinkContextProvider;
import org.apache.cxf.jaxrs.sse.SseEventSinkImpl;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.SseEventSink;
import java.util.concurrent.CompletionStage;

public class TomEESseEventSinkContextProvider extends SseEventSinkContextProvider implements ContextProvider<SseEventSink> {
    @Override
    protected SseEventSink createSseEventSink(final HttpServletRequest request,
                                              final MessageBodyWriter<OutboundSseEvent> writer,
                                              final AsyncResponse async, final Integer bufferSize) {
        if (bufferSize != null) {
            return new TomEESseEventSink(writer, async, request.getAsyncContext(), bufferSize);
        } else {
            return new TomEESseEventSink(writer, async, request.getAsyncContext());
        }
    }

    public static class TomEESseEventSink extends SseEventSinkImpl implements SseEventSink {
        public TomEESseEventSink(final MessageBodyWriter<OutboundSseEvent> writer, final AsyncResponse async, final AsyncContext ctx) {
            super(writer, async, ctx);
        }

        public TomEESseEventSink(final MessageBodyWriter<OutboundSseEvent> writer, final AsyncResponse async, final AsyncContext ctx, final int bufferSize) {
            super(writer, async, ctx, bufferSize);
        }

        @Override
        public CompletionStage<?> send(OutboundSseEvent event) {
            if (isClosed()) {
                throw new IllegalStateException("The event sink is closed");
            }

            return super.send(event);
        }
    }
}
