/**
 *
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
package org.apache.openejb.core.cmp.jpa;

import junit.framework.TestCase;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.openejb.core.TempClassLoader;
import org.apache.openejb.javaagent.Agent;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.persistence.PersistenceClassLoaderHandler;
import org.apache.openejb.persistence.PersistenceUnitInfoImpl;
import org.apache.openejb.resource.jdbc.dbcp.BasicDataSource;
import org.apache.openejb.resource.jdbc.dbcp.BasicManagedDataSource;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceProvider;
import jakarta.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;

public class UnenhancedTest extends TestCase {
    private static final String PERSISTENCE_PROVIDER = "org.apache.openjpa.persistence.PersistenceProviderImpl";

    private GeronimoTransactionManager transactionManager;
    private DataSource jtaDs;
    private DataSource nonJtaDs;
    private EntityManagerFactory entityManagerFactory;

    private boolean enhance;

    public void setUp() throws Exception {
        super.setUp();

        // setup tx mgr
        transactionManager = new GeronimoTransactionManager();
        SystemInstance.get().setComponent(TransactionSynchronizationRegistry.class, transactionManager);

        // Put tx mgr into SystemInstance so OpenJPA can find it
        SystemInstance.get().setComponent(TransactionManager.class, transactionManager);

        // init databases
        jtaDs = createJtaDataSource(transactionManager);
        nonJtaDs = createNonJtaDataSource();
    }

    public void tearDown() throws Exception {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }

        if (nonJtaDs != null) {
            final Connection connection = nonJtaDs.getConnection();
            final Statement statement = connection.createStatement();
            statement.execute("SHUTDOWN");
            close(statement);
            close(connection);
        }

        nonJtaDs = null;
        jtaDs = null;

        // diable any enhancers we added
        enhance = false;

        super.tearDown();
    }

    public void testEnhancedComplexIdJta() throws Exception {
        runTest("complexId", PersistenceUnitTransactionType.JTA, true);
    }

    public void testEnhancedComplexIdResourceLocal() throws Exception {
        runTest("complexId", PersistenceUnitTransactionType.RESOURCE_LOCAL, true);
    }

    public void testUnenhancedComplexIdJta() throws Exception {
        runTest("complexId", PersistenceUnitTransactionType.JTA, false);
    }

    public void testUnenhancedComplexIdResourceLocal() throws Exception {
        runTest("complexId", PersistenceUnitTransactionType.RESOURCE_LOCAL, false);
    }

    public void testEnhancedComplexIdSubclassJta() throws Exception {
        runTest("complexIdSubclass", PersistenceUnitTransactionType.JTA, true);
    }

    public void testEnhancedComplexIdSubclassResourceLocal() throws Exception {
        runTest("complexIdSubclass", PersistenceUnitTransactionType.RESOURCE_LOCAL, true);
    }

    // todo OpenJPA
    public void XtestUnenhancedComplexIdSubclassJta() throws Exception {
        runTest("complexIdSubclass", PersistenceUnitTransactionType.JTA, false);
    }

    // todo OpenJPA
    public void XtestUnenhancedComplexIdSubclassResourceLocal() throws Exception {
        runTest("complexIdSubclass", PersistenceUnitTransactionType.RESOURCE_LOCAL, false);
    }

    public void testEnhancedGeneratedIdJta() throws Exception {
        runTest("generatedId", PersistenceUnitTransactionType.JTA, true);
    }

    public void testEnhancedGeneratedIdResourceLocal() throws Exception {
        runTest("generatedId", PersistenceUnitTransactionType.RESOURCE_LOCAL, true);
    }

    // todo OpenJPA
    public void XtestUnenhancedGeneratedIdJta() throws Exception {
        runTest("generatedId", PersistenceUnitTransactionType.JTA, false);
    }

    // todo OpenJPA
    public void XtestUnenhancedGeneratedIdResourceLocal() throws Exception {
        runTest("generatedId", PersistenceUnitTransactionType.RESOURCE_LOCAL, false);
    }

    public void testEnhancedGeneratedIdSubclassJta() throws Exception {
        runTest("generatedIdSubclass", PersistenceUnitTransactionType.JTA, true);
    }

    public void testEnhancedGeneratedIdSubclassResourceLocal() throws Exception {
        runTest("generatedIdSubclass", PersistenceUnitTransactionType.RESOURCE_LOCAL, true);
    }

    // todo OpenJPA
    public void XtestUnenhancedGeneratedIdSubclassJta() throws Exception {
        runTest("generatedIdSubclass", PersistenceUnitTransactionType.JTA, false);
    }

    // todo OpenJPA
    public void XtestUnenhancedGeneratedIdSubclassResourceLocal() throws Exception {
        runTest("generatedIdSubclass", PersistenceUnitTransactionType.RESOURCE_LOCAL, false);
    }

    public void testEnhancedCollectionJta() throws Exception {
        runTest("collection", PersistenceUnitTransactionType.JTA, true);
    }

    public void testEnhancedCollectionResourceLocal() throws Exception {
        runTest("collection", PersistenceUnitTransactionType.RESOURCE_LOCAL, true);
    }

    // todo OpenJPA
    public void XtestUnenhancedCollectionJta() throws Exception {
        runTest("collection", PersistenceUnitTransactionType.JTA, false);
    }

    // todo OpenJPA
    public void XtestUnenhancedCollectionResourceLocal() throws Exception {
        runTest("collection", PersistenceUnitTransactionType.RESOURCE_LOCAL, false);
    }

    private void runTest(final String methodName, final PersistenceUnitTransactionType transactionType, final boolean enhance) throws Exception {
        this.enhance = enhance;

        final ClassLoader loader = new FilteredChildFirstClassLoader(getClass().getClassLoader(), "org.apache.openejb.core.cmp.jpa");

        final PersistenceClassLoaderHandler persistenceClassLoaderHandler = new PersistenceClassLoaderHandler() {

            public void addTransformer(final String unitId, final ClassLoader classLoader, final ClassFileTransformer classFileTransformer) {
                final Instrumentation instrumentation = Agent.getInstrumentation();
                if (instrumentation != null) {
                    instrumentation.addTransformer(new ControllableTransformer(classFileTransformer));
                }
            }

            public void destroy(final String unitId) {
            }

            public ClassLoader getNewTempClassLoader(final ClassLoader classLoader) {
                return new TempClassLoader(classLoader);
            }
        };

        final PersistenceUnitInfoImpl unitInfo = new PersistenceUnitInfoImpl(persistenceClassLoaderHandler);
        unitInfo.setPersistenceUnitName("CMP");
        unitInfo.setPersistenceProviderClassName(PERSISTENCE_PROVIDER);
        unitInfo.setClassLoader(loader);
        unitInfo.setExcludeUnlistedClasses(false);
        unitInfo.setJtaDataSource(jtaDs);
        unitInfo.setNonJtaDataSource(nonJtaDs);
        unitInfo.addManagedClassName("org.apache.openejb.core.cmp.jpa.ComplexSuperclass");
        unitInfo.addManagedClassName("org.apache.openejb.core.cmp.jpa.ComplexSubclass");
        unitInfo.addManagedClassName("org.apache.openejb.core.cmp.jpa.ComplexStandalone");
        unitInfo.addManagedClassName("org.apache.openejb.core.cmp.jpa.GeneratedStandalone");
        unitInfo.addManagedClassName("org.apache.openejb.core.cmp.jpa.GeneratedSuperclass");
        unitInfo.addManagedClassName("org.apache.openejb.core.cmp.jpa.GeneratedSubclass");
        unitInfo.addManagedClassName("org.apache.openejb.core.cmp.jpa.OneStandalone");
        unitInfo.addManagedClassName("org.apache.openejb.core.cmp.jpa.ManyStandalone");

        // Handle Properties
        final Properties properties = new Properties();
        properties.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(SchemaAction='add,deleteTableContents',ForeignKeys=true)");
        properties.setProperty("openjpa.RuntimeUnenhancedClasses", "supported");
        properties.setProperty("openjpa.Log", "DefaultLevel=INFO");
        unitInfo.setProperties(properties);

        unitInfo.setTransactionType(transactionType);

        unitInfo.getManagedClassNames().add("org.apache.openejb.core.cmp.jpa.Employee");

        final PersistenceProvider persistenceProvider = (PersistenceProvider) getClass().getClassLoader().loadClass(PERSISTENCE_PROVIDER).newInstance();
        entityManagerFactory = persistenceProvider.createContainerEntityManagerFactory(unitInfo, new HashMap());


        // create the test object (via reflection)
        final Object testObject = loader.loadClass("org.apache.openejb.core.cmp.jpa.UnenhancedUnits").newInstance();
        set(testObject, "TransactionManager", TransactionManager.class, transactionManager);
        set(testObject, "EntityManagerFactory", EntityManagerFactory.class, entityManagerFactory);


        // invoke the test (via reflection)
        Thread.currentThread().setContextClassLoader(loader);
        invoke(testObject, "setUp");
        try {
            invoke(testObject, methodName);
        } finally {
            invoke(testObject, "tearDown");
        }
    }

    private DataSource createJtaDataSource(final TransactionManager transactionManager) throws Exception {
        final BasicManagedDataSource ds = new BasicManagedDataSource(getClass().getName() + ".createJtaDs");
        ds.setTransactionManager(transactionManager);
        ds.setDriverClassName("org.hsqldb.jdbcDriver");
        ds.setUrl("jdbc:hsqldb:mem:JpaTest");
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setMaxTotal(100);
        ds.setMaxWait(10000);
        ds.setTestOnBorrow(true);
        return ds;
    }

    private DataSource createNonJtaDataSource() throws Exception {
        final BasicDataSource ds = new BasicDataSource(getClass().getName() + ".createNonJtaDs");
        ds.setDriverClassName("org.hsqldb.jdbcDriver");
        ds.setUrl("jdbc:hsqldb:mem:JpaTest");
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setMaxTotal(100);
        ds.setMaxWait(10000);
        ds.setTestOnBorrow(true);
        return ds;
    }

    private static void set(final Object instance, final String parameterName, final Class type, final Object value) throws Exception {
        try {
            instance.getClass().getMethod("set" + parameterName, type).invoke(instance, value);
        } catch (final InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                throw e;
            }
        }
    }

    private static void invoke(final Object instance, final String methodName) throws Exception {
        try {
            instance.getClass().getMethod(methodName).invoke(instance);
        } catch (final InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                throw e;
            }
        }
    }

    private static void close(final Statement statement) {
        if (statement == null) {
            return;
        }
        try {
            statement.close();
        } catch (final SQLException e) {
        }
    }

    private static void close(final Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (final SQLException e) {
        }
    }

    private class ControllableTransformer implements ClassFileTransformer {
        private final ClassFileTransformer transformer;


        public ControllableTransformer(final ClassFileTransformer transformer) {
            this.transformer = transformer;
        }


        public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {
            if (enhance) {
                return transformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
            } else {
                return null;
            }
        }
    }

    public class FilteredChildFirstClassLoader extends URLClassLoader {
        protected String packagePrefix;

        public FilteredChildFirstClassLoader(final ClassLoader parent, final String packagePrefix) {
            super(new URL[0], parent);
            this.packagePrefix = packagePrefix;
        }

        public Class loadClass(final String name) throws ClassNotFoundException {
            return loadClass(name, false);
        }

        protected synchronized Class loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
            // see if we've already loaded it
            final Class c = findLoadedClass(name);
            if (c != null) {
                return c;
            }

            if (!name.startsWith(packagePrefix)) {
                return Class.forName(name, resolve, getParent());
            }

            final String resourceName = name.replace('.', '/') + ".class";
            final InputStream in = getResourceAsStream(resourceName);
            if (in == null) {
                throw new ClassNotFoundException(name);
            }

            // 80% of class files are smaller then 6k
            final ByteArrayOutputStream bout = new ByteArrayOutputStream(8 * 1024);

            // copy the input stream into a byte array
            byte[] bytes = new byte[0];
            try {
                final byte[] buf = new byte[4 * 1024];
                for (int count = -1; (count = in.read(buf)) >= 0; ) {
                    bout.write(buf, 0, count);
                }
                bytes = bout.toByteArray();
            } catch (final IOException e) {
                throw new ClassNotFoundException(name, e);
            }

            // define the package
            final int packageEndIndex = name.lastIndexOf('.');
            if (packageEndIndex != -1) {
                final String packageName = name.substring(0, packageEndIndex);
                if (getPackage(packageName) == null) {
                    definePackage(packageName, null, null, null, null, null, null, null);
                }
            }

            // define the class
            try {
                return defineClass(name, bytes, 0, bytes.length);
            } catch (final SecurityException e) {
                // possible prohibited package: defer to the parent
                return super.loadClass(name, resolve);
            }
        }
    }
}
