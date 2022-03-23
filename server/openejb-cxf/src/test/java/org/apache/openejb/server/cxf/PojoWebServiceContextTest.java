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
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.jws.WebService;
import jakarta.xml.ws.WebServiceContext;

import static org.junit.Assert.assertTrue;

@EnableServices("jax-ws")
@RunWith(ApplicationComposer.class)
public class PojoWebServiceContextTest {
    @Module
    public WebApp module() {
        return new WebApp().contextRoot("/test").addServlet("ws", MyWebservice.class.getName(), "/ws");
    }

    @Test
    public void checkInjection() {
        assertTrue(MyWebservice.ok);
    }

    @WebService
    public static interface MyWsApi {
        String test();
    }

    @WebService
    public static class MyWebservice implements MyWsApi {
        private static boolean ok = false;

        @Resource
        private WebServiceContext ctx;

        @PostConstruct
        public void check() {
            ok = ctx != null;
        }

        @Override
        public String test() {
            return "ok";
        }
    }
}
