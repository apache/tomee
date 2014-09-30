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
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
    protected boolean deployOpenEjbApplication = true;

    @Config
    private File catalinaBase = JAXRS_APP.getParentFile().getParentFile();

    @Config
    private File deployedFile = JAXRS_APP;

    @Config
    private String args = "-Dopenejb.classloader.forced-load=" + Endpoint.class.getPackage().getName();

    @Test
    public void simpleStart() throws Exception {
        assertThat(IO.slurp(new URL(url + "/app-jaxrs/ping")).trim(), is("pong"));
        mojo.reload();
        assertThat(IO.slurp(new URL(url + "/app-jaxrs/ping")).trim(), is("pong"));
    }
}
