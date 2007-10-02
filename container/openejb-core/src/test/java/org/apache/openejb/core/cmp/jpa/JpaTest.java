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

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.security.ProtectionDomain;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

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

import junit.framework.TestCase;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.openejb.core.TemporaryClassLoader;
import org.apache.openejb.javaagent.Agent;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.persistence.PersistenceClassLoaderHandler;
import org.apache.openejb.persistence.PersistenceUnitInfoImpl;
import org.apache.openejb.resource.jdbc.BasicDataSource;
import org.apache.openejb.resource.jdbc.BasicManagedDataSource;
import org.apache.xbean.naming.context.ImmutableContext;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_TRANSIENT;

public class JpaTest extends TestCase {
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

        public static void install(Map bindings) throws NamingException {
            immutableContext = new ImmutableContext(bindings);
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, MockInitialContextFactory.class.getName());
            new InitialContext();
        }

        public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
            return immutableContext;
        }
    }

    public void tearDown() throws Exception {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }

        if (nonJtaDs != null) {
            Connection connection = nonJtaDs.getConnection();
            Statement statement = connection.createStatement();
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

        Object jpaTestObject = getClass().getClassLoader().loadClass("org.apache.openejb.core.cmp.jpa.JpaTestObject").newInstance();
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

        Object jpaTestObject = getClass().getClassLoader().loadClass("org.apache.openejb.core.cmp.jpa.JpaTestObject").newInstance();
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
        PersistenceClassLoaderHandler persistenceClassLoaderHandler = new PersistenceClassLoaderHandler() {
            public void addTransformer(ClassLoader classLoader, ClassFileTransformer classFileTransformer) {
                Instrumentation instrumentation = Agent.getInstrumentation();
                instrumentation.addTransformer(classFileTransformer);
            }

            public ClassLoader getNewTempClassLoader(ClassLoader classLoader) {
                return new TemporaryClassLoader(classLoader);
            }
        };

        Agent.getInstrumentation().addTransformer(new ClassFileTransformer() {
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                if (!className.equals("org/apache/openejb/core/cmp/jpa/Employee")) {
                    return null;
                }
                byte[] newBytes = addNewField(classfileBuffer);
                return newBytes;
            }
        });

        PersistenceUnitInfoImpl unitInfo = new PersistenceUnitInfoImpl(persistenceClassLoaderHandler);
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
        unitInfo.getMappingFileNames().add("META-INF/jpa-test-mappings.xml");

        // Handle Properties
        Properties properties = new Properties();
        properties.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        properties.setProperty("openjpa.Log", "DefaultLevel=WARN");
        unitInfo.setProperties(properties);

        unitInfo.setTransactionType(transactionType);

        unitInfo.getManagedClassNames().add("org.apache.openejb.core.cmp.jpa.Employee");

        PersistenceProvider persistenceProvider = (PersistenceProvider) getClass().getClassLoader().loadClass(PERSISTENCE_PROVIDER).newInstance();
        EntityManagerFactory emf = persistenceProvider.createContainerEntityManagerFactory(unitInfo, new HashMap());

        return emf;
    }

    private static void set(Object instance, String parameterName, Class type, Object value) throws Exception {
        try {
            instance.getClass().getMethod("set" + parameterName, type).invoke(instance, value);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                throw e;
            }
        }
    }

    private static void invoke(Object instance, String methodName) throws Exception {
        try {
            instance.getClass().getMethod(methodName).invoke(instance);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                throw e;
            }
        }
    }

    private void initializeDatabase(DataSource dataSource) throws SQLException {
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

    private void createTable(DataSource dataSource, String tableName, String create) throws SQLException {
        try {
            execute(dataSource, "DROP TABLE " + tableName);
        } catch (Exception e) {
            // not concerned
        }
        execute(dataSource, create);
    }

    private DataSource createJtaDataSource(TransactionManager transactionManager) throws Exception {
        BasicManagedDataSource ds = new BasicManagedDataSource();
        ds.setTransactionManager(transactionManager);
        ds.setDriverClassName("org.hsqldb.jdbcDriver");
        ds.setUrl("jdbc:hsqldb:mem:JpaTest");
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setMaxActive(100);
        ds.setMaxWait(10000);
        ds.setTestOnBorrow(true);
        return ds;
    }

    private DataSource createNonJtaDataSource() throws Exception {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.hsqldb.jdbcDriver");
        ds.setUrl("jdbc:hsqldb:mem:JpaTest");
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setMaxActive(100);
        ds.setMaxWait(10000);
        ds.setTestOnBorrow(true);
        return ds;
    }


    public boolean execute(DataSource ds, String statement) throws SQLException {
        boolean retval;
        Connection connection = null;
        try {
            connection = ds.getConnection();

            Statement stmt = connection.createStatement();
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

    private static void close(Statement statement) {
        if (statement == null) {
            return;
        }
        try {
            statement.close();
        } catch (SQLException e) {
        }
    }

    private static void close(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
        }
    }

    public static byte[] addNewField(byte[] origBytes) {
        ClassWriter classWriter = new ClassWriter(true);

        FieldAdderClassVisitor visitor = new FieldAdderClassVisitor(classWriter);

        ClassReader classReader = new ClassReader(origBytes);
        classReader.accept(visitor, false);

        byte[] newBytes = classWriter.toByteArray();
        return newBytes;
    }

    public static class FieldAdderClassVisitor extends ClassAdapter {
        public FieldAdderClassVisitor(ClassVisitor classVisitor) {
            super(classVisitor);
        }

        public void visitEnd() {
            // add new private transient String newField${System.currentTimeMills()}
            cv.visitField(ACC_PRIVATE + ACC_TRANSIENT, "newField"  + System.currentTimeMillis(), "Ljava/lang/String;", null, null);
            cv.visitEnd();
        }
    }
}
