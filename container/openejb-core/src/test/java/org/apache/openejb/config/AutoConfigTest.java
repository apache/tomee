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
package org.apache.openejb.config;

import junit.framework.TestCase;

import javax.annotation.Resource;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.net.URL;
import java.io.File;

import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.sys.AbstractService;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.ResourceReferenceInfo;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.PersistenceUnitInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.loader.SystemInstance;

/**
 * @version $Revision$ $Date$
 */
public class AutoConfigTest extends TestCase {

    public void test() throws Exception {

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        assembler.createResource(config.configureService(new org.apache.openejb.config.sys.Resource("defaultDataSource", "DataSource", null), ResourceInfo.class));
        assembler.createResource(config.configureService(new org.apache.openejb.config.sys.Resource("yellowDataSource", "DataSource", null), ResourceInfo.class));
        assembler.createResource(config.configureService(new org.apache.openejb.config.sys.Resource("PurpleDataSource", "DataSource", null), ResourceInfo.class));

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(WidgetBean.class));

        EjbJarInfo ejbJarInfo = config.configureApplication(ejbJar);

        EnterpriseBeanInfo beanInfo = ejbJarInfo.enterpriseBeans.get(0);

        Map<String, ResourceReferenceInfo> refs = new HashMap<String,ResourceReferenceInfo>();
        for (ResourceReferenceInfo ref : beanInfo.jndiEnc.resourceRefs) {
            refs.put(ref.referenceName.replaceAll(".*/",""), ref);
        }

        ResourceReferenceInfo info;
        info = refs.get("yellowDataSource");
        assertNotNull(info);
        assertEquals("yellowDataSource", info.resourceID);

        info = refs.get("orangeDataSource");
        assertNotNull(info);
        assertEquals("defaultDataSource", info.resourceID);

        info = refs.get("purpleDataSource");
        assertNotNull(info);
        assertEquals("PurpleDataSource", info.resourceID);

    }

    public void testJtaDataSourceAutoCreate() throws Exception {

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        AbstractService resource = new org.apache.openejb.config.sys.Resource("Orange", "DataSource");
        resource.getProperties().put("JdbcDriver", OrangeDriver.class.getName());
        resource.getProperties().put("JdbcUrl", "jdbc:orange:some:stuff");
        resource.getProperties().put("JtaManaged", "false");

        ResourceInfo nonJtaDataSource = config.configureService(resource, ResourceInfo.class);
        assembler.createResource(nonJtaDataSource);

        // Setup app

        PersistenceUnit unit = new PersistenceUnit("orange-unit");
        unit.setNonJtaDataSource("Orange");

        AppModule app = new AppModule(this.getClass().getClassLoader(), "test");
        app.getPersistenceModules().add(new PersistenceModule("root", new Persistence(unit)));

        // Create app

        AppInfo appInfo = config.configureApplication(app);
        assembler.createApplication(appInfo);

        // Check results

        PersistenceUnitInfo unitInfo = appInfo.persistenceUnits.get(0);
        assertNotNull(unitInfo);

        assertEquals("java:openejb/Resource/"+nonJtaDataSource.id, unitInfo.nonJtaDataSource);

        OpenEjbConfiguration configuration = SystemInstance.get().getComponent(OpenEjbConfiguration.class);

        ResourceInfo jtaDataSource = configuration.facilities.resources.get(1);
        assertNotNull(jtaDataSource);
        assertEquals(nonJtaDataSource.id+"Jta", jtaDataSource.id);
        assertEquals(nonJtaDataSource.service, jtaDataSource.service);
        assertEquals(nonJtaDataSource.className, jtaDataSource.className);
        assertEquals(nonJtaDataSource.properties.get("JdbcDriver"), jtaDataSource.properties.get("JdbcDriver"));
        assertEquals(nonJtaDataSource.properties.get("JdbcUrl"), jtaDataSource.properties.get("JdbcUrl"));
        assertEquals("true", jtaDataSource.properties.get("JtaManaged"));

        fail("");
    }

    public static class OrangeDriver implements java.sql.Driver {
        public boolean acceptsURL(String url) throws SQLException {
            return false;
        }

        public Connection connect(String url, Properties info) throws SQLException {
            return null;
        }

        public int getMajorVersion() {
            return 0;
        }

        public int getMinorVersion() {
            return 0;
        }

        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
            return new DriverPropertyInfo[0];
        }

        public boolean jdbcCompliant() {
            return false;
        }
    }

    public static interface Widget {
    }


    public static class WidgetBean implements Widget {

        @Resource javax.jms.Queue redQueue;
        @Resource javax.jms.Queue blueQueue;
        @Resource javax.jms.Queue greenQueue;

        @Resource javax.sql.DataSource yellowDataSource;
        @Resource javax.sql.DataSource orangeDataSource;
        @Resource javax.sql.DataSource purpleDataSource;
    }
}
