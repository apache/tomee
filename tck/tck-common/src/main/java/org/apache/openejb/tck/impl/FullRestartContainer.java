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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.tck.impl;

import org.apache.openejb.client.RemoteInitialContextFactory;
import org.apache.openejb.config.RemoteServer;
import org.apache.openejb.loader.Options;
import org.apache.tomee.catalina.facade.ExceptionManagerFacade;
import org.jboss.testharness.api.DeploymentException;
import org.jboss.testharness.spi.Containers;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

/**
 * flow:
 * - copy file
 * - start the server
 * - stop the server
 * - remove the file
 */
public class FullRestartContainer extends AbstractContainers implements Containers {
    private static final File WEBAPP_DIR = new File(System.getProperty("openejb.home"), "webapps/");
    private static final File APPS_DIR = new File(System.getProperty("openejb.home"), "apps/");

    private RemoteServer server;
    private Exception exception;
    private File currentFile;

    public FullRestartContainer() {
        System.out.println("ContainersImpl=" + FullRestartContainer.class.getName());
    }

    @Override
    public DeploymentException getDeploymentException() {
        if (exception instanceof DeploymentException) {
            return (DeploymentException) exception;
        }
        System.out.println("BADCAST");
        return new DeploymentException("", exception);
    }

    @Override
    public boolean deploy(InputStream archive, String name) throws IOException {
        if (name.endsWith("war")) {
            currentFile = new File(WEBAPP_DIR, name);
        } else {
            currentFile = new File(APPS_DIR, name);
        }

        System.out.println(currentFile);
        writeToFile(currentFile, archive);

        addLogFiles();

        server = new RemoteServer(100, true);
        try {
            server.start();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }

        return (exception = lookup().exception()) == null;
    }

    private static void addLogFiles() throws IOException {
        final File tomee = new File(System.getProperty("openejb.home"));
        if (tomee.exists()) {
            final File log4j = new File(tomee, "lib/log4j.xml");
            final FileWriter log4jWriter = new FileWriter(log4j);
            log4jWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<!DOCTYPE log4j:configuration SYSTEM \"log4j.dtd\">\n" +
                    "<log4j:configuration xmlns:log4j=\"http://jakarta.apache.org/log4j/\" debug=\"false\">\n" +
                    "  <appender name=\"stdout\" class=\"org.apache.log4j.ConsoleAppender\">\n" +
                    "    <param name=\"Target\" value=\"System.out\" />\n" +
                    "    <layout class=\"org.apache.log4j.SimpleLayout\" />\n" +
                    "  </appender>\n" +
                    "  <root>\n" +
                    "    <priority value=\"INFO\"/>\n" +
                    "    <appender-ref ref=\"stdout\"/>\n" +
                    "  </root>\n" +
                    "</log4j:configuration>\n");
            log4jWriter.close();

            final File systemProperties = new File(tomee, "conf/system.properties");
            final FileWriter systemPropertiesWriter = new FileWriter(systemProperties);
            systemPropertiesWriter.write("openejb.logger.external=true");
            systemPropertiesWriter.close();
        } else {
            System.out.println("tomee folder doesn't exist");
        }
    }

    @Override
    public void undeploy(String name) throws IOException {
        server.destroy();
        File folder = new File(currentFile.getParentFile(), currentFile.getName().substring(0, currentFile.getName().length() - 4));
        if (folder.exists()) {
            delete(folder);
        }
        delete(currentFile);
    }

    @Override
    public void setup() throws IOException {
        // no-op
    }

    @Override
    public void cleanup() throws IOException {
        // no-op
    }

    private ExceptionManagerFacade lookup() {
        final Options options = new Options(System.getProperties());
        final Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
        String port = System.getProperty("server.http.port");
        if (port != null) {
            System.out.println("provider url = " + "http://localhost:" + port + "/openejb/ejb");
            props.put(Context.PROVIDER_URL, options.get(Context.PROVIDER_URL,"http://localhost:" + port + "/openejb/ejb"));
        } else {
            throw new RuntimeException("Please set the tomee port as a system property");
        }

        try {
            InitialContext context = new InitialContext(props);
            return (ExceptionManagerFacade) context.lookup("openejb/ExceptionManagerFacadeRemote");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
