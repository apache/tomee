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

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.jws.WebService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.ws.BindingType;
import jakarta.xml.ws.WebServiceRef;
import jakarta.xml.ws.soap.SOAPBinding;
import java.io.IOException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@EnableServices("jax-ws")
@RunWith(ApplicationComposer.class)
public class SoapBinding12ClientTest {
    @Module
    public WebApp module() {
        return new WebApp().contextRoot("/test").addServlet("ws", MockWebService12.class.getName(), "MyWebservice12");
    }

    @WebServiceRef(wsdlLocation = "http://127.0.0.1:4204/test/MyWebservice12?wsdl")
    private MyWsApi client;

    @Test
    public void check() throws IOException {
        assertThat(IO.slurp(new URL("http://127.0.0.1:4204/test/MyWebservice12?wsdl")), containsString("<soap12"));
        assertEquals("ok", client.test(new Input("ok")).getAttribute());
    }

    @WebService(serviceName = "MyWebservice12Service")
    @BindingType(SOAPBinding.SOAP12HTTP_BINDING)
    public static interface MyWsApi {
        Output test(Input input);
    }

    /**
     * We mock the following WebService (to avoid colocalized issue/luck):
     *
     * @WebService
     * @Singleton
     * @BindingType(SOAPBinding.SOAP12HTTP_BINDING) public static class MyWebservice12 implements MyWsApi {
     * @Override public Output test(final Input in) {
     * return new Output(in.getAttribute());
     * }
     * }
     */
    public static class MockWebService12 extends HttpServlet {
        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            if ("GET".equals(req.getMethod())) {
                resp.getWriter().write("<?xml version='1.0' encoding='UTF-8'?>" +
                    "<wsdl:definitions xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" xmlns:tns=\"http://cxf.server.openejb.apache.org/\" xmlns:soap12=\"http://schemas.xmlsoap.org/wsdl/soap12/\" xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:ns1=\"http://schemas.xmlsoap.org/soap/http\" name=\"MyWebservice12Service\" targetNamespace=\"http://cxf.server.openejb.apache.org/\">\n" +
                    "  <wsdl:types>\n" +
                    "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:tns=\"http://cxf.server.openejb.apache.org/\" elementFormDefault=\"unqualified\" targetNamespace=\"http://cxf.server.openejb.apache.org/\" version=\"1.0\">\n" +
                    "<xs:element name=\"input\" type=\"tns:input\"/>\n" +
                    "<xs:element name=\"output\" type=\"tns:output\"/>\n" +
                    "<xs:element name=\"test\" type=\"tns:test\"/>\n" +
                    "<xs:element name=\"testResponse\" type=\"tns:testResponse\"/>\n" +
                    "<xs:complexType name=\"test\">\n" +
                    "<xs:sequence>\n" +
                    "<xs:element minOccurs=\"0\" name=\"arg0\" type=\"tns:input\"/>\n" +
                    "</xs:sequence>\n" +
                    "</xs:complexType>\n" +
                    "<xs:complexType name=\"input\">\n" +
                    "<xs:sequence>\n" +
                    "<xs:element minOccurs=\"0\" name=\"attribute\" type=\"xs:string\"/>\n" +
                    "</xs:sequence>\n" +
                    "</xs:complexType>\n" +
                    "<xs:complexType name=\"testResponse\">\n" +
                    "<xs:sequence>\n" +
                    "<xs:element minOccurs=\"0\" name=\"return\" type=\"tns:output\"/>\n" +
                    "</xs:sequence>\n" +
                    "</xs:complexType>\n" +
                    "<xs:complexType name=\"output\">\n" +
                    "<xs:sequence>\n" +
                    "<xs:element minOccurs=\"0\" name=\"attribute\" type=\"xs:string\"/>\n" +
                    "</xs:sequence>\n" +
                    "</xs:complexType>\n" +
                    "</xs:schema>\n" +
                    "  </wsdl:types>\n" +
                    "  <wsdl:message name=\"testResponse\">\n" +
                    "    <wsdl:part element=\"tns:testResponse\" name=\"parameters\">\n" +
                    "    </wsdl:part>\n" +
                    "  </wsdl:message>\n" +
                    "  <wsdl:message name=\"test\">\n" +
                    "    <wsdl:part element=\"tns:test\" name=\"parameters\">\n" +
                    "    </wsdl:part>\n" +
                    "  </wsdl:message>\n" +
                    "  <wsdl:portType name=\"MyWsApi\">\n" +
                    "    <wsdl:operation name=\"test\">\n" +
                    "      <wsdl:input message=\"tns:test\" name=\"test\">\n" +
                    "    </wsdl:input>\n" +
                    "      <wsdl:output message=\"tns:testResponse\" name=\"testResponse\">\n" +
                    "    </wsdl:output>\n" +
                    "    </wsdl:operation>\n" +
                    "  </wsdl:portType>\n" +
                    "  <wsdl:binding name=\"MyWebservice12ServiceSoapBinding\" type=\"tns:MyWsApi\">\n" +
                    "    <soap12:binding style=\"document\" transport=\"http://schemas.xmlsoap.org/soap/http\"/>\n" +
                    "    <wsdl:operation name=\"test\">\n" +
                    "      <soap12:operation soapAction=\"\" style=\"document\"/>\n" +
                    "      <wsdl:input name=\"test\">\n" +
                    "        <soap12:body use=\"literal\"/>\n" +
                    "      </wsdl:input>\n" +
                    "      <wsdl:output name=\"testResponse\">\n" +
                    "        <soap12:body use=\"literal\"/>\n" +
                    "      </wsdl:output>\n" +
                    "    </wsdl:operation>\n" +
                    "  </wsdl:binding>\n" +
                    "  <wsdl:service name=\"MyWebservice12Service\">\n" +
                    "    <wsdl:port binding=\"tns:MyWebservice12ServiceSoapBinding\" name=\"MyWebservice12Port\">\n" +
                    "      <soap:address location=\"http://127.0.0.1:4204/test/MyWebservice12\"/>\n" +
                    "    </wsdl:port>\n" +
                    "  </wsdl:service>\n" +
                    "</wsdl:definitions>");
            } else {
                resp.getWriter().write("" +
                    "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">" +
                    "  <soap:Body>" +
                    "    <ns2:testResponse xmlns:ns2=\"http://cxf.server.openejb.apache.org/\">" +
                    "      <return><attribute>ok</attribute></return>" +
                    "    </ns2:testResponse>" +
                    "  </soap:Body>" +
                    "</soap:Envelope>");
            }
            resp.setContentType("application/soap+xml");
        }
    }

    @XmlRootElement
    public static class Output {
        private String attribute;

        public Output(final String v) {
            attribute = v;
        }

        public Output() {
            // no-op
        }

        public String getAttribute() {
            return attribute;
        }

        public void setAttribute(String attribute) {
            this.attribute = attribute;
        }
    }

    @XmlRootElement
    public static class Input {
        private String attribute;

        public Input(final String v) {
            attribute = v;
        }

        public Input() {
            // no-op
        }

        public String getAttribute() {
            return attribute;
        }

        public void setAttribute(String attribute) {
            this.attribute = attribute;
        }
    }
}
