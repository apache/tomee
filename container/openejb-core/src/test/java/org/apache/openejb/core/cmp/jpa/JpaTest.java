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
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.persistence.PersistenceUnitInfoImpl;
import org.apache.openejb.resource.SharedLocalConnectionManager;
import org.apache.openejb.resource.jdbc.JdbcManagedConnectionFactory;
import org.apache.openejb.test.entity.cmp2.Employee;
import org.apache.openejb.test.entity.cmr.onetoone.ABean_JPA;
import org.apache.openejb.test.entity.cmr.onetoone.BBean_JPA;
import org.apache.openjpa.event.AbstractLifecycleListener;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.PersistenceProviderImpl;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.Status;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

public class JpaTest extends TestCase {
    private PersistenceUnitTransactionType transactionType;
    private EntityTransaction transaction;
    private TransactionManager transactionManager;
    private EntityManager entityManager;
    private DataSource jtaDs;
    private DataSource nonJtaDs;
    private int davidPk;
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

    protected void tearDown() throws Exception {
        if (nonJtaDs != null) {
            Connection connection = nonJtaDs.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("SHUTDOWN");
            close(statement);
            close(connection);
        }

        if (entityManager != null && entityManager.isOpen()) {
            if (transactionType == PersistenceUnitTransactionType.RESOURCE_LOCAL) {
                try {
                    entityManager.getTransaction().commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    if (transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION) {
                        transactionManager.rollback();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            entityManager.close();
        }
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
        nonJtaDs = null;
        jtaDs = null;
        entityManager = null;
        super.tearDown();
    }

    public void testJta() throws Exception {
        transactionType = PersistenceUnitTransactionType.JTA;
        jpaLifecycle();
    }

    public void testResourceLocal() throws Exception {
        transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;
        jpaLifecycle();
    }

    public void jpaLifecycle() throws Exception {
        createEntityManagerFactory();

        beginTx();
        assertTrue(entityManager.isOpen());

        Employee david = entityManager.find(Employee.class, davidPk);
        assertTrue(entityManager.contains(david));

        assertEquals(david.id, davidPk);
        assertEquals(david.firstName, "David");
        assertEquals(david.lastName, "Blevins");
        commitTx();

        beginTx();

        david = entityManager.find(Employee.class, davidPk);
        assertTrue(entityManager.contains(david));

        assertEquals(david.id, davidPk);
        assertEquals(david.firstName, "David");
        assertEquals(david.lastName, "Blevins");

        commitTx();
        beginTx();

        david = (Employee) entityManager.createQuery("select e from Employee e where e.firstName='David'").getSingleResult();
        assertTrue(entityManager.contains(david));

        assertEquals(david.id, davidPk);
        assertEquals(david.firstName, "David");
        assertEquals(david.lastName, "Blevins");

        commitTx();
        beginTx();

        david = entityManager.find(Employee.class, davidPk);
        entityManager.remove(david);
        assertFalse(entityManager.contains(david));
        david = entityManager.find(Employee.class, davidPk);
        assertNull(david);

        commitTx();
        beginTx();

        Employee dain = new Employee();
        dain.firstName = "Dain";
        dain.lastName = "Sundstrom";
        assertFalse(entityManager.contains(dain));

        entityManager.persist(dain);

        // extract primary key seems to require a flush followed by a merge
        entityManager.flush();
        dain = entityManager.merge(dain);
        int dainId = dain.id;

        assertTrue(entityManager.contains(dain));

        commitTx();


        beginTx();

        dain = entityManager.find(Employee.class, dainId);
        assertTrue(entityManager.contains(dain));

        assertEquals(dain.id, dainId);
        assertEquals(dain.firstName, "Dain");
        assertEquals(dain.lastName, "Sundstrom");

        commitTx();
        beginTx();

        dain = (Employee) entityManager.createQuery("select e from Employee e").getSingleResult();
        assertTrue(entityManager.contains(dain));

        assertEquals(dain.id, dainId);
        assertEquals(dain.firstName, "Dain");
        assertEquals(dain.lastName, "Sundstrom");
        commitTx();

        beginTx();
        ABean_JPA a = new ABean_JPA();
        a.setField1(2);
        entityManager.persist(a);
        BBean_JPA b = new BBean_JPA();
        b.setField1(22);
        entityManager.persist(b);
        commitTx();

        beginTx();
        b = entityManager.find(BBean_JPA.class, 22);
        a = entityManager.find(ABean_JPA.class, 2);
        a.OpenEJB_addCmr("b", b);
        b.OpenEJB_addCmr("a", a);
        commitTx();

//        dump();
        
        beginTx();
        b = entityManager.find(BBean_JPA.class, 22);
        assertNotNull(b);
        assertEquals(new Integer(22), b.getField1());
//        a = (ABean_JPA) b.OpenEJB_getCmr("a");
//        assertNotNull(a);
//        assertEquals(new Integer(2), a.getField1());
        commitTx();
    }

    private void createEntityManager() {
        entityManager = entityManagerFactory.createEntityManager();

        OpenJPAEntityManager openjpaEM = (OpenJPAEntityManager) entityManager;
        openjpaEM.addLifecycleListener(new OpenJPALifecycleListener(), (Class[])null);
    }

    private void createEntityManagerFactory() {
        PersistenceUnitInfoImpl unitInfo = new PersistenceUnitInfoImpl();
        unitInfo.setPersistenceUnitName("CMP");
        unitInfo.setPersistenceProviderClassName(PersistenceProviderImpl.class.getName());
        unitInfo.setClassLoader(getClass().getClassLoader());
        unitInfo.setExcludeUnlistedClasses(false);
        unitInfo.setJtaDataSource(jtaDs);
        unitInfo.setNonJtaDataSource(nonJtaDs);

        unitInfo.setMappingFileNames(Collections.singletonList("META-INF/jpa.mapping.xml"));

        // Handle Properties
        Properties properties = new Properties();
        unitInfo.setProperties(properties);

        unitInfo.setTransactionType(transactionType);

        PersistenceProvider persistenceProvider = new PersistenceProviderImpl();
        entityManagerFactory = persistenceProvider.createContainerEntityManagerFactory(unitInfo, new HashMap());
    }

    private void beginTx() throws Exception {
        createEntityManager();

        String msg = "BEGIN_TX";
        log(msg);
        if (transactionType == PersistenceUnitTransactionType.JTA) {
            transactionManager.begin();
            entityManager.joinTransaction();
        } else {
            transaction = entityManager.getTransaction();
            transaction.begin();
        }
    }

    private void log(String msg) {
//        System.out.println(msg);
    }

    private void commitTx() throws Exception {
        log("  BEFORE_COMMIT_TX");
        try {
            if (transactionType == PersistenceUnitTransactionType.JTA) {
                transactionManager.commit();
            } else {
                transaction.commit();
            }
        } finally {
            if (entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
            entityManager = null;
            log("AFTER_COMMIT_TX");
        }
    }

    private void initializeDatabase(DataSource dataSource) throws SQLException {
        createTable(dataSource, "employee", "CREATE TABLE employee ( id IDENTITY, first_name VARCHAR(20), last_name VARCHAR(20))");
        execute(dataSource, "INSERT INTO employee (first_name, last_name) VALUES ('David', 'Blevins')");

        createTable(dataSource, "OneToOneA", "CREATE TABLE OneToOneA(A1 INTEGER, A2 VARCHAR(50))");
        createTable(dataSource, "OneToOneB"," CREATE TABLE OneToOneB(B1 INTEGER, B2 VARCHAR(50), B3 INTEGER, B4 VARCHAR(50), FKA1 INTEGER)");
        execute(dataSource, "INSERT INTO OneToOneA(A1, A2) VALUES(1, 'value1')");
        execute(dataSource, "INSERT INTO OneToOneA(A1, A2) VALUES(2, 'value2')");
        execute(dataSource, "INSERT INTO OneToOneB(B1, B2, FKA1) VALUES(11, 'value11', 1)");

//        dump();

        // get the pk for the inserted row
        Connection connection = null;
        Statement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from employee");
            resultSet.next();
            davidPk = resultSet.getInt("id");
        } finally {
            close(statement);
            close(connection);
        }
    }

    private void createTable(DataSource dataSource, String tableName, String create) throws java.sql.SQLException {
        try{
            execute(dataSource, "DROP TABLE "  + tableName);
        } catch (Exception e){
            // not concerned
        }
        execute(dataSource, create);
    }

    private DataSource createJtaDataSource(TransactionManager transactionManager) throws Exception {
        JdbcManagedConnectionFactory mcf = new JdbcManagedConnectionFactory();
        mcf.setDriver("org.hsqldb.jdbcDriver");
        mcf.setUrl("jdbc:hsqldb:mem:JpaTest");
        mcf.setDefaultUserName("sa");
        mcf.setDefaultPassword("");
        mcf.start();

        SharedLocalConnectionManager connectionManager = new SharedLocalConnectionManager();
        connectionManager.setTransactionManager(transactionManager);

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


    public boolean execute(DataSource ds, String statement) throws java.sql.SQLException {
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

    private class OpenJPALifecycleListener extends AbstractLifecycleListener {
        protected void eventOccurred(LifecycleEvent event) {
            int type = event.getType();
            switch (type) {
                case LifecycleEvent.BEFORE_PERSIST:
                    log("  BEFORE_PERSIST");
                    break;
                case LifecycleEvent.AFTER_PERSIST:
                    log("  AFTER_PERSIST");
                    break;
                case LifecycleEvent.AFTER_LOAD:
                    log("  AFTER_LOAD");
                    break;
                case LifecycleEvent.BEFORE_STORE:
                    log("  BEFORE_STORE");
                    break;
                case LifecycleEvent.AFTER_STORE:
                    log("  AFTER_STORE");
                    break;
                case LifecycleEvent.BEFORE_CLEAR:
                    log("  BEFORE_CLEAR");
                    break;
                case LifecycleEvent.AFTER_CLEAR:
                    log("  AFTER_CLEAR");
                    break;
                case LifecycleEvent.BEFORE_DELETE:
                    log("  BEFORE_DELETE");
                    break;
                case LifecycleEvent.AFTER_DELETE:
                    log("  AFTER_DELETE");
                    break;
                case LifecycleEvent.BEFORE_DIRTY:
                    log("  BEFORE_DIRTY");
                    break;
                case LifecycleEvent.AFTER_DIRTY:
                    log("  AFTER_DIRTY");
                    break;
                case LifecycleEvent.BEFORE_DIRTY_FLUSHED:
                    log("  BEFORE_DIRTY_FLUSHED");
                    break;
                case LifecycleEvent.AFTER_DIRTY_FLUSHED:
                    log("  AFTER_DIRTY_FLUSHED");
                    break;
                case LifecycleEvent.BEFORE_DETACH:
                    log("  BEFORE_DETACH");
                    break;
                case LifecycleEvent.AFTER_DETACH:
                    log("  AFTER_DETACH");
                    break;
                case LifecycleEvent.BEFORE_ATTACH:
                    log("  BEFORE_ATTACH");
                    break;
                case LifecycleEvent.AFTER_ATTACH:
                    log("  AFTER_ATTACH");
                    break;
                case LifecycleEvent.AFTER_REFRESH:
                    log("  AFTER_REFRESH");
                    break;
                default:
                    log("  default");
                    break;
            }
            super.eventOccurred(event);
        }

        public void beforePersist(LifecycleEvent event) {
            eventOccurred(event);
        }

        public void afterRefresh(LifecycleEvent event) {
            eventOccurred(event);
        }

        public void beforeDetach(LifecycleEvent event) {
            eventOccurred(event);
        }

        public void afterDetach(LifecycleEvent event) {
            eventOccurred(event);
        }

        public void beforeAttach(LifecycleEvent event) {
            eventOccurred(event);
        }

        public void afterAttach(LifecycleEvent event) {
            eventOccurred(event);
        }
    }

    protected void dump() throws SQLException {
        dumpTable(nonJtaDs, "employee");
        dumpTable(nonJtaDs, "OneToOneA");
        dumpTable(nonJtaDs, "OneToOneB");
    }

    public static void dumpTable(DataSource ds, String table) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = ds.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM " + table);
            ResultSetMetaData setMetaData = resultSet.getMetaData();
            int columnCount = setMetaData.getColumnCount();
            while(resultSet.next()) {
                StringBuilder row = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) {
                        row.append(", ");
                    }
                    String name = setMetaData.getColumnName(i);
                    Object value = resultSet.getObject(i);
                    row.append(name).append("=").append(value);
                }
                System.out.println(row);
            }
        } finally {
            close(resultSet);
            close(statement);
            close(connection);
        }
    }

    private static void close(ResultSet resultSet) {
        if (resultSet == null) return;
        try {
            resultSet.close();
        } catch (SQLException e) {
        }
    }

    private static void close(Statement statement) {
        if (statement == null) return;
        try {
            statement.close();
        } catch (SQLException e) {
        }
    }

    private static void close(Connection connection) {
        if (connection == null) return;
        try {
            connection.close();
        } catch (SQLException e) {
        }
    }
}
