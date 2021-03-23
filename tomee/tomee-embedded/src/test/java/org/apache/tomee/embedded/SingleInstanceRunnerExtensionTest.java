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
package org.apache.tomee.embedded;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.testing.Application;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.ContainerProperties;
import org.apache.openejb.testing.RandomPort;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.tomee.embedded.event.TomEEEmbeddedApplicationRunnerInjection;
import org.apache.tomee.embedded.junit.jupiter.RunWithTomEEEmbedded;
import org.apache.tomee.embedded.junit.jupiter.TomEEEmbeddedExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Properties;

import static org.junit.Assert.*;

// just a manual test to check it works, can't be executed with the rest of the suite,
// we could use a different surefire execution if we want to add it to the default run
//-Dtomee.application-composer.application=org.apache.tomee.embedded.SingleInstanceRunnerExtensionTest$TheApp
@RunWithTomEEEmbedded
public class SingleInstanceRunnerExtensionTest {
    @Application // app can have several injections/helpers
    private TheApp app;

    @RandomPort("http") // @RandomPort are propagated by value + type only (both need to match ATM)
    private int port;

    @Test
    public void run() {
        assertNotNull(SystemInstance.get().getComponent(Assembler.class));
        assertEquals("val", SystemInstance.get().getProperty("simple"));
        assertEquals("set", SystemInstance.get().getProperty("t"));
        assertEquals("p", SystemInstance.get().getProperty("prog"));
        assertEquals("128463", SystemInstance.get().getProperty("my.server.port"));
        assertEquals("true", SystemInstance.get().getProperty("configurer"));
        assertNotEquals(8080, app.port);
        assertTrue(app.base.toExternalForm().endsWith("/app"));
        assertEquals(app.port, port);
        assertNotNull(app.task);
        assertNotNull(app.tasks);
        assertEquals(1, app.tasks.size());
        assertEquals(app.task, app.tasks.iterator().next());
        assertEquals(app.task, MyTask.instance);
        assertNotNull(app.custom);
    }

    @Application
    @Classes(context = "app")
    @ContainerProperties({
            @ContainerProperties.Property(name = "simple", value = "val"),
            @ContainerProperties.Property(name = "tomee.embedded.application.runner.properties.t", value = "${t.value}"),
            @ContainerProperties.Property(name = "tomee.embedded.application.runner.t.value", value = "set")
    })
    @TomEEEmbeddedApplicationRunner.LifecycleTasks(MyTask.class)
    // can start a ftp/sftp/elasticsearch/mongo/... server before tomee
    @TomEEEmbeddedApplicationRunner.Configurers(SetMyProperty.class)
    public static class TheApp {
        @RandomPort("http")
        private int port;

        @RandomPort("http")
        private URL base;

        @TomEEEmbeddedApplicationRunner.LifecycleTask
        private MyTask task;

        @TomEEEmbeddedApplicationRunner.LifecycleTask
        private Collection<LifecycleTask> tasks;

        @org.apache.openejb.testing.Configuration
        public Properties add() {
            return new PropertiesBuilder().p("prog", "p").build();
        }

        private Custom custom;

        public void doInject(@Observes final TomEEEmbeddedApplicationRunnerInjection injector) {
            injector.inject(Custom.class, new Custom())
                    .inject(NotHere.class, new NotHere());
        }
    }

    public static class NotHere {
    }

    public static class Custom {
    }

    public static class MyTask implements LifecycleTask {
        private static MyTask instance;

        @Override
        public Closeable beforeContainerStartup() {
            instance = this;
            System.out.println(">>> start");
            System.setProperty("my.server.port", "128463");
            return new Closeable() {
                @Override
                public void close() throws IOException {
                    System.out.println(">>> close");
                }
            };
        }
    }

    public static class SetMyProperty implements TomEEEmbeddedApplicationRunner.Configurer {
        @Override
        public void configure(final Configuration configuration) {
            configuration.property("configurer", "true");
        }
    }
}
