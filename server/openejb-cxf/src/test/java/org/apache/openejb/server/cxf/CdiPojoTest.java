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

import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;
import jakarta.jws.WebService;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@EnableServices("jax-ws")
@RunWith(ApplicationComposer.class)
public class CdiPojoTest {

    private static int port = -1;

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
    }

    @Configuration
    public Properties props() {
        return new PropertiesBuilder()
            .p("httpejbd.port", Integer.toString(port))
            .p(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true")
            .build();
    }

    @Module
    @Classes(value = {MyWebservice.class, ACdiTaste.class}, cdi = true)
    public WebApp module() {
        return new WebApp().contextRoot("/test").addServlet("ws", MyWebservice.class.getName(), "/ws");
    }

    @Test
    public void checkInjection() throws MalformedURLException {
        final MyWsApi api = Service.create(new URL("http://localhost:" + port + "/test/ws?wsdl"),
            new QName("http://cxf.server.openejb.apache.org/", "MyWebserviceService"))
            .getPort(MyWsApi.class);
        assertEquals("ok", api.test());
    }

    public static class ACdiTaste {
        public String ok() {
            return "ok";
        }
    }

    @WebService
    public static interface MyWsApi {
        String test();
    }

    @WebService
    public static class MyWebservice implements MyWsApi {
        @Inject
        private ACdiTaste cdi;

        @Override
        public String test() {
            if (cdi == null) {
                return "cdi injection failed";
            }
            return cdi.ok();
        }
    }
}
