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
package org.apache.openejb.server.cxf;

import junit.framework.TestCase;
import org.apache.openejb.config.DeploymentFilterable;
import org.apache.openejb.server.cxf.fault.AuthenticatorService;
import org.apache.openejb.server.cxf.fault.WrongPasswordException;
import org.apache.openejb.server.cxf.fault.WrongPasswordRuntimeException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

/**
 * @version $Rev$
 */
public class CxfJaxWsProviderTest extends TestCase {

    //START SNIPPET: setup	
    private InitialContext initialContext;

    //Random port to avoid test conflicts
    private static final int port = Integer.parseInt(System.getProperty("httpejbd.port", "" + org.apache.openejb.util.NetworkUtil.getNextAvailablePort()));

    protected void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(DeploymentFilterable.CLASSPATH_INCLUDE, ".*openejb-cxf.*");
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
        properties.setProperty("openejb.embedded.remotable", "true");

        //Just for this test we change the default port from 4204 to avoid conflicts
        properties.setProperty("httpejbd.port", "" + port);

        initialContext = new InitialContext(properties);
    }
    //END SNIPPET: setup

    public void test00_runCheckedException() {
        try {
            AuthenticatorService withHandler = Service.create(
                new URL("http://localhost:" + port + "/openejb-cxf/AuthenticatorServiceBean?wsdl"),
                new QName("http://superbiz.org/wsdl", "AuthenticatorServiceBeanService"))
                .getPort(AuthenticatorService.class);
            assertNotNull(withHandler);

            AuthenticatorService noHandler = Service.create(
                new URL("http://localhost:" + port + "/openejb-cxf/AuthenticatorServiceBeanNoHandler?wsdl"),
                new QName("http://superbiz.org/wsdl", "AuthenticatorServiceBeanNoHandlerService"))
                .getPort(AuthenticatorService.class);
            assertNotNull(noHandler);

            try {
                withHandler.authenticate("John", "Doe");
            } catch (WrongPasswordException e) {
                System.out.println("My lovely checked exception...");
            } catch (Throwable e) {
                e.printStackTrace();
                fail("A throwable instead of a checked exception...");
            }

            try {
                noHandler.authenticate("John", "Doe");
            } catch (WrongPasswordException e) {
                System.out.println("My lovely checked exception...");
            } catch (Throwable e) {
                e.printStackTrace();
                fail("A throwable instead of a checked exception...");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            fail("?!? invalid URL ???");
        }

    }

    public void test01_runRuntimeException() {
        try {
            AuthenticatorService withHandler = Service.create(
                new URL("http://localhost:" + port + "/openejb-cxf/AuthenticatorServiceBean?wsdl"),
                new QName("http://superbiz.org/wsdl", "AuthenticatorServiceBeanService"))
                .getPort(AuthenticatorService.class);
            assertNotNull(withHandler);

            AuthenticatorService noHandler = Service.create(
                new URL("http://localhost:" + port + "/openejb-cxf/AuthenticatorServiceBeanNoHandler?wsdl"),
                new QName("http://superbiz.org/wsdl", "AuthenticatorServiceBeanNoHandlerService"))
                .getPort(AuthenticatorService.class);
            assertNotNull(noHandler);

            try {
                withHandler.authenticateRuntime("John", "Doe");
            } catch (WrongPasswordRuntimeException e) {
                e.printStackTrace();
                fail("My checked exception instead of a throwableS...");
            } catch (Throwable e) {
                System.out.println("A throwable exception...");
            }


            try {
                noHandler.authenticateRuntime("John", "Doe");
            } catch (WrongPasswordRuntimeException e) {
                e.printStackTrace();
                fail("My checked exception instead of a throwableS...");
            } catch (Throwable e) {
                System.out.println("A throwable exception...");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            fail("?!? invalid URL ???");
        }

    }

}
