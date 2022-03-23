/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
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

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.RandomPort;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@EnableServices("jaxrs")
@Classes(cdi = true, innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
@ContainerProperties(@ContainerProperties.Property(
        name = "cxf.jaxrs.providers",
        value = "org.apache.cxf.jaxrs.provider.json.JSONProvider"
))
public class JettisonCompatTest {
    @RandomPort("http")
    private URL root;

    @Test
    public void run() throws IOException {
        assertEquals("{\"jet\":{\"name\":\"test\"}}", IO.slurp(new URL(root.toExternalForm() + "openejb/jettison")));
    }

    @Path("jettison")
    @ApplicationScoped
    public static class JettisonEndpoint {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Jet get() {
            return new Jet();
        }
    }

    @XmlRootElement
    public static class Jet {
        private String name = "test";

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }
}
