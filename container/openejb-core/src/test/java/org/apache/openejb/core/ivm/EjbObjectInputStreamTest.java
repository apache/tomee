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
package org.apache.openejb.core.ivm;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class EjbObjectInputStreamTest {

    private InitialContext context;
    private Assembler assembler;
    private ConfigurationFactory config;
    private String oldWhitelist;
    private String oldBlacklist;

    @Before
    public void setUp() throws Exception {
        oldWhitelist = System.getProperty("tomee.serialization.class.whitelist");
        oldBlacklist = System.getProperty("tomee.serialization.class.blacklist");

        System.setProperty("tomee.serialization.class.whitelist", "org.apache.openejb.,java.lang.SecurityException,java.lang.RuntimeException,java.lang.Exception,"
                + "java.lang.Throwable,java.lang.StackTraceElement,java.util.Collections,java.util.ArrayList,java.util.Properties,java.util.Hashtable,java.util.HashSet,"
                + "java.net.URI,java.util.TreeSet,java.util.LinkedHashSet,java.lang.String");

        System.setProperty("tomee.serialization.class.blacklist", "-");

        config = new ConfigurationFactory();
        assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        assembler.createContainer(config.configureService(StatelessSessionContainerInfo.class));

        final Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        context = new InitialContext(props);
    }

    @After
    public void tearDown() throws Exception {
        for (final AppInfo appInfo : assembler.getDeployedApplications()) {
            assembler.destroyApplication(appInfo.path);
        }
        SystemInstance.get().setComponent(Assembler.class, null);
        SystemInstance.get().setComponent(ContainerSystem.class, null);

        if (oldWhitelist != null) {
            System.setProperty("tomee.serialization.class.whitelist", oldWhitelist);
        }

        if (oldWhitelist != null) {
            System.setProperty("tomee.serialization.class.blacklist", oldBlacklist);
        }
    }

    @AfterClass
    public static void afterClass() throws Exception {
        OpenEJB.destroy();
    }

    @Test
    public void testShouldAllowPrimitiveArraysToBeSerialized() throws Exception {
        final EjbJar ejbJar = new EjbJar();
        final StatelessBean statelessBean = new StatelessBean(TestBean.class);
        ejbJar.addEnterpriseBean(statelessBean);

        final AppModule app = new AppModule(this.getClass().getClassLoader(), "Test");
        final EjbModule ejbModule = new EjbModule(ejbJar);
        ejbModule.setModuleId("EjbModule");
        app.getEjbModules().add(ejbModule);
        assembler.createApplication(config.configureApplication(app));

        final TestRemote testBean = (TestRemote) context.lookup("TestBeanRemote");
        assertEquals('J', testBean.getChar());
        assertEquals("Hello", new String(testBean.getCharArray()));
        assertEquals("test", new String(testBean.getCharArrayArray()[0]));
        assertEquals("run", new String(testBean.getCharArrayArray()[1]));
        assertEquals(1, testBean.getIntArray()[0]);
        assertEquals(2, testBean.getIntArray()[1]);
        assertEquals(true, testBean.getBooleanArray()[0]);
        assertEquals(false, testBean.getBooleanArray()[1]);
        assertEquals(0xD, testBean.getByteArray()[0]);
        assertEquals(0xE, testBean.getByteArray()[1]);
        assertEquals(0xA, testBean.getByteArray()[2]);
        assertEquals(0xD, testBean.getByteArray()[3]);
        assertEquals(0xB, testBean.getByteArray()[4]);
        assertEquals(0xE, testBean.getByteArray()[5]);
        assertEquals(0xE, testBean.getByteArray()[6]);
        assertEquals(0xF, testBean.getByteArray()[7]);
        assertEquals(1, testBean.getShortArray()[0]);
        assertEquals(2, testBean.getShortArray()[1]);
        assertEquals(1.1f, testBean.getFloatArray()[0], 0.001);
        assertEquals(2.2f, testBean.getFloatArray()[1], 0.001);
        assertEquals(5L, testBean.getLongArray()[0]);
        assertEquals(6L, testBean.getLongArray()[1]);
        assertEquals(1.1, testBean.getDoubleArray()[0], 0.001);
        assertEquals(2.2, testBean.getDoubleArray()[1], 0.001);
    }

    public interface TestRemote {
        char[] getCharArray();

        char[][] getCharArrayArray();

        char getChar();

        int[] getIntArray();

        boolean[] getBooleanArray();

        byte[] getByteArray();

        short[] getShortArray();

        float[] getFloatArray();

        long[] getLongArray();

        double[] getDoubleArray();
    }

    @Stateless
    @Remote(TestRemote.class)
    public static class TestBean implements TestRemote {

        @Override
        public char[] getCharArray() {
            return "Hello".toCharArray();
        }

        @Override
        public char[][] getCharArrayArray() {
            return new char[] [] { new char[] { 't', 'e', 's', 't' }, new char[] { 'r', 'u', 'n' }};
        }

        @Override
        public char getChar() {
            return 'J';
        }

        @Override
        public int[] getIntArray() {
            return new int[] { 1, 2 };
        }

        @Override
        public boolean[] getBooleanArray() {
            return new boolean[] { true, false };
        }

        @Override
        public byte[] getByteArray() {
            return new byte[] { 0xD, 0xE, 0xA, 0xD, 0xB, 0xE, 0xE, 0xF };
        }

        @Override
        public short[] getShortArray() {
            return new short[] { 1, 2 };
        }

        @Override
        public float[] getFloatArray() {
            return new float[] { 1.1f, 2.2f };
        }

        @Override
        public long[] getLongArray() {
            return new long[] { 5L, 6L };
        }

        @Override
        public double[] getDoubleArray() {
            return new double[] { 1.1, 2.2 };
        }
    }

}
