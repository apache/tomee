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

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.assembler.classic.WebAppBuilder;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Component;
import org.apache.openejb.junit.Configuration;
import org.apache.openejb.junit.Module;
import org.apache.openejb.server.cxf.rs.beans.MyRESTApplication;
import org.apache.openejb.server.cxf.rs.beans.RestWithInjections;
import org.apache.openejb.server.cxf.rs.beans.SimpleEJB;
import org.apache.openejb.web.LightweightWebAppBuilder;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class SimpleApplicationTest {
    public static final String BASE_URL = "http://localhost:4204/foo/my-app";

    @Component
    public WebAppBuilder webAppBuilder() {
        return new LightweightWebAppBuilder();
    }

    @Configuration
    public Properties configuration() {
        final Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        return properties;
    }

    @Module
    public WebModule war() {
        final WebModule webModule = new WebModule(new WebApp(), "/foo", Thread.currentThread().getContextClassLoader(), "", "foo");
        webModule.getRestApplications().add(MyRESTApplication.class.getName());
        webModule.setFinder(new AnnotationFinder(new ClassesArchive(RestWithInjections.class, SimpleEJB.class)));
        return webModule;
    }

    @Test
    public void first() {
        String hi = WebClient.create(BASE_URL).path("/first/hi").get(String.class);
        assertEquals("Hi from REST World!", hi);
    }

    @Test
    public void second() {
        String hi = WebClient.create(BASE_URL).path("/second/hi2/2nd").get(String.class);
        assertEquals("hi 2nd", hi);
    }

    @Test
    public void expert() throws Exception {
        Response response = WebClient.create(BASE_URL).path("/expert/still-hi").post("Pink Floyd");
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());

        InputStream is = (InputStream) response.getEntity();
        StringWriter writer = new StringWriter();
        int c;
        while ((c = is.read()) != -1) {
            writer.write(c);
        }
        assertEquals("hi Pink Floyd", writer.toString());
    }

    @Test
    public void nonListed() { // default handler from openejb-http
        assertEquals("", WebClient.create(BASE_URL).path("/non-listed/yata/foo").get(String.class));
    }

    @Test
    public void hooked() {
        assertEquals(true, WebClient.create(BASE_URL).path("/hooked/post").get(Boolean.class));
    }

    @Test
    public void injectEjb() {
        assertEquals(true, WebClient.create(BASE_URL).path("/inject/ejb").get(Boolean.class));
    }
}
