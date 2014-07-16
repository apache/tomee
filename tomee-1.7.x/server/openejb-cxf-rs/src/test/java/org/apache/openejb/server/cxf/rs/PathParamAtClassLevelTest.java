/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.config.DeploymentFilterable;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.Stateless;
import javax.ejb.embeddable.EJBContainer;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class PathParamAtClassLevelTest {
    private static EJBContainer container;

    @BeforeClass
    public static void start() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(DeploymentFilterable.CLASSPATH_INCLUDE, ".*openejb-cxf-rs.*");
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        container = EJBContainer.createEJBContainer(properties);
    }

    @AfterClass
    public static void close() throws Exception {
        if (container != null) {
            container.close();
        }
    }

    @Test
    public void rest() {
        String response = WebClient.create("http://localhost:4204/openejb-cxf-rs").path("/match/openejb/test/normal").get(String.class);
        assertEquals("openejb", response);
    }

    @Stateless
    @Path("/match/{name}/test")
    public static class DoesItMatchWithPathParamAtClassLevel {
        @Path("/normal")
        @GET
        public String normal(@PathParam("name") String name) {
            return name;
        }
    }
}
