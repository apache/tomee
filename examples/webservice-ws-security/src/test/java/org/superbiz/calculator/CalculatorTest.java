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
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.handler.WSHandlerConstants;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CalculatorTest extends TestCase {

    //START SNIPPET: setup
    protected void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
        properties.setProperty("openejb.embedded.remotable", "true");

        new InitialContext(properties);
    }
    //END SNIPPET: setup

    //START SNIPPET: webservice
    public void testCalculatorViaWsInterface() throws Exception {
        Service calcService = Service.create(new URL("http://127.0.0.1:4204/CalculatorImpl?wsdl"),
                new QName("http://superbiz.org/wsdl", "CalculatorWsService"));
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

        assertEquals(10, calc.sum(4, 6));
    }

    public void testCalculatorViaWsInterfaceWithTimestamp1way() throws Exception {
        Service calcService = Service.create(new URL("http://127.0.0.1:4204/CalculatorImplTimestamp1way?wsdl"),
                new QName("http://superbiz.org/wsdl", "CalculatorWsService"));
        assertNotNull(calcService);

        // for debugging (ie. TCPMon)
        calcService.addPort(new QName("http://superbiz.org/wsdl",
                "CalculatorWsService2"), SOAPBinding.SOAP12HTTP_BINDING,
                "http://127.0.0.1:8204/CalculatorImplTimestamp1way");

//        CalculatorWs calc = calcService.getPort(
//        	new QName("http://superbiz.org/wsdl", "CalculatorWsService2"),
//		CalculatorWs.class);

        CalculatorWs calc = calcService.getPort(CalculatorWs.class);

        Client client = ClientProxy.getClient(calc);
        Endpoint endpoint = client.getEndpoint();
        endpoint.getOutInterceptors().add(new SAAJOutInterceptor());

        Map<String, Object> outProps = new HashMap<String, Object>();
        outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.TIMESTAMP);
        WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
        endpoint.getOutInterceptors().add(wssOut);

        assertEquals(12, calc.multiply(3, 4));
    }

    public void testCalculatorViaWsInterfaceWithTimestamp2ways() throws Exception {
        Service calcService = Service.create(new URL("http://127.0.0.1:4204/CalculatorImplTimestamp2ways?wsdl"),
                new QName("http://superbiz.org/wsdl", "CalculatorWsService"));
        assertNotNull(calcService);

        // for debugging (ie. TCPMon)
        calcService.addPort(new QName("http://superbiz.org/wsdl",
                "CalculatorWsService2"), SOAPBinding.SOAP12HTTP_BINDING,
                "http://127.0.0.1:8204/CalculatorImplTimestamp2ways");

//        CalculatorWs calc = calcService.getPort(
//        	new QName("http://superbiz.org/wsdl", "CalculatorWsService2"),
//		CalculatorWs.class);

        CalculatorWs calc = calcService.getPort(CalculatorWs.class);

        Client client = ClientProxy.getClient(calc);
        Endpoint endpoint = client.getEndpoint();
        endpoint.getOutInterceptors().add(new SAAJOutInterceptor());
        endpoint.getInInterceptors().add(new SAAJInInterceptor());

        Map<String, Object> outProps = new HashMap<String, Object>();
        outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.TIMESTAMP);
        WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
        endpoint.getOutInterceptors().add(wssOut);

        Map<String, Object> inProps = new HashMap<String, Object>();
        inProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.TIMESTAMP);
        WSS4JInInterceptor wssIn = new WSS4JInInterceptor(inProps);
        endpoint.getInInterceptors().add(wssIn);

        assertEquals(12, calc.multiply(3, 4));
    }

    public void testCalculatorViaWsInterfaceWithUsernameTokenPlainPassword() throws Exception {
        Service calcService = Service.create(new URL("http://127.0.0.1:4204/CalculatorImplUsernameTokenPlainPassword?wsdl"),
                new QName("http://superbiz.org/wsdl", "CalculatorWsService"));
        assertNotNull(calcService);

        // for debugging (ie. TCPMon)
        calcService.addPort(new QName("http://superbiz.org/wsdl",
                "CalculatorWsService2"), SOAPBinding.SOAP12HTTP_BINDING,
                "http://127.0.0.1:8204/CalculatorImplUsernameTokenPlainPassword");

//        CalculatorWs calc = calcService.getPort(
//        	new QName("http://superbiz.org/wsdl", "CalculatorWsService2"),
//        	CalculatorWs.class);

        CalculatorWs calc = calcService.getPort(CalculatorWs.class);

        Client client = ClientProxy.getClient(calc);
        Endpoint endpoint = client.getEndpoint();
        endpoint.getOutInterceptors().add(new SAAJOutInterceptor());

        Map<String, Object> outProps = new HashMap<String, Object>();
        outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
        outProps.put(WSHandlerConstants.USER, "jane");
        outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
        outProps.put(WSHandlerConstants.PW_CALLBACK_REF, new CallbackHandler() {

            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
                pc.setPassword("waterfall");
            }
        });

        WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
        endpoint.getOutInterceptors().add(wssOut);

        assertEquals(10, calc.sum(4, 6));
    }

    public void testCalculatorViaWsInterfaceWithUsernameTokenHashedPassword() throws Exception {
        Service calcService = Service.create(new URL("http://127.0.0.1:4204/CalculatorImplUsernameTokenHashedPassword?wsdl"),
                new QName("http://superbiz.org/wsdl", "CalculatorWsService"));
        assertNotNull(calcService);

        // for debugging (ie. TCPMon)
        calcService.addPort(new QName("http://superbiz.org/wsdl",
                "CalculatorWsService2"), SOAPBinding.SOAP12HTTP_BINDING,
                "http://127.0.0.1:8204/CalculatorImplUsernameTokenHashedPassword");

//        CalculatorWs calc = calcService.getPort(
//        	new QName("http://superbiz.org/wsdl", "CalculatorWsService2"),
//        	CalculatorWs.class);

        CalculatorWs calc = calcService.getPort(CalculatorWs.class);

        Client client = ClientProxy.getClient(calc);
        Endpoint endpoint = client.getEndpoint();
        endpoint.getOutInterceptors().add(new SAAJOutInterceptor());

        Map<String, Object> outProps = new HashMap<String, Object>();
        outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
        outProps.put(WSHandlerConstants.USER, "jane");
        outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_DIGEST);
        outProps.put(WSHandlerConstants.PW_CALLBACK_REF, new CallbackHandler() {

            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
                pc.setPassword("waterfall");
            }
        });

        WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
        endpoint.getOutInterceptors().add(wssOut);

        assertEquals(10, calc.sum(4, 6));
    }

    public void testCalculatorViaWsInterfaceWithUsernameTokenPlainPasswordEncrypt() throws Exception {
        Service calcService = Service.create(new URL("http://127.0.0.1:4204/CalculatorImplUsernameTokenPlainPasswordEncrypt?wsdl"),
                new QName("http://superbiz.org/wsdl", "CalculatorWsService"));
        assertNotNull(calcService);

        // for debugging (ie. TCPMon)
        calcService.addPort(new QName("http://superbiz.org/wsdl",
                "CalculatorWsService2"), SOAPBinding.SOAP12HTTP_BINDING,
                "http://127.0.0.1:8204/CalculatorImplUsernameTokenPlainPasswordEncrypt");

//        CalculatorWs calc = calcService.getPort(
//        	new QName("http://superbiz.org/wsdl", "CalculatorWsService2"),
//        	CalculatorWs.class);

        CalculatorWs calc = calcService.getPort(CalculatorWs.class);

        Client client = ClientProxy.getClient(calc);
        Endpoint endpoint = client.getEndpoint();
        endpoint.getOutInterceptors().add(new SAAJOutInterceptor());

        Map<String, Object> outProps = new HashMap<String, Object>();
        outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN
                + " " + WSHandlerConstants.ENCRYPT);
        outProps.put(WSHandlerConstants.USER, "jane");
        outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
        outProps.put(WSHandlerConstants.PW_CALLBACK_REF, new CallbackHandler() {

            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
                pc.setPassword("waterfall");
            }
        });
        outProps.put(WSHandlerConstants.ENC_PROP_FILE, "META-INF/CalculatorImplUsernameTokenPlainPasswordEncrypt-client.properties");
        outProps.put(WSHandlerConstants.ENCRYPTION_USER, "serveralias");

        WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
        endpoint.getOutInterceptors().add(wssOut);

        assertEquals(10, calc.sum(4, 6));
    }

    public void testCalculatorViaWsInterfaceWithSign() throws Exception {
        Service calcService = Service.create(new URL("http://127.0.0.1:4204/CalculatorImplSign?wsdl"),
                new QName("http://superbiz.org/wsdl", "CalculatorWsService"));
        assertNotNull(calcService);

        // for debugging (ie. TCPMon)
        calcService.addPort(new QName("http://superbiz.org/wsdl",
                "CalculatorWsService2"), SOAPBinding.SOAP12HTTP_BINDING,
                "http://127.0.0.1:8204/CalculatorImplSign");

//      CalculatorWs calc = calcService.getPort(
//	new QName("http://superbiz.org/wsdl", "CalculatorWsService2"),
//	CalculatorWs.class);

        CalculatorWs calc = calcService.getPort(CalculatorWs.class);

        Client client = ClientProxy.getClient(calc);
        Endpoint endpoint = client.getEndpoint();
        endpoint.getOutInterceptors().add(new SAAJOutInterceptor());

        Map<String, Object> outProps = new HashMap<String, Object>();
        outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.SIGNATURE);
        outProps.put(WSHandlerConstants.USER, "clientalias");
        outProps.put(WSHandlerConstants.PW_CALLBACK_REF, new CallbackHandler() {

            @Override
            public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
                pc.setPassword("clientPassword");
            }
        });
        outProps.put(WSHandlerConstants.SIG_PROP_FILE, "META-INF/CalculatorImplSign-client.properties");
        outProps.put(WSHandlerConstants.SIG_KEY_ID, "IssuerSerial");

        WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
        endpoint.getOutInterceptors().add(wssOut);

        assertEquals(24, calc.multiply(4, 6));
    }
    //END SNIPPET: webservice
}
