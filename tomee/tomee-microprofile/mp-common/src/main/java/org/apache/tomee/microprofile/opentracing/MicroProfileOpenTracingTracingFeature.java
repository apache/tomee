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

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.smallrye.opentracing.contrib.jaxrs2.client.ClientSpanDecorator;
import io.smallrye.opentracing.contrib.jaxrs2.client.ClientTracingFeature;
import io.smallrye.opentracing.contrib.jaxrs2.internal.URIUtils;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

import java.util.Collections;

public class MicroProfileOpenTracingTracingFeature implements Feature {

    private final ClientTracingFeature delegate;

    public MicroProfileOpenTracingTracingFeature(final Tracer tracer) {
        this.delegate = new ClientTracingFeature.Builder(tracer)
            .withDecorators(Collections.singletonList(new ClientSpanDecorator() {
                @Override
                public void decorateRequest(ClientRequestContext requestContext, Span span) {
                    Tags.COMPONENT.set(span, "jaxrs");
                    Tags.HTTP_METHOD.set(span, requestContext.getMethod());

                    String url = URIUtils.url(requestContext.getUri());
                    if (url != null) {
                        Tags.HTTP_URL.set(span, url);
                    }
                }

                @Override
                public void decorateResponse(ClientResponseContext responseContext, Span span) {
                    Tags.HTTP_STATUS.set(span, responseContext.getStatus());
                }
            }))
            .withTraceSerialization(false)
            .build();
    }

    @Override
    public boolean configure(FeatureContext context) {
        return this.delegate.configure(context);
    }

}
