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
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.PersistenceUnitInfo;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.loader.SystemInstance;

import javax.naming.NamingException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public class AutoConfigPersistenceUnitsTest extends TestCase {
    private ConfigurationFactory config;
    private Assembler assembler;
    private List<ResourceInfo> resources;

    protected void setUp() throws Exception {
        config = new ConfigurationFactory();
        assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        OpenEjbConfiguration configuration = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
        resources = configuration.facilities.resources;
    }

    /**
     * Existing data source "Orange", jta managed
     * Existing data source "OrangeUnmanaged", not jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <jta-data-source>Orange</jta-data-source>
     *      <non-jta-data-source>OrangeUnamanged</non-jta-data-source>
     * </persistence-unit>
     *
     * This is the happy path.
     *
     * @throws Exception
     */
    public void test() throws Exception {

        ResourceInfo jta = addDataSource("Orange", OrangeDriver.class, "jdbc:orange:some:stuff", true);
        ResourceInfo nonJta = addDataSource("OrangeUnmanaged", OrangeDriver.class, "jdbc:orange:some:stuff", false);

        assertSame(jta, resources.get(0));
        assertSame(nonJta, resources.get(1));

        PersistenceUnitInfo unitInfo = addPersistenceUnit("orange-unit", "orange", "orangeUnmanaged");

        assertNotNull(unitInfo);

        assertEquals(jta.id, unitInfo.jtaDataSource);
        assertEquals(nonJta.id, unitInfo.nonJtaDataSource);
    }

    /**
     * Existing data source "Orange", jta managed
     * Existing data source "OrangeUnmanaged", not jta managed
     * Existing data source "Lime", jta managed
     * Existing data source "LimeUnmanaged", not jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <jta-data-source>Orange</jta-data-source>
     *      <non-jta-data-source>OrangeUnmanaged</non-jta-data-source>
     * </persistence-unit>
     * <persistence-unit name="lime-unit">
     *      <jta-data-source>Lime</jta-data-source>
     *      <non-jta-data-source>LimeUnmanaged</non-jta-data-source>
     * </persistence-unit>
     *
     * This is the happy path.
     *
     * @throws Exception
     */
    public void testMultiple() throws Exception {

        ResourceInfo orangeJta = addDataSource("Orange", OrangeDriver.class, "jdbc:orange:some:stuff", true);
        ResourceInfo orangeNonJta = addDataSource("OrangeUnmanaged", OrangeDriver.class, "jdbc:orange:some:stuff", false);
        ResourceInfo limeJta = addDataSource("Lime", LimeDriver.class, "jdbc:lime:some:stuff", true);
        ResourceInfo limeNonJta = addDataSource("LimeUnmanaged", LimeDriver.class, "jdbc:lime:some:stuff", false);

        assertSame(orangeJta, resources.get(0));
        assertSame(orangeNonJta, resources.get(1));
        assertSame(limeJta, resources.get(2));
        assertSame(limeNonJta, resources.get(3));

        PersistenceUnit unit1 = new PersistenceUnit("orange-unit");
        unit1.setJtaDataSource("Orange");
        unit1.setNonJtaDataSource("OrangeUnmanaged");

        PersistenceUnit unit2 = new PersistenceUnit("lime-unit");
        unit2.setJtaDataSource("Lime");
        unit2.setNonJtaDataSource("LimeUnmanaged");

        AppModule app = new AppModule(this.getClass().getClassLoader(), "test-app");
        app.getPersistenceModules().add(new PersistenceModule("root", new Persistence(unit1, unit2)));

        // Create app

        AppInfo appInfo = config.configureApplication(app);
        assembler.createApplication(appInfo);

        // Check results

        PersistenceUnitInfo orangeUnit = appInfo.persistenceUnits.get(0);
        PersistenceUnitInfo limeUnit = appInfo.persistenceUnits.get(1);

        assertNotNull(orangeUnit);

        assertEquals(orangeJta.id, orangeUnit.jtaDataSource);
        assertEquals(orangeNonJta.id, orangeUnit.nonJtaDataSource);

        assertNotNull(limeUnit);

        assertEquals(limeJta.id, limeUnit.jtaDataSource);
        assertEquals(limeNonJta.id, limeUnit.nonJtaDataSource);
    }

    /**
     * Existing data source "Orange", not controlled by us
     * Existing data source "OrangeUnmanaged", also not controlled by us
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <jta-data-source>Orange</jta-data-source>
     *      <non-jta-data-source>OrangeUnamanged</non-jta-data-source>
     * </persistence-unit>
     *
     * @throws Exception
     */
    public void testThirdPartyDataSources() throws Exception {

        ResourceInfo jta = addDataSource("Orange", OrangeDriver.class, "jdbc:orange:some:stuff", null);
        ResourceInfo nonJta = addDataSource("OrangeUnmanaged", OrangeDriver.class, "jdbc:orange:some:stuff", null);

        assertSame(jta, resources.get(0));
        assertSame(nonJta, resources.get(1));

        PersistenceUnitInfo unitInfo = addPersistenceUnit("orange-unit", "orange", "orangeUnmanaged");

        assertNotNull(unitInfo);

        assertEquals(jta.id, unitInfo.jtaDataSource);
        assertEquals(nonJta.id, unitInfo.nonJtaDataSource);
    }


    /**
     * Existing data source "Orange", not controlled by us
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <jta-data-source>Orange</jta-data-source>
     * </persistence-unit>
     *
     * Here we should just let them try and get by with
     * just the one data source.
     *
     * @throws Exception
     */
    public void testThirdPartyDataSources2() throws Exception {

        ResourceInfo dataSource = addDataSource("Orange", OrangeDriver.class, "jdbc:orange:some:stuff", null);

        assertSame(dataSource, resources.get(0));

        PersistenceUnitInfo unitInfo = addPersistenceUnit("orange-unit", "orange", null);

        assertNotNull(unitInfo);

        assertEquals(dataSource.id, unitInfo.jtaDataSource);
        assertNull(unitInfo.nonJtaDataSource);
    }

    /**
     * Existing data source "Orange", not controlled by us
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <non-jta-data-source>Orange</non-jta-data-source>
     * </persistence-unit>
     *
     * Here we should just let them try and get by with
     * just the one data source.
     *
     * @throws Exception
     */
    public void testThirdPartyDataSources3() throws Exception {

        ResourceInfo dataSource = addDataSource("Orange", OrangeDriver.class, "jdbc:orange:some:stuff", null);

        assertSame(dataSource, resources.get(0));

        PersistenceUnitInfo unitInfo = addPersistenceUnit("orange-unit", null, "orange");

        assertNotNull(unitInfo);

        assertNull(unitInfo.jtaDataSource);
        assertEquals(dataSource.id, unitInfo.nonJtaDataSource);
    }

    /**
     * Existing data source "Orange", not controlled by us
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <jta-data-source>Orange</jta-data-source>
     *      <non-jta-data-source>Orange</non-jta-data-source>
     * </persistence-unit>
     *
     * Here we should just let them try and get by with
     * both jta and non-jta references pointed at the same
     * data source.
     *
     * @throws Exception
     */
    public void testThirdPartyDataSources4() throws Exception {

        ResourceInfo dataSource = addDataSource("Orange", OrangeDriver.class, "jdbc:orange:some:stuff", null);

        assertSame(dataSource, resources.get(0));

        PersistenceUnitInfo unitInfo = addPersistenceUnit("orange-unit",  "orange", "orange");

        assertNotNull(unitInfo);

        assertEquals(dataSource.id, unitInfo.jtaDataSource);
        assertEquals(dataSource.id, unitInfo.nonJtaDataSource);
    }

    /**
     * Existing data source "Orange", jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <jta-data-source>Orange</jta-data-source>
     *      <non-jta-data-source>Orange</non-jta-data-source>
     * </persistence-unit>
     *
     * They used the same data source for both the
     * jta-data-source and non-jta-data-source and we
     * can determine the data source will not work as
     * a non-jta-data-source
     *
     * We should generate the missing data source for them
     * based on the one they supplied.
     *
     * @throws Exception
     */
    public void testSameDataSourceForBoth1() throws Exception {

        ResourceInfo supplied = addDataSource("Orange", OrangeDriver.class, "jdbc:orange:some:stuff", true);

        assertSame(supplied, resources.get(0));

        PersistenceUnitInfo unitInfo = addPersistenceUnit("orange-unit", "orange", "orange");

        assertNotNull(unitInfo);

        ResourceInfo generated = resources.get(1);

        assertEquals(supplied.id, unitInfo.jtaDataSource);
        assertEquals(generated.id, unitInfo.nonJtaDataSource);


        assertNotNull(generated);
        assertEquals(supplied.id + "NonJta", generated.id);
        assertEquals(supplied.service, generated.service);
        assertEquals(supplied.className, generated.className);
        assertEquals(supplied.properties.get("JdbcDriver"), generated.properties.get("JdbcDriver"));
        assertEquals(supplied.properties.get("JdbcUrl"), generated.properties.get("JdbcUrl"));
        assertEquals("false", generated.properties.get("JtaManaged"));

    }


    /**
     * Existing data source "Orange", not jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <jta-data-source>Orange</jta-data-source>
     *      <non-jta-data-source>Orange</non-jta-data-source>
     * </persistence-unit>
     *
     * They used the same data source for both the
     * jta-data-source and non-jta-data-source and we
     * can determine the data source will not work as
     * a jta-data-source
     *
     * We should generate the missing data source for them
     * based on the one they supplied.
     *
     * @throws Exception
     */
    public void testSameDataSourceForBoth2() throws Exception {

        ResourceInfo supplied = addDataSource("Orange", OrangeDriver.class, "jdbc:orange:some:stuff", false);

        assertSame(supplied, resources.get(0));

        PersistenceUnitInfo unitInfo = addPersistenceUnit("orange-unit", "orange", "orange");

        assertNotNull(unitInfo);

        ResourceInfo generated = resources.get(1);

        assertEquals(generated.id, unitInfo.jtaDataSource);
        assertEquals(supplied.id, unitInfo.nonJtaDataSource);


        assertNotNull(generated);
        assertEquals(supplied.id + "Jta", generated.id);
        assertEquals(supplied.service, generated.service);
        assertEquals(supplied.className, generated.className);
        assertEquals(supplied.properties.get("JdbcDriver"), generated.properties.get("JdbcDriver"));
        assertEquals(supplied.properties.get("JdbcUrl"), generated.properties.get("JdbcUrl"));
        assertEquals("true", generated.properties.get("JtaManaged"));

    }

    /**
     * Existing data source "OrangeOne", jta managed
     * Existing data source "OrangeTwo", not jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <jta-data-source>OrangeOne</jta-data-source>
     *      <non-jta-data-source>OrangeOne</non-jta-data-source>
     * </persistence-unit>
     *
     * They used the same data source for both the
     * jta-data-source and non-jta-data-source and we
     * can determine the data source will not work as
     * a non-jta-data-source
     * BUT
     * they have explicitly configured a data source
     * that nearly matches the named datasource and
     * would be identical to what we would auto-create
     *
     * @throws Exception
     */
    public void testSameDataSourceForBoth3() throws Exception {

        ResourceInfo jta = addDataSource("OrangeOne", OrangeDriver.class, "jdbc:orange:some:stuff", true);
        ResourceInfo nonJta = addDataSource("OrangeTwo", OrangeDriver.class, "jdbc:orange:some:stuff", false);

        assertSame(jta, resources.get(0));
        assertSame(nonJta, resources.get(1));

        PersistenceUnitInfo unitInfo = addPersistenceUnit("orange-unit", "orangeOne", "orangeOne");

        assertNotNull(unitInfo);

        assertEquals(jta.id, unitInfo.jtaDataSource);
        assertEquals(nonJta.id, unitInfo.nonJtaDataSource);
    }

    /**
     * Existing data source "OrangeOne", jta managed
     * Existing data source "OrangeTwo", not jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <jta-data-source>OrangeTwo</jta-data-source>
     *      <non-jta-data-source>OrangeTwo</non-jta-data-source>
     * </persistence-unit>
     *
     * They used the same data source for both the
     * jta-data-source and non-jta-data-source and we
     * can determine the data source will not work as
     * a jta-data-source
     * BUT
     * they have explicitly configured a data source
     * that nearly matches the named datasource and
     * would be identical to what we would auto-create
     *
     * @throws Exception
     */
    public void testSameDataSourceForBoth4() throws Exception {

        ResourceInfo jta = addDataSource("OrangeOne", OrangeDriver.class, "jdbc:orange:some:stuff", true);
        ResourceInfo nonJta = addDataSource("OrangeTwo", OrangeDriver.class, "jdbc:orange:some:stuff", false);

        assertSame(jta, resources.get(0));
        assertSame(nonJta, resources.get(1));

        PersistenceUnitInfo unitInfo = addPersistenceUnit("orange-unit", "orangeTwo", "orangeTwo");

        assertNotNull(unitInfo);

        assertEquals(jta.id, unitInfo.jtaDataSource);
        assertEquals(nonJta.id, unitInfo.nonJtaDataSource);
    }

    /**
     * Existing data source "Orange", jta managed
     * Existing data source "OrangeUnmanaged", not jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <jta-data-source>java:foo/bar/baz/Orange</jta-data-source>
     *      <non-jta-data-source>java:foo/bar/baz/OrangeUnamanged</non-jta-data-source>
     * </persistence-unit>
     *
     * The datasources should be mapped correctly despite the
     * vendor specific prefix.
     *
     * @throws Exception
     */
    public void testShortName() throws Exception {

        ResourceInfo jta = addDataSource("Orange", OrangeDriver.class, "jdbc:orange:some:stuff", true);
        ResourceInfo nonJta = addDataSource("OrangeUnmanaged", OrangeDriver.class, "jdbc:orange:some:stuff", false);

        assertSame(jta, resources.get(0));
        assertSame(nonJta, resources.get(1));

        PersistenceUnitInfo unitInfo = addPersistenceUnit("orange-unit", "java:foo/bar/baz/orange", "java:foo/bar/baz/orangeUnmanaged");

        assertNotNull(unitInfo);

        assertEquals(jta.id, unitInfo.jtaDataSource);
        assertEquals(nonJta.id, unitInfo.nonJtaDataSource);
    }

    /**
     * Existing data source "Orange", jta managed
     * Existing data source "OrangeUnmanaged", not jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <jta-data-source>DoesNotExist</jta-data-source>
     *      <non-jta-data-source>AlsoDoesNotExist</non-jta-data-source>
     * </persistence-unit>
     *
     * We should automatically hook them up to the configured
     * datasources that do match
     *
     * @throws Exception
     */
    public void testInvalidRefs() throws Exception {

        ResourceInfo jta = addDataSource("Orange", OrangeDriver.class, "jdbc:orange:some:stuff", true);
        ResourceInfo nonJta = addDataSource("OrangeUnmanaged", OrangeDriver.class, "jdbc:orange:some:stuff", false);

        assertSame(jta, resources.get(0));
        assertSame(nonJta, resources.get(1));

        PersistenceUnitInfo unitInfo = addPersistenceUnit("orange-unit", "DoesNotExist", "AlsoDoesNotExist");

        assertNotNull(unitInfo);

        assertEquals(jta.id, unitInfo.jtaDataSource);
        assertEquals(nonJta.id, unitInfo.nonJtaDataSource);
    }

    // ---

    /**
     * Existing data source "OrangeOne", not jta managed
     * Existing data source "OrangeTwo", not jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <jta-data-source>OrangeOne</jta-data-source>
     *      <non-jta-data-source>OrangeTwo</non-jta-data-source>
     * </persistence-unit>
     *
     * This configuration should be rejected
     *
     * @throws Exception
     */
    public void testJtaRefToContrarilyConfiguredDataSource() throws Exception {

        ResourceInfo nonJta1 = addDataSource("OrangeOne", OrangeDriver.class, "jdbc:orange:some:stuff", false);
        ResourceInfo nonJta2 = addDataSource("OrangeTwo", OrangeDriver.class, "jdbc:orange:some:stuff", false);

        assertSame(nonJta1, resources.get(0));
        assertSame(nonJta2, resources.get(1));

        try {
            addPersistenceUnit("orange-unit", "orangeOne", "orangeTwo");
            fail("Configuration should be rejected");
        } catch (OpenEJBException e) {
            // pass
        }
    }

    /**
     * Existing data source "OrangeOne", jta managed
     * Existing data source "OrangeTwo", jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <jta-data-source>OrangeOne</jta-data-source>
     *      <non-jta-data-source>OrangeTwo</non-jta-data-source>
     * </persistence-unit>
     *
     * This configuration should be rejected
     *
     * @throws Exception
     */
    public void testNonJtaRefToContrarilyConfiguredDataSource() throws Exception {

        ResourceInfo jta1 = addDataSource("OrangeOne", OrangeDriver.class, "jdbc:orange:some:stuff", true);
        ResourceInfo jta2 = addDataSource("OrangeTwo", OrangeDriver.class, "jdbc:orange:some:stuff", true);

        assertSame(jta1, resources.get(0));
        assertSame(jta2, resources.get(1));

        try {
            addPersistenceUnit("orange-unit", "orangeOne", "orangeTwo");
            fail("Configuration should be rejected");
        } catch (OpenEJBException e) {
            // pass
        }
    }

    /**
     * Existing data source "OrangeOne", not jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <jta-data-source>OrangeOne</jta-data-source>
     * </persistence-unit>
     *
     * This configuration should be rejected
     *
     * @throws Exception
     */
    public void testJtaRefToContrarilyConfiguredDataSource2() throws Exception {

        ResourceInfo jta = addDataSource("OrangeOne", OrangeDriver.class, "jdbc:orange:some:stuff", false);

        assertSame(jta, resources.get(0));

        try {
            addPersistenceUnit("orange-unit", "orangeOne", null);
            fail("Configuration should be rejected");
        } catch (OpenEJBException e) {
            // pass
        }
    }

    /**
     * Existing data source "OrangeOne", jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <non-jta-data-source>OrangeOne</non-jta-data-source>
     * </persistence-unit>
     *
     * This configuration should be rejected
     *
     * @throws Exception
     */
    public void testNonJtaRefToContrarilyConfiguredDataSource2() throws Exception {

        ResourceInfo jta = addDataSource("OrangeOne", OrangeDriver.class, "jdbc:orange:some:stuff", true);

        assertSame(jta, resources.get(0));

        try {
            addPersistenceUnit("orange-unit", null, "orangeOne");
            fail("Configuration should be rejected");
        } catch (OpenEJBException e) {
            // pass
        }
    }

    // ---

    /**
     * Existing data source "Orange" not jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <non-jta-data-source>Orange</non-jta-data-source>
     * </persistence-unit>
     *
     * We should generate a <jta-data-source> based on
     * the <non-jta-data-source>
     *
     * @throws Exception
     */
    public void testMissingJtaDataSource() throws Exception {

        ResourceInfo supplied = addDataSource("Orange", OrangeDriver.class, "jdbc:orange:some:stuff", false);
        assertSame(supplied, resources.get(0));

        PersistenceUnitInfo unitInfo = addPersistenceUnit("orange-unit", null, "orange");

        assertNotNull(unitInfo);

        ResourceInfo generated = resources.get(1);

        assertEquals(supplied.id, unitInfo.nonJtaDataSource);
        assertEquals(generated.id, unitInfo.jtaDataSource);


        assertNotNull(generated);
        assertEquals(supplied.id + "Jta", generated.id);
        assertEquals(supplied.service, generated.service);
        assertEquals(supplied.className, generated.className);
        assertEquals(supplied.properties.get("JdbcDriver"), generated.properties.get("JdbcDriver"));
        assertEquals(supplied.properties.get("JdbcUrl"), generated.properties.get("JdbcUrl"));
        assertEquals("true", generated.properties.get("JtaManaged"));
    }

    /**
     * Existing data source "Orange" jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <jta-data-source>Orange</jta-data-source>
     * </persistence-unit>
     *
     * We should generate a <non-jta-data-source> based on
     * the <jta-data-source>
     *
     * @throws Exception
     */
    public void testMissingNonJtaDataSource() throws Exception {

        ResourceInfo supplied = addDataSource("Orange", OrangeDriver.class, "jdbc:orange:some:stuff", true);
        assertSame(supplied, resources.get(0));

        PersistenceUnitInfo unitInfo = addPersistenceUnit("orange-unit", "orange", null);

        assertNotNull(unitInfo);

        ResourceInfo generated = resources.get(1);

        assertEquals(supplied.id, unitInfo.jtaDataSource);
        assertEquals(generated.id, unitInfo.nonJtaDataSource);


        assertEquals(supplied.id + "NonJta", generated.id);
        assertEquals(supplied.service, generated.service);
        assertEquals(supplied.className, generated.className);
        assertEquals(supplied.properties.get("JdbcDriver"), generated.properties.get("JdbcDriver"));
        assertEquals(supplied.properties.get("JdbcUrl"), generated.properties.get("JdbcUrl"));
        assertEquals("false", generated.properties.get("JtaManaged"));
    }

    // ---

    /**
     * Existing data source "Orange", not jta managed
     * Existing data source "Lime", jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <non-jta-data-source>Orange</non-jta-data-source>
     * </persistence-unit>
     *
     * We should generate a <jta-data-source> based on
     * the <non-jta-data-source>.  We should not select
     * the Lime datasource which is for a different database.
     *
     * @throws Exception
     */
    public void testInvalidOptionsJta() throws Exception {

        ResourceInfo supplied = addDataSource("Orange", OrangeDriver.class, "jdbc:orange:some:stuff", false);
        ResourceInfo badMatch = addDataSource("Lime", LimeDriver.class, "jdbc:lime:some:stuff", true);

        assertSame(supplied, resources.get(0));
        assertSame(badMatch, resources.get(1));

        PersistenceUnitInfo unitInfo = addPersistenceUnit("orange-unit", null, "orange");

        assertNotNull(unitInfo);

        ResourceInfo generated = resources.get(2);

        assertEquals(generated.id, unitInfo.jtaDataSource);
        assertEquals(supplied.id, unitInfo.nonJtaDataSource);


        assertEquals(supplied.id + "Jta", generated.id);
        assertEquals(supplied.service, generated.service);
        assertEquals(supplied.className, generated.className);
        assertEquals(supplied.properties.get("JdbcDriver"), generated.properties.get("JdbcDriver"));
        assertEquals(supplied.properties.get("JdbcUrl"), generated.properties.get("JdbcUrl"));
        assertEquals("true", generated.properties.get("JtaManaged"));
    }

    /**
     * Existing data source "Orange", jta managed
     * Existing data source "Lime", non jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <jta-data-source>Orange</jta-data-source>
     * </persistence-unit>
     *
     * We should generate a <non-jta-data-source> based on
     * the <jta-data-source>.  We should not select the
     * Lime datasource which is for a different database.
     *
     * @throws Exception
     */
    public void testInvalidOptionsNonJta() throws Exception {

        ResourceInfo supplied = addDataSource("Orange", OrangeDriver.class, "jdbc:orange:some:stuff", true);
        ResourceInfo badMatch = addDataSource("Lime", LimeDriver.class, "jdbc:lime:some:stuff", false);

        assertSame(supplied, resources.get(0));
        assertSame(badMatch, resources.get(1));

        PersistenceUnitInfo unitInfo = addPersistenceUnit("orange-unit", "orange", null);

        assertNotNull(unitInfo);


        ResourceInfo generated = resources.get(2);

        assertEquals(supplied.id, unitInfo.jtaDataSource);
        assertEquals(generated.id, unitInfo.nonJtaDataSource);


        assertEquals(supplied.id + "NonJta", generated.id);
        assertEquals(supplied.service, generated.service);
        assertEquals(supplied.className, generated.className);
        assertEquals(supplied.properties.get("JdbcDriver"), generated.properties.get("JdbcDriver"));
        assertEquals(supplied.properties.get("JdbcUrl"), generated.properties.get("JdbcUrl"));
        assertEquals("false", generated.properties.get("JtaManaged"));
    }

    // ---

    /**
     * Existing data source "Orange", not jta managed
     * Existing data source "Lime", jta managed
     * Existing data source "JtaOrange", jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <non-jta-data-source>Orange</non-jta-data-source>
     * </persistence-unit>
     *
     * We should select the <jta-data-source> based on
     * the closest match to the <non-jta-data-source>
     *
     * @throws Exception
     */
    public void testPossiblyAmbiguousJtaOptions() throws Exception {

        ResourceInfo supplied = addDataSource("Orange", OrangeDriver.class, "jdbc:orange:some:stuff", false);
        ResourceInfo badMatch = addDataSource("Lime", LimeDriver.class, "jdbc:lime:some:stuff", true);
        ResourceInfo goodMatch = addDataSource("JtaOrange", OrangeDriver.class, "jdbc:orange:some:stuff", true);

        assertSame(supplied, resources.get(0));
        assertSame(badMatch, resources.get(1));
        assertSame(goodMatch, resources.get(2));

        PersistenceUnitInfo unitInfo = addPersistenceUnit("orange-unit", null, "orange");

        assertNotNull(unitInfo);


        assertEquals(goodMatch.id, unitInfo.jtaDataSource);
        assertEquals(supplied.id, unitInfo.nonJtaDataSource);
    }

    /**
     * Existing data source "Orange", jta managed
     * Existing data source "Lime", not jta managed
     * Existing data source "OrangeUnamanged", not jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     *      <jta-data-source>Orange</jta-data-source>
     * </persistence-unit>
     *
     * We should select the <non-jta-data-source> based on
     * the closest match to the <jta-data-source>
     *
     * @throws Exception
     */
    public void testPossiblyAmbiguousNonJtaOptions() throws Exception {

        ResourceInfo supplied = addDataSource("Orange", OrangeDriver.class, "jdbc:orange:some:stuff", true);
        ResourceInfo badMatch = addDataSource("Lime", LimeDriver.class, "jdbc:lime:some:stuff", false);
        ResourceInfo goodMatch = addDataSource("OrangeUnmanaged", OrangeDriver.class, "jdbc:orange:some:stuff", false);

        assertSame(supplied, resources.get(0));
        assertSame(badMatch, resources.get(1));
        assertSame(goodMatch, resources.get(2));

        PersistenceUnitInfo unitInfo = addPersistenceUnit("orange-unit", "orange", null);

        assertNotNull(unitInfo);


        assertEquals(supplied.id, unitInfo.jtaDataSource);
        assertEquals(goodMatch.id, unitInfo.nonJtaDataSource);
    }

    // ---

    /**
     * Existing data source "Orange", not jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     * </persistence-unit>
     *
     * The <non-jta-data-source> should be auto linked
     * to the Orange data source
     *
     * We should generate a <jta-data-source> based on
     * the <non-jta-data-source>
     *
     * @throws Exception
     */
    public void testEmptyUnitOneAvailableNonJtaDataSource() throws Exception {

        ResourceInfo supplied = addDataSource("Orange", OrangeDriver.class, "jdbc:orange:some:stuff", false);

        assertSame(supplied, resources.get(0));

        PersistenceUnitInfo unitInfo = addPersistenceUnit("orange-unit", null, null);

        assertNotNull(unitInfo);

        ResourceInfo generated = resources.get(1);

        assertEquals(generated.id, unitInfo.jtaDataSource);
        assertEquals(supplied.id, unitInfo.nonJtaDataSource);


        assertEquals(supplied.id + "Jta", generated.id);
        assertEquals(supplied.service, generated.service);
        assertEquals(supplied.className, generated.className);
        assertEquals(supplied.properties.get("JdbcDriver"), generated.properties.get("JdbcDriver"));
        assertEquals(supplied.properties.get("JdbcUrl"), generated.properties.get("JdbcUrl"));
        assertEquals("true", generated.properties.get("JtaManaged"));
    }

    /**
     * Existing data source "Orange", jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     * </persistence-unit>
     *
     * The <jta-data-source> should be auto linked
     * to the Orange data source
     *
     * We should generate a <non-jta-data-source> based on
     * the <jta-data-source>
     *
     * @throws Exception
     */
    public void testEmptyUnitOneAvailableJtaDataSource() throws Exception {

        ResourceInfo supplied = addDataSource("Orange", OrangeDriver.class, "jdbc:orange:some:stuff", true);

        assertSame(supplied, resources.get(0));

        PersistenceUnitInfo unitInfo = addPersistenceUnit("orange-unit", null, null);

        assertNotNull(unitInfo);


        ResourceInfo generated = resources.get(1);

        assertEquals(supplied.id, unitInfo.jtaDataSource);
        assertEquals(generated.id, unitInfo.nonJtaDataSource);


        assertEquals(supplied.id + "NonJta", generated.id);
        assertEquals(supplied.service, generated.service);
        assertEquals(supplied.className, generated.className);
        assertEquals(supplied.properties.get("JdbcDriver"), generated.properties.get("JdbcDriver"));
        assertEquals(supplied.properties.get("JdbcUrl"), generated.properties.get("JdbcUrl"));
        assertEquals("false", generated.properties.get("JtaManaged"));
    }

    // ---

    /**
     * Existing data source "Orange", not jta managed
     *
     * Persistence xml like so:
     *
     * <persistence-unit name="orange-unit">
     * </persistence-unit>
     *
     * A set of default data sources should be generated
     *
     * The <non-jta-data-source> should be auto linked
     * to the Default JDBC Database data source
     *
     * The <jta-data-source> should be auto linked
     * to the Default Unmanaged JDBC Database data source
     *
     * @throws Exception
     */
    public void testEmptyUnitNoAvailableDataSources() throws Exception {

        assertEquals(0, resources.size());

        PersistenceUnitInfo unitInfo = addPersistenceUnit("orange-unit", null, null);

        assertNotNull(unitInfo);

        ResourceInfo jta = resources.get(0);
        ResourceInfo nonJta = resources.get(1);

        assertEquals("Default JDBC Database", jta.id);
        assertEquals("Default Unmanaged JDBC Database", nonJta.id);

        assertEquals(jta.id, unitInfo.jtaDataSource);
        assertEquals(nonJta.id, unitInfo.nonJtaDataSource);
    }

    // --------------------------------------------------------------------------------------------
    //  Convenience methods
    // --------------------------------------------------------------------------------------------

    private PersistenceUnitInfo addPersistenceUnit(String unitName, String jtaDataSource, String nonJtaDataSource) throws OpenEJBException, IOException, NamingException {
        PersistenceUnit unit = new PersistenceUnit(unitName);
        unit.setJtaDataSource(jtaDataSource);
        unit.setNonJtaDataSource(nonJtaDataSource);

        AppModule app = new AppModule(this.getClass().getClassLoader(), unitName + "-app");
        app.getPersistenceModules().add(new PersistenceModule("root", new Persistence(unit)));

        // Create app

        AppInfo appInfo = config.configureApplication(app);
        assembler.createApplication(appInfo);

        // Check results

        return appInfo.persistenceUnits.get(0);
    }

    private ResourceInfo addDataSource(String id, Class driver, String url, Boolean managed) throws OpenEJBException {
        Resource resource = new Resource(id, "DataSource");
        resource.getProperties().put("JdbcDriver", driver.getName());
        resource.getProperties().put("JdbcUrl", url);
        resource.getProperties().put("JtaManaged", managed + " ");  // space should be trimmed later, this verifies that.

        ResourceInfo resourceInfo = config.configureService(resource, ResourceInfo.class);

        if (managed == null){
            // Strip out the JtaManaged property so we can mimic
            // datasources that we don't control
            resourceInfo.properties.remove("JtaManaged");
        }

        assembler.createResource(resourceInfo);
        return resourceInfo;
    }

    public static class Driver implements java.sql.Driver {
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

    public static class OrangeDriver extends Driver {}

    public static class LimeDriver extends Driver {}
}
