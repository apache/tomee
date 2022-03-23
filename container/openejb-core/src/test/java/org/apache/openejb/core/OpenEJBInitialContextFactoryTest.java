/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.ejb.Singleton;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
@Classes(innerClassesAsBean = true)
@ContainerProperties(@ContainerProperties.Property(name = "db", value = "new://Resource?type=DataSource"))
public class OpenEJBInitialContextFactoryTest {
    @Test
    public void run() throws Exception {
        final Callable<Boolean> innerTest = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                final Context ctx = new InitialContext(new PropertiesBuilder()
                        .p(Context.INITIAL_CONTEXT_FACTORY, OpenEJBInitialContextFactory.class.getName())
                        .build());

                // ejbs
                assertEquals("ejb", SomeEjb.class.cast(ctx.lookup("java:global/openejb/SomeEjb")).from());
                assertEquals("ejb", SomeEjb.class.cast(ctx.lookup("global/openejb/SomeEjb")).from());
                assertEquals("ejb", SomeEjb.class.cast(ctx.lookup("java:openejb/local/SomeEjbLocalBean")).from());
                assertEquals("ejb", SomeEjb.class.cast(ctx.lookup("openejb/local/SomeEjbLocalBean")).from());
                assertEquals("ejb", SomeEjb.class.cast(ctx.lookup("openejb:local/SomeEjbLocalBean")).from());
                assertEquals("ejb", SomeEjb.class.cast(ctx.lookup("SomeEjbLocalBean")).from());

                // resources (hibernate use case for instance)
                assertTrue(DataSource.class.isInstance(ctx.lookup("openejb:Resource/db")));
                assertTrue(DataSource.class.isInstance(ctx.lookup("java:openejb/Resource/db")));
                assertTrue(DataSource.class.isInstance(ctx.lookup("openejb/Resource/db")));
                assertTrue(DataSource.class.isInstance(ctx.lookup("db")));
                return true;
            }
        };

        // in an unmanaged thread
        final AtomicBoolean result = new AtomicBoolean(false);
        final Thread outOfContext = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    result.set(innerTest.call());
                } catch (final Exception e) {
                    result.set(false);
                }
            }
        });
        outOfContext.start();
        outOfContext.join();
        assertTrue(result.get());

        // and in a managed thread
        assertTrue(innerTest.call());
    }

    @Singleton
    public static class SomeEjb {
        public String from() {
            return "ejb";
        }
    }
}
