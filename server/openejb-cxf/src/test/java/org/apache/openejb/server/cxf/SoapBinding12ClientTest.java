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

import jakarta.ejb.Singleton;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.jws.WebService;
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
        return new WebApp().contextRoot("/test").addServlet("ws", MyWebservice12.class.getName(), "MyWebservice12");
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

    @WebService
    @Singleton
    @BindingType(SOAPBinding.SOAP12HTTP_BINDING)
    public static class MyWebservice12 implements MyWsApi {

        public Output test(final Input in) {
            return new Output(in.getAttribute());
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
