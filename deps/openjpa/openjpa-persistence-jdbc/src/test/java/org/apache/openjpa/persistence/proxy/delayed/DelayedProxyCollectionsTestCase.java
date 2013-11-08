/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.persistence.proxy.delayed;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.kernel.DetachedStateManager;
import org.apache.openjpa.persistence.test.SQLListenerTestCase;
import org.apache.openjpa.util.DelayedProxy;
import org.apache.openjpa.util.Proxy;
import org.apache.openjpa.util.ProxyCollection;

/**
 * Verifies generic delay-load capabilities for delay-load collection proxies.
 */
public abstract class DelayedProxyCollectionsTestCase extends SQLListenerTestCase {
    
    protected static Set<String> _ignoreMethods;
    protected static Set<String> _delayMethods;
    protected static Set<Class<?>> _ignoreInterfaces;
    
    public void setUp(Object...props) {
        List<Object> parms = new ArrayList<Object>();
        parms.addAll(Arrays.asList(
                CLEAR_TABLES,
                "openjpa.ProxyManager", "delayCollectionLoading=true",
                Award.class, 
                Location.class,
                Product.class,
                Certification.class));
        parms.addAll(Arrays.asList(props));
        super.setUp(parms.toArray());
    }
    
    public abstract IAccount createAccount(String name, IUserIdentity ui);
    public abstract IDepartment createDepartment();
    public abstract IDepartment findDepartment(EntityManager em, int id);
    public abstract IEmployee createEmployee();
    public abstract Collection<IEmployee> createEmployees();
    public abstract IMember createMember(String name);
    public abstract IUserIdentity findUserIdentity(EntityManager em, int id);
    public abstract IUserIdentity createUserIdentity();
    public abstract Collection<Product> createProducts();
    public abstract Collection<Certification> createCertifications();
    public abstract IEmployee getEmployee(Collection<IEmployee> emps, int idx);
    public abstract Collection<Award> createAwards();
    public abstract Product getProduct(Collection<Product> products, int idx);
    public abstract Collection<Location> createLocations();
    public abstract Collection<IAccount> createAccounts();

    static {
        // non-indexed delay-capable methods
        _delayMethods = new HashSet<String>();
        // generic collection
        _delayMethods.add(stringMethodName("add", new Class<?>[] {Object.class}));
        _delayMethods.add(stringMethodName("remove", new Class<?>[] {Object.class}));
        _delayMethods.add(stringMethodName("removeAll", new Class<?>[] {Collection.class}));
        _delayMethods.add(stringMethodName("addAll", new Class<?>[] {Collection.class}));
        // queue
        _delayMethods.add(stringMethodName("offer", new Class<?>[] {Object.class}));
        // vector
        _delayMethods.add(stringMethodName("addElement", new Class<?>[] {Object.class}));
        _delayMethods.add(stringMethodName("removeElement", new Class<?>[] {Object.class}));

        // non-trigger methods
        _ignoreMethods = new HashSet<String>();
        _ignoreMethods.add(stringMethodName("trimToSize", null));
        _ignoreMethods.add(stringMethodName("ensureCapacity", new Class<?>[] {int.class}));
        _ignoreMethods.add(stringMethodName("comparator", null));
        
        // non-trigger base Object methods
        _ignoreMethods.add(stringMethodName("wait", new Class<?>[] {long.class}));
        _ignoreMethods.add(stringMethodName("wait", null));
        _ignoreMethods.add(stringMethodName("wait", new Class<?>[] {long.class, int.class}));
        _ignoreMethods.add(stringMethodName("getClass", null));
        _ignoreMethods.add(stringMethodName("notify", null));
        _ignoreMethods.add(stringMethodName("notifyAll", null));
                
        _ignoreInterfaces = new HashSet<Class<?>>();
        _ignoreInterfaces.add(DelayedProxy.class);
        _ignoreInterfaces.add(Proxy.class);
        _ignoreInterfaces.add(ProxyCollection.class);
    }

    public static String stringMethodName(Method m) {
        return stringMethodName(m.getName(), m.getParameterTypes());
    }

    public static String stringMethodName(String m, Class<?>[] types) {
        StringBuilder sb = new StringBuilder(m);
        if (types != null) {
            for (Class<?> type : types) {
                sb.append(":");
                sb.append(type.getName());
            }
        }
        return sb.toString();
    }
    
