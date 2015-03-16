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
package org.apache.openejb.resource.jdbc;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DataSourceFactoryTest {
    private static Method mtd;

    @BeforeClass
    public static void findMethod() throws NoSuchMethodException {
        mtd = DataSourceFactory.class.getDeclaredMethod("usePool", Properties.class);
        mtd.setAccessible(true);
    }

    @After
    @Before
    public void reset() {
        SystemInstance.reset();
    }

    @Test
    public void run() throws Exception {
        assertFalse(Boolean.class.cast(mtd.invoke(null, new Properties())));
        assertTrue(Boolean.class.cast(mtd.invoke(null, new PropertiesBuilder().p("openejb.datasource.pool", "true").build())));
        assertFalse(Boolean.class.cast(mtd.invoke(null, new PropertiesBuilder().p("openejb.datasource.pool", "false").build())));
        assertTrue(Boolean.class.cast(mtd.invoke(null, new PropertiesBuilder().p("initialPoolSize", "1").build())));
        assertTrue(Boolean.class.cast(mtd.invoke(null, new PropertiesBuilder().p("maxPoolSize", "1").build())));
        SystemInstance.get().setProperty("openejb.datasource.pool", "true");
        assertTrue(Boolean.class.cast(mtd.invoke(null, new Properties())));
        SystemInstance.get().setProperty("openejb.datasource.pool", "false");
        assertFalse(Boolean.class.cast(mtd.invoke(null, new Properties())));
    }
}
