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
package org.apache.tomee.microprofile.tck.opentracing;

import org.apache.cxf.jaxrs.client.spec.ClientBuilderImpl;
import org.apache.johnzon.jaxrs.JohnzonProvider;
import org.apache.johnzon.mapper.Mapper;
import org.apache.johnzon.mapper.MapperBuilder;
import org.eclipse.microprofile.opentracing.tck.tracer.TestSpanTree;

import java.util.Collections;

/**
 * CXF does not scan for @Provider in ClientBuilder, so when we receive the server payload, we can't deserialize it
 * with Johnzon
 */
public class MicroProfileOpenTracingClientBuilder extends ClientBuilderImpl {

    public MicroProfileOpenTracingClientBuilder() {
        super();
        final Mapper mapper = new MapperBuilder()
            .setFailOnUnknownProperties(false)
            .build();
        register(new JohnzonProvider<TestSpanTree>(mapper, Collections.emptyList()));
    }
}
