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
package org.superbiz.ws.security;

import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import jakarta.ejb.embeddable.EJBContainer;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.SOAPFaultException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CalculatorTest {

    @Test
    public void call() throws MalformedURLException {
        final EJBContainer container = EJBContainer.createEJBContainer(new Properties() {{
            setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
            setProperty("httpejbd.port", "0"); // random port to avoid issue on CI, default is 4204
        }});
        final int port = Integer.parseInt(SystemInstance.get().getProperty("httpejbd.port")); // get back the random port

        // normal call

        final Service service = Service.create(
                new URL("http://127.0.0.1:" + port + "/webservice-ws-with-resources-config/CalculatorBean?wsdl"),
                new QName("http://security.ws.superbiz.org/", "CalculatorBeanService"));

        final Calculator calculator = service.getPort(Calculator.class);
        ClientProxy.getClient(calculator).getOutInterceptors().add(
                new WSS4JOutInterceptor(new HashMap<String, Object>() {{
                    put("action", "UsernameToken");
                    put("user", "openejb");
                    put("passwordType", "PasswordText");
                    put("passwordCallbackRef", new CallbackHandler() {
                        @Override
                        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                            final WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
                            pc.setPassword("tomee");
                        }
                    });
                }}));

        assertEquals(5, calculator.add(2, 3));

        // bad auth

        final Calculator calculator2 = service.getPort(Calculator.class);
        ClientProxy.getClient(calculator2).getOutInterceptors().add(
                new WSS4JOutInterceptor(new HashMap<String, Object>() {{
                    put("action", "UsernameToken");
                    put("user", "openejb");
                    put("passwordType", "PasswordText");
                    put("passwordCallbackRef", new CallbackHandler() {
                        @Override
                        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                            final WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
                            pc.setPassword("wrong");
                        }
                    });
                }}));

        try {
            assertEquals(5, calculator2.add(2, 3));
        } catch (SOAPFaultException sfe) {
            assertThat(sfe.getMessage(), CoreMatchers.containsString("A security error was encountered when verifying the message"));
        }

        container.close();

        // valid it passed because all was fine and not because the server config was not here
        assertTrue(PasswordCallbackHandler.wasCalled());
    }
}