    public Set<String> methodsToIgnore() {
        return _ignoreMethods;
    }

    public Award createAward() {
        Award a = new Award();
        a.setAwdName("Employee of the Month " + new Random().nextInt(999999));
        a.setAwdType("Certificate");
        return a;
    }

    public Certification createCertification() {
        Certification c = new Certification();
        c.setName("Certification XYZ " + new Random().nextInt(999999));
        c.setCertDate(new Date());
        return c;
    }

    public Product createProduct() {
        Product p = new Product();
        p.setName("Product : " + new Random().nextInt(999999));
        return p;
    }

    public Location createLocation() {
        Location l = new Location();
        l.setAddress(new Random().nextInt(9999) + " Wandering Way");
        l.setCity("Somewhere");
        l.setZip(Integer.toString(new Random().nextInt(99999)));
        return l;
    }
    
    /*
     * Verify an element can be non-index removed from a delayed proxy collection
     * without triggering a load of the collection.
     */
    public void testSingleRemove() {
        
        EntityManager em = emf.createEntityManager();
        
        // Create a new department and an employee
        IDepartment d = createDepartment();
        IEmployee e = createEmployee();
        e.setDept(d);
        e.setEmpName("John");
        IEmployee e2 = createEmployee();
        e2.setDept(d);
        e2.setEmpName("Joe");
        Collection<IEmployee> emps = createEmployees();
        emps.add(e);
        emps.add(e2);
        d.setEmployees(emps);
        
        em.getTransaction().begin();
        em.persist(d);
        em.getTransaction().commit();
        em.clear();
        
        resetSQL();
        d = findDepartment(em, d.getId());
        // assert the select did not contain the employee table
        assertNoneSQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
        assertNotNull(d);
        emps = d.getEmployees();
        // assert there was no select
        assertNoneSQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
        assertTrue(emps instanceof DelayedProxy);
        DelayedProxy dep = (DelayedProxy)emps;
        dep.setDirectAccess(true);
        assertEquals(0, emps.size());
        dep.setDirectAccess(false);
        assertNotNull(emps);

        // remove the employee from the collection
        resetSQL();
        em.getTransaction().begin();
        emps.remove(e);
        em.getTransaction().commit();
        // assert the delete from the join table
        assertAnySQLAnyOrder("DELETE FROM DC_DEP_EMP .*");
        // assert no select from employee or dept table
        assertNoneSQLAnyOrder("SELECT .* DC_EMPLOYEE .*", "SELECT .* DC_DEPARTMENT .*");
        
        // iterate the collection and assert a select from the employee table
        // and that the expected entity is returned
        resetSQL();
        assertEquals(1, emps.size());
        assertAnySQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
        IEmployee e3 = getEmployee(emps, 0);
        assertEquals(e2, e3);
        em.close();
    }

    /*
     * Verify an element can be non-index added to a delayed proxy collection
     * without triggering a load on the collection.
     */
    public void testSingleAdd() {
        EntityManager em = emf.createEntityManager();
        
        // Create a new department and an employee
        IDepartment d = createDepartment();
        IEmployee e = createEmployee();
        e.setDept(d);
        e.setEmpName("John");
        Collection<IEmployee> emps = createEmployees();
        emps.add(e);
        d.setEmployees(emps);
        
        em.getTransaction().begin();
        em.persist(d);
        em.getTransaction().commit();
        em.clear();
        
        resetSQL();
        d = findDepartment(em, d.getId());
        // assert the select did not contain the employee table
        assertNoneSQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
        assertNotNull(d);
        emps = d.getEmployees();
        // assert there was no select
        assertNoneSQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
        assertTrue(emps instanceof DelayedProxy);
        DelayedProxy dep = (DelayedProxy)emps;
        dep.setDirectAccess(true);
        assertEquals(0, emps.size());
        dep.setDirectAccess(false);
        assertNotNull(emps);

        // add an employee to the collection
        resetSQL();
        em.getTransaction().begin();
        IEmployee e2 = createEmployee();
        e2.setDept(d);
        e2.setEmpName("Joe");
        emps.add(e2);
        em.getTransaction().commit();
        // assert the insert into the employee and join table
        assertAnySQLAnyOrder("INSERT INTO DC_DEP_EMP .*");
        assertAnySQLAnyOrder("INSERT INTO DC_EMPLOYEE .*");
        // assert no select from employee or dept table
        assertNoneSQLAnyOrder("SELECT .* DC_EMPLOYEE .*", "SELECT .* DC_DEPARTMENT .*");
        
        // call contains and assert a select from the employee table
        // occurred that the expected entities are returned.
        resetSQL();
        assertTrue(emps.contains(e));
        assertTrue(emps.contains(e2));
        assertAnySQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
        resetSQL();
        assertEquals(2, emps.size());
        // verify a second SQL was not issued to get the size
        assertNoneSQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
        em.close();
    }
    
