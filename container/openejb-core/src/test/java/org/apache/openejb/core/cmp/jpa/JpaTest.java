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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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

    public void testJta() throws Exception {
        transactionType = PersistenceUnitTransactionType.JTA;
        jpaLifecycle();
    }

    public void testResourceLocal() throws Exception {
        transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;
        jpaLifecycle();
    }

    public void jpaLifecycle() throws Exception {
        createEntityManager();
        assertTrue(entityManager.isOpen());

        beginTx();

        int davidPk = 1;
        Employee david = entityManager.find(Employee.class, davidPk);
        assertTrue(entityManager.contains(david));

        assertEquals(david.id, davidPk);
        assertEquals(david.firstName, "David");
        assertEquals(david.lastName, "Blevins");
        commitTx();

        entityManager.clear();
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


        entityManager.close();
        assertFalse(entityManager.isOpen());

    }

    private void createEntityManager() {
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
        properties.setProperty("openjpa.jdbc.DBDictionary", "org.apache.openjpa.jdbc.sql.DerbyDictionary");
        unitInfo.setProperties(properties);

        unitInfo.setTransactionType(transactionType);

        PersistenceProvider persistenceProvider = new PersistenceProviderImpl();
        EntityManagerFactory emf = persistenceProvider.createContainerEntityManagerFactory(unitInfo, new HashMap());
        entityManager = emf.createEntityManager();

        OpenJPAEntityManager openjpaEM = (OpenJPAEntityManager) entityManager;
        openjpaEM.addLifecycleListener(new OpenJPALifecycleListener(), (Class[])null);
    }

    private void beginTx() throws Exception {
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
            log("AFTER_COMMIT_TX");
        }
    }

    private void initializeDatabase(DataSource dataSource) throws SQLException {
//        try {
//            execute(dataSource, "DROP TABLE OPENJPA_SEQUENCE_TABLE");
//        } catch (Exception ignored) {
//        }
//        execute(dataSource, "CREATE TABLE OPENJPA_SEQUENCE_TABLE ( ID VARCHAR(20) PRIMARY KEY, SEQUENCE_VALUE INT)");
//        execute(dataSource, "INSERT INTO OPENJPA_SEQUENCE_TABLE (ID, SEQUENCE_VALUE) VALUES ('employee', 2000)");
        try {
            execute(dataSource, "DROP TABLE employee");
        } catch (Exception ignored) {
        }
        execute(dataSource, "CREATE TABLE employee ( id integer GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), first_name VARCHAR(20), last_name VARCHAR(20))");
        execute(dataSource, "INSERT INTO employee (first_name, last_name) VALUES ('David', 'Blevins')");
    }

    private DataSource createJtaDataSource(TransactionManager transactionManager) throws Exception {
        JdbcManagedConnectionFactory mcf = new JdbcManagedConnectionFactory();
        mcf.setDriver("org.apache.derby.jdbc.EmbeddedDriver");
        mcf.setUrl("jdbc:derby:derbyDB;create=true");
        mcf.setDefaultUserName("admin");
        mcf.setDefaultPassword("pass");
        mcf.start();

        SharedLocalConnectionManager connectionManager = new SharedLocalConnectionManager();
        connectionManager.setTransactionManager(transactionManager);

        DataSource connectionFactory = (DataSource) mcf.createConnectionFactory(connectionManager);
        return connectionFactory;
    }

    private DataSource createNonJtaDataSource() throws Exception {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
        ds.setUrl("jdbc:derby:derbyDB;create=true");
        ds.setUsername("admin");
        ds.setPassword("pass");
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
                stmt.close();
            }
        } finally {
            if (connection != null) {
                connection.close();
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
}
