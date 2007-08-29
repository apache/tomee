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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb;

import junit.framework.TestCase;
import org.apache.openejb.server.ejbd.EjbServer;
import org.apache.openejb.server.ServiceDaemon;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.loader.SystemInstance;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class AuthTest extends TestCase {
    public void test() throws Exception {
        EjbServer ejbServer = new EjbServer();

        Properties initProps = new Properties();
        initProps.setProperty("openejb.deployments.classpath.include", "");
        initProps.setProperty("openejb.deployments.classpath.filter.descriptors", "true");
        OpenEJB.init(initProps, new ServerFederation());
        ejbServer.init(new Properties());

        ServiceDaemon serviceDaemon = new ServiceDaemon(ejbServer, 0, "localhost");
        serviceDaemon.start();

        int port = serviceDaemon.getPort();


        try {

            // good creds
            Properties props = new Properties();
            props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
            props.put("java.naming.provider.url", "ejbd://127.0.0.1:" + port);
            props.put(Context.SECURITY_PRINCIPAL, "jonathan");
            props.put(Context.SECURITY_CREDENTIALS, "secret");
            new InitialContext(props);


            try {
                props.put(Context.SECURITY_PRINCIPAL, "alfred");
                props.put(Context.SECURITY_CREDENTIALS, "doesnotexist");
                new InitialContext(props);
            } catch (javax.naming.AuthenticationException e) {
                // pass -- user does not exist
            }

        } catch (NamingException e) {
            throw e;
        } finally {
            serviceDaemon.stop();
            OpenEJB.destroy();
        }

    }
}
