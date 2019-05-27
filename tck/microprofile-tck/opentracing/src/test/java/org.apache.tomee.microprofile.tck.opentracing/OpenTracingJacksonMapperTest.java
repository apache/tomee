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

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.microprofile.opentracing.tck.tracer.TestTracer;
import org.testng.annotations.Test;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES;

public class OpenTracingJacksonMapperTest {
    @Test
    public void testMapper() throws Exception {
        final String json = "{\"spans\":[{\"traceId\":1,\"spanId\":2,\"logEntries\":[]," +
                            "\"finishMicros\":1534436131217000,\"startMicros\":1534436131205000,\"simulated\":false," +
                            "\"cachedOperationName\":\"GET:org.eclipse.microprofile.opentracing.tck.application" +
                            ".TestServerWebServices.simpleTest\",\"parentId\":0,\"tags\":{\"http" +
                            ".url\":\"http://localhost:64388/microprofile-opentracing/rest/testServices/simpleTest\"," +
                            "\"http.status_code\":200,\"component\":\"jaxrs\",\"span.kind\":\"server\",\"http" +
                            ".method\":\"GET\"}}]}";

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.readValue(json, TestTracer.class);
    }
}
