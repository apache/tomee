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

import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.lang.management.ManagementFactory;

// intended for custom jmx server as jmxmp
public class JMXServerListener implements LifecycleListener {
    private String protocol = null; // default if null is jmxmp
    private String host = null; // if null localhost
    private int port = -1;
    private String urlPath = null; // if null empty

    private JMXConnectorServer server = null;

    @Override
    public synchronized void lifecycleEvent(final LifecycleEvent event) {
        try {
            if (server == null && Lifecycle.START_EVENT.equals(event.getType())) {
                server = JMXConnectorServerFactory.newJMXConnectorServer(
                        new JMXServiceURL(protocol, host, port, urlPath),
                        null,
                        ManagementFactory.getPlatformMBeanServer());
                server.start();
            } else if (server != null && Lifecycle.STOP_EVENT.equals(event.getType())) {
                server.stop();
                server = null;
            }
        } catch (final Exception e) {
            throw new JMXException(e);
        }
    }

    private static class JMXException extends RuntimeException {
        public JMXException(final Exception e) {
            super(e);
        }
    }
}
