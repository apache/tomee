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
package org.apache.openejb.assembler.classic;

import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.SimpleLog;
import org.hsqldb.jdbc.pool.JDBCXADataSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.sql.DataSourceDefinition;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SimpleLog
@Classes(cdi = true, innerClassesAsBean = true)
@RunWith(ApplicationComposer.class)
public class XADataSourceDefinitionTest {
    @DataSourceDefinition(
        name = "java:app/xads",
        className = "org.hsqldb.jdbc.pool.JDBCXADataSource",
        url = "jdbc:hsqldb:mem:XADataSourceDefinitionTest",
        user = "sa"
    )
    public static class Define {
    }

    @DataSourceDefinition(
        name = "java:app/xads2",
        className = "org.hsqldb.jdbc.pool.JDBCXADataSource",
        url = "jdbc:hsqldb:mem:XADataSourceDefinitionTest2",
        user = "sa",
        transactional = false
    )
    public static class DefineNonJta {
    }

    @Test
    public void jta() throws Exception {
        final Object lookup = new InitialContext().lookup("java:app/xads");
        assertTrue(DataSource.class.isInstance(lookup)); // jta so back to a ds

        final DataSource ds = DataSource.class.cast(lookup);
        try (final Connection c = ds.getConnection()) {
            assertEquals("jdbc:hsqldb:mem:XADataSourceDefinitionTest", c.getMetaData().getURL());
        }
    }

    @Test
    public void nonJta() throws Exception {
        assertTrue(JDBCXADataSource.class.isInstance(new InitialContext().lookup("java:app/xads2")));
    }
}
