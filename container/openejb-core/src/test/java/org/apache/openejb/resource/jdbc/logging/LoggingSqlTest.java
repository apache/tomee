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
package org.apache.openejb.resource.jdbc.logging;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.log.LoggerCreator;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.JuliLogStream;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.reflection.Reflections;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import javax.annotation.Resource;
import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(ApplicationComposer.class)
public class LoggingSqlTest {
    @Resource
    private DataSource ds;

    @Module
    public WebApp war() {
        return new WebApp();
    }

    @Configuration
    public Properties config() {
        return new PropertiesBuilder()
                .p("openejb.log.async", "false")
                .p("db", "new://Resource?type=DataSource")
                .p("db.LogSql", "true")
                .p("db.LogSqlPackages", "org.apache.openejb.resource.jdbc.logging.LoggingSqlTest")
                .build();
    }

    @Test
    public void checkOutput() throws Exception {
        final Logger logger = Logger.getInstance(LogCategory.OPENEJB_SQL, LoggingPreparedSqlStatement.class);
        final JuliLogStream stream = JuliLogStream.class.cast(Reflections.get(logger, "logStream"));
        final LoggerCreator julCreator = LoggerCreator.class.cast(Reflections.get(stream, "logger"));
        final java.util.logging.Logger actualLogger = julCreator.call();
        final Collection<String> msgs = new LinkedList<String>();
        final Handler handler = new Handler() {
            @Override
            public void publish(final LogRecord record) {
                msgs.add(record.getMessage());
            }

            @Override
            public void flush() {
                // no-op
            }

            @Override
            public void close() throws SecurityException {
                // no-op
            }
        };
        actualLogger.addHandler(handler);

        final Connection c = ds.getConnection();
        final PreparedStatement preparedStatement = c.prepareStatement("select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");
        preparedStatement.execute();
        preparedStatement.close();
        c.close();

        actualLogger.removeHandler(handler);
        assertEquals(1, msgs.size());

        final String msg = msgs.iterator().next();
        assertTrue(msg.contains("select 1 from INFORMATION_SCHEMA.SYSTEM_USERS"));
        assertTrue(msg.contains("stack: -> org.apache.openejb.resource.jdbc.logging.LoggingSqlTest.checkOutput:"));
    }
}
