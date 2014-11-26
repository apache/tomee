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

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.ApplicationConfiguration;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.Singleton;
import javax.jws.WebService;
import javax.xml.ws.WebServiceRef;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@EnableServices("jax-ws")
@RunWith(ApplicationComposer.class)
public class WebServiceInjectionTest {
    @Module
    @Classes(innerClassesAsBean = true)
    public WebApp module() {
        return new WebApp();
    }

    @WebServiceRef
    private MyWsApi api;

    @Test
    public void checkInjection() {
        // assertEquals("ok", api.test()); // local call so skip it but check config which is actually the only interesting thing
        final Client client = ClientProxy.getClient(api);
        assertNotNull(client);
        assertEquals(1, client.getOutInterceptors().size());
        assertTrue(LoggingOutInterceptor.class.isInstance(client.getOutInterceptors().iterator().next()));
    }

    @ApplicationConfiguration
    public Properties props() {
        // return new PropertiesBuilder().p("cxf.jaxws.client.out-interceptors", LoggingOutInterceptor.class.getName()).build();
        // return new PropertiesBuilder().p("cxf.jaxws.client.{http://cxf.server.openejb.apache.org/}MyWebservicePort.out-interceptors", LoggingOutInterceptor.class.getName()).build();
        return new PropertiesBuilder()
                .p("cxf.jaxws.client.{http://cxf.server.openejb.apache.org/}MyWebservicePort.out-interceptors", "loo")
                .p("loo", "new://Service?class-name=" + LoggingOutInterceptor.class.getName())
                .build();
    }

    @WebService
    public static interface MyWsApi {
        String test();
    }

    @WebService
    @Singleton
    public static class MyWebservice implements MyWsApi {
        @Override
        public String test() {
            return "ok";
        }
    }
}
