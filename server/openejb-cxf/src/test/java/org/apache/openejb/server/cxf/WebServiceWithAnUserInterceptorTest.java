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
import org.apache.openejb.config.DeploymentFilterable;
import org.apache.openejb.util.NetworkUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import jakarta.ejb.Stateless;
import jakarta.ejb.embeddable.EJBContainer;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.InvocationContext;
import jakarta.jws.WebService;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import java.net.URL;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WebServiceWithAnUserInterceptorTest {
    private static EJBContainer container;
    private static int port = -1;

    @BeforeClass
    public static void start() {
        port = NetworkUtil.getNextAvailablePort();
        final Properties properties = new Properties();
        properties.setProperty(DeploymentFilterable.CLASSPATH_INCLUDE, ".*openejb-cxf.*");
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        properties.setProperty("httpejbd.port", Integer.toString(port));
        container = EJBContainer.createEJBContainer(properties);
    }

    @AfterClass
    public static void close() {
        container.close();
    }

    @Test
    public void test() throws Exception {
        Foo foo = Service.create(
            new URL("http://localhost:" + port + "/openejb-cxf/FooImpl?wsdl"),
            new QName("http://cxf.server.openejb.apache.org/", "FooImplService"))
            .getPort(Foo.class);
        assertNotNull(foo);
        assertEquals("bar", foo.hi());
    }

    @WebService
    @Stateless
    @Interceptors({BarInterceptor.class})
    public static class FooImpl implements Foo {
        public String hi() {
            return "foo";
        }
    }

    @WebService(portName = "FooImplPort")
    public static interface Foo {
        String hi();
    }

    public static class BarInterceptor {
        @AroundInvoke
        public Object around(InvocationContext ic) throws Exception {
            return "bar";
        }
    }
}
