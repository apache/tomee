/*
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
package org.apache.openejb.arquillian.tests.jaxws.basicauth;

import jakarta.ejb.Singleton;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

/**
 * A singleton EJB exposing a business method over SOAP. Packaged in a WAR (EJB-in-WAR),
 * so its web service is registered by TomcatWsRegistry#addWsContainer via the addServlet
 * route (the web application context already exists), rather than deployInFakeWebapp().
 */
@Singleton
@WebService(name = "Greeter",
            targetNamespace = "http://basicauth.jaxws.tests.arquillian.openejb.apache.org/",
            serviceName = "GreeterService",
            portName = "GreeterPort")
public class GreeterBean implements GreeterWs {

    @Override
    @WebMethod
    public String greet(@WebParam(name = "name") final String name) {
        return "Hello, " + name;
    }
}
