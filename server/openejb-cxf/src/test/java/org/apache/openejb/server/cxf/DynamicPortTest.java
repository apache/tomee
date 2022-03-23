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
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.jws.WebService;
import jakarta.xml.ws.WebServiceRef;
import java.net.MalformedURLException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@EnableServices("jax-ws")
@RunWith(ApplicationComposer.class)
public class DynamicPortTest {
    @Configuration
    public Properties config() {
        return new PropertiesBuilder().property("httpejbd.port", "0").build();
    }

    @Module
    @Classes(value = DynamicImpl.class)
    public WebApp module() {
        return new WebApp().contextRoot("/test").addServlet("ws", DynamicImpl.class.getName(), "/ws");
    }

    @WebServiceRef
    private Dynamic client;

    @Test
    public void checkHandlersAreCDIBeans() throws MalformedURLException {
        assertEquals("ok", client.test());
    }

    @WebService
    public static interface Dynamic {
        String test();
    }

    @WebService
    public static class DynamicImpl implements Dynamic {
        @Override
        public String test() {
            return "ok";
        }
    }
}

