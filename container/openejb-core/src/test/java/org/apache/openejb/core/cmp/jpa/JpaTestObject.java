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

import org.apache.openejb.test.entity.cmp.ComplexCmpBean;
import org.apache.openejb.test.entity.cmp.UnknownCmpBean;
import org.apache.openejb.test.entity.cmp.BasicCmpBean;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.TransactionManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

public class JpaTestObject extends junit.framework.Assert {
    private EntityManagerFactory entityManagerFactory;
    private TransactionManager transactionManager;
    private DataSource nonJtaDs;

    private EntityManager entityManager;
    private EntityTransaction transaction;
    private static final String MCLAUGHLIN = "Brett McLaughlin";
    private static final String FLANAGAN = "David Flanagan";
    private static final String TIGER = "Java 5.0 Tiger";
    private static final String JAVASCRIPT = "JavaScript";
    private static final String ENTERPRISE_APPS = "Building Java Enterprise Applications";

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public DataSource getNonJtaDs() {
        return nonJtaDs;
    }

    public void setNonJtaDs(DataSource nonJtaDs) {
        this.nonJtaDs = nonJtaDs;
    }

    public void setUp() throws Exception {
    }

    public void tearDown() throws Exception {
        if (entityManager != null && entityManager.isOpen()) {
            if (transactionManager == null) {
                try {
                    if (entityManager.getTransaction().getRollbackOnly()) {
                        entityManager.getTransaction().rollback();
                    } else {
                        entityManager.getTransaction().commit();
                    }
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

        nonJtaDs = null;
        entityManager = null;
        entityManagerFactory = null;
        transactionManager = null;
    }

    public void jpaLifecycle() throws Exception {
        newDeleteNew();
        employeeTest();
        billTest();
        cmpText();
        complexCmpText();
        unknownCmpText();
        manytoMany();

//        beginTx();
//        ABean_ABean a = new ABean_ABean();
//        a.setField1(2);
//        entityManager.persist(a);
//        BBean_BBean b = new BBean_BBean();
//        b.setField1(22);
//        entityManager.persist(b);
//        commitTx();
//
//        beginTx();
//        b = entityManager.find(BBean_BBean.class, 22);
//        a = entityManager.find(ABean_ABean.class, 2);
//        a.OpenEJB_addCmr("b", b);
//        b.OpenEJB_addCmr("a", a);
//        commitTx();
//
////        dump();
//
//        beginTx();
//        b = entityManager.find(BBean_BBean.class, 22);
//        assertNotNull(b);
//        assertEquals(new Integer(22), b.getField1());
////        a = (ABean_ABean) b.OpenEJB_getCmr("a");
////        assertNotNull(a);
////        assertEquals(new Integer(2), a.getField1());
//        commitTx();
    }

    private void newDeleteNew() throws Exception {
        beginTx();

        // Create new
        Person dain = new Person();
        dain.setName("dain");
        assertFalse(entityManager.contains(dain));
        entityManager.persist(dain);
        entityManager.flush();
        dain = entityManager.merge(dain);
        assertTrue(entityManager.contains(dain));

        // Find and verify
        dain = entityManager.find(Person.class, "dain");
        assertNotNull(dain);
        assertEquals("dain", dain.getName());

        // Delete
        entityManager.remove(dain);
        assertFalse(entityManager.contains(dain));

        // Recreate
        dain = new Person();
        dain.setName("dain");
        assertFalse(entityManager.contains(dain));
        entityManager.persist(dain);
        entityManager.flush();
        dain = entityManager.merge(dain);
        assertTrue(entityManager.contains(dain));

        // Find and verify
        dain = entityManager.find(Person.class, "dain");
        // assertNotNull(dain); // <<<<<<< FAILS
        // assertEquals("dain", dain.getName());

        commitTx();
    }
    
    private void employeeTest() throws Exception {
        // get the pk for the inserted row
        int davidPk = -1;
        {
            Connection connection = null;
            Statement statement = null;
            try {
                connection = nonJtaDs.getConnection();
                statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select * from employee");
                resultSet.next();
                davidPk = resultSet.getInt("id");
            } finally {
                close(statement);
                close(connection);
            }
        }

        beginTx();
        assertTrue(entityManager.isOpen());

        Employee david = entityManager.find(Employee.class, davidPk);
        assertTrue(entityManager.contains(david));

        assertEquals(david.getId(), davidPk);
        assertEquals(david.getFirstName(), "David");
        assertEquals(david.getLastName(), "Blevins");
        commitTx();

        beginTx();

        david = entityManager.find(Employee.class, davidPk);
        assertTrue(entityManager.contains(david));

        assertEquals(david.getId(), davidPk);
        assertEquals(david.getFirstName(), "David");
        assertEquals(david.getLastName(), "Blevins");

        commitTx();
        beginTx();

        david = (Employee) entityManager.createQuery("select e from Employee e where e.firstName='David'").getSingleResult();
        assertTrue(entityManager.contains(david));

        assertEquals(david.getId(), davidPk);
        assertEquals(david.getFirstName(), "David");
        assertEquals(david.getLastName(), "Blevins");

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
        dain.setFirstName("Dain");
        dain.setLastName("Sundstrom");
        assertFalse(entityManager.contains(dain));

        entityManager.persist(dain);

        // extract primary key seems to require a flush followed by a merge
        entityManager.flush();
        dain = entityManager.merge(dain);
        int dainId = dain.getId();

        assertTrue(entityManager.contains(dain));

        commitTx();


        beginTx();

        dain = entityManager.find(Employee.class, dainId);
        assertTrue(entityManager.contains(dain));

        assertEquals(dain.getId(), dainId);
        assertEquals(dain.getFirstName(), "Dain");
        assertEquals(dain.getLastName(), "Sundstrom");

        commitTx();
        beginTx();

        dain = (Employee) entityManager.createQuery("select e from Employee e").getSingleResult();
        assertTrue(entityManager.contains(dain));

        assertEquals(dain.getId(), dainId);
        assertEquals(dain.getFirstName(), "Dain");
        assertEquals(dain.getLastName(), "Sundstrom");

        commitTx();
    }

    public void billTest() throws Exception {
        // get the pk for the inserted row
        BillPk basicPk;

        {
            Connection connection = null;
            Statement statement = null;
            try {
                connection = nonJtaDs.getConnection();
                statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("select * from bill");
                resultSet.next();

                basicPk = new BillPk(resultSet.getLong("billNumber"), resultSet.getLong("billVersion"), resultSet.getLong("billRevision"));
            } finally {
                close(statement);
                close(connection);
            }
        }

        beginTx();
        assertTrue(entityManager.isOpen());

        Bill basic = entityManager.find(Bill.class, basicPk);
        assertTrue(entityManager.contains(basic));

        assertTrue(basicPk.equals(new BillPk(1, 0, 0)));

        assertEquals(basic.getBillNumber(), 1);
        assertEquals(basic.getBillVersion(), 0);
        assertEquals(basic.getBillRevision(), 0);
        assertEquals(basic.getBillDescription(), "Basic Model");

        commitTx();
        beginTx();

        basic = (Bill) entityManager.createQuery("select e from Bill e where e.billDescription='Basic Model'").getSingleResult();
        assertTrue(entityManager.contains(basic));

        assertTrue(basicPk.equals(new BillPk(1, 0, 0)));

        assertEquals(basic.getBillNumber(), 1);
        assertEquals(basic.getBillVersion(), 0);
        assertEquals(basic.getBillRevision(), 0);
        assertEquals(basic.getBillDescription(), "Basic Model");

        commitTx();
        beginTx();

        basic = entityManager.find(Bill.class, basicPk);
        entityManager.remove(basic);
        assertFalse(entityManager.contains(basic));
        basic = entityManager.find(Bill.class, basicPk);
        assertNull(basic);

        commitTx();
        beginTx();

        Bill advanced = new Bill();
        advanced.setBillNumber(1000);
        advanced.setBillVersion(2000);
        advanced.setBillRevision(3000);
        advanced.setBillDescription("This is so advanced!!");
        assertFalse(entityManager.contains(advanced));

        entityManager.persist(advanced);

        // extract primary key seems to require a flush followed by a merge
        entityManager.flush();
        advanced = entityManager.merge(advanced);

        BillPk advancedPk = new BillPk(advanced.getBillNumber(), advanced.getBillVersion(), advanced.getBillRevision());

        assertTrue(entityManager.contains(advanced));

        commitTx();
        beginTx();

        advanced = entityManager.find(Bill.class, advancedPk);
        assertTrue(entityManager.contains(advanced));

        assertTrue(advancedPk.equals(new BillPk(1000, 2000, 3000)));
        assertEquals(advanced.getBillNumber(), 1000);
        assertEquals(advanced.getBillVersion(), 2000);
        assertEquals(advanced.getBillRevision(), 3000);
        assertEquals(advanced.getBillDescription(), "This is so advanced!!");

        commitTx();
        beginTx();

        advanced = (Bill) entityManager.createQuery("select e from Bill e").getSingleResult();
        assertTrue(entityManager.contains(advanced));

        assertTrue(advancedPk.equals(new BillPk(1000, 2000, 3000)));
        assertEquals(advanced.getBillNumber(), 1000);
        assertEquals(advanced.getBillVersion(), 2000);
        assertEquals(advanced.getBillRevision(), 3000);
        assertEquals(advanced.getBillDescription(), "This is so advanced!!");

        commitTx();
    }

    private void cmpText() throws Exception {
        beginTx();

        BasicCmpBean basicCmpBean = new BasicCmpBean_Subclass();
        basicCmpBean.ejbCreateObject("Joe Blow");
        assertFalse(entityManager.contains(basicCmpBean));

        entityManager.persist(basicCmpBean);

        // extract primary key seems to require a flush followed by a merge
        entityManager.flush();
        basicCmpBean = entityManager.merge(basicCmpBean);
        int joeId = basicCmpBean.getPrimaryKey();

        assertTrue(entityManager.contains(basicCmpBean));

        commitTx();
        beginTx();

        basicCmpBean = new BasicCmpBean_Subclass();
        basicCmpBean.ejbCreateObject("Lloyd Dobler");
        assertFalse(entityManager.contains(basicCmpBean));

        entityManager.persist(basicCmpBean);

        // extract primary key seems to require a flush followed by a merge
        entityManager.flush();
        basicCmpBean = entityManager.merge(basicCmpBean);
        int lloydId = basicCmpBean.getPrimaryKey();

        assertTrue(entityManager.contains(basicCmpBean));
        commitTx();


        beginTx();

        BasicCmpBean joe = (BasicCmpBean) entityManager.createQuery("select e from BasicCmpBean_Subclass e where e.primaryKey=" + joeId).getSingleResult();
        assertTrue(entityManager.contains(joe));

        assertEquals(joe.getPrimaryKey(), joeId);
        assertEquals(joe.getFirstName(), "Joe");
        assertEquals(joe.getLastName(), "Blow");

        BasicCmpBean lloyd = (BasicCmpBean) entityManager.createQuery("select e from BasicCmpBean_Subclass e where e.primaryKey=" + lloydId).getSingleResult();
        assertTrue(entityManager.contains(lloyd));

        assertEquals(lloyd.getPrimaryKey(), lloydId);
        assertEquals(lloyd.getFirstName(), "Lloyd");
        assertEquals(lloyd.getLastName(), "Dobler");

        commitTx();
    }

    private void complexCmpText() throws Exception {
        beginTx();

        ComplexCmpBean complexCmpBean = new ComplexCmpBean_Subclass();
        complexCmpBean.ejbCreateObject("Joe Blow");
        assertFalse(entityManager.contains(complexCmpBean));

        entityManager.persist(complexCmpBean);

        // extract primary key seems to require a flush followed by a merge
        entityManager.flush();
        complexCmpBean = entityManager.merge(complexCmpBean);

        assertTrue(entityManager.contains(complexCmpBean));

        commitTx();
        beginTx();

        complexCmpBean = new ComplexCmpBean_Subclass();
        complexCmpBean.ejbCreateObject("Lloyd Dobler");
        assertFalse(entityManager.contains(complexCmpBean));

        entityManager.persist(complexCmpBean);

        // extract primary key seems to require a flush followed by a merge
        entityManager.flush();
        complexCmpBean = entityManager.merge(complexCmpBean);

        assertTrue(entityManager.contains(complexCmpBean));
        commitTx();


        beginTx();

        ComplexCmpBean joe = (ComplexCmpBean) entityManager.createQuery("select e from ComplexCmpBean_Subclass e where e.firstName='Joe'").getSingleResult();
        assertTrue(entityManager.contains(joe));

        assertEquals(joe.getFirstName(), "Joe");
        assertEquals(joe.getLastName(), "Blow");

        ComplexCmpBean lloyd = (ComplexCmpBean) entityManager.createQuery("select e from ComplexCmpBean_Subclass e where e.firstName='Lloyd'").getSingleResult();
        assertTrue(entityManager.contains(lloyd));

        assertEquals(lloyd.getFirstName(), "Lloyd");
        assertEquals(lloyd.getLastName(), "Dobler");

        commitTx();
    }

    private void unknownCmpText() throws Exception {
        beginTx();

        UnknownCmpBean unknownCmpBean = new UnknownCmpBean_Subclass();
        unknownCmpBean.ejbCreateObject("Joe Blow");
        assertFalse(entityManager.contains(unknownCmpBean));

        entityManager.persist(unknownCmpBean);

        // extract primary key seems to require a flush followed by a merge
        entityManager.flush();
        unknownCmpBean = entityManager.merge(unknownCmpBean);
        Object joeId = unknownCmpBean.getPrimaryKey();

        assertTrue(entityManager.contains(unknownCmpBean));

        commitTx();
        beginTx();

        unknownCmpBean = new UnknownCmpBean_Subclass();
        unknownCmpBean.ejbCreateObject("Lloyd Dobler");
        assertFalse(entityManager.contains(unknownCmpBean));

        entityManager.persist(unknownCmpBean);

        // extract primary key seems to require a flush followed by a merge
        entityManager.flush();
        unknownCmpBean = entityManager.merge(unknownCmpBean);
        Object lloydId = unknownCmpBean.getPrimaryKey();

        assertTrue(entityManager.contains(unknownCmpBean));
        commitTx();


        beginTx();

        UnknownCmpBean joe = (UnknownCmpBean) entityManager.createQuery("select e from UnknownCmpBean_Subclass e where e.firstName='Joe'").getSingleResult();
        assertTrue(entityManager.contains(joe));

        assertEquals(joe.getPrimaryKey(), joeId);
        assertEquals(joe.getFirstName(), "Joe");
        assertEquals(joe.getLastName(), "Blow");

        UnknownCmpBean lloyd = (UnknownCmpBean) entityManager.createQuery("select e from UnknownCmpBean_Subclass e where e.firstName='Lloyd'").getSingleResult();
        assertTrue(entityManager.contains(lloyd));

        assertEquals(lloyd.getPrimaryKey(), lloydId);
        assertEquals(lloyd.getFirstName(), "Lloyd");
        assertEquals(lloyd.getLastName(), "Dobler");

        commitTx();
    }

    private void manytoMany() throws Exception {
        beginTx();

        AuthorBean mcLaughlin = new AuthorBean(MCLAUGHLIN);
        AuthorBean flanagan = new AuthorBean(FLANAGAN);

        BookBean tiger = new BookBean(TIGER);
        BookBean javaScript = new BookBean(JAVASCRIPT);

        BookBean enterpriseApps = new BookBean(ENTERPRISE_APPS);

        entityManager.persist(mcLaughlin);
        entityManager.persist(flanagan);
        entityManager.persist(tiger);
        entityManager.persist(javaScript);
        entityManager.persist(enterpriseApps);

        commitTx();

        link(MCLAUGHLIN, TIGER);
        link(MCLAUGHLIN, ENTERPRISE_APPS);
        link(FLANAGAN, TIGER);
        link(FLANAGAN, JAVASCRIPT);

        beginTx();

        BookBean book = entityManager.find(BookBean.class, TIGER);
        assertTrue(entityManager.contains(book));

        assertEquals(book.getTitle(), TIGER);
        Set<AuthorBean> authors = book.getAuthors();
        assertEquals(authors.size(), 2);

        commitTx();
    }

    private void link(String authorName, String title) throws Exception {
        beginTx();
        AuthorBean author = entityManager.find(AuthorBean.class, authorName);
        assertNotNull("Author not found " + authorName, author);

        BookBean book = entityManager.find(BookBean.class, title);
        assertNotNull("Book not found " + title, book);

        book.OpenEJB_addCmr("authors", author);
        author.OpenEJB_addCmr("books", book);
        commitTx();
    }

    private void createEntityManager() {
        entityManager = entityManagerFactory.createEntityManager();

//        OpenJPAEntityManager openjpaEM = (OpenJPAEntityManager) entityManager;
//        openjpaEM.addLifecycleListener(new JpaTestObject.OpenJPALifecycleListener(), (Class[])null);
    }

    private void beginTx() throws Exception {
        createEntityManager();

        String msg = "BEGIN_TX";
        log(msg);
        if (transactionManager != null) {
            transactionManager.begin();
            entityManager.joinTransaction();
        } else {
            transaction = entityManager.getTransaction();
            transaction.begin();
        }
    }

    public void log(String msg) {
//        System.out.println(msg);
    }

    private void commitTx() throws Exception {
        log("  BEFORE_COMMIT_TX");
        try {
            if (transactionManager != null) {
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

//    private class OpenJPALifecycleListener extends AbstractLifecycleListener {
//        protected void eventOccurred(LifecycleEvent event) {
//            int type = event.getType();
//            switch (type) {
//                case LifecycleEvent.BEFORE_PERSIST:
//                    log("  BEFORE_PERSIST");
//                    break;
//                case LifecycleEvent.AFTER_PERSIST:
//                    log("  AFTER_PERSIST");
//                    break;
//                case LifecycleEvent.AFTER_LOAD:
//                    log("  AFTER_LOAD");
//                    break;
//                case LifecycleEvent.BEFORE_STORE:
//                    log("  BEFORE_STORE");
//                    break;
//                case LifecycleEvent.AFTER_STORE:
//                    log("  AFTER_STORE");
//                    break;
//                case LifecycleEvent.BEFORE_CLEAR:
//                    log("  BEFORE_CLEAR");
//                    break;
//                case LifecycleEvent.AFTER_CLEAR:
//                    log("  AFTER_CLEAR");
//                    break;
//                case LifecycleEvent.BEFORE_DELETE:
//                    log("  BEFORE_DELETE");
//                    break;
//                case LifecycleEvent.AFTER_DELETE:
//                    log("  AFTER_DELETE");
//                    break;
//                case LifecycleEvent.BEFORE_DIRTY:
//                    log("  BEFORE_DIRTY");
//                    break;
//                case LifecycleEvent.AFTER_DIRTY:
//                    log("  AFTER_DIRTY");
//                    break;
//                case LifecycleEvent.BEFORE_DIRTY_FLUSHED:
//                    log("  BEFORE_DIRTY_FLUSHED");
//                    break;
//                case LifecycleEvent.AFTER_DIRTY_FLUSHED:
//                    log("  AFTER_DIRTY_FLUSHED");
//                    break;
//                case LifecycleEvent.BEFORE_DETACH:
//                    log("  BEFORE_DETACH");
//                    break;
//                case LifecycleEvent.AFTER_DETACH:
//                    log("  AFTER_DETACH");
//                    break;
//                case LifecycleEvent.BEFORE_ATTACH:
//                    log("  BEFORE_ATTACH");
//                    break;
//                case LifecycleEvent.AFTER_ATTACH:
//                    log("  AFTER_ATTACH");
//                    break;
//                case LifecycleEvent.AFTER_REFRESH:
//                    log("  AFTER_REFRESH");
//                    break;
//                default:
//                    log("  default");
//                    break;
//            }
//            super.eventOccurred(event);
//        }
//
//        public void beforePersist(LifecycleEvent event) {
//            eventOccurred(event);
//        }
//
//        public void afterRefresh(LifecycleEvent event) {
//            eventOccurred(event);
//        }
//
//        public void beforeDetach(LifecycleEvent event) {
//            eventOccurred(event);
//        }
//
//        public void afterDetach(LifecycleEvent event) {
//            eventOccurred(event);
//        }
//
//        public void beforeAttach(LifecycleEvent event) {
//            eventOccurred(event);
//        }
//
//        public void afterAttach(LifecycleEvent event) {
//            eventOccurred(event);
//        }
//    }

    protected void dump() throws SQLException {
        JpaTestObject.dumpTable(nonJtaDs, "employee");
        JpaTestObject.dumpTable(nonJtaDs, "OneToOneA");
        JpaTestObject.dumpTable(nonJtaDs, "OneToOneB");
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
            JpaTestObject.close(resultSet);
            JpaTestObject.close(statement);
            JpaTestObject.close(connection);
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
