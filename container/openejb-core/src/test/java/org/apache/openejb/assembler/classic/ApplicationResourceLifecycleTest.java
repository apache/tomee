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

import org.apache.openejb.config.sys.Resources;
import org.apache.openejb.testing.ApplicationComposers;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testing.SimpleLog;
import org.junit.Test;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Classes
@SimpleLog
public class ApplicationResourceLifecycleTest {
    @Resource(name = "test")
    private MyResource resource;

    @Module
    public Resources resources() {
        return new Resources() {{
            getResource().add(new org.apache.openejb.config.sys.Resource() {{
                setId("test");
                setClassName(MyResource.class.getName());
            }});
        }};
    }

    @Test
    public void lifecycle() throws Exception {
        new ApplicationComposers(this).evaluate(this, new Runnable() {
            @Override
            public void run() {
                assertTrue(resource.init);
                assertFalse(resource.destroy);
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
