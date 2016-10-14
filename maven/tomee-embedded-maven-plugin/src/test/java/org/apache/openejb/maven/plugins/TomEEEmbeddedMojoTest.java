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
package org.apache.openejb.maven.plugins;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.openejb.config.DeploymentFilterable;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.System.lineSeparator;
import static java.lang.Thread.sleep;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TomEEEmbeddedMojoTest {
    @Before // there can be a small latency stopping/restarting tomcat so ensure we don't have conflicts between tests
    public void ensureTomcatIsDown() throws MalformedObjectNameException, IntrospectionException, ReflectionException {
        for (int i = 0; i < 10; i++) {
            try {
                assertFalse(SystemInstance.isInitialized());
                try {
                    assertNull(ManagementFactory.getPlatformMBeanServer().getMBeanInfo(new ObjectName("Tomcat:type=Server")));
                } catch (final InstanceNotFoundException e) {
                    // ok
                }
            } catch (final AssertionError ae) {
                try {
                    sleep(1000);
                } catch (final InterruptedException e) {
                    Thread.interrupted();
                    fail();
                }
            }
        }
    }

    @Test
    public void run() throws MojoFailureException, MojoExecutionException, IOException, InterruptedException {
        final File docBase = new File("target/TomEEEmbeddedMojoTest/base");
        docBase.mkdirs();
        try (final FileWriter w = new FileWriter(new File(docBase, "index.html"))) {
            w.write("initial");
        }

        // we use a dynamic InputStream to be able to simulate commands without hacking System.in
        final Input input = new Input();
        final Semaphore reloaded = new Semaphore(0);
        final CountDownLatch started = new CountDownLatch(1);
        final TomEEEmbeddedMojo mojo = new TomEEEmbeddedMojo() {
            @Override
            protected Scanner newScanner() {
                return new Scanner(input);
            }
        };
        mojo.classpathAsWar = true;
        mojo.httpPort = NetworkUtil.getNextAvailablePort();
        mojo.ssl = false;
        mojo.docBase = docBase;
        mojo.forceReloadable = true;
        mojo.webResourceCached = false;
        mojo.containerProperties = new HashMap<>();
        mojo.containerProperties.put(DeploymentFilterable.CLASSPATH_INCLUDE, ".*tomee-embedded-maven-plugin.*");
        mojo.containerProperties.put("openejb.additional.include", "tomee-embedded-maven-plugin");
        mojo.setLog(new SystemStreamLog() { // not the best solution but fine for now...
            @Override
            public void info(final CharSequence charSequence) {
                final String string = charSequence.toString();
                if (string.startsWith("TomEE embedded started on") || string.equals("can't start TomEE")) {
                    started.countDown();
                } else if (string.contains("Redeployed /")) {
                    reloaded.release();
                }
                super.info(charSequence);
            }
        });

        final CountDownLatch stopped = doStart(started, mojo);
        assertEquals("ok", IO.slurp(new URL("http://localhost:" + mojo.httpPort + "/endpoint/")).trim());

        long initTs = timestamp(mojo);

        assertEquals("initial", IO.slurp(new URL("http://localhost:" + mojo.httpPort + "/")).trim());
        try (final FileWriter w = new FileWriter(new File(docBase, "index.html"))) {
            w.write("changed");
        }
        assertEquals("changed", IO.slurp(new URL("http://localhost:" + mojo.httpPort + "/")).trim());

        assertEquals(timestamp(mojo), initTs);

        for (int i = 0; i < 4; i++) { // ensure it works multiple times
            System.out.println("Reloading, #" + (i + 1));

            try { // ensure timestamp changed even on super fast machines
                sleep(200);
            } catch (final InterruptedException e) {
                Thread.interrupted();
                fail();
            }

            input.write("reload");
            reloaded.tryAcquire(5, TimeUnit.MINUTES);
            final long newTimestamp = timestamp(mojo);
            assertTrue(Integer.toString(i) + " iteration", newTimestamp > initTs);
            initTs = newTimestamp;

            try { // check timestamp is fixed and we didn't code wrong the test
                sleep(200);
            } catch (final InterruptedException e) {
                Thread.interrupted();
                fail();
            }
            assertEquals(timestamp(mojo), initTs);

        }

        input.write("exit");
        stopped.await(5, TimeUnit.MINUTES);
        input.close();
    }

    @Test
    public void customWebResource() throws Exception {
        final File docBase = new File("target/TomEEEmbeddedMojoTest/customWebResource");
        docBase.mkdirs();
        try (final FileWriter w = new FileWriter(new File(docBase, "index.html"))) {
            w.write("resource");
        }

        // we use a dynamic InputStream to be able to simulate commands without hacking System.in
        final Input input = new Input();
        final Semaphore reloaded = new Semaphore(0);
        final CountDownLatch started = new CountDownLatch(1);
        final TomEEEmbeddedMojo mojo = new TomEEEmbeddedMojo() {
            @Override
            protected Scanner newScanner() {
                return new Scanner(input);
            }
        };
        mojo.classpathAsWar = true;
        mojo.httpPort = NetworkUtil.getNextAvailablePort();
        mojo.ssl = false;
        mojo.webResources = singletonList(docBase);
        mojo.webResourceCached = false;
        mojo.setLog(new SystemStreamLog() { // not the best solution but fine for now...
            @Override
            public void info(final CharSequence charSequence) {
                final String string = charSequence.toString();
                if (string.startsWith("TomEE embedded started on") || string.equals("can't start TomEE")) {
                    started.countDown();
                } else if (string.contains("Redeployed /")) {
                    reloaded.release();
                }
                super.info(charSequence);
            }
        });

        final CountDownLatch stopped = doStart(started, mojo);
        assertEquals("resource", IO.slurp(new URL("http://localhost:" + mojo.httpPort + "/")).trim());

        input.write("exit");
        stopped.await(5, TimeUnit.MINUTES);
        input.close();
    }

    @Test
    public void customScript() throws Exception {
        Assume.assumeFalse(System.getProperty("java.version").startsWith("1.7"));

        // we use a dynamic InputStream to be able to simulate commands without hacking System.in
        final Input input = new Input();
        final Semaphore reloaded = new Semaphore(0);
        final CountDownLatch started = new CountDownLatch(1);
        final TomEEEmbeddedMojo mojo = new TomEEEmbeddedMojo() {
            @Override
            protected Scanner newScanner() {
                return new Scanner(input);
            }
        };
        mojo.classpathAsWar = true;
        mojo.httpPort = NetworkUtil.getNextAvailablePort();
        mojo.ssl = false;
        mojo.webResourceCached = false;
        mojo.jsCustomizers = singletonList(
                "var File = Java.type('java.io.File');" +
                        "var FileWriter = Java.type('java.io.FileWriter');" +
                        "var out = new File(catalinaBase, 'conf/app.conf');" +
                        "var writer = new FileWriter(out);" +
                        "writer.write('test=ok');" +
                        "writer.close();");
        mojo.setLog(new SystemStreamLog() { // not the best solution but fine for now...
            @Override
            public void info(final CharSequence charSequence) {
                final String string = charSequence.toString();
                if (string.startsWith("TomEE embedded started on") || string.equals("can't start TomEE")) {
                    started.countDown();
                } else if (string.contains("Redeployed /")) {
                    reloaded.release();
                }
                super.info(charSequence);
            }
        });

        CountDownLatch stopped = null;
        try {
            stopped = doStart(started, mojo);
            final File appConf = new File(System.getProperty("catalina.base"), "conf/app.conf");
            assertTrue(appConf.exists());
            assertEquals("ok", IO.readProperties(appConf).getProperty("test", "ko"));
        } finally {
            input.write("exit");
            if (stopped != null) {
                stopped.await(5, TimeUnit.MINUTES);
            }
            input.close();
        }
    }

    private CountDownLatch doStart(final CountDownLatch started, final TomEEEmbeddedMojo mojo) {
        final CountDownLatch stopped = new CountDownLatch(1);

        final AtomicReference<Exception> error = new AtomicReference<>();
        final Thread mojoThread = new Thread() {
            {
                setName("Mojo-Starter");
            }

            @Override
            public void run() {
                try {
                    mojo.execute();
                } catch (final Exception e) {
                    error.set(e);
                } finally {
                    stopped.countDown();
                }
            }
        };
        mojoThread.start();
        try {
            started.await(10, TimeUnit.MINUTES);
        } catch (final InterruptedException e) {
            Thread.interrupted();
        }

        assertNull("all started fine", error.get());
        return stopped;
    }

    private long timestamp(final TomEEEmbeddedMojo mojo) throws IOException {
        return Long.parseLong(IO.slurp(new URL("http://localhost:" + mojo.httpPort + "/endpoint/timestamp")).trim());
    }

    private static final class Input extends InputStream {
        private final Semaphore inputSema = new Semaphore(0);
        private InputStream currentInput = null;
        private boolean metEnd = true;

        private void write(final String data) {
            try {
                currentInput = new ByteArrayInputStream((data + lineSeparator()).getBytes("UTF-8"));
            } catch (final UnsupportedEncodingException e) {
                currentInput = new ByteArrayInputStream((data + lineSeparator()).getBytes());
            }
            inputSema.release();
        }

        @Override
        public int read() throws IOException {
            int read;
            if (currentInput == null || (read = currentInput.read()) < 0) {
                if (!metEnd) { // ensure scanner gets an end event
                    metEnd = true;
                    currentInput = null;
                    return -1;
                }

                try {
                    inputSema.acquire(1);
                } catch (final InterruptedException e) {
                    Thread.interrupted();
                    fail();
                }
                read = currentInput != null ? currentInput.read() : -1;
                if (read > 0) {
                    metEnd = false;
                }
            }
            return read;
        }

        @Override
        public void close() throws IOException {
            inputSema.release();
            super.close();
        }
    }
}
