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
package org.apache.openejb.arquillian.tests.jaxrs.application;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.openejb.arquillian.tests.jaxrs.beans.MyFirstRestClass;
import org.apache.openejb.arquillian.tests.jaxrs.beans.MySecondRestClass;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class MultipleApplicationsTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "MultipleApplicationsTest.war")
            .addClasses(Application1.class, Application2.class, MyFirstRestClass.class, MySecondRestClass.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void app1() {
        assertEquals("Hi from REST World!", WebClient.create(base.toExternalForm()).path("app1/first/hi").get(String.class));
    }

    @Test
    public void app2() {
        assertEquals("hi bar", WebClient.create(base.toExternalForm()).path("app2/second/hi2/bar").get(String.class));
    }

    @ApplicationPath("app1")
    public static class Application1 extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            return Collections.<Class<?>>singleton(MyFirstRestClass.class);
        }
    }

    @ApplicationPath("app2")
    public static class Application2 extends Application {
        @Override
        public Set<Class<?>> getClasses() {
            return Collections.<Class<?>>singleton(MySecondRestClass.class);
        }
    }
}
