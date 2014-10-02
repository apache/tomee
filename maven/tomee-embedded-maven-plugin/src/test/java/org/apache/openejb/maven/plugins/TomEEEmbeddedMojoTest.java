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
import org.apache.openejb.util.NetworkUtil;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TomEEEmbeddedMojoTest {
    @Test
    public void run() throws MojoFailureException, MojoExecutionException, IOException {
        final CountDownLatch started = new CountDownLatch(1);
        final TomEEEmbeddedMojo mojo = new TomEEEmbeddedMojo();
        mojo.classpathAsWar = true;
        mojo.httpPort = NetworkUtil.getNextAvailablePort();
        mojo.ssl = false;
        mojo.containerProperties = new HashMap<>();
        mojo.containerProperties.put(DeploymentFilterable.CLASSPATH_INCLUDE, ".*tomee-embedded-maven-plugin.*");
        mojo.containerProperties.put("openejb.additional.include", "tomee-embedded-maven-plugin");
        mojo.setLog(new SystemStreamLog() { // not the best solution...
            @Override
            public void info(final CharSequence charSequence) {
                final String string = charSequence.toString();
                if (string.startsWith("TomEE embedded started on") || string.equals("can't start TomEE")) {
                    started.countDown();
                }
                super.info(charSequence);
            }
        });

        final InputStream originalIn = System.in;
        final ByteArrayInputStream newIn = new ByteArrayInputStream("exit".getBytes());
        final CountDownLatch sendExitLatch = new CountDownLatch(1);
        final CountDownLatch stopped = new CountDownLatch(1);
        System.setIn(new InputStream() {
            @Override
            public int read() throws IOException {
                try {
                    sendExitLatch.await();
                } catch (final InterruptedException e) {
                    Thread.interrupted();
                }
                return newIn.read();
            }
        });

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
            try {
                started.await(10, TimeUnit.MINUTES);
            } catch (final InterruptedException e) {
                Thread.interrupted();
            }
            assertNull("all started fine", error.get());
            assertEquals("ok", IO.slurp(new URL("http://localhost:" + mojo.httpPort + "/endpoint/")).trim());

            sendExitLatch.countDown();
            try {
                stopped.await(5, TimeUnit.MINUTES);
            } catch (final InterruptedException e) {
                Thread.interrupted();
            }
        } finally {
            System.setIn(originalIn);
        }
    }
}
