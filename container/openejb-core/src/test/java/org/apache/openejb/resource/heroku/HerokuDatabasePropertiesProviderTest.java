/**
 *
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
package org.apache.openejb.resource.heroku;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.testng.PropertiesBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HerokuDatabasePropertiesProviderTest {
    @Before
    @After
    public void reset() {
        SystemInstance.reset();
    }

    @Test
    public void herokuToJava() {
        SystemInstance.get().setProperty("DATABASE_URL", "postgres://user:pwd@host.com:5432/db");
        assertEquals(
                new PropertiesBuilder()
                        .p("Password", "pwd")
                        .p("JdbcUrl", "jdbc:postgresql://host.com:5432/db")
                        .p("UserName", "user")
                        .p("JdbcDriver", "org.postgresql.Driver")
                        .build(),
                new HerokuDatabasePropertiesProvider().provides());
    }
}
