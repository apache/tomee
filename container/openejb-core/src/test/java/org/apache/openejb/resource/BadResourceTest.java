/*
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
package org.apache.openejb.resource;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.SimpleLog;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;

@SimpleLog
@RunWith(ApplicationComposer.class)
public class BadResourceTest {

    @Configuration
    public Properties configuration() {
        
        return new PropertiesBuilder()
                .p("Resource1", "new://Resource?class-name=org.apache.openejb.resource.BadResourceTest$MyFactory&factory-name=create")
                .p("Resource1.name", "Resource1")
                .p("Resource1.Lazy", "true")
                .p("Resource2", "new://Resource?class-name=org.apache.openejb.resource.BadResourceTest$MyFactory&factory-name=create")
                .p("Resource2.name", "Resource2")
                .p("Resource2.Lazy", "true")
                .build();
    }

    @Module
    public WebApp webApp() {
        return new WebApp();
    }

    private static final Collection<Runnable> POST_CONTAINER_VALIDATIONS = new LinkedList<Runnable>();

    @AfterClass
    public static void lastValidations() { // late to make the test failing (ie junit report will be broken) but better than destroying eagerly the resource
        for (final Runnable runnable : POST_CONTAINER_VALIDATIONS) {
            runnable.run();
        }
        POST_CONTAINER_VALIDATIONS.clear();
    }

    @Test
    public void ensureCleanup() {
        POST_CONTAINER_VALIDATIONS.add(new Runnable() {
            @Override
            public void run() {
                check("openejb/Resource/Resource1");
                check("openejb/Resource/Resource2");
            }

            public void check(final String jndiName) {
                try {
                    InitialContext ctx = new InitialContext();
                    ctx.lookup(jndiName);
                    Assert.fail("Resource not cleaned up");
                } catch (NameNotFoundException e) {
                    // ignore, we expect this exception
                } catch (Throwable e) {
                    Assert.fail(e.getMessage());
                }
            }
        });
    }
    
    public static class MyResource {
        private String name;

        public MyResource(String name) {
            super();
            this.name = name;
        }

        public MyResource() {
            super();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class MyFactory {
        private Properties properties;
        
        public Object create() {
            final String name = (String) properties.remove("name");
            
            if (name.equals("Resource1")) {
                throw new RuntimeException("Not creating this resource");
            }
            
            return new MyResource(name);
        }
    }
}
