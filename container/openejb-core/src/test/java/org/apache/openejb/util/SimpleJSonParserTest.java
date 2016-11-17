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
package org.apache.openejb.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertNotNull;

public class SimpleJSonParserTest {
    @Test(expected = IllegalArgumentException.class/*not stackoverflow*/)
    public void avoidInfiniteLoop_TOMEE1970() throws IOException { // was throwing java.lang.OutOfMemoryError: Java heap space
        assertNotNull(SimpleJSonParser.read(new ByteArrayInputStream(("{\n" +
                "  \"resources\":{\n" +
                "    \"jdbc/test\":{\n" +
                "      \"type\":\"DataSource\",\n" +
                "      \"classpath\":\"whatever\",\n" +
                "      \"properties\":{\n" +
                "        \"a\":\"b\n" +  // no closing quote is the TOMEE-1970 issue
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n").getBytes(StandardCharsets.UTF_8))));
    }
}
