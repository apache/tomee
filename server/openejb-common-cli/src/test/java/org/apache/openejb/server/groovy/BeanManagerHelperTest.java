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
package org.apache.openejb.server.groovy;

import org.apache.openejb.jee.Beans;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.util.OpenEJBScripter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Named;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class BeanManagerHelperTest {
    private static OpenEJBScripter.BeanManagerHelper helper = new OpenEJBScripter.BeanManagerHelper();

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.setProperty("openejb.deployments.classpath.filter.systemapps", "false");
        return p;
    }

    @Module
    public Beans ejbJar() {
        final Beans beans = new Beans();
        beans.addManagedClass(Pojo.class);
        return beans;
    }

    @Before
    public void resetID() {
        Pojo.ID = 5;
    }

    @Test
    public void getInstanceFromClass() {
        for (int i = 1; i < 5; i++) {
            final Pojo pojo = (Pojo) helper.beanFromClass("BeanManagerHelperTest", Pojo.class.getName());
            assertEquals(5 + i, pojo.id);
        }
    }

    @Test
    public void getInstanceFromName() {
        for (int i = 1; i < 5; i++) {
            final Pojo pojo = (Pojo) helper.beanFromName("BeanManagerHelperTest", "pojo");
            assertEquals(5 + i, pojo.id);
        }
    }

    @Named
    public static class Pojo {
        public static int ID;
        public int id = ++ID;
    }
}
