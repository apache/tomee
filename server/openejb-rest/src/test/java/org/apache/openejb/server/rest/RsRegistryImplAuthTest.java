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

import org.apache.openejb.server.httpd.HttpListener;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RsRegistryImplAuthTest {
    private static final HttpListener NOOP_LISTENER = (request, response) -> {
        // no-op
    };

    @Test
    public void onlyBasicOrNoneAreAccepted() {
        for (final String auth : new String[]{"DIGEST", "CLIENT-CERT"}) {
            try {
                new RsRegistryImpl().createRsHttpListener(
                        "app", "web", NOOP_LISTENER, Thread.currentThread().getContextClassLoader(),
                        "/rest/.*", "localhost", auth, "realm");
                fail("auth method " + auth + " must be rejected");
            } catch (final IllegalArgumentException expected) {
                assertTrue(expected.getMessage().contains(auth));
            }
        }
    }
}
