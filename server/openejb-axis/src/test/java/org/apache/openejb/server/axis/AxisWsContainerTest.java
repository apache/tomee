/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.axis;

import org.apache.axis.constants.Style;
import org.apache.axis.constants.Use;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.encoding.TypeMapping;
import org.apache.axis.encoding.TypeMappingRegistryImpl;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.openejb.server.webservices.WsConstants;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AxisWsContainerTest extends AbstractTestCase {
    public AxisWsContainerTest(String testName) {
        super(testName);
    }

    public void testInvokeSOAP() throws Exception {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        JavaServiceDesc serviceDesc = new JavaServiceDesc();
        serviceDesc.setEndpointURL("http://127.0.0.1:8080/axis/services/echo");
        //serviceDesc.setWSDLFile(portInfo.getWsdlURL().toExternalForm());
        serviceDesc.setStyle(Style.RPC);
        serviceDesc.setUse(Use.ENCODED);

        TypeMappingRegistryImpl tmr = new TypeMappingRegistryImpl();
        tmr.doRegisterFromVersion("1.3");
        TypeMapping typeMapping = tmr.getOrMakeTypeMapping(serviceDesc.getUse().getEncoding());

        serviceDesc.setTypeMappingRegistry(tmr);
        serviceDesc.setTypeMapping(typeMapping);

        OperationDesc op = new OperationDesc();
        op.setName("echoString");
        op.setStyle(Style.RPC);
        op.setUse(Use.ENCODED);
        Class beanClass = EchoBean.class;
        op.setMethod(beanClass.getMethod("echoString", String.class));
        ParameterDesc parameter =
            new ParameterDesc(
                new QName("http://ws.apache.org/echosample", "in0"),
                ParameterDesc.IN,
                typeMapping.getTypeQName(String.class),
                String.class,
                false,
                false);
        op.addParameter(parameter);
        serviceDesc.addOperationDesc(op);

        serviceDesc.getOperations();
        ReadOnlyServiceDesc sd = new ReadOnlyServiceDesc(serviceDesc);

        Class pojoClass = cl.loadClass("org.apache.openejb.server.axis.EchoBean");

        RPCProvider provider = new PojoProvider();
        SOAPService service = new SOAPService(null, provider, null);
        service.setServiceDescription(sd);
        service.setOption("className","org.apache.openejb.server.axis.EchoBean");
        URL wsdlURL = new URL("http://fake/echo.wsdl");
        URI location = new URI(serviceDesc.getEndpointURL());
        Map wsdlMap = new HashMap();

        AxisWsContainer container = new AxisWsContainer(wsdlURL, service, wsdlMap, cl);

        InputStream in = cl.getResourceAsStream("echoString-req.txt");

        try {
            AxisRequest req =
                new AxisRequest(
                    504,
                    "text/xml; charset=utf-8",
                    in,
                    0,
                    new HashMap<String,String>(),
                    location,
                    new HashMap<String,String>(),
                    "127.0.0.1");
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            AxisResponse res = new AxisResponse("text/xml; charset=utf-8", "127.0.0.1", null, null, 8080, out);
            req.setAttribute(WsConstants.POJO_INSTANCE, pojoClass.newInstance());
            container.onMessage(req, res);
            
            out.flush();
//            log.debug(new String(out.toByteArray()));
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                    // ignore
                }
            }
        }
    }
}
