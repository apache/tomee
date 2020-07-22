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
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.core.TempClassLoader;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.persistence.PersistenceClassLoaderHandler;
import org.apache.openejb.persistence.PersistenceUnitInfoImpl;
import org.apache.openejb.resource.jdbc.dbcp.BasicDataSource;
import org.apache.openejb.resource.jdbc.dbcp.BasicManagedDataSource;
import org.apache.xbean.asm8.ClassReader;
import org.apache.xbean.asm8.ClassVisitor;
import org.apache.xbean.asm8.ClassWriter;
import org.apache.xbean.asm8.Opcodes;
import org.apache.xbean.naming.context.ImmutableContext;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import static org.apache.xbean.asm8.Opcodes.ACC_PRIVATE;
import static org.apache.xbean.asm8.Opcodes.ACC_TRANSIENT;

public class JpaTest extends TestCase {
    static {
        try {
            final Class<?> classRedefinerClass = ParentClassLoaderFinder.Helper.get().loadClass("org.apache.openjpa.enhance.ClassRedefiner");
            final Field field = classRedefinerClass.getDeclaredField("_canRedefine");
            field.setAccessible(true);
            field.set(null, Boolean.FALSE);
        } catch (final Exception e) {

        }
    }

    //    private static final String PERSISTENCE_PROVIDER = "org.apache.cayenne.jpa.Provider";
    private static final String PERSISTENCE_PROVIDER = "org.apache.openjpa.persistence.PersistenceProviderImpl";

    private PersistenceUnitTransactionType transactionType;
    private GeronimoTransactionManager transactionManager;
    private DataSource jtaDs;
    private DataSource nonJtaDs;
    private EntityManagerFactory entityManagerFactory;

    public void setUp() throws Exception {
        super.setUp();


        // setup tx mgr
        transactionManager = new GeronimoTransactionManager();
        SystemInstance.get().setComponent(TransactionSynchronizationRegistry.class, transactionManager);

        // setup naming
        MockInitialContextFactory.install(Collections.singletonMap("java:comp/TransactionSynchronizationRegistry", transactionManager));
        assertSame(transactionManager, new InitialContext().lookup("java:comp/TransactionSynchronizationRegistry"));

        // Put tx mgr into SystemInstance so OpenJPA can find it
        SystemInstance.get().setComponent(TransactionManager.class, transactionManager);

        // init databases
        jtaDs = createJtaDataSource(transactionManager);
        nonJtaDs = createNonJtaDataSource();
        initializeDatabase(jtaDs);
    }

    public static class MockInitialContextFactory implements InitialContextFactory {
        private static ImmutableContext immutableContext;

        public static void install(final Map bindings) throws NamingException {
            immutableContext = new ImmutableContext(bindings);
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, MockInitialContextFactory.class.getName());
            new InitialContext();
        }

        public Context getInitialContext(final Hashtable<?, ?> environment) throws NamingException {
            return immutableContext;
        }
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

