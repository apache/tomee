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
package org.apache.openejb;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.ejbd.EjbServer;
import org.apache.openejb.test.TestManager;
import org.apache.openejb.test.entity.bmp.BmpTestSuite;
import org.apache.openejb.test.entity.cmp.CmpTestSuite;
import org.apache.openejb.test.stateful.StatefulTestSuite;
import org.apache.openejb.test.stateless.StatelessTestSuite;

import java.util.Properties;

import javax.naming.Context;

/**
 * To run from intellij or another IDE add
 * <p/>
 * -Dopenejb.home=/Users/dblevins/work/openejb3/server/openejb-ejbd/target/test-classes
 *
 * @version $Revision$ $Date$
 */
public class RemoteiTest extends org.apache.openejb.test.TestSuite {

    protected void setUp() throws Exception {
        System.setProperty("openejb.test.server", EjbTestServer.class.getName());
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
        TestSuite suite = new RemoteiTest();
        suite.addTest(StatelessTestSuite.suite());
        suite.addTest(StatefulTestSuite.suite());
        suite.addTest(BmpTestSuite.suite());
        suite.addTest(CmpTestSuite.suite());
//        suite.addTest(Cmp2TestSuite.suite());
        return suite;
    }

    public static class EjbTestServer implements org.apache.openejb.test.TestServer {
        private ServiceDaemon serviceDaemon;
        private int port;

        public void init(Properties props) {
            try {
                EjbServer ejbServer = new EjbServer();
                props.put("openejb.deployments.classpath", "true");
                OpenEJB.init(props, new ServerFederation());
                ejbServer.init(props);

                serviceDaemon = new ServiceDaemon(ejbServer, 0, "localhost");

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
            props.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
            props.put(Context.PROVIDER_URL, "ejbd://localhost:" + port);
            return props;
        }
    }
}
