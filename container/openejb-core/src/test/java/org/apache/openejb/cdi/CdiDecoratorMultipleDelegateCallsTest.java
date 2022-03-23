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
package org.apache.openejb.cdi;

import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.Beans;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class CdiDecoratorMultipleDelegateCallsTest {
    //@Local
    public static interface Service {
        String touch();
    }

    //@Stateless
    @Named("service")
    public static class ServiceImpl implements Service {
        public String touch() {
            return getClass().getName();
        }

    }

    @Decorator
    public static class ServiceDecorator implements Service {

        @Inject
        @Delegate
        private Service service;

        public String touch() { // org.apache.webbeans.decorator.DelegateHandler uses ejbContext.proceed() so that's not possible
            service.touch();
            return service.touch();
        }

    }

    @Module
    @Classes({ServiceImpl.class})
    public EjbModule classes() {
        final Beans beans = new Beans();
        beans.addDecorator(ServiceDecorator.class);

        final EjbModule jar = new EjbModule(new EjbJar());
        jar.setBeans(beans);
        return jar;
    }

    @Inject
    private Service service;

    @Test
    //@Ignore("currently broken")
    public void check() {
        assertTrue(service.touch().startsWith("org.apache.openejb.cdi.CdiDecoratorMultipleDelegateCallsTest$ServiceImpl"));
    }
}
