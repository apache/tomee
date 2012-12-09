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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Archives;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @version $Rev$ $Date$
 */
public class AutoDeployerTest extends Assert {
    @Before
    @After
    public void stopAutoDeployer() {
        final AutoDeployer autoDeployer = SystemInstance.get().getComponent(AutoDeployer.class);
        if (autoDeployer != null) {
            autoDeployer.stop();
        }
        SystemInstance.reset();
    }

    @Test
    public void test() throws Exception {
        final File tmpdir = Files.tmpdir();
        final File apps = Files.mkdir(tmpdir, "myapps");
        final File conf = Files.mkdir(tmpdir, "conf");

        final Properties properties = new Properties();
        properties.setProperty("openejb.deployments.classpath", "false");
        properties.setProperty("openejb.deployment.unpack.location", "false");
        properties.setProperty("openejb.home", tmpdir.getAbsolutePath());
        properties.setProperty("openejb.base", tmpdir.getAbsolutePath());

        SystemInstance.init(properties);

        { // Setup the configuration location
            final File config = new File(conf, "openejb.xml");
            IO.writeString(config, "<openejb><Deployments autoDeploy=\"true\" dir=\"myapps\"/> </openejb>");
            SystemInstance.get().setProperty("openejb.configuration", config.getAbsolutePath());
        }

        final ConfigurationFactory configurationFactory = new ConfigurationFactory();
        configurationFactory.init(properties);
        final OpenEjbConfiguration configuration = configurationFactory.getOpenEjbConfiguration();

        { // Check the ContainerSystemInfo

            final List<String> autoDeploy = configuration.containerSystem.autoDeploy;
            assertEquals(1, autoDeploy.size());
            assertEquals("myapps", autoDeploy.get(0));
        }

        final Assembler assembler = new Assembler();
        assembler.buildContainerSystem(configuration);

        /// start with the testing...

        assertFalse(Yellow.deployed);
        assertFalse(Orange.deployed);

        final File deployed = Files.path(apps, "colors.ear");

        // Hot deploy the EAR
        final File ear = createEar(tmpdir, Orange.class, State.class);
        IO.copy(ear, deployed);

        Orange.state.waitForChange(1, TimeUnit.MINUTES);

        assertFalse(Yellow.deployed);
        assertTrue(Orange.deployed);

        Files.delete(deployed);

        Orange.state.waitForChange(1, TimeUnit.MINUTES);

        assertFalse(Yellow.deployed);
        assertFalse(Orange.deployed);
    }

    @Test
    public void testAltUnpackDir() throws Exception {
        final File tmpdir = Files.tmpdir();
        final File apps = Files.mkdir(tmpdir, "myapps");
        final File conf = Files.mkdir(tmpdir, "conf");

        final Properties properties = new Properties();
        properties.setProperty("openejb.deployments.classpath", "false");
        properties.setProperty("tomee.unpack.dir", "work");
        properties.setProperty("openejb.home", tmpdir.getAbsolutePath());
        properties.setProperty("openejb.base", tmpdir.getAbsolutePath());

        SystemInstance.init(properties);

        { // Setup the configuration location
            final File config = new File(conf, "openejb.xml");
            IO.writeString(config, "<openejb><Deployments autoDeploy=\"true\" dir=\"myapps\"/> </openejb>");
            SystemInstance.get().setProperty("openejb.configuration", config.getAbsolutePath());
        }

        final ConfigurationFactory configurationFactory = new ConfigurationFactory();
        configurationFactory.init(properties);
        final OpenEjbConfiguration configuration = configurationFactory.getOpenEjbConfiguration();

        { // Check the ContainerSystemInfo

            final List<String> autoDeploy = configuration.containerSystem.autoDeploy;
            assertEquals(1, autoDeploy.size());
            assertEquals("myapps", autoDeploy.get(0));
        }

        final Assembler assembler = new Assembler();
        assembler.buildContainerSystem(configuration);

        /// start with the testing...

        assertFalse(Yellow.deployed);
        assertFalse(Orange.deployed);

        final File deployed = Files.path(apps, "colors.ear");
        deployed.deleteOnExit();

        // Hot deploy the EAR
        final File ear = createEar(tmpdir, Orange.class, State.class);
        ear.deleteOnExit();
        IO.copy(ear, deployed);
        assertTrue(deployed.exists());

        Orange.state.waitForChange(1, TimeUnit.MINUTES);

        assertFalse(Yellow.deployed);
        assertTrue(Orange.deployed);

        Files.delete(deployed);

        Orange.state.waitForChange(1, TimeUnit.MINUTES);

        assertFalse(Yellow.deployed);
        assertFalse(Orange.deployed);
    }

    private File createEar(File tmpdir, final Class<?>... aClass) throws IOException {
        final File ear = new File(tmpdir, "colors.ear");
        final Map<String, Object> contents = new HashMap<String, Object>();
        contents.put("foo.jar", Archives.jarArchive(aClass));
        Archives.jarArchive(ear, contents);
        return ear;
    }

    public static class State {
        private volatile boolean b;
        private final Lock lock = new ReentrantLock();
        private final Condition condition = lock.newCondition();

        public void toggle() {
            lock.lock();
            try {
                b = !b;
                condition.signal();
            } finally {
                lock.unlock();
            }
        }

        public boolean get() {
            return b;
        }

        public void waitForChange(long time, TimeUnit unit) {
            lock.lock();
            try {
                condition.await(time, unit);
            } catch (InterruptedException e) {
                Thread.interrupted();
            } finally {
                lock.unlock();
            }

        }
    }

    @Singleton
    @Startup
    public static class Orange {

        public static volatile boolean deployed;
        public static final State state = new State();

        @PostConstruct
        private void startup() {
            deployed = true;
            state.toggle();
        }

        @PreDestroy
        private void shutdown() {
            deployed = false;
            state.toggle();
        }
    }

    @Singleton
    @Startup
    public static class Yellow {

        public static volatile boolean deployed;

        @PostConstruct
        private void startup() {
            deployed = true;
        }

        @PreDestroy
        private void shutdown() {
            deployed = false;
        }
    }
}
