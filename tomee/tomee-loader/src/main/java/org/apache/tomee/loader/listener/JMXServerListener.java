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
package org.apache.tomee.loader.listener;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.lang.management.ManagementFactory;

// intended for custom jmx server as jmxmp
public class JMXServerListener implements LifecycleListener {
    private static final Log LOGGER = LogFactory.getLog(JMXServerListener.class);

    private String protocol; // default if null is jmxmp
    private String host; // if null localhost
    private int port = -1;
    private String urlPath; // if null empty

    private JMXConnectorServer server;
    private JMXServiceURL serviceURL;

    @Override
    public synchronized void lifecycleEvent(final LifecycleEvent event) {
        try {
            if (server == null && Lifecycle.START_EVENT.equals(event.getType())) {
                serviceURL = new JMXServiceURL(protocol, host, port, urlPath);
                server = JMXConnectorServerFactory.newJMXConnectorServer(serviceURL, null,
                                                    ManagementFactory.getPlatformMBeanServer());
                server.start();
                LOGGER.info("Started JMX server: " + serviceURL.toString());
            } else if (server != null && Lifecycle.STOP_EVENT.equals(event.getType())) {
                server.stop();
                server = null;
                LOGGER.info("Stopped JMX server: " + serviceURL.toString());
            }
        } catch (final Exception e) {
            throw new JMXException(e);
        }
    }

    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    public void setUrlPath(final String urlPath) {
        this.urlPath = urlPath;
    }

    private static class JMXException extends RuntimeException {
        public JMXException(final Exception e) {
            super(e);
        }
    }
}
