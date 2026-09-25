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
package org.apache.openejb.arquillian.tests.jaxrs.routing;

import org.apache.openejb.loader.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.Singleton;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class OptionsTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive service() {
        return ShrinkWrap.create(WebArchive.class, "OptionsTest.war")
            .addClass(OptionsTest.class);
    }

    @Test
    public void check() throws Exception {
        final HttpURLConnection conn = HttpURLConnection.class.cast(new URL(base.toExternalForm() + "options").openConnection());
        conn.setRequestMethod("OPTIONS");
        assertEquals("ok", IO.slurp(conn.getInputStream()));
        conn.getInputStream().close();
    }

    @Singleton
    @Path("options")
    public static class OptionsBean {
        @OPTIONS
        public String providers() {
            return "ok";
        }

    }
}
