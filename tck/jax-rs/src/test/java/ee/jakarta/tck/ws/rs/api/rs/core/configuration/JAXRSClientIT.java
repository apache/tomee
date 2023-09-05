/*
 * Copyright (c) 2012, 2021 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package ee.jakarta.tck.ws.rs.api.rs.core.configuration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Configurable;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.ext.MessageBodyReader;
import ee.jakarta.tck.ws.rs.api.rs.core.configurable.Assertable;
import ee.jakarta.tck.ws.rs.api.rs.core.configurable.CallableProvider;
import ee.jakarta.tck.ws.rs.api.rs.core.configurable.Registrar;
import ee.jakarta.tck.ws.rs.api.rs.core.configurable.SingleCheckAssertable;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

    private static final long serialVersionUID = -6325966971451285868L;

    private int registeredPropertiesCnt = -1;

    private int registeredClassesCnt = -1;

    private int registeredInstancesCnt = -1;

    @BeforeEach
    void logStartTest(TestInfo testInfo) {
        TestUtil.logMsg("STARTING TEST : " + testInfo.getDisplayName());
    }

    @AfterEach
    void logFinishTest(TestInfo testInfo) {
        TestUtil.logMsg("FINISHED TEST : " + testInfo.getDisplayName());
    }

    /*
     * @testName: getPropertiesTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:995; JAXRS:JAVADOC:758;
     * 
     * @test_Strategy: Get the immutable bag of configuration properties. Set the
     * new configuration property
     */
    @Test
    public void getPropertiesTest() throws Fault {
        Assertable assertable = new Assertable() {

            @Override
            public void check1OnClient(Client client) throws Fault {
                assertSizeAndLog(client.getConfiguration(), 1);
            }

            @Override
            public void check2OnTarget(WebTarget target) throws Fault {
                assertSizeAndLog(target.getConfiguration(), 2);
            }

            void assertSizeAndLog(Configuration config, int size) throws Fault {
                int pSize = config.getProperties().size();
                assertEqualsInt(pSize, size + registeredPropertiesCnt,
                        "getConfiguration().getProperties() is not unexpected", getLocation(), "got", pSize,
                        "properties");
                logMsg("Found", pSize, "properties");
            }

        };
        checkConfigWithProperties(assertable);
    }

    /*
     * @testName: getPropertiesIsImmutableTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:995; JAXRS:JAVADOC:758;
     * 
     * @test_Strategy: Get the immutable bag of configuration properties. Set the
     * new configuration property
     */
    @Test
    public void getPropertiesIsImmutableTest() throws Fault {
        Assertable assertable = new Assertable() {

            @Override
            public void check1OnClient(Client client) throws Fault {
                setNewProperty(client.getConfiguration());
                assertSizeAndLog(client.getConfiguration(), 1);
            }

            @Override
            public void check2OnTarget(WebTarget target) throws Fault {
                setNewProperty(target.getConfiguration());
                assertSizeAndLog(target.getConfiguration(), 2);
            }

            void setNewProperty(Configuration config) {
                try {
                    config.getProperties().put("property88", "property88");
                } catch (Exception e) {
                    // can throw an exception or do nothing
                    // or getProperties can be hard copy
                }
            }

            void assertSizeAndLog(Configuration config, int size) throws Fault {
                assertTrue(config.getProperties().size() == size + registeredPropertiesCnt,
                        "getConfiguration().getProperties() is NOT immutable " + getLocation() + " got "
                                + config.getProperties().size());
                logMsg("Found", config.getProperties().size(), " properties");
            }

        };
        checkConfigWithProperties(assertable);
    }

    /*
     * @testName: getPropertyTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:996; JAXRS:JAVADOC:758;
     * 
     * @test_Strategy: Get the value for the property with a given name. Set the new
     * configuration property
     */
    @Test
    public void getPropertyTest() throws Fault {
        Assertable assertable = new Assertable() {

            @Override
            public void check1OnClient(Client client) throws Fault {
                assertPropertyIsSet(client.getConfiguration(), "property0");
                logMsg("Found", client.getConfiguration().getProperty("property0"));
            }

            @Override
            public void check2OnTarget(WebTarget target) throws Fault {
                assertPropertyIsSet(target.getConfiguration(), "property0");
                assertPropertyIsSet(target.getConfiguration(), "property1");
                logMsg("Found", target.getConfiguration().getProperty("property1"));
            }

            void assertPropertyIsSet(Configuration config, String property) throws Fault {
                assertTrue(config.getProperty(property).equals(property),
                        "config.setProperty() did not set anything: " + getLocation());
            }

        };
        checkConfigWithProperties(assertable);
    }

    /*
     * @testName: getPropertyIsNullTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:996; JAXRS:JAVADOC:758;
     * 
     * @test_Strategy: null if the property with such name is not configured.
     */
    @Test
    public void getPropertyIsNullTest() throws Fault {
        Assertable assertable = new SingleCheckAssertable() {
            void assertPropertyIsNull(Configurable<?> config) throws Fault {
                assertTrue(config.getConfiguration().getProperty("property88") == null,
                        "#getProperty('nonexisting') != null " + getLocation());
                logMsg("#getProperty('nonexisting') is null as expected");
            }

            @Override
            protected void check(Configurable<?> configurable) throws Fault {
                assertPropertyIsNull(configurable);
            }

        };
        checkConfigWithProperties(assertable);
    }

    /*
     * @testName: getClassesTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:992; JAXRS:JAVADOC:758;
     * 
     * @test_Strategy: Get the immutable set of registered provider classes to be
     * instantiated, injected and utilized in the scope of the configured instance.
     * A provider class is a Java class with a jakarta.ws.rs.ext.Provider annotation
     * declared on the class that implements a specific service interface.
     * 
     * Register a provider class to be instantiated
     */
    @Test
    public void getClassesTest() throws Fault {
        Assertable assertable = new Assertable() {

            @Override
            public void check1OnClient(Client client) throws Fault {
                assertSizeAndLog(client.getConfiguration(), 1);
            }

            @Override
            public void check2OnTarget(WebTarget target) throws Fault {
                assertSizeAndLog(target.getConfiguration(), 2);
            }

            void assertSizeAndLog(Configuration config, int count) throws Fault {
                assertTrue(config.getClasses().size() == count + registeredClassesCnt,
                        "config.getClasses() return unexcepted size " + getLocation() + " : "
                                + config.getClasses().size());
                logMsg("Found", config.getClasses().size(), "providers");
            }

        };
        Class<?>[] providerClasses = new Class[] { CallableProvider1.class, CallableProvider2.class };
        checkConfig(assertable, providerClasses);
    }

    /*
     * @testName: getClassesIsImmutableTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:992; JAXRS:JAVADOC:758;
     * 
     * @test_Strategy: Get the immutable set of registered provider classes to be
     * instantiated, injected and utilized in the scope of the configured instance.
     * A provider class is a Java class with a jakarta.ws.rs.ext.Provider annotation
     * declared on the class that implements a specific service interface.
     *
     * Register a provider class to be instantiated
     */
    @Test
    public void getClassesIsImmutableTest() throws Fault {
        Assertable assertable = new Assertable() {

            @Override
            public void check1OnClient(Client client) throws Fault {
                assertSizeAndLog(client.getConfiguration(), 0);
                registerNewProviderInstance(client.getConfiguration());
                assertSizeAndLog(client.getConfiguration(), 0);
            }

            @Override
            public void check2OnTarget(WebTarget target) throws Fault {
                registerNewProviderInstance(target.getConfiguration());
                assertSizeAndLog(target.getConfiguration(), 0);
            }

            void assertSizeAndLog(Configuration config, int count) throws Fault {
                if (config.getClasses().size() == 1)
                    logMsg("Found", config.getClasses().iterator().next());
                assertTrue(config.getClasses().size() == count + registeredClassesCnt,
                        "config.getClasses() return unexcepted size " + getLocation() + " : "
                                + config.getClasses().size());
                logMsg("Found", config.getClasses().size(), "providers");
            }

            void registerNewProviderInstance(Configuration config) {
                Class<?> clz = CallableProvider.class;
                try {
                    config.getClasses().add(clz);
                } catch (Exception e) {
                    // can throw exception or do nothing
                    // when adding to this immutable set
                    // or it can be a new hard copied set
                }
            }

        };
        checkConfigWithProperties(assertable);
    }

    /*
     * @testName: getClassesIsNeverNullTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:992; JAXRS:JAVADOC:758;
     * 
     * @test_Strategy: The returned value shall never be null.
     *
     * Register a provider ("singleton") instance
     */
    @Test
    public void getClassesIsNeverNullTest() throws Fault {
        Assertable assertable = new SingleCheckAssertable() {

            @Override
            protected void check(Configurable<?> configurable) throws Fault {
                assertNotNullAndLog(configurable);
            }

            void assertNotNullAndLog(Configurable<?> config) throws Fault {
                assertNotNull(config.getConfiguration().getClasses(), "#getClasses shall never be null", getLocation());
                logMsg("#getClasses() is not null as expected", getLocation());
            }
        };
        checkConfigWithProperties(assertable);
    }

    /*
     * @testName: getInstancesTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:994; JAXRS:JAVADOC:758;
     * 
     * @test_Strategy: Get the immutable set of registered provider instances to be
     * utilized by the configurable instance.
     * 
     * Register a provider ("singleton") instance
     */
    @Test
    public void getInstancesTest() throws Fault {
        Assertable assertable = new Assertable() {

            @Override
            public void check1OnClient(Client client) throws Fault {
                assertSizeAndLog(client.getConfiguration(), 1);
            }

            @Override
            public void check2OnTarget(WebTarget target) throws Fault {
                assertSizeAndLog(target.getConfiguration(), 2);
            }

            void assertSizeAndLog(Configuration config, int count) throws Fault {
                assertTrue(config.getInstances().size() == count + registeredInstancesCnt,
                        "config.getClasses() return unexcepted size " + getLocation() + " : "
                                + config.getInstances().size());
                logMsg("Found", config.getInstances().size(), "providers");
            }

        };
        Object[] providerObjects = createProviderInstances();
        checkConfig(assertable, providerObjects);
    }

    /*
     * @testName: getInstancesIsImmutableTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:994; JAXRS:JAVADOC:758;
     * 
     * @test_Strategy: Get the immutable set of registered provider instances to be
     * utilized by the configurable instance.
     * 
     * Register a provider ("singleton") instance
     */
    @Test
    public void getInstancesIsImmutableTest() throws Fault {
        Assertable assertable = new SingleCheckAssertable() {
            @Override
            protected void check(Configurable<?> configurable) throws Fault {
                registerNewProviderInstance(configurable.getConfiguration());
                assertSizeAndLog(configurable.getConfiguration(), 0);
            }

            void assertSizeAndLog(Configuration config, int count) throws Fault {
                assertTrue(config.getClasses().size() == count + registeredClassesCnt,
                        "config.getClasses() return unexcepted size " + getLocation() + " : "
                                + config.getClasses().size());
                logMsg("Found", config.getClasses().size(), "providers");
            }

            void registerNewProviderInstance(Configuration config) {
                try {
                    config.getInstances().add(new CallableProvider());
                } catch (Exception e) {
                    // can throw exception or do nothing
                    // when adding to this immutable set
                    // or it can be a new hard copied set
                }
            }
        };
        checkConfigWithProperties(assertable);
    }

    /*
     * @testName: getContractsTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:993;
     * 
     * @test_Strategy: Get the extension contract registration information for a
     * component of a given class.
     */
    @Test
    public void getContractsTest() throws Fault {
        final int priority = 124;
        Assertable assertable = new SingleCheckAssertable() {

            void assertNotNullAndLog(Configurable<?> config) throws Fault {
                Map<Class<?>, Integer> map = config.getConfiguration().getContracts(CallableProvider.class);
                assertEqualsInt(1, map.size(), "Unexpected contract size", map.size());
                Class<?> contract = map.entrySet().iterator().next().getKey();
                assertEquals(MessageBodyReader.class, contract, "Unexpected contract", contract, getLocation());
                logMsg("#getContracts() is", contract, "as expected", getLocation());
                int p = map.get(contract);
                assertEqualsInt(priority, p, "Unexpected priority", p);
                logMsg("Found expected priority", p);
            }

            @Override
            protected void check(Configurable<?> configurable) throws Fault {
                assertNotNullAndLog(configurable);
            }
        };

        Registrar registrar = new Registrar() {
            @Override
            public void register(Configurable<?> config, Object registerable) {
                Map<Class<?>, Integer> map = new HashMap<Class<?>, Integer>();
                map.put(MessageBodyReader.class, priority);
                config.register(CallableProvider.class, map);
            }
        };
        checkConfigWithProperties(registrar, assertable);
    }

    /*
     * @testName: getContractsIsNeverNullTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:993;
     * 
     * @test_Strategy: Get the extension contract registration information for a
     * component of a given class. Method does not return null.
     */
    @Test
    public void getContractsIsNeverNullTest() throws Fault {
        Assertable assertable = new SingleCheckAssertable() {

            void assertNotNullAndLog(Configurable<?> config) throws Fault {
                Map<Class<?>, Integer> map = config.getConfiguration().getContracts(MessageBodyReader.class);
                assertNotNull(map, "getContracts is null", getLocation());
                logMsg("#getContracts() is not null as expected", getLocation());
            }

            @Override
            protected void check(Configurable<?> configurable) throws Fault {
                assertNotNullAndLog(configurable);
            }
        };

        checkConfigWithProperties(assertable);
    }

    /*
     * @testName: getInstancesIsNeverNullTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:994; JAXRS:JAVADOC:758;
     * 
     * @test_Strategy: The returned value shall never be null.
     *
     * Register a provider ("singleton") instance
     */
    @Test
    public void getInstancesIsNeverNullTest() throws Fault {
        Assertable assertable = new SingleCheckAssertable() {

            void assertNotNullAndLog(Configurable<?> config) throws Fault {
                assertNotNull(config.getConfiguration().getInstances(), "config.getInstances() shall never be null",
                        getLocation());
                logMsg("#getInstances() is not null as expected", getLocation());
            }

            @Override
            protected void check(Configurable<?> configurable) throws Fault {
                assertNotNullAndLog(configurable);
            }
        };
        checkConfigWithProperties(assertable);
    }

    /*
     * @testName: getPropertyNamesTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:997;
     * 
     * @test_Strategy: Returns an immutable collection containing the property names
     * available within the context of the current configuration instance.
     */
    @Test
    public void getPropertyNamesTest() throws Fault {
        Assertable assertable = new Assertable() {

            @Override
            public void check1OnClient(Client client) throws Fault {
                assertSizeAndLog(client.getConfiguration(), 1);
            }

            @Override
            public void check2OnTarget(WebTarget target) throws Fault {
                assertSizeAndLog(target.getConfiguration(), 2);
            }

            void assertSizeAndLog(Configuration config, int size) throws Fault {
                int names = config.getPropertyNames().size();
                assertEqualsInt(names, size + registeredPropertiesCnt, "getPropertyNames() is unexpected",
                        getLocation(), "got", names, "properties");
                logMsg("Found", names, "properties");
            }
        };

        checkConfigWithProperties(assertable);
    }

    /*
     * @testName: getPropertyNamesIsImmutableTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:997; JAXRS:JAVADOC:758;
     * 
     * @test_Strategy: Returns an immutable collection containing the property names
     * available within the context of the current configuration instance. Set the
     * new configuration property
     */
    @Test
    public void getPropertyNamesIsImmutableTest() throws Fault {
        Assertable assertable = new Assertable() {

            @Override
            public void check1OnClient(Client client) throws Fault {
                setNewProperty(client.getConfiguration());
                assertSizeAndLog(client.getConfiguration(), 1);
            }

            @Override
            public void check2OnTarget(WebTarget target) throws Fault {
                setNewProperty(target.getConfiguration());
                assertSizeAndLog(target.getConfiguration(), 2);
            }

            void setNewProperty(Configuration config) {
                try {
                    config.getPropertyNames().add("property88");
                } catch (Exception e) {
                    // can throw an exception or do nothing
                    // or getPropertyNames can be hard copy
                }
            }

            void assertSizeAndLog(Configuration config, int size) throws Fault {
                int pSize = config.getPropertyNames().size();
                assertEqualsInt(pSize, size + registeredPropertiesCnt, "getPropertyNames() is NOT immutable",
                        getLocation(), "got", pSize, "properties");
                logMsg("Found", pSize, " properties");
            }

        };
        checkConfigWithProperties(assertable);
    }

    /*
     * @testName: getRuntimeTypeTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:998;
     * 
     * @test_Strategy: Get the runtime type of this configuration context.
     */
    @Test
    public void getRuntimeTypeTest() throws Fault {
        Assertable assertable = new SingleCheckAssertable() {

            void assertNotNullAndLog(Configurable<?> config) throws Fault {
                assertEquals(RuntimeType.CLIENT, config.getConfiguration().getRuntimeType(),
                        "getRuntimeType() is unexpected", config.getConfiguration().getRuntimeType(), getLocation());
                logMsg("#getRuntimeType() is", RuntimeType.CLIENT, "as expected", getLocation());
            }

            @Override
            protected void check(Configurable<?> configurable) throws Fault {
                assertNotNullAndLog(configurable);
            }
        };
        checkConfigWithProperties(assertable);
    }

    /*
     * @testName: isRegisteredProviderRegisteredInstanceTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:1001; JAXRS:JAVADOC:1002;
     * 
     * @test_Strategy: Check if a particular JAX-RS component instance (such as
     * providers or features) has been previously registered in the runtime
     * configuration context.
     * 
     * Method returns true only in case an instance equal to the component instance
     * is already present among the components previously registered in the
     * configuration context.
     */
    @Test
    public void isRegisteredProviderRegisteredInstanceTest() throws Fault {
        Assertable assertable = new Assertable() {
            CallableProvider1 p1 = new CallableProvider1();

            CallableProvider2 p2 = new CallableProvider2();

            @Override
            public void check1OnClient(Client client) throws Fault {
                client.register(p1);
                assertSizeAndLog(client.getConfiguration(), 1);
            }

            @Override
            public void check2OnTarget(WebTarget target) throws Fault {
                target.register(p2);
                assertSizeAndLog(target.getConfiguration(), 2);
            }

            void assertSizeAndLog(Configuration config, int size) throws Fault {
                assertFalse(config.isRegistered(CallableProvider.class), "CallableProvider is unexpectedly registered");
                switch (size) {
                    case 2:
                        assertFalse(config.isRegistered(new CallableProvider2()),
                                "CallableProvider2 is registered " + getLocation());
                        assertTrue(config.isRegistered(p2),
                                "CallableProvider2.class is NOT registered " + getLocation());
                        assertTrue(config.isRegistered(CallableProvider2.class),
                                "CallableProvider2.class is NOT registered " + getLocation());
                        logMsg("Found registered CallableProvider2 as expected", getLocation());
                    case 1:
                        assertFalse(config.isRegistered(new CallableProvider1()),
                                "CallableProvider1 is registered " + getLocation());
                        assertTrue(config.isRegistered(p1),
                                "CallableProvider1.class is NOT registered " + getLocation());
                        assertTrue(config.isRegistered(CallableProvider1.class),
                                "CallableProvider1.class is NOT registered " + getLocation());
                        logMsg("Found registered CallableProvider1 as expected", getLocation());
                }
            }

        };
        checkConfigWithProperties(assertable);
    }

    /*
     * @testName: isRegisteredProviderRegisteredClassTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:1001; JAXRS:JAVADOC:1002;
     * 
     * @test_Strategy: Check if a particular JAX-RS component instance (such as
     * providers or features) has been previously registered in the runtime
     * configuration context.
     */
    @Test
    public void isRegisteredProviderRegisteredClassTest() throws Fault {
        Assertable assertable = new Assertable() {

            @Override
            public void check1OnClient(Client client) throws Fault {
                client.register(CallableProvider1.class);
                assertSizeAndLog(client.getConfiguration(), 1);
            }

            @Override
            public void check2OnTarget(WebTarget target) throws Fault {
                target.register(CallableProvider2.class);
                assertSizeAndLog(target.getConfiguration(), 2);
            }

            void assertSizeAndLog(Configuration config, int size) throws Fault {
                assertFalse(config.isRegistered(new CallableProvider()), "CallableProvider is unexpectedly registered");
                assertFalse(config.isRegistered(CallableProvider.class), "CallableProvider is unexpectedly registered");
                switch (size) {
                    case 2:
                        assertFalse(config.isRegistered(new CallableProvider2()),
                                "CallableProvider2 is registered " + getLocation());
                        assertTrue(config.isRegistered(CallableProvider2.class),
                                "CallableProvider2.class is NOT registered " + getLocation());
                        logMsg("Found registered CallableProvider2 as expected", getLocation());
                    case 1:
                        assertFalse(config.isRegistered(new CallableProvider1()),
                                "CallableProvider1 is registered " + getLocation());
                        assertTrue(config.isRegistered(CallableProvider1.class),
                                "CallableProvider1.class is NOT registered " + getLocation());
                        logMsg("Found registered CallableProvider1 as expected", getLocation());
                }
            }

        };
        checkConfigWithProperties(assertable);
    }

    // ///////////////////////////////////////////////////////////////////////

    /**
     * the same as createFetureInstances but with properties
     */
    private static String[] createPropertyInstances() {
        String[] properties = new String[] { "property0", "property1" };
        return properties;
    }

    /**
     * Provider has to not be anonymous class, because we need @Provider annotation
     * there
     */
    private static Object[] createProviderInstances() {
        Object[] instances = new CallableProvider[] { new CallableProvider() {
        }, new CallableProvider() {
        } };
        return instances;
    }

    /**
     * Check on every possible setting of configuration by a String property
     */
    private void checkConfigWithProperties(Assertable assertable) throws Fault {
        String[] properties = createPropertyInstances();
        checkConfig(assertable, properties);
    }

    /**
     * Check on every possible setting of configuration by a String property
     */
    private void checkConfigWithProperties(Registrar registrar, Assertable assertable) throws Fault {
        String[] properties = createPropertyInstances();
        checkConfig(registrar, assertable, properties);
    }

    /**
     * Check on every possible setting of configuration by a Feature or a singleton
     * 
     */
    private void checkConfig(Assertable assertable, Object[] registerables) throws Fault {
        checkConfig(new Registrar(), assertable, registerables);
    }

    protected void checkConfig(Registrar registrar, Assertable assertable, Object[] registerables) throws Fault {
        Client client = ClientBuilder.newClient();
        Configuration config = client.getConfiguration();
        registeredPropertiesCnt = config.getProperties().size();
        registeredClassesCnt = config.getClasses().size();
        registeredInstancesCnt = config.getInstances().size();
        logMsg("Already registered", registeredClassesCnt, "classes");
        logMsg("Already registered", registeredInstancesCnt, "instances");
        logMsg("Already registered", registeredPropertiesCnt, "properties");

        register(registrar, client, registerables[0]);
        assertable.check1OnClient(client);
        assertable.incrementLocation();

        WebTarget target = client.target("http://tck.cts:888");
        register(registrar, target, registerables[1]);
        assertable.check2OnTarget(target);
        assertable.incrementLocation();
    }

    protected void register(Registrar registrar, Configurable<?> config, Object registerable) {
        registrar.register(config, registerable);
    }
}