    /*
     * Verify a mix of non-indexed add and remove operations can occur without
     * triggering a load on a delayed collection. 
     */
    public void testMixedAddRemove() {
        EntityManager em = emf.createEntityManager();
        
        // Create a new department and an employee
        IDepartment d = createDepartment();
        IEmployee e = createEmployee();
        e.setDept(d);
        e.setEmpName("John");
        Collection<IEmployee> emps = createEmployees();
        emps.add(e);
        d.setEmployees(emps);
        
        em.getTransaction().begin();
        em.persist(d);
        em.getTransaction().commit();
        em.clear();
        
        resetSQL();
        d = findDepartment(em, d.getId());
        // assert the select did not contain the employee table
        assertNoneSQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
        assertNotNull(d);
        emps = d.getEmployees();
        // assert there was no select
        assertNoneSQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
        assertTrue(emps instanceof DelayedProxy);
        DelayedProxy dep = (DelayedProxy)emps;
        dep.setDirectAccess(true);
        assertEquals(0, emps.size());
        dep.setDirectAccess(false);
        assertNotNull(emps);

        // add an employee to the collection and remove the same employee and commit
        resetSQL();
        em.getTransaction().begin();
        IEmployee e2 = createEmployee();
        e2.setDept(d);
        e2.setEmpName("Joe");
        emps.add(e2);
        emps.remove(e2);
        em.getTransaction().commit();
        // assert the insert into the entity and join table
        assertNoneSQLAnyOrder("INSERT INTO DC_DEP_EMP .*");
        assertNoneSQLAnyOrder("INSERT INTO DC_EMPLOYEE .*");
        // assert no select from employee or dept table
        assertNoneSQLAnyOrder("SELECT .* DC_EMPLOYEE .*", "SELECT .* DC_DEPARTMENT .*");

        // add two employees to the collection and remove one and commit
        resetSQL();
        em.getTransaction().begin();
        IEmployee e3 = createEmployee();
        e3.setDept(d);
        e3.setEmpName("Rhonda");
        emps.add(e3);

        IEmployee e4 = createEmployee();
        e4.setDept(d);
        e4.setEmpName("Maria");
        emps.add(e4);
        emps.remove(e3);
        em.getTransaction().commit();
        // assert the insert into the employee and join table
        assertAnySQLAnyOrder("INSERT INTO DC_DEP_EMP .*");
        assertAnySQLAnyOrder("INSERT INTO DC_EMPLOYEE .*");
        // assert no select from employee or dept table
        assertNoneSQLAnyOrder("SELECT .* DC_EMPLOYEE .*", "SELECT .* DC_DEPARTMENT .*");

        // call contains and assert a select from the employee table
        // occurred that the expected entities are returned.
        resetSQL();
        assertTrue(emps.contains(e));
        assertFalse(emps.contains(e2));
        assertFalse(emps.contains(e3));
        assertTrue(emps.contains(e4));
        assertAnySQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
        resetSQL();
        assertEquals(2, emps.size());
        // verify a second SQL was not issued to get the size
        assertNoneSQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
        em.close();
    }

    /*
     * Verify that an eagerly loaded collection with delayed load enabled
     * functions as expected.
     */
    public void testEagerCollection() {
        EntityManager em = emf.createEntityManager();

        // Create a new department and 
        IDepartment d = createDepartment();
        Collection<Product> products = createProducts();
        
        Product p = createProduct();
        products.add(p);
        Product p2 = createProduct();
        products.add(p2);
        d.setProducts(products);
        
        em.getTransaction().begin();
        em.persist(d);
        em.getTransaction().commit();
        resetSQL();

        em.clear();
        
        d = findDepartment(em, d.getId());
        assertAnySQLAnyOrder("SELECT .* DC_DEP_PRD .*");
        resetSQL();
        products = d.getProducts();
        assertTrue(products instanceof DelayedProxy);
        ProxyCollection pxycoll = (ProxyCollection)products;
        assertFalse(pxycoll.getOwner().isDelayed(pxycoll.getOwnerField()));
        Product p3 = getProduct(products, 0);
        assertTrue(products.contains(p3));
        assertEquals(2, products.size());
        assertNoneSQLAnyOrder("SELECT .* DC_DEPARTMENT .*", "SELECT .* DC_DEP_PRD .*");
        em.close();
    }
    
