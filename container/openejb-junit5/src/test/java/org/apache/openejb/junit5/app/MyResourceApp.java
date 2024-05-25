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
package org.apache.openejb.junit5.app;

import jakarta.annotation.Resource;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.testing.Application;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;

import java.util.Properties;

@Application
public class MyResourceApp {

    @Module
    @Classes(cdi = true, value = {MyResourceApp.class, MyService.class})
    public EjbJar modules() {
        return new EjbJar();
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("myResource", "new://Resource?class-name=org.apache.openejb.junit5.app.MyResourceApp$MyResource" +
                "&constructor=attr1, attr2");
        p.put("myResource.attr1", "value1");
        p.put("myResource.attr2", "value2");
        return p;
    }

    @Resource(name = "myResource")
    private MyResource resource;

    public MyResource getResource() {
        return resource;
    }

    public static class MyService {
        @Resource(name = "myResource")
        private MyResource resource;

        public MyResource getResource() {
            return resource;
        }
    }
    public static class MyResource {

        private final String attr1;
        private final String attr2;

        public MyResource(String attr1, String attr2) {
            this.attr1 = attr1;
            this.attr2 = attr2;
        }

        public String getAttr1() {
            return attr1;
        }

        public String getAttr2() {
            return attr2;
        }
    }
}
