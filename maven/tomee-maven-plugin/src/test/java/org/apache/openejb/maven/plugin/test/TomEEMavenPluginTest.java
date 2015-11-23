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
package org.apache.openejb.maven.plugin.test;

import org.apache.openejb.loader.IO;
import org.apache.openejb.maven.plugin.TomEEMavenPluginRule;
import org.apache.openejb.maven.plugin.Url;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class TomEEMavenPluginTest {

    private final Logger logger = Logger.getLogger(TomEEMavenPluginTest.class.getName());

    @Rule
    public TomEEMavenPluginRule TMPRule = new TomEEMavenPluginRule();

    @Url
    private String url;

    @Test
    public void simpleStart() throws Exception {
        final String slurp = slurp(URI.create(url + "/docs").toURL(), 5);
        assertThat(slurp, containsString("Apache Tomcat"));
    }

    private String slurp(final URL url, int attempts) throws IOException {
        try {
            return IO.slurp(url);
        } catch (final IOException e) {
            if (attempts < 1) {
                logger.log(Level.SEVERE, "Failed to connect to: " + url, e);
                throw e;
            } else {
                try {
                    Thread.sleep(1000);
                    return slurp(url, --attempts);
                } catch (final InterruptedException ie) {
                    //
                }
            }
        }

        return "";
    }
}
