/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.calculator;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class CalculatorTest {

    //START SNIPPET: setup
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static JavaArchive jar() {
        return ShrinkWrap.create(JavaArchive.class, "webservice-security.jar")
                .addClasses(CalculatorImpl.class, CalculatorRemote.class, CalculatorWs.class)
                .addAsManifestResource("META-INF/ejb-jar.xml", "ejb-jar.xml")
                .addAsManifestResource("META-INF/openejb-jar.xml", "openejb-jar.xml");
    }

    // the WSDL is protected by the same BASIC auth as the endpoint
    @Before
    public void wsdlCredentials() {
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("jane", "waterfall".toCharArray());
            }
        });
    }

    @After
    public void resetWsdlCredentials() {
        Authenticator.setDefault(null);
    }
    //END SNIPPET: setup

    /**
     * Create a webservice client using wsdl url
     *
     * @throws Exception
     */
    //START SNIPPET: webservice
    @Test
    public void testCalculatorViaWsInterface() throws Exception {
        URL url = new URL(base.toExternalForm() + "CalculatorImpl?wsdl");
        QName calcServiceQName = new QName("http://superbiz.org/wsdl", "CalculatorWsService");
        Service calcService = Service.create(url, calcServiceQName);
        assertNotNull(calcService);

        CalculatorWs calc = calcService.getPort(CalculatorWs.class);
        ((BindingProvider) calc).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "jane");
        ((BindingProvider) calc).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "waterfall");
        assertEquals(10, calc.sum(4, 6));
        assertEquals(12, calc.multiply(3, 4));
    }
    //END SNIPPET: webservice

}
