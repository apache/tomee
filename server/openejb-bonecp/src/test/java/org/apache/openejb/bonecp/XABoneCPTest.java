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
package org.apache.openejb.bonecp;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.hsqldb.jdbc.pool.JDBCXAConnectionWrapper;
import org.hsqldb.jdbc.pool.JDBCXADataSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import jakarta.annotation.Resource;
import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(ApplicationComposer.class)
public class XABoneCPTest {
    @Resource(name = "xadb")
    private DataSource ds;

    @Module
    public EjbJar mandatory() {
        return new EjbJar();
    }

    @Configuration
    public Properties props() {
        return new PropertiesBuilder()
            .p("openejb.jdbc.datasource-creator", BoneCPDataSourceCreator.class.getName())

            .p("txMgr", "new://TransactionManager?type=TransactionManager")
            .p("txMgr.txRecovery", "true")
            .p("txMgr.logFileDir", "target/test/xa/howl")

                // real XA datasources
            .p("xa", "new://Resource?class-name=" + JDBCXADataSource.class.getName())
            .p("xa.url", "jdbc:hsqldb:mem:tomcat-xa")
            .p("xa.user", "sa")
            .p("xa.password", "")
            .p("xa.SkipImplicitAttributes", "true")

            .p("xadb", "new://Resource?type=DataSource")
            .p("xadb.xaDataSource", "xa")
            .p("xadb.JtaManaged", "true")

            .build();
    }

    @Test
    public void check() throws SQLException {
        assertNotNull(ds);
        final Connection c = ds.getConnection();
        assertNotNull(c);
        assertThat(c.getMetaData().getConnection(), instanceOf(JDBCXAConnectionWrapper.class));
        c.close();

    }
}
