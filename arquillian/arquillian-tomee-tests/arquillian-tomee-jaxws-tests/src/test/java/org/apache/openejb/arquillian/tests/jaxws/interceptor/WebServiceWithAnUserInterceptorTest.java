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

package org.apache.openejb.arquillian.tests.jaxws.interceptor;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.Stateless;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptors;
import jakarta.interceptor.InvocationContext;
import jakarta.jws.WebService;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class WebServiceWithAnUserInterceptorTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "openejb-cxf.war")
                .addClasses(WebServiceWithAnUserInterceptorTest.class, FooImpl.class, Foo.class, BarInterceptor.class);
    }

    @Test
    public void test() throws Exception {
        Foo foo = Service.create(
            new URL(base.toExternalForm() + "webservices/FooImpl?wsdl"),
            new QName("http://interceptor.jaxws.tests.arquillian.openejb.apache.org/", "FooImplService"))
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
