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
package org.apache.openejb.assembler.classic;

import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.naming.NamingException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class LazyResourceTest {
    @Test
    public void lazy() throws NamingException {
        assertEquals(1, MyResource1.count);
        assertEquals(0, MyResource2.count);
        assertTrue(MyResource2.class.isInstance(
                SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext().lookup("openejb/Resource/r2")));
        assertEquals(1, MyResource2.count);
    }

    @Module
    public AppModule application() {
        final EjbModule ejbModule = new EjbModule(new EjbJar());

        final AppModule appModule = new AppModule(Thread.currentThread().getContextClassLoader(), null);
        appModule.getEjbModules().add(ejbModule);

        return appModule;
    }

    @Configuration
    public Properties config() {
        return new PropertiesBuilder()
            .p("r1", "new://Resource?class-name=org.apache.openejb.assembler.classic.LazyResourceTest$MyResource1")
            .p("r2", "new://Resource?class-name=org.apache.openejb.assembler.classic.LazyResourceTest$MyResource2")
            .p("r2.Lazy", "true")
            .build();
    }

    public static class MyResource1 {
        public static volatile int count = 0;

        public MyResource1() {
            count++;
        }
    }

    public static class MyResource2 {
        public static volatile int count = 0;

        public MyResource2() {
            count++;
        }
    }
}
