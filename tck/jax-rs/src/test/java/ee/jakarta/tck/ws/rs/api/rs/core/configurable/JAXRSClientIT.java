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

package ee.jakarta.tck.ws.rs.api.rs.core.configurable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Configurable;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Feature;
import ee.jakarta.tck.ws.rs.common.JAXRSCommonClient;
import ee.jakarta.tck.ws.rs.lib.util.TestUtil;

/*
 * @class.setup_props: webServerHost;
 *                     webServerPort;
 *                     ts_home;
 */
public class JAXRSClientIT extends JAXRSCommonClient {

    private static final long serialVersionUID = -6880902064949040518L;

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
     * @testName: registerFeatureClassReturningFalseTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:754;
     * 
     * @test_Strategy: Register a feature meta-provider to be instantiated and used
     * in the scope of this configurable context.
     * 
     * Any subsequent registration attempts for a component type, for which a class
     * or instance-based registration already exists in the system MUST be rejected
     * by the JAX-RS implementation and a warning SHOULD be raised to inform the
     * user about the rejected registration.
     */
    @Test
    public void registerFeatureClassReturningFalseTest() throws Fault {
        Assertable assertable = new SingleCheckAssertable() {
            @Override
            protected void check(Configurable<?> configurable) throws Fault {
                assertSizeAndLog(configurable);
            }

            void assertSizeAndLog(Configurable<?> config) throws Fault {
                int cnt = config.getConfiguration().getClasses().size();
                assertEqualsInt(cnt, 1 + registeredClassesCnt, "unexpected number of registered classes found:", cnt,
                        getLocation());
                logMsg("Found", cnt, "features");
            }

        };
        Class<?>[] classes = new Class<?>[] { FeatureReturningFalse.class, FeatureReturningFalse.class };
        checkConfig(assertable, classes);
    }

    /*
     * @testName: registerFeatureClassReturningFalseWithPriorityTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:755;
     * 
     * @test_Strategy: Register a feature meta-provider to be instantiated and used
     * in the scope of this configurable context.
     * 
     * Any subsequent registration attempts for a component type, for which a class
     * or instance-based registration already exists in the system MUST be rejected
     * by the JAX-RS implementation and a warning SHOULD be raised to inform the
     * user about the rejected registration.
     */
    @Test
    public void registerFeatureClassReturningFalseWithPriorityTest() throws Fault {
        Assertable assertable = new SingleCheckAssertable() {
            @Override
            protected void check(Configurable<?> configurable) throws Fault {
                assertSizeAndLog(configurable);
            }

            void assertSizeAndLog(Configurable<?> config) throws Fault {
                int cnt = config.getConfiguration().getClasses().size();
                assertEqualsInt(cnt, 1 + registeredClassesCnt, "unexpected number of registered classes found:", cnt,
                        getLocation());
                logMsg("Found", cnt, "features");
            }

        };

        Registrar registrar = new Registrar() {
            int priority = 0;

            @Override
            public void register(Configurable<?> config, Object registerable) {
                config.register((Class<?>) registerable, ++priority);
            }
        };

        Class<?>[] classes = new Class<?>[] { FeatureReturningFalse.class, FeatureReturningFalse.class };
        checkConfig(registrar, assertable, classes);
    }

    /*
     * @testName: registerFeatureInstanceReturningFalseTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:758;
     * 
     * @test_Strategy: Register a feature. In case the registered provider is a
     * client-side Feature feature, this object instantiates the feature and invokes
     * the Feature#configure(FeatureContext) method and lets the feature update it's
     * internal configuration state.
     */
    @Test
    public void registerFeatureInstanceReturningFalseTest() throws Fault {
        final Feature feature = new FeatureReturningFalse();
        Assertable assertable = new SingleCheckAssertable() {
            @Override
            protected void check(Configurable<?> configurable) throws Fault {
                assertSizeAndLog(configurable);
            }

            void assertSizeAndLog(Configurable<?> config) throws Fault {
                int cnt = config.getConfiguration().getInstances().size();
                assertEqualsInt(cnt, 1 + registeredInstancesCnt, "unexpected number of registered instances found:",
                        cnt, getLocation());
                logMsg("Found", cnt, "features");
            }

        };
        Object[] features = new Object[] { feature, feature };
        checkConfig(assertable, features);
    }

    /*
     * @testName: registerClassContractsTest
     * 
     * @assertion_ids: JAXRS:JAVADOC:756;
     * 
     * @test_Strategy: Register a feature meta-provider to be instantiated and used
     * in the scope of this configurable context.
     * 
     * Any subsequent registration attempts for a component type, for which a class
     * or instance-based registration already exists in the system MUST be rejected
     * by the JAX-RS implementation and a warning SHOULD be raised to inform the
     * user about the rejected registration.
     */
    @Test
    public void registerClassContractsTest() throws Fault {
        Assertable assertable = new SingleCheckAssertable() {
            @Override
            protected void check(Configurable<?> configurable) throws Fault {
                assertSizeAndLog(configurable);
            }

            void assertSizeAndLog(Configurable<?> config) throws Fault {
                int cnt = config.getConfiguration().getClasses().size();
                assertEqualsInt(cnt, 1 + registeredClassesCnt, "unexpected number of registered classes found:", cnt,
                        getLocation());
                logMsg("Found", cnt, "features");
            }

        };

        Registrar registrar = new Registrar() {
            int priority = 0;

            @Override
            public void register(Configurable<?> config, Object registerable) {
                config.register((Class<?>) registerable, ++priority);
            }
        };

        Class<?>[] classes = new Class<?>[] { FeatureReturningFalse.class, FeatureReturningFalse.class };
        checkConfig(registrar, assertable, classes);
    }

    // ///////////////////////////////////////////////////////////////////////
    /**
     * Check on every possible setting of configuration by a Feature or a singleton
     * 
     */
    protected void checkConfig(Assertable assertable, Object[] registerables) throws Fault {
        checkConfig(new Registrar(), assertable, registerables);
    }

    protected void checkConfig(Registrar registrar, Assertable assertable, Object[] registerables) throws Fault {
        Client client = ClientBuilder.newClient();
        Configuration config = client.getConfiguration();
        registeredClassesCnt = config.getClasses().size();
        registeredInstancesCnt = config.getInstances().size();
        logMsg("Already registered", registeredClassesCnt, "classes");
        logMsg("Already registered", registeredInstancesCnt, "instances");

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
