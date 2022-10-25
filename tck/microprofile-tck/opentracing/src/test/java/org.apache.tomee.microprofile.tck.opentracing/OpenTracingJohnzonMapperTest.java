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

import org.apache.johnzon.mapper.Mapper;
import org.eclipse.microprofile.opentracing.tck.tracer.TestSpan;
import org.eclipse.microprofile.opentracing.tck.tracer.TestTracer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

public class OpenTracingJohnzonMapperTest {
    @Test
    public void testMapper() throws Exception {
        final String json = "{\"spans\":[{\"traceId\":1,\"spanId\":2,\"logEntries\":[]," +
                            "\"finishMicros\":1534436131217000,\"startMicros\":1534436131205000,\"simulated\":false," +
                            "\"cachedOperationName\":\"GET:org.eclipse.microprofile.opentracing.tck.application" +
                            ".TestServerWebServices.simpleTest\",\"parentId\":0,\"tags\":{\"http" +
                            ".url\":\"http://localhost:64388/microprofile-opentracing/rest/testServices/simpleTest\"," +
                            "\"http.status_code\":200,\"component\":\"jaxrs\",\"span.kind\":\"server\",\"http" +
                            ".method\":\"GET\"}}]}";

        final Mapper objectMapper = new MicroProfileOpenTrackingContextResolver().getContext(null);
        final TestTracer tracer = objectMapper.readObject(json, TestTracer.class);

        Assert.assertNotNull(tracer);
        Assert.assertEquals(1, tracer.getSpans().size());

        final TestSpan testSpan = tracer.getSpans().get(0);
        Assert.assertNotNull(testSpan);
        Assert.assertNotNull(testSpan.getTags());
        Assert.assertEquals(5, testSpan.getTags().size());

        // very important because it is not really define per spec so not really portable but the TCK for OpenTracing
        // are developed such as it expects BigInteger
        Assert.assertTrue(testSpan.getTags().containsKey("http.status_code"));
        Assert.assertEquals(new BigDecimal("200"), testSpan.getTags().get("http.status_code"));
    }
}
