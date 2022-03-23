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

import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class PropertiesProviderTest {
    @EJB
    private CustomPropBean bean;

    @Test
    public void validAliases() {
        assertEquals("r2", bean.getR2().value);
        assertEquals("ok", bean.getR2().noConflict);
        assertEquals("r1", bean.getR1().value);
        assertEquals("ok", bean.getR1().noConflict);
    }

    @Module
    public EnterpriseBean bean() {
        return new SingletonBean(CustomPropBean.class).localBean();
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();

        p.put("r1", "new://Resource?" +
                "class-name=org.apache.openejb.resource.PropertiesProviderTest$MyResource&" +
                "properties-provider=org.apache.openejb.resource.PropertiesProviderTest$PropertiesProvider");
        p.put("r1.value", "will be overriden");
        p.put("r1.noConflict", "ok");

        p.put("r2", "new://Resource?" +
                        "class-name=org.apache.openejb.resource.PropertiesProviderTest$MyResource");
        p.put("r2.value", "r2");
        p.put("r2.noConflict", "ok");

        return p;
    }

    public static class CustomPropBean {
        @Resource(name = "r1")
        private MyResource r1;

        @Resource(name = "r2")
        private MyResource r2;

        public MyResource getR1() {
            return r1;
        }

        public MyResource getR2() {
            return r2;
        }
    }

    public static class MyResource {
        private String value;
        private String noConflict;
    }

    public static class PropertiesProvider {
        private Properties p;

        public void setProperties(final Properties p) {
            this.p = p;
        }

        public Properties provides() {
            return new Properties() {{
                putAll(p);
                setProperty("value", "r1"); // override
            }};
        }
    }
}
