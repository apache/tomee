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
package org.apache.openejb.arquillian.tests.jaxws.fault;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @version $Rev$
 */
@RunWith(Arquillian.class)
public class CxfJaxWsProviderTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "openejb-cxf.war")
                .addClasses(AuthenticatorService.class, AuthenticatorServiceBean.class, AuthenticatorServiceBeanNoHandler.class,
                        DummyInterceptor.class, WrongPasswordException.class, WrongPasswordRuntimeException.class)
                .addAsResource(AuthenticatorServiceBean.class.getPackage(), "handler.xml");
    }

    @Test
    public void test00_runCheckedException() {
        try {
            AuthenticatorService withHandler = Service.create(
                new URL(base.toExternalForm() + "webservices/AuthenticatorServiceBean?wsdl"),
                new QName("http://superbiz.org/wsdl", "AuthenticatorServiceBeanService"))
                .getPort(AuthenticatorService.class);
            assertNotNull(withHandler);

            AuthenticatorService noHandler = Service.create(
                new URL(base.toExternalForm() + "webservices/AuthenticatorServiceBeanNoHandler?wsdl"),
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

    @Test
    public void test01_runRuntimeException() {
        try {
            AuthenticatorService withHandler = Service.create(
                new URL(base.toExternalForm() + "webservices/AuthenticatorServiceBean?wsdl"),
                new QName("http://superbiz.org/wsdl", "AuthenticatorServiceBeanService"))
                .getPort(AuthenticatorService.class);
            assertNotNull(withHandler);

            AuthenticatorService noHandler = Service.create(
                new URL(base.toExternalForm() + "webservices/AuthenticatorServiceBeanNoHandler?wsdl"),
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
