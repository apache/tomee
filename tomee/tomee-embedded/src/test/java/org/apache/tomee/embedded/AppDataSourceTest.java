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
package org.apache.tomee.embedded;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Jdk14Logger;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.loader.IO;
import org.junit.Test;

import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.junit.Assert.assertTrue;

public class AppDataSourceTest {
    @Test
    public void run() throws OpenEJBException, NamingException, IOException {
        final File tempWar = new File("target/AppDataSourceTest");
        tempWar.mkdirs();
        new File(tempWar, "WEB-INF").mkdirs();
        IO.writeString(new File(tempWar, "WEB-INF/resources.xml"),
                "<resources>\n" +
                    "<Resource id=\"java:app/gace/MyDS\" type=\"DataSource\">\n" +
                    "DataSourceCreator=dbcp\n" +
                    "</Resource>\n" +
                "</resources>\n");
        final Collection<LogRecord> records = new ArrayList<>();
        try (final Container c = new Container(new Configuration().randomHttpPort())) {
            Jdk14Logger.class.cast(LogFactory.getLog(BasicDataSource.class)).getLogger().addHandler(new Handler() {
                @Override
                public void publish(final LogRecord record) {
                    if (record.getLevel() == Level.SEVERE || record.getLevel() == Level.WARNING) {
                        records.add(record);
                    }
                }

                @Override
                public void flush() {
                    // no-op
                }

                @Override
                public void close() throws SecurityException {
                    // no-op
                }
            });
            c.deploy(null, tempWar);
        }

        // if we have the JMX bug of dbcp2 integration (in 7.0.0) then we have a WARNING record from BasicDataSource.close()
        // saying:
        // Failed to unregister the JMX name:
        //     Tomcat:type=DataSource,host=localhost,context=/AppDataSourceTest,class=javax.sql.DataSource,name="openejb/Resource/AppDataSourceTest/app/gace/MyDS"
        assertTrue(records.isEmpty());
    }
}
