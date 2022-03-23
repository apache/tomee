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
package jug.rest;

import jug.routing.DataSourceInitializer;
import jug.routing.PollingRouter;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.loader.IO;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.annotation.Resource;
import jakarta.ejb.embeddable.EJBContainer;
import jakarta.inject.Inject;
import javax.naming.NamingException;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class SubjectServiceTest {

    private static EJBContainer container;

    @Inject
    private DataSourceInitializer init;

    @Resource(name = "ClientRouter", type = PollingRouter.class)
    private PollingRouter router;

    @BeforeClass
    public static void start() {
        final Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        properties.setProperty(EJBContainer.APP_NAME, "polling/api");
        properties.setProperty(EJBContainer.PROVIDER, "openejb");
        container = EJBContainer.createEJBContainer(properties);
    }

    @Before
    public void inject() throws NamingException {
        container.getContext().bind("inject", this);
        init.init();
    }

    @AfterClass
    public static void stop() {
        container.close();
    }

    @Test
    public void createVote() throws IOException {
        final Response response = WebClient.create("http://localhost:4204/polling/")
                .path("api/subject/create")
                .accept("application/json")
                .query("name", "TOMEE_JUG_JSON")
                .post("was it cool?");
        final String output = IO.slurp((InputStream) response.getEntity());
        assertTrue("output doesn't contain TOMEE_JUG_JSON '" + output + "'", output.contains("TOMEE_JUG_JSON"));
    }
}
