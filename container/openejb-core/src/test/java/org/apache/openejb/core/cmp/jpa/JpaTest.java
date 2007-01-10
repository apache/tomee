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
import java.util.HashMap;
import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.openejb.javaagent.Agent;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.persistence.PersistenceClassLoaderHandler;
import org.apache.openejb.persistence.PersistenceUnitInfoImpl;
import org.apache.openejb.core.TemporaryClassLoader;
import org.apache.openejb.resource.SharedLocalConnectionManager;
import org.apache.openejb.resource.jdbc.JdbcManagedConnectionFactory;
import org.apache.openjpa.persistence.PersistenceProviderImpl;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_TRANSIENT;

public class JpaTest extends TestCase {
    private PersistenceUnitTransactionType transactionType;
    private TransactionManager transactionManager;
    private DataSource jtaDs;
    private DataSource nonJtaDs;
    private EntityManagerFactory entityManagerFactory;

    public void setUp() throws Exception {
        super.setUp();

        // setup tx mgr
        transactionManager = new GeronimoTransactionManager();

        // Put tx mgr into SystemInstance so OpenJPA can find it
        SystemInstance.get().setComponent(TransactionManager.class, transactionManager);

        // init databases
        jtaDs = createJtaDataSource(transactionManager);
        nonJtaDs = createNonJtaDataSource();
        initializeDatabase(jtaDs);
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

    private EntityManagerFactory createEntityManagerFactory() {
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
        unitInfo.setPersistenceProviderClassName(PersistenceProviderImpl.class.getName());
        unitInfo.setClassLoader(getClass().getClassLoader());
        unitInfo.setExcludeUnlistedClasses(false);
        unitInfo.setJtaDataSource(jtaDs);
        unitInfo.setNonJtaDataSource(nonJtaDs);
        unitInfo.addManagedClassName("org.apache.openejb.core.cmp.jpa.Employee");

        // Handle Properties
        Properties properties = new Properties();
        unitInfo.setProperties(properties);

        unitInfo.setTransactionType(transactionType);

        PersistenceProvider persistenceProvider = new PersistenceProviderImpl();
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
        createTable(dataSource, "employee", "CREATE TABLE employee ( id IDENTITY, first_name VARCHAR(20), last_name VARCHAR(20))");
        execute(dataSource, "INSERT INTO employee (first_name, last_name) VALUES ('David', 'Blevins')");

        createTable(dataSource, "OneToOneA", "CREATE TABLE OneToOneA(A1 INTEGER, A2 VARCHAR(50))");
        createTable(dataSource, "OneToOneB", " CREATE TABLE OneToOneB(B1 INTEGER, B2 VARCHAR(50), B3 INTEGER, B4 VARCHAR(50), FKA1 INTEGER)");
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
        JdbcManagedConnectionFactory mcf = new JdbcManagedConnectionFactory("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:JpaTest", "sa", "", false);

        SharedLocalConnectionManager connectionManager = new SharedLocalConnectionManager(transactionManager);

        DataSource connectionFactory = (DataSource) mcf.createConnectionFactory(connectionManager);
        return connectionFactory;
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
