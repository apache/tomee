/**
 *
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
package org.apache.openejb.server.httpd;

import org.apache.openejb.test.TestManager;
import org.apache.openejb.test.entity.bmp.BmpTestSuite;
import org.apache.openejb.test.entity.cmp.CmpTestSuite;
import org.apache.openejb.test.stateful.StatefulTestSuite;
import org.apache.openejb.test.stateless.StatelessTestSuite;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.ejbd.EjbServer;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.Properties;

/**
 * To run from intellij or another IDE add
 *
 * -Dopenejb.home=/Users/dblevins/work/openejb3/server/openejb-httpd/target/test-classes
 *
 * @version $Revision$ $Date$
 */
public class HttpEjbServerTest extends org.apache.openejb.test.TestSuite {

    protected void setUp() throws Exception {
        System.setProperty("openejb.test.server", HttpEjbTestServer.class.getName());
//        System.setProperty("openejb.test.database", org.apache.openejb.test.DerbyTestDatabase.class.getName());
        System.setProperty("openejb.test.database", org.apache.openejb.test.HsqldbTestDatabase.class.getName());
        TestManager.init(null);
        TestManager.start();
    }

    protected void tearDown() throws Exception {
        TestManager.stop();
        OpenEJB.destroy();
    }

    public static Test suite() {
        TestSuite suite = new HttpEjbServerTest();
        suite.addTest(StatelessTestSuite.suite());
        suite.addTest(StatefulTestSuite.suite());
        suite.addTest(BmpTestSuite.suite());
        suite.addTest(CmpTestSuite.suite());
        return suite;
    }

    public static class HttpEjbTestServer implements org.apache.openejb.test.TestServer {
        private ServiceDaemon serviceDaemon;
        private int port;

        public void init(Properties props) {
            try {
                EjbServer ejbServer = new EjbServer();
                ServerServiceAdapter adapter = new ServerServiceAdapter(ejbServer);
                HttpServer httpServer = new HttpServer(adapter);

                props.put("openejb.deployments.classpath", "true");
                OpenEJB.init(props, new ServerFederation());
                ejbServer.init(props);

                serviceDaemon = new ServiceDaemon(httpServer, 0, "localhost");

            } catch (Exception e) {
                throw new RuntimeException("Unable to initialize Test Server.", e);
            }
        }

        public void start() {
            try {
                serviceDaemon.start();
                port = serviceDaemon.getPort();
            } catch (ServiceException e) {
                throw new RuntimeException("Unable to start Test Server.", e);
            }
        }

        public void stop() {
            try {
                serviceDaemon.stop();
            } catch (ServiceException e) {
                throw new RuntimeException("Unable to stop Test Server.", e);
            }
        }

        public Properties getContextEnvironment() {
            Properties props = new Properties();
            props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
            props.put("java.naming.provider.url", "http://127.0.0.1:"+port+"/rjp");
            props.put("java.naming.security.principal", "testuser");
            props.put("java.naming.security.credentials", "testpassword");
            return props;
        }
    }
}
