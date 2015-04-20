/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.embedded;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.HostConfig;
import org.apache.openejb.assembler.DeployerEjb;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.util.NetworkUtil;
import org.apache.tomee.loader.TomcatHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// this test should be done on a tomee remote
// but easier to write asserts in embedded mode
public class TomEEUndeployTest {
    private static final String WORK_DIR = "target/embedded-undeploy";
    private static final File APP = new File(WORK_DIR + "/my-app");

    private Container container;
    private int http;

    @Test
    public void normalUndeploy() throws Exception {
        container.start();
        assertEquals(0, webapps().length);
        container.deploy(APP.getName(), APP);
        assertEquals(1, webapps().length);
        container.undeploy(APP.getName());
        assertEquals(0, webapps().length);
    }

    @Test
    public void ejbDeployer() throws Exception {
        container.start();
        assertEquals(0, webapps().length);
        final DeployerEjb deployerEjb = new DeployerEjb();
        deployerEjb.deploy(APP.getAbsolutePath());
        assertEquals(1, webapps().length);
        deployerEjb.undeploy(APP.getAbsolutePath());
        assertEquals(0, webapps().length);
    }

    @Test
    public void justAContextStop() throws Exception {
        container.start();
        assertEquals(0, webapps().length);
        final StandardHost standardHost = StandardHost.class.cast(TomcatHelper.getServer().findService("Tomcat").getContainer().findChild("localhost"));
        final HostConfig listener = new HostConfig(); // not done in embedded but that's the way autodeploy works in normal tomcat
        standardHost.addLifecycleListener(listener);
        createWebapp(new File(WORK_DIR, "tomee/webapps/my-webapp"));
        listener.lifecycleEvent(new LifecycleEvent(standardHost, Lifecycle.START_EVENT, standardHost));
        assertEquals(1, webapps().length);
        webapps()[0].stop();
        assertEquals(1, webapps().length);
        webapps()[0].start();
        assertEquals(1, webapps().length);
        assertEquals("test", IO.slurp(new URL("http://localhost:" + http + "/my-webapp/")));
    }

    @Test
    public void tomcatLifecycle() throws Exception {
        container.start();
        assertEquals(0, webapps().length);
        final StandardHost standardHost = StandardHost.class.cast(TomcatHelper.getServer().findService("Tomcat").getContainer().findChild("localhost"));
        final HostConfig listener = new HostConfig(); // not done in embedded but that's the way autodeploy works in normal tomcat
        standardHost.addLifecycleListener(listener);
        createWebapp(new File(WORK_DIR, "tomee/webapps/my-webapp"));
        listener.lifecycleEvent(new LifecycleEvent(standardHost, Lifecycle.START_EVENT, standardHost));
        assertEquals(1, webapps().length);
    }

    private static org.apache.catalina.Container[] webapps() {
        return TomcatHelper.getServer().findService("Tomcat").getContainer().findChild("localhost").findChildren();
    }

    @BeforeClass
    public static void createDirs() throws IOException {
        createWebapp(APP);
    }

    private static void createWebapp(final File app) throws IOException {
        Files.mkdirs(app);
        IO.copy(new ByteArrayInputStream("test".getBytes()), new File(app, "index.html"));
        Files.mkdirs(new File(app, "WEB-INF"));
        assertTrue(app.exists());
    }

    @Before
    public void start() throws Exception {
        final Configuration configuration = new Configuration();
        configuration.setHttpPort(NetworkUtil.getNextAvailablePort());
        configuration.setDir(WORK_DIR + "/tomee");
        http = configuration.getHttpPort();

        container = new Container();
        container.setup(configuration);
    }

    @After
    public void stop() throws Exception {
        container.close();
    }
}
