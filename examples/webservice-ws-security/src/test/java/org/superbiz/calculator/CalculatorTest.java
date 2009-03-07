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
package org.superbiz.calculator;

import junit.framework.TestCase;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.handler.WSHandlerConstants;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.ws.Service;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CalculatorTest extends TestCase {

    //START SNIPPET: setup
    private InitialContext initialContext;

    protected void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        properties.setProperty("openejb.embedded.remotable", "true");

        initialContext = new InitialContext(properties);
    }
    //END SNIPPET: setup

    /**
     * Create a webservice client using wsdl url
     *
     * @throws Exception
     */
    //START SNIPPET: webservice
    public void testCalculatorViaWsInterface() throws Exception {
        Service calcService = Service.create(new URL("http://127.0.0.1:4204/CalculatorImpl?wsdl"), null);
        assertNotNull(calcService);

        CalculatorWs calc = calcService.getPort(CalculatorWs.class);

        Client client = ClientProxy.getClient(calc);
        Endpoint endpoint = client.getEndpoint();
        endpoint.getOutInterceptors().add(new SAAJOutInterceptor());

        Map<String, Object> outProps = new HashMap<String, Object>();
        outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
        outProps.put(WSHandlerConstants.USER, "jane");
        outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
        outProps.put(WSHandlerConstants.PW_CALLBACK_REF, new CallbackHandler() {

            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
                pc.setPassword("waterfall");
            }
        });

        WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
        endpoint.getOutInterceptors().add(wssOut);

        assertEquals(10, calc.sum(4,6));
	    assertEquals(12, calc.multiply(3,4));
    }
    //END SNIPPET: webservice

}