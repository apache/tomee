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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.resource.jdbc.driver;

import org.hsqldb.jdbcDriver;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Driver;
import java.sql.DriverManager;

/**
 * @version $Rev$ $Date$
 */
public class AlternativeDriverTest extends Assert {

    @Test
    public void testConnect() throws Exception {
        final AlternativeDriver orange = new AlternativeDriver(new jdbcDriver(), "jdbc:hsqldb:mem:orange");

        final Driver general = DriverManager.getDriver(orange.getUrl());

        assertNotSame(general, orange);

        orange.register();

        assertSame(orange, DriverManager.getDriver(orange.getUrl()));
        assertSame(general, DriverManager.getDriver(orange.getUrl() + "foo"));

        final AlternativeDriver green = new AlternativeDriver(new jdbcDriver(), "jdbc:hsqldb:mem:green");
        green.register();

        assertSame(orange, DriverManager.getDriver(orange.getUrl()));
        assertSame(green, DriverManager.getDriver(green.getUrl()));
        assertSame(general, DriverManager.getDriver(orange.getUrl() + "foo"));
        assertSame(general, DriverManager.getDriver(green.getUrl() + "bar"));

        final AlternativeDriver green2 = new AlternativeDriver(new jdbcDriver(), "jdbc:hsqldb:mem:green");
        green2.register();

        assertSame(orange, DriverManager.getDriver(orange.getUrl()));
        assertSame(green2, DriverManager.getDriver(green.getUrl()));
        assertSame(general, DriverManager.getDriver(orange.getUrl() + "foo"));
        assertSame(general, DriverManager.getDriver(green.getUrl() + "bar"));

        green2.deregister();

        assertSame(orange, DriverManager.getDriver(orange.getUrl()));
        assertSame(green, DriverManager.getDriver(green.getUrl()));
        assertSame(general, DriverManager.getDriver(orange.getUrl() + "foo"));
        assertSame(general, DriverManager.getDriver(green.getUrl() + "bar"));
    }

}
