/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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
package org.apache.openejb.server.rest;

import org.apache.openejb.assembler.classic.WebAppInfo;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RESTServiceDeployedServiceTest {
    @Test
    public void isInWebAppContextMatching() {
        for (final String sc : asList("foo", "/foo", "/foo##1234")) {
            final RESTService.DeployedService service = new RESTService.DeployedService("http://localhost", sc, null, "1");
            for (final String c : new String[]{
                    "foo", "/foo", "/foo##1234"
            }) {
                assertTrue(sc + " ? " + c, service.isInWebApp("1", new WebAppInfo() {{
                    contextRoot = c;
                }}));
            }
            for (final String c : new String[]{
                    "bar", "/bar", "", "ROOT", "/ROOT", "foo2", "/foo2"
            }) {
                assertFalse(sc + " ? " + c, service.isInWebApp("1", new WebAppInfo() {{
                    contextRoot = c;
                }}));
            }
        }
    }
}
