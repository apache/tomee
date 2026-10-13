/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.openejb.maven.plugin;

import org.apache.maven.plugin.logging.SystemStreamLog;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AbstractCommandMojoTest {
    @Test
    public void warnsForCredentialsOverHttpToRemoteHost() {
        final List<String> warnings = new ArrayList<>();
        final AbstractCommandMojo mojo = mojo("staging.example.com", "8080", null, warnings);
        mojo.user = "admin";
        mojo.password = "secret";

        final String url = mojo.providerUrl();
        assertEquals("http://staging.example.com:8080/tomee/ejb", url);
        mojo.warnIfCleartextCredentials(url);
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("staging.example.com"));
    }

    @Test
    public void noWarningForLoopbackHost() {
        for (final String host : new String[]{"localhost", "127.0.0.1", "::1"}) {
            final List<String> warnings = new ArrayList<>();
            final AbstractCommandMojo mojo = mojo(host, "8080", null, warnings);
            mojo.user = "admin";
            mojo.password = "secret";
            mojo.warnIfCleartextCredentials(mojo.providerUrl());
            assertTrue(host, warnings.isEmpty());
        }
    }

    @Test
    public void noWarningWithoutCredentials() {
        final List<String> warnings = new ArrayList<>();
        final AbstractCommandMojo mojo = mojo("staging.example.com", "8080", null, warnings);
        mojo.warnIfCleartextCredentials(mojo.providerUrl());
        assertTrue(warnings.isEmpty());
    }

    @Test
    public void noWarningOverHttps() {
        final List<String> warnings = new ArrayList<>();
        final AbstractCommandMojo mojo = mojo("staging.example.com", "8080", "8443", warnings);
        mojo.forceHttps = true;
        mojo.user = "admin";
        mojo.password = "secret";

        final String url = mojo.providerUrl();
        assertEquals("https://staging.example.com:8443/tomee/ejb", url);
        mojo.warnIfCleartextCredentials(url);
        assertTrue(warnings.isEmpty());
    }

    private static AbstractCommandMojo mojo(final String host, final String httpPort, final String httpsPort,
                                            final List<String> warnings) {
        final AbstractCommandMojo mojo = new AbstractCommandMojo() {
            @Override
            public void execute() {
                // no-op
            }
        };
        mojo.tomeeHost = host;
        mojo.tomeeHttpPort = httpPort;
        mojo.tomeeHttpsPort = httpsPort;
        mojo.ejbdEndpoint = "/tomee/ejb";
        mojo.setLog(new SystemStreamLog() {
            @Override
            public void warn(final CharSequence content) {
                warnings.add(content.toString());
            }
        });
        return mojo;
    }
}
