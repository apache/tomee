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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.example.jaxws;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;

import static java.lang.Integer.toString;
import static org.junit.Assert.assertTrue;

import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

@EnableServices("jax-ws")
@RunWith(ApplicationComposer.class)
public class MeetingPlannerTest {
    private static final int JAX_WS_PORT = NetworkUtil.getNextAvailablePort();

    @Configuration
    public Properties configuration() {
        return new PropertiesBuilder().p("httpejbd.port", Integer.toString(JAX_WS_PORT)).build();
    }
    @Module
    @Classes(cdi = true, value = { MeetingPlannerImpl.class, LazyAgenda.class })
    public WebApp war() {
        return new WebApp()
                .contextRoot("/demo")
                .addServlet("jaxws", MeetingPlannerImpl.class.getName(), "/meeting-planner");
    }

    @Test
    public void book() throws MalformedURLException {
        final Service service = Service.create(
                new URL("http://127.0.0.1:" + JAX_WS_PORT + "/demo/meeting-planner?wsdl"),
                new QName("http://jaxws.example.superbiz.org/", "MeetingPlannerImplService"));
        final MeetingPlanner planner = service.getPort(MeetingPlanner.class);
        assertTrue(planner.book(new Date(System.currentTimeMillis() + 1000000)));
    }
}
