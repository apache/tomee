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
package org.superbiz.servlet;

import javax.jws.HandlerChain;
import javax.jws.WebService;

@WebService(
        portName = "HelloPojoPort",
        serviceName = "HelloPojoService",
        targetNamespace = "http://examples.org/wsdl",
        endpointInterface = "org.superbiz.servlet.HelloPojo"
)
@HandlerChain(file = "server-handlers.xml")
public class HelloPojoService implements HelloPojo {

    public String hello(String name) {
        WebserviceServlet.write("                HelloPojoService hello(" + name + ")");
        if (name == null) {
            name = "World";
        }
        return "Hello " + name + " from Pojo Webservice!";
    }
}
