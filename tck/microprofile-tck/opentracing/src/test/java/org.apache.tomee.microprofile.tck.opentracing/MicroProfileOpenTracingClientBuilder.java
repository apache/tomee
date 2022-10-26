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
 * CXF does not scan for @Provider in ClientBuilder, so when we receive the server payload, we can't deserialize it.
 * Also, we can tune what the provider does under the cover
 */
public class MicroProfileOpenTracingClientBuilder extends ClientBuilderImpl {

    public MicroProfileOpenTracingClientBuilder() {
        super();

        // we could add openejb-cxf-rs maven module as test and register the TomEEJsonbProvider which would work
        // to deserialize the http.status_code as a BigDecimal as opposed to an Integer as required by the TCK
        // but this is the TCK client only to run the TCK, so the shorter version bellow works fine
        // register(new TomEEJsonbProvider<TestSpanTree>());

        // Johnzon jaxrs/jsonb Provider with its configuration
        final Mapper mapper = new MapperBuilder()
            .setFailOnUnknownProperties(false)
            // very important or the assert will fail because the TCK expects a BigDecimal for the status code
            // as opposed to a regular integer. We can configure the behavior with Johnzon, with Jackson it would just fail
            // the TCK and SmallRye are developed using RestEasy which is using Yasson under the cover and it does that by
            // default - hence the following change
            // https://github.com/eclipse/microprofile-opentracing/commit/fb9557a39c5d1216b1a22eebb3f8508e1ba067ff#diff-7d2ffd37d7235895694d34ea99cc68775cd34966ffdc91886a17da55d625440eL346
            .setUseBigDecimalForObjectNumbers(true)
            .build();
        register(new JohnzonProvider<TestSpanTree>(mapper, Collections.emptyList()));

    }
}
