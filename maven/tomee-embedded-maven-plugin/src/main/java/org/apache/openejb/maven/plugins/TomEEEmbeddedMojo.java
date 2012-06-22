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

import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.tomee.embedded.Configuration;
import org.apache.tomee.embedded.Container;

/**
 * @goal run
 * @requiresDependencyResolution runtime
 */
public class TomEEEmbeddedMojo extends AbstractMojo {
    /**
     * @parameter expression="${project.packaging}"
     */
    protected String packaging;

    /**
     * @parameter default-value="${project.build.directory}/${project.build.finalName}"
     * @readonly
     */
    protected File warFile;

    /** config of the container itself -> match 1-1 Configuration attributes -->

    /**
     * @parameter expression="${tomee-embedded-plugin.http}" default-value="8080"
     */
    private int httpPort;

    /**
     * @parameter expression="${tomee-embedded-plugin.ajp}" default-value="8009"
     */
    private int ajpPort = 8009;

    /**
     * @parameter expression="${tomee-embedded-plugin.stop}" default-value="8005"
     */
    private int stopPort;

    /**
     * @parameter expression="${tomee-embedded-plugin.host}" default-value="localhost"
     */
    private String host;

    /**
     * @parameter expression="${tomee-embedded-plugin.lib}" default-value="${project.build.directory}/apache-tomee-embedded"
     * @optional
     */
    protected String dir;

    /**
     * @parameter
     * @optional
     */
    private File serverXml;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if ("pom".equals(packaging)) {
            getLog().warn("this project is a pom, it is not deployable");
            return;
        }

        final CountDownLatch latch = new CountDownLatch(1);

        final Container container = new Container();
        final Configuration config  = getConfig();
        container.setup(config);
        try {
            container.start();

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        container.undeploy(warFile.getAbsolutePath());
                        container.stop();
                    } catch (Exception e) {
                        getLog().error("can't stop TomEE", e);
                    } finally {
                        latch.countDown();
                    }
                }
            });

            container.deploy(warFile.getName(), warFile);

            getLog().info("TomEE embedded started on " + config.getHost() + ":" + config.getHttpPort());
        } catch (Exception e) {
            getLog().error("can't start TomEE", e);
        }

        try {
            latch.await();
        } catch (Exception e) {
            Thread.interrupted();
        }
    }

    private Configuration getConfig() { // lazy way but it works fine
        final Configuration config = new Configuration();
        for (Field field : getClass().getDeclaredFields()) {
            try {
                final Field configField = Configuration.class.getDeclaredField(field.getName());
                field.setAccessible(true);
                configField.setAccessible(true);

                final Object value = field.get(this);
                if (value != null) {
                    configField.set(config, value);
                    getLog().info("using " + field.getName()  + " = " + value);
                }
            } catch (NoSuchFieldException nsfe) {
                // ignored
            } catch (Exception e) {
                 getLog().warn("can't initialize attribute " + field.getName());
            }

        }
        return config;
    }
}
