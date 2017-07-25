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
package org.apache.openejb.assembler.classic;

import org.apache.openejb.config.EjbModule;
import org.apache.openejb.config.sys.Resources;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.testing.ApplicationComposers;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SimpleLog
public class ApplicationResourceLifecycleTest {
    @Resource(name = "test")
    private MyResource resource;

    @Classes({MyResource.class})
    @Module
    public EjbModule resources() {

        final EjbModule ejbModule = new EjbModule(new EjbJar());
        ejbModule.initResources(new Resources() {{
            getResource().add(new org.apache.openejb.config.sys.Resource() {{
                setId("test");
                setClassName(MyResource.class.getName());
            }});
        }});

        return ejbModule;
    }

    @Test
    public void lifecycle() throws Exception {
        new ApplicationComposers(this).evaluate(this, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                assertTrue(resource.init);
                assertFalse(resource.destroy);
                return null;
            }
        });
        assertTrue(resource.init);
        assertTrue(resource.destroy);
    }

    public static class MyResource {
        private boolean init;
        private boolean destroy;

        @PostConstruct
        private void init() {
            init = true;
        }

        @PreDestroy
        private void destroy() {
            destroy = true;
        }

        public boolean isInit() {
            return init;
        }

        public boolean isDestroy() {
            return destroy;
        }
    }
}
