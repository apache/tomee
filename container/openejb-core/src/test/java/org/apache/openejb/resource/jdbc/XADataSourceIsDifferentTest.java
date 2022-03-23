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

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.Files;
import org.apache.openejb.resource.jdbc.dbcp.DbcpDataSourceCreator;
import org.apache.openejb.resource.jdbc.pool.DataSourceCreator;
import org.apache.openejb.resource.jdbc.xa.IsDifferentXaDataSourceWrapper;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.Module;
import org.hsqldb.jdbc.pool.JDBCXADataSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import javax.sql.XADataSource;
import jakarta.transaction.UserTransaction;
import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertFalse;

@RunWith(ApplicationComposer.class)
public class XADataSourceIsDifferentTest {
    @Resource
    private UserTransaction ut;

    @Resource(name = "xaFacade")
    private XADataSource ds;

    @Test
    public void run() throws Exception {
        ut.begin();
        try {
            assertFalse(ds.getXAConnection().getXAResource().isSameRM(ds.getXAConnection().getXAResource()));
        } finally {
            ut.commit();
        }
    }

    @Configuration
    public Properties config() {
        final File file = new File("target/test/xa/howl");
        if (file.isDirectory()) {
            Files.delete(file);
        }

        final Properties p = new Properties();
        p.put(DataSourceCreator.class.getName(), DbcpDataSourceCreator.class.getName()); // default dbcp pool supports xaDataSource config, not our proxy layer

        p.put("txMgr", "new://TransactionManager?type=TransactionManager");
        p.put("txMgr.txRecovery", "true");
        p.put("txMgr.logFileDir", "target/test/xa/howl");

        p.put("xa", "new://Resource?class-name=" + JDBCXADataSource.class.getName());
        p.put("xa.url", "jdbc:hsqldb:mem:xa");
        p.put("xa.user", "sa");
        p.put("xa.password", "");
        p.put("xa.SkipImplicitAttributes", "true"); // conflict with connectionProperties

        p.put("xaFacade", "new://Resource?class-name=" + IsDifferentXaDataSourceWrapper.class.getName() + "&constructor=delegate");
        p.put("xaFacade.delegate", "@xa");

        return p;
    }

    @Module
    public EjbJar app() throws Exception {
        return new EjbJar();
    }
}