        super.tearDown();
    }

    public void testJta() throws Exception {
        transactionType = PersistenceUnitTransactionType.JTA;
        entityManagerFactory = createEntityManagerFactory();

        final Object jpaTestObject = getClass().getClassLoader().loadClass("org.apache.openejb.core.cmp.jpa.JpaTestObject").newInstance();
        set(jpaTestObject, "EntityManagerFactory", EntityManagerFactory.class, entityManagerFactory);
        set(jpaTestObject, "TransactionManager", TransactionManager.class, transactionManager);
        set(jpaTestObject, "NonJtaDs", DataSource.class, nonJtaDs);

        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        invoke(jpaTestObject, "setUp");
        try {
            invoke(jpaTestObject, "jpaLifecycle");
        } finally {
            invoke(jpaTestObject, "tearDown");
        }
    }

    public void testResourceLocal() throws Exception {
        transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;
        entityManagerFactory = createEntityManagerFactory();

        final Object jpaTestObject = getClass().getClassLoader().loadClass("org.apache.openejb.core.cmp.jpa.JpaTestObject").newInstance();
        set(jpaTestObject, "EntityManagerFactory", EntityManagerFactory.class, entityManagerFactory);
        set(jpaTestObject, "NonJtaDs", DataSource.class, nonJtaDs);

        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        invoke(jpaTestObject, "setUp");
        try {
            invoke(jpaTestObject, "jpaLifecycle");
        } finally {
            invoke(jpaTestObject, "tearDown");
        }
    }

    private EntityManagerFactory createEntityManagerFactory() throws Exception {
        final PersistenceClassLoaderHandler persistenceClassLoaderHandler = new PersistenceClassLoaderHandler() {

            public void addTransformer(final String unitId, final ClassLoader classLoader, final ClassFileTransformer classFileTransformer) {
                /*
                Instrumentation instrumentation = Agent.getInstrumentation();
                instrumentation.addTransformer(classFileTransformer);
                */
            }

            public void destroy(final String unitId) {
            }

            public ClassLoader getNewTempClassLoader(final ClassLoader classLoader) {
                return new TempClassLoader(classLoader);
            }
        };

        /*
        Agent.getInstrumentation().addTransformer(new ClassFileTransformer() {
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                if (!className.equals("org/apache/openejb/core/cmp/jpa/Employee")) {
                    return null;
                }
                byte[] newBytes = addNewField(classfileBuffer);
                return newBytes;
            }
        });
        */

        final PersistenceUnitInfoImpl unitInfo = new PersistenceUnitInfoImpl(persistenceClassLoaderHandler);
        unitInfo.setPersistenceUnitName("CMP");
        unitInfo.setPersistenceProviderClassName(PERSISTENCE_PROVIDER);
        unitInfo.setClassLoader(getClass().getClassLoader());
        unitInfo.setExcludeUnlistedClasses(false);
        unitInfo.setJtaDataSource(jtaDs);
        unitInfo.setNonJtaDataSource(nonJtaDs);
        unitInfo.addManagedClassName("org.apache.openejb.core.cmp.jpa.Employee");
        unitInfo.addManagedClassName("org.apache.openejb.core.cmp.jpa.Bill");
        unitInfo.addManagedClassName("org.apache.openejb.core.cmp.jpa.EmbeddedBill");
        unitInfo.addManagedClassName("org.apache.openejb.core.cmp.jpa.Person");
        unitInfo.addManagedClassName("org.apache.openejb.core.cmp.jpa.EmbeddedBillPk");
        unitInfo.getMappingFileNames().add("META-INF/jpa-test-mappings.xml");

        // Handle Properties
        final Properties properties = new Properties();
        properties.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        properties.setProperty("openjpa.Log", "DefaultLevel=WARN");
        properties.setProperty("openjpa.RuntimeUnenhancedClasses", "supported");
        unitInfo.setProperties(properties);

        unitInfo.setTransactionType(transactionType);

        unitInfo.getManagedClassNames().add("org.apache.openejb.core.cmp.jpa.Employee");

        final PersistenceProvider persistenceProvider = (PersistenceProvider) getClass().getClassLoader().loadClass(PERSISTENCE_PROVIDER).newInstance();
        final EntityManagerFactory emf = persistenceProvider.createContainerEntityManagerFactory(unitInfo, new HashMap());

        return emf;
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

    private void initializeDatabase(final DataSource dataSource) throws SQLException {
        // employee
        createTable(dataSource, "employee", "CREATE TABLE employee ( id IDENTITY PRIMARY KEY, first_name VARCHAR(255), last_name VARCHAR(255))");
        execute(dataSource, "INSERT INTO employee (first_name, last_name) VALUES ('David', 'Blevins')");

        // bill
        createTable(dataSource, "bill", "CREATE TABLE bill ( billNumber BIGINT NOT NULL, billVersion BIGINT NOT NULL, billRevision BIGINT NOT NULL, billDescription VARCHAR(255) )");
        execute(dataSource, "INSERT INTO bill (billNumber, billVersion, billRevision, billDescription) VALUES (1, 0, 0, 'Basic Model')");

        // embedded bill
        createTable(dataSource, "embeddedBill", "CREATE TABLE embeddedBill ( billNumber BIGINT NOT NULL, billVersion BIGINT NOT NULL, billRevision BIGINT NOT NULL, billDescription VARCHAR(255) )");
        execute(dataSource, "INSERT INTO embeddedBill (billNumber, billVersion, billRevision, billDescription) VALUES (2, 0, 0, 'Advanced Model')");

        // relationship bean
        createTable(dataSource, "OneToOneA", "CREATE TABLE OneToOneA(A1 INTEGER, A2 VARCHAR(255))");
        createTable(dataSource, "OneToOneB", " CREATE TABLE OneToOneB(B1 INTEGER, B2 VARCHAR(255), B3 INTEGER, B4 VARCHAR(255), FKA1 INTEGER)");
        execute(dataSource, "INSERT INTO OneToOneA(A1, A2) VALUES(1, 'value1')");
        execute(dataSource, "INSERT INTO OneToOneA(A1, A2) VALUES(2, 'value2')");
        execute(dataSource, "INSERT INTO OneToOneB(B1, B2, FKA1) VALUES(11, 'value11', 1)");
    }

    private void createTable(final DataSource dataSource, final String tableName, final String create) throws SQLException {
        try {
            execute(dataSource, "DROP TABLE " + tableName);
        } catch (final Exception e) {
            // not concerned
        }
        execute(dataSource, create);
    }

    private DataSource createJtaDataSource(final TransactionManager transactionManager) throws Exception {
        final BasicManagedDataSource ds = new BasicManagedDataSource(getClass().getName() + "createJtaDs");
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
        final BasicDataSource ds = new BasicDataSource(getClass().getName() + "createNonJtaDs");
        ds.setDriverClassName("org.hsqldb.jdbcDriver");
        ds.setUrl("jdbc:hsqldb:mem:JpaTest");
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setMaxTotal(100);
        ds.setMaxWait(10000);
        ds.setTestOnBorrow(true);
        return ds;
    }


    public boolean execute(final DataSource ds, final String statement) throws SQLException {
        boolean retval;
        Connection connection = null;
        try {
            connection = ds.getConnection();

            final Statement stmt = connection.createStatement();
            try {
                retval = stmt.execute(statement);
            } finally {
                close(stmt);
            }
        } finally {
            if (connection != null) {
                close(connection);
            }
        }
        return retval;
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

    public static byte[] addNewField(final byte[] origBytes) {
        final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        final FieldAdderClassVisitor visitor = new FieldAdderClassVisitor(classWriter);

        final ClassReader classReader = new ClassReader(origBytes);
        classReader.accept(visitor, 0);

        return classWriter.toByteArray();
    }

    public static class FieldAdderClassVisitor extends ClassVisitor {
        public FieldAdderClassVisitor(final ClassVisitor classVisitor) {
            super(Opcodes.ASM7, classVisitor);
        }

        public void visitEnd() {
            // add new private transient String newField${System.currentTimeMills()}
            cv.visitField(ACC_PRIVATE + ACC_TRANSIENT, "newField" + System.currentTimeMillis(), "Ljava/lang/String;", null, null);
            cv.visitEnd();
        }
    }
}
