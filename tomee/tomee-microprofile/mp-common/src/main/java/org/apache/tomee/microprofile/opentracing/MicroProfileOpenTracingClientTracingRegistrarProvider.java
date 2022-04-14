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

import io.opentracing.Tracer;
import io.opentracing.contrib.concurrent.TracedExecutorService;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.client.ClientBuilder;
import org.eclipse.microprofile.opentracing.ClientTracingRegistrarProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MicroProfileOpenTracingClientTracingRegistrarProvider implements ClientTracingRegistrarProvider {

    public ClientBuilder configure(ClientBuilder clientBuilder) {
        return configure(clientBuilder, Executors.newFixedThreadPool(10));
    }

    public ClientBuilder configure(ClientBuilder clientBuilder, ExecutorService executorService) {
        final Tracer tracer = CDI.current().select(Tracer.class).get();
        return clientBuilder.executorService(new TracedExecutorService(executorService, tracer))
                            .register(new MicroProfileOpenTracingTracingFeature(tracer));
    }
}