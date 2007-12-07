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

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceRef;
import javax.jws.HandlerChain;
import java.io.IOException;

public class WebserviceServlet extends HttpServlet {

    @WebServiceRef
    @HandlerChain(file = "client-handlers.xml")
    private HelloPojo helloPojo;

    @WebServiceRef
    @HandlerChain(file = "client-handlers.xml")
    private HelloEjb helloEjb;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        ServletOutputStream out = response.getOutputStream();

        OUT = out;
        try {
            out.println("Pojo Webservice");
            out.println("    helloPojo.hello(\"Bob\")=" + helloPojo.hello("Bob"));
            out.println();
            out.println("    helloPojo.hello(null)=" + helloPojo.hello(null));
            out.println();
            out.println("EJB Webservice");
            out.println("    helloEjb.hello(\"Bob\")=" + helloEjb.hello("Bob"));
            out.println();
            out.println("    helloEjb.hello(null)=" + helloEjb.hello(null));
            out.println();
        } finally {
            OUT = out;
        }
    }

    private static ServletOutputStream OUT;
    public static void write(String message) {
        try {
            ServletOutputStream out = OUT;
            out.println(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
