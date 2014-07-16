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

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.server.cxf.handler.SimpleHandler;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

@EnableServices("jax-ws")
@RunWith(ApplicationComposer.class)
public class CdiHandlersTest {
    @Module
    @Classes(value = { MyHandledWebservice.class, ACdiSimpleTaste.class, SimpleHandler.class }, cdi = true)
    public WebApp module() {
        return new WebApp().contextRoot("/test").addServlet("ws", MyHandledWebservice.class.getName(), "/ws");
    }

    @Test
    public void checkHandlersAreCDIBeans() throws MalformedURLException {
        SimpleHandler.reset();
        Service.create(new URL("http://localhost:4204/test/ws?wsdl"),
                       new QName("http://cxf.server.openejb.apache.org/", "MyHandledWebserviceService"))
                .getPort(MyHandledWsApi.class).test();
        assertTrue(SimpleHandler.close);
        assertTrue(SimpleHandler.handled);
        assertTrue(SimpleHandler.pre);
        assertTrue(SimpleHandler.post);
    }
 
    public static class ACdiSimpleTaste {
        public String ok() {
            return "ok";
        }
    }

    @WebService
    public static interface MyHandledWsApi {
        String test();
    }

    @WebService
    @HandlerChain(file = "/handlers.xml")
    public static class MyHandledWebservice implements MyHandledWsApi {
        @Override
        public String test() {
            return "ok";
        }
    }
}

