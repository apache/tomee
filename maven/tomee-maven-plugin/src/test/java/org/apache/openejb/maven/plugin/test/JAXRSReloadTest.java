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

import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.maven.plugin.Config;
import org.apache.openejb.maven.plugin.Mojo;
import org.apache.openejb.maven.plugin.TomEEMavenPluginRule;
import org.apache.openejb.maven.plugin.UpdatableTomEEMojo;
import org.apache.openejb.maven.plugin.Url;
import org.apache.openejb.maven.plugin.app.Endpoint;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.lang.Thread.sleep;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class JAXRSReloadTest {
    private static File JAXRS_APP = new File("target/tests/webapps/app-jaxrs");

    @BeforeClass
    public static void createJAXRSWebApp() throws IOException {
        Files.mkdirs(JAXRS_APP);

        final String resource = Endpoint.class.getName().replace(".", "/") + ".class";
        final File to = new File(JAXRS_APP, "WEB-INF/classes/" + resource);
        if (to.exists()) { // already done
            return;
        }
        Files.mkdirs(to.getParentFile());
        IO.copy(new File("target/test-classes/" + resource), to);
    }

    @Rule
    public TomEEMavenPluginRule TMPRule = new TomEEMavenPluginRule();

    @Mojo
    private UpdatableTomEEMojo mojo;

    @Url
    private String url;

    @Config
    private boolean removeDefaultWebapps = true;

    @Config
    protected boolean forceReloadable = true;

    @Config
    private File catalinaBase = JAXRS_APP.getParentFile().getParentFile();

    @Config
    private File deployedFile = JAXRS_APP;

    @Config
    private String args = "-Dtomee.remote.support=true -Dopenejb.classloader.forced-load=" + Endpoint.class.getPackage().getName();

    @Test
    public void simpleStart() throws Exception {
        // eager check setup is ok and it works (avoid to mix redeployment checks with simple deployment)
        assertThat(slurp(new URL(url + "/app-jaxrs/ping")).trim(), is("pong"));

        long lastTime = 0;
        for (int i = 0; i < 10; i++) {
            sleep(100); // just to make time more explicit
            mojo.reload();

            // it still works
            assertThat(slurp(new URL(url + "/app-jaxrs/ping")).trim(), is("pong"));

            // we redeployed since we have a new deployment date
            final long time = Long.parseLong(slurp(new URL(url + "/app-jaxrs/ping/time")).trim());
            if (i > 0) {
                assertTrue(time >= lastTime);
            }
            lastTime = time;
        }
    }

    private static String slurp(final URL url) throws IOException {
        final HttpURLConnection urlConnection = HttpURLConnection.class.cast(url.openConnection());
        try {
            urlConnection.setRequestProperty("Accept", "text/plain");
            return IO.slurp(urlConnection.getInputStream());
        } finally {
            urlConnection.disconnect();
        }
    }
}
