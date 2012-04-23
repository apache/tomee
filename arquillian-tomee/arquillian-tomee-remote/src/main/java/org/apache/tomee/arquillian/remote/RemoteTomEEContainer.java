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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.arquillian.remote;

import org.apache.openejb.arquillian.common.Files;
import org.apache.openejb.arquillian.common.Setup;
import org.apache.openejb.arquillian.common.TomEEContainer;
import org.apache.openejb.config.RemoteServer;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * TODO: delete old embedded adapter, move the tests and set those up
 */
public class RemoteTomEEContainer extends TomEEContainer<RemoteTomEEConfiguration> {

    private static final Logger logger = Logger.getLogger(RemoteTomEEContainer.class.getName());

    private RemoteServer container;
    private boolean shutdown = false;

    public void start() throws LifecycleException {
        // see if TomEE is already running by checking the http port
        if (Setup.isRunning(configuration.getHttpPort())) {

            logger.info(String.format("TomEE found running on port %s", configuration.getHttpPort()));

            return;
        }

        shutdown = true;

        try {

            configure();

            container = new RemoteServer();

            container.start();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to start remote container", e);
            throw new LifecycleException("Unable to start remote container:" + e.getMessage(), e);
        }
    }

    private void configure() throws LifecycleException, IOException {
        final File workingDirectory = new File(configuration.getDir()).getAbsoluteFile();

        if (workingDirectory.exists()) {

            Files.assertDir(workingDirectory);

        } else {

            Files.mkdir(workingDirectory);
            Files.deleteOnExit(workingDirectory);
        }

        Files.readable(workingDirectory);
        Files.writable(workingDirectory);

        File openejbHome = Setup.findHome(workingDirectory);

        if (openejbHome == null) {
            openejbHome = Setup.downloadAndUnpack(workingDirectory, configuration.getArtifactName());

            logger.log(Level.INFO, "Downloaded container to: " + openejbHome);
        }

        Files.assertDir(openejbHome);
        Files.readable(openejbHome);
        Files.writable(openejbHome);

        Setup.updateServerXml(openejbHome, configuration.getHttpPort(), configuration.getStopPort(), configuration.getAjpPort());

        Setup.exportProperties(openejbHome, configuration);

        if (configuration.isRemoveUnusedWebapps()) {
            Setup.removeUselessWebapps(openejbHome);
        }

        if (logger.isLoggable(Level.FINE)) {
            Map<Object, Object> map = new TreeMap(System.getProperties());
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                logger.log(Level.FINE, String.format("%s = %s\n", entry.getKey(), entry.getValue()));
            }
        }
    }

    public void stop() throws LifecycleException {
        // only stop the container if we started it
        if (shutdown) {
            container.stop();
        }
    }

    public Class<RemoteTomEEConfiguration> getConfigurationClass() {
        return RemoteTomEEConfiguration.class;
    }
}
