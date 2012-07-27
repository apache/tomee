/**
 *
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
package org.superbiz.servlet;

import javax.xml.ws.Service;
import java.io.PrintStream;
import java.net.URL;

public class WebserviceClient {
    /**
     * Unfortunately, to run this example with CXF you need to have a HUGE class path.  This
     * is just what is required to run CXF:
     * <p/>
     * jaxb-api-2.0.jar
     * jaxb-impl-2.0.3.jar
     * <p/>
     * saaj-api-1.3.jar
     * saaj-impl-1.3.jar
     * <p/>
     * <p/>
     * cxf-api-2.0.2-incubator.jar
     * cxf-common-utilities-2.0.2-incubator.jar
     * cxf-rt-bindings-soap-2.0.2-incubator.jar
     * cxf-rt-core-2.0.2-incubator.jar
     * cxf-rt-databinding-jaxb-2.0.2-incubator.jar
     * cxf-rt-frontend-jaxws-2.0.2-incubator.jar
     * cxf-rt-frontend-simple-2.0.2-incubator.jar
     * cxf-rt-transports-http-jetty-2.0.2-incubator.jar
     * cxf-rt-transports-http-2.0.2-incubator.jar
     * cxf-tools-common-2.0.2-incubator.jar
     * <p/>
     * geronimo-activation_1.1_spec-1.0.jar
     * geronimo-annotation_1.0_spec-1.1.jar
     * geronimo-ejb_3.0_spec-1.0.jar
     * geronimo-jpa_3.0_spec-1.1.jar
     * geronimo-servlet_2.5_spec-1.1.jar
     * geronimo-stax-api_1.0_spec-1.0.jar
     * jaxws-api-2.0.jar
     * axis2-jws-api-1.3.jar
     * <p/>
     * wsdl4j-1.6.1.jar
     * xml-resolver-1.2.jar
     * XmlSchema-1.3.1.jar
     */
    public static void main(String[] args) throws Exception {
        PrintStream out = System.out;

        Service helloPojoService = Service.create(new URL("http://localhost:8080/ejb-examples/hello?wsdl"), null);
        HelloPojo helloPojo = helloPojoService.getPort(HelloPojo.class);
        out.println();
        out.println("Pojo Webservice");
        out.println("    helloPojo.hello(\"Bob\")=" + helloPojo.hello("Bob"));
        out.println("    helloPojo.hello(null)=" + helloPojo.hello(null));
        out.println();

        Service helloEjbService = Service.create(new URL("http://localhost:8080/HelloEjbService?wsdl"), null);
        HelloEjb helloEjb = helloEjbService.getPort(HelloEjb.class);
        out.println();
        out.println("EJB Webservice");
        out.println("    helloEjb.hello(\"Bob\")=" + helloEjb.hello("Bob"));
        out.println("    helloEjb.hello(null)=" + helloEjb.hello(null));
        out.println();
    }
}