    /*
     * Verify that a DB ordered collection is not delay load capable.
     */
    public void testOrderedCollection() {
        EntityManager em = emf.createEntityManager();
        
        // Create a new department and persist
        IDepartment d = createDepartment();
        
        Location l = createLocation();
        Collection<Location> locs = createLocations();
        locs.add(l);
        d.setLocations(locs);
        
        em.getTransaction().begin();
        em.persist(d);
        em.getTransaction().commit();
        
        em.clear();
        
        d = findDepartment(em, d.getId());
        assertNoneSQLAnyOrder("SELECT .* DC_DEP_LOC .*");
        // verify that the collection is not delay loaded and does not trigger a load
        resetSQL();
        Collection<Location> locations = d.getLocations();
        assertAnySQLAnyOrder("SELECT .* DC_DEP_LOC .*");
        resetSQL();
        assertTrue(locations instanceof DelayedProxy);
        ProxyCollection pxycoll = (ProxyCollection)locations;
        assertFalse(pxycoll.getOwner().isDelayed(pxycoll.getOwnerField()));
        assertEquals(1, locations.size());
        assertNoneSQLAnyOrder("SELECT .* DC_DEPARTMENT .*", "SELECT .* DC_DEP_LOC .*");
        em.close();
    }
    
    /*
     * Verify that a collection will load upon serialization
     */
    public void testSerialization() {
        EntityManager em = emf.createEntityManager();
        
        // Create a new department and an employee
        IDepartment d = createDepartment();
        IEmployee e = createEmployee();
        e.setDept(d);
        e.setEmpName("John");
        Collection<IEmployee> emps = createEmployees();
        emps.add(e);
        d.setEmployees(emps);
        
        em.getTransaction().begin();
        em.persist(d);
        em.getTransaction().commit();
        em.clear();
        
        resetSQL();
        d = findDepartment(em, d.getId());
        // assert the select did not contain the employee table
        assertNoneSQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
        assertNotNull(d);
        emps = d.getEmployees();
        // assert there was no select
        assertNoneSQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
        assertTrue(emps instanceof DelayedProxy);
        DelayedProxy dep = (DelayedProxy)emps;
        dep.setDirectAccess(true);
        assertEquals(0, emps.size());
        dep.setDirectAccess(false);
        assertNotNull(emps);

        // add an employee to the collection
        resetSQL();
        em.getTransaction().begin();
        IEmployee e2 = createEmployee();
        e2.setDept(d);
        e2.setEmpName("Joe");
        emps.add(e2);
        em.getTransaction().commit();
        // assert the insert into the employee and join table
        assertAnySQLAnyOrder("INSERT INTO DC_DEP_EMP .*");
        assertAnySQLAnyOrder("INSERT INTO DC_EMPLOYEE .*");
        // assert no select from employee or dept table
        assertNoneSQLAnyOrder("SELECT .* DC_EMPLOYEE .*", "SELECT .* DC_DEPARTMENT .*");
        
        resetSQL();
        try {
            // Serialize the department entity and verify the employee collection was loaded
            IDepartment d2 = roundtrip(d);
            assertAnySQLAnyOrder("SELECT .* DC_EMPLOYEE .*", "SELECT .* DC_DEP_EMP .*");
            emps = d2.getEmployees();
            assertTrue(emps.contains(e));
            assertTrue(emps.contains(e2));
            assertEquals(2, emps.size());
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
        
        em.close();
    }
    
    /*
     * Verify that a lazy collection of embeddables works as expected
     * (delays load) with delayed loading enabled
     */
    public void testLazyEmbeddableCollection() {
        EntityManager em = emf.createEntityManager();
        
        IDepartment d = createDepartment();
        
        Collection<Certification> certs = createCertifications();
        certs.add(createCertification());
        certs.add(createCertification());
        d.setCertifications(certs);
        
        em.getTransaction().begin();
        em.persist(d);
        em.getTransaction().commit();
        
        resetSQL();

        em.clear();
        
        d = findDepartment(em, d.getId());
        assertNoneSQLAnyOrder("SELECT .* DC_DEP_CERT .*");
        resetSQL();
        certs = d.getCertifications();
        assertNoneSQLAnyOrder("SELECT .* DC_DEP_CERT .*");
        assertTrue(certs instanceof DelayedProxy);
        assertEquals(2,certs.size());
        assertAnySQLAnyOrder("SELECT .* DC_DEP_CERT .*");
        
        em.close();
    }

    /*
     * Verify that an eager collection of embeddables works as expected
     * (no delay load) with delayed loading enabled
     */
    public void testEagerEmbeddableCollection() {
        EntityManager em = emf.createEntityManager();
        
        IDepartment d = createDepartment();
        
        Collection<Award> awards = createAwards();
        awards.add(createAward());
        awards.add(createAward());
        awards.add(createAward());
        d.setAwards(awards);
        
        em.getTransaction().begin();
        em.persist(d);
        em.getTransaction().commit();
        
        resetSQL();

        em.clear();
        
        d = findDepartment(em, d.getId());
        assertAnySQLAnyOrder("SELECT .* DC_DEP_AWD .*");
        resetSQL();
        awards = d.getAwards();
        ProxyCollection pxycoll = (ProxyCollection)awards;
        assertFalse(pxycoll.getOwner().isDelayed(pxycoll.getOwnerField()));
        assertNoneSQLAnyOrder("SELECT .* DC_DEP_AWD .*");
        assertTrue(awards instanceof DelayedProxy);
        assertEquals(3,awards.size());
        assertNoneSQLAnyOrder("SELECT .* DC_DEP_AWD .*");
        
        em.close();
    }


    /*
     * Verify that a collection can be loaded post detachment
     */
    public void testPostDetach() {
        EntityManager em = emf.createEntityManager();
        
        // Create a new department and an employee
        IDepartment d = createDepartment();
        IEmployee e = createEmployee();
        e.setDept(d);
        e.setEmpName("John");
        Collection<IEmployee> emps = createEmployees();
        emps.add(e);
        d.setEmployees(emps);
        
        em.getTransaction().begin();
        em.persist(d);
        em.getTransaction().commit();
        resetSQL();
        em.clear();
        
        d = findDepartment(em, d.getId());
        emps = d.getEmployees();
        em.close();
        
        // assert there was no select on the employee table
        assertNoneSQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
        assertTrue(emps instanceof DelayedProxy);
        DelayedProxy dep = (DelayedProxy)emps;
        dep.setDirectAccess(true);
        assertEquals(0, emps.size());
        dep.setDirectAccess(false);
        assertNotNull(emps);
        // call contains and assert a select from the employee table
        // occurred that the expected entities are returned.
        resetSQL();
        assertTrue(emps.contains(e));
        e = getEmployee(emps, 0);
        assertAnySQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
        resetSQL();
        assertEquals(1, emps.size());
        // Verify the delay load entity is detached
        assertTrue(e instanceof PersistenceCapable);
        PersistenceCapable pc = (PersistenceCapable)e;
        assertTrue(pc.pcGetStateManager() instanceof DetachedStateManager);
        // verify a second SQL was not issued to get the size
        assertNoneSQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
        
        // add a employee to the collection and merge
        IEmployee e2 = createEmployee();
        e2.setDept(d);
        emps.add(e2);
        em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(d);
        em.getTransaction().commit();
        emps = d.getEmployees();
        // assert the insert into the employee and join table
        assertAnySQLAnyOrder("INSERT INTO DC_DEP_EMP .*");
        assertAnySQLAnyOrder("INSERT INTO DC_EMPLOYEE .*");
        assertEquals(2, emps.size());
        em.close();

        // remove an employee from the collection and merge
        emps.remove(e);
        em = emf.createEntityManager();
        em.getTransaction().begin();
        em.merge(d);
        em.getTransaction().commit();
        emps = d.getEmployees();
        
        // assert the delete from the join table
        assertAnySQLAnyOrder("DELETE FROM DC_DEP_EMP .*");
        assertEquals(1, emps.size());
        em.close();
    }
    
    /*
     * Verify that a lazy collection within an embeddable can be
     * delayed.  The to-many in the embeddable uses 
     */
    public void testEmbeddableRelationship() {
        EntityManager em = emf.createEntityManager();
        
        IUserIdentity ui = createUserIdentity();
        IMember m = createMember("Member 1");
        ui.setMember(m);
        
        Collection<IAccount> accounts = createAccounts();
        IAccount checking = createAccount("Checking", ui);
        accounts.add(checking);
        IAccount savings = createAccount("Savings", ui);
        accounts.add(savings);
        
        em.getTransaction().begin();
        em.persist(ui);
        em.persist(checking);
        em.persist(savings);
        em.getTransaction().commit();
        
        em.clear();
        
        ui = findUserIdentity(em, ui.getId());
        
        m = ui.getMember();
        resetSQL();
        accounts = m.getAccounts();
        
        ProxyCollection pxycoll = (ProxyCollection)accounts;
        assertTrue(pxycoll.getOwner().isDelayed(pxycoll.getOwnerField()));
        assertNoneSQLAnyOrder("SELECT .* DC_ACCOUNT .*");
        assertTrue(accounts instanceof DelayedProxy);
        // Trigger a load via iterator
        int count = 0;
        for (IAccount a : accounts) {
            count++;
        }
        assertEquals(2,count);
        assertAnySQLAnyOrder("SELECT .* DC_ACCOUNT .*");
        
        em.close();
    }

    /**
     * Verifies proxy methods which require loading the collection will trigger a
     * load.
     */
    public void testProxyMethods() {
        // Load up a collection
        EntityManager em = emf.createEntityManager();
        
        // Create a new department and employees
        IDepartment d = createDepartment();
        Collection<IEmployee> emps = createEmployees();
        for (int i = 0; i < 50; i++) {
            IEmployee e = createEmployee();
            e.setDept(d);
            e.setEmpName("Employee: " + i);
            emps.add(e);
        }
        d.setEmployees(emps);
        
        em.getTransaction().begin();
        em.persist(d);
        em.getTransaction().commit();
        em.clear();
        
        resetSQL();
        
        // build a list of public proxy methods
        // exclude those methods that are certain not to cause a load
        // add(Object) remove(Object), addAll(Collection), removeAll(Collection), poll?, copy()
        Class<?> collType = emps.getClass();
        Method[] methods = collType.getMethods();
        for (Method m : methods) {
            if (!excludeMethod(m)) {
                buildAndInvoke(m, em, d.getId(), emps);
            }
        }
        em.close();
    }

    private void buildAndInvoke(Method m, EntityManager em, int id, Collection<IEmployee> emps) {
        em.clear();
        resetSQL();
        IDepartment d = findDepartment(em, id);
        Collection<?> emps2 = d.getEmployees();
        assertTrue(emps2 instanceof DelayedProxy);
        assertNoneSQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
        try {
            m.invoke(emps2, buildArgs(m, emps));
            // not checking result or exception, just whether load was triggered
        } catch (Throwable t) {
            // gulp
        }
        if (_delayMethods.contains(stringMethodName(m))) {
            assertNoneSQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
        } else {
            assertAnySQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
        }
    }

    /**
     * Build up a set of generic args just to get the basic calls through.
     */
    private Object[] buildArgs(Method m, Collection<?> emps) {
        Class<?>[] parmTypes = m.getParameterTypes();
        if (parmTypes == null) {
            return new Object[]{};
        }
        int intNum = 0;
        int objNum = 0;
        Object[] parms = new Object[parmTypes.length];
        for (int i = 0; i < parmTypes.length; i++) {
            Class<?> parmType = parmTypes[i];
            if (parmTypes[i].equals(int.class)) {
                parms[i] = intNum;
                intNum++;
                continue;
            }
            if (parmTypes[i].equals(boolean.class)) {
                parms[i] = true;
                continue;
            }
            if (parmTypes[i].equals(Object.class)) {
                parms[i] = emps.toArray()[objNum];
                objNum++;
                continue;
            }
            if (parmTypes[i].isAssignableFrom(Collection.class)) {
                parms[i] = emps;
                continue;
            }
        }
        return parms;
    }

    /*
     * Determines whether a proxy method should be invoked
     */
    private boolean excludeMethod(Method m) {
        if(_ignoreInterfaces.contains(m.getDeclaringClass())) {
            return true;
        }
        if (_ignoreMethods.contains(stringMethodName(m))) {
            return true;
        }
        return false;
    }
}
