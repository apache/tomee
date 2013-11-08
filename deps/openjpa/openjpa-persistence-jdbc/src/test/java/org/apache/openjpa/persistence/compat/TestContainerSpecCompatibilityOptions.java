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
package org.apache.openjpa.persistence.compat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.conf.Compatibility;
import org.apache.openjpa.conf.Specification;
import org.apache.openjpa.lib.jdbc.AbstractJDBCListener;
import org.apache.openjpa.lib.jdbc.JDBCEvent;
import org.apache.openjpa.lib.jdbc.JDBCListener;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.jdbc.SQLSniffer;
import org.apache.openjpa.persistence.test.ContainerEMFTest;

// Tests same functionality as TestSpecCompatibilityOptions, except that 
// this variation runs through the path a JEE container uses to create
// an EMF.
public class TestContainerSpecCompatibilityOptions 
    extends ContainerEMFTest {
    
    protected List<String> sql = new ArrayList<String>();
    protected int sqlCount;

    @Override
    public String getPersistenceResourceName() {
        return "org/apache/openjpa/persistence/compat/persistence_2_0.xml";
    }

    @Override
    public String getPersistenceUnitName() {
        return "persistence_2_0";
    }
    
    /*
     * Verifies compatibility options and spec level are appropriate
     * for a version 2 persistence.xml
     */
    public void testJPA1CompatibilityOptions() {
        OpenJPAEntityManagerFactorySPI emf1 =
        (OpenJPAEntityManagerFactorySPI)
            createContainerEMF("persistence_1_0",
                "org/apache/openjpa/persistence/compat/" +
                "persistence_1_0.xml", null);

        Compatibility compat = emf1.getConfiguration().getCompatibilityInstance();
        assertTrue(compat.getFlushBeforeDetach());
        assertTrue(compat.getCopyOnDetach());
        assertTrue(compat.getIgnoreDetachedStateFieldForProxySerialization());
        assertTrue(compat.getPrivatePersistentProperties());
        assertFalse(compat.isAbstractMappingUniDirectional());
        assertFalse(compat.isNonDefaultMappingAllowed());
        String vMode = emf1.getConfiguration().getValidationMode();
        assertEquals("NONE", vMode);
        Specification spec = emf1.getConfiguration().getSpecificationInstance();
        assertEquals("JPA", spec.getName().toUpperCase());
        assertEquals(spec.getVersion(), 1);
        
        closeEMF(emf1);

    }

    /*
     * Verifies compatibility options and spec level are appropriate
     * for a version 2 persistence.xml
     */
    public void testJPA2CompatibilityOptions() {
        Compatibility compat = emf.getConfiguration().getCompatibilityInstance();
        assertFalse(compat.getFlushBeforeDetach());
        assertFalse(compat.getCopyOnDetach());
        assertFalse(compat.getIgnoreDetachedStateFieldForProxySerialization());
        assertFalse(compat.getPrivatePersistentProperties());
        assertTrue(compat.isAbstractMappingUniDirectional());
        assertTrue(compat.isNonDefaultMappingAllowed());
        String vMode = emf.getConfiguration().getValidationMode();
        assertEquals("AUTO", vMode);
        Specification spec = emf.getConfiguration().getSpecificationInstance();
        assertEquals("JPA", spec.getName().toUpperCase());
        assertEquals(spec.getVersion(), 2);
    }

    /*
     * Per JPA 2.0, Relationships in mapped superclass must be unidirectional.
     * An exceptioin will be thrown when a bi-directional relation is detected in
     * a mapped superclass. 
     */
    public void testMappedSuperClass() {
        List<Class<?>> types = new ArrayList<Class<?>>();
        types.add(EntityA.class);
        types.add(EntityB.class);
        types.add(MappedSuper.class);
        OpenJPAEntityManagerFactorySPI oemf = createEMF2_0(types);
        EntityManager em = null;
        try {
            em = oemf.createEntityManager();
            EntityA a = new EntityA();
            a.setId(1);
            EntityB b = new EntityB();
            b.setId(1);
            a.setEntityB(b);
            b.setEntityA(a);
            em.getTransaction().begin();
            em.persist(a);
            em.persist(b);
            em.getTransaction().commit();
            em.close();
            fail("An exceptioin will be thrown for a bi-directional relation declared in mapped superclass");
        } catch (org.apache.openjpa.persistence.ArgumentException e) {
            if (em != null) {
                em.getTransaction().rollback();
                em.close();
            }
        } finally {
            closeEMF(oemf);
        }
    }

    /**
     * Per JPA 2.0, the following one-to-many mappings are supported.
     * (1) uni-/OneToMany/foreign key strategy
     * (2) uni-/OneToMany/join table strategy (default)
     * (3) bi-/OneToMany/foreign key strategy (default)
     * (4) bi-/OneToMany/join table strategy
     * The JoinColumn and JoinTable annotations or corresponding XML 
     * elements must be used to specify such non-default mappings
     * 
     * For (1), the spec provides the following example (Sec 11.1.36):
     * Example 3: Unidirectional One-to-Many association using a foreign 
     * key mapping:
     * In Customer class:
     * @OneToMany(orphanRemoval=true)
     * @JoinColumn(name="CUST_ID") // join column is in table for Order
     * public Set<Order> getOrders() {return orders;}
     * 
     * For (4), Bi-directional One-t-Many association using the join 
     * table mapping:
     * In Customer class:
     * @OneToMany(mappedBy="customer")
     * @JoinTable(
     *   name="Customer_Orders",
     *   joinColumns=
     *     @JoinColumn(name="Order_ID", referencedColumnName="ID"),
     *    inverseJoinColumns=
     *     @JoinColumn(name="Cust_ID", referencedColumnName="ID")
     *  )
     *  public Set<Order> getOrders() {return orders;}
     *  
     *  Note that in this scenario, @JoinTable is required. Simply applying @JoinColumn 
     *  without @JoinTable will result in an exception thrown by openjpa.
     * 
     */
    public void testOneToManyRelation() {
        List<Class<?>> types = new ArrayList<Class<?>>();
        types.add(EntityC.class);
        types.add(EntityC_B1MFK.class);
        types.add(EntityC_B1MJT.class);
        types.add(EntityC_U1MFK.class);
        types.add(Bi_1ToM_FK.class);
        types.add(Bi_1ToM_JT.class);
        types.add(Uni_1ToM_FK.class);
        types.add(Uni_1ToM_JT.class);
        OpenJPAEntityManagerFactorySPI oemf = createEMF2_0(types);
        EntityManager em = oemf.createEntityManager();
        
        try {
            // trigger table creation
            em.getTransaction().begin();
            em.getTransaction().commit();
            assertSQLFragnments(sql, "CREATE TABLE Bi1MJT_C", "C_ID", "Bi1MJT_ID");
            assertSQLFragnments(sql, "CREATE TABLE C_B1MFK", "BI1MFK_ID");
            assertSQLFragnments(sql, "CREATE TABLE Uni1MJT_C", "Uni1MJT_ID", "C_ID");
            assertSQLFragnments(sql, "CREATE TABLE C_B1MFK", "BI1MFK_ID");
            assertSQLFragnments(sql, "CREATE TABLE C_U1MFK", "Uni1MFK_ID");
            crudUni1MFK(em);
            crudUni1MJT(em);
            crudBi1MFK(em);
            crudBi1MJT(em);
        } catch (Exception e) {
            e.printStackTrace();
            fail("OneToMany mapping failed with exception message: " + e.getMessage());
        } finally {
            em.close();
            closeEMF(oemf);            
        }
    }
    
    // non default
    public void crudUni1MFK(EntityManager em) {
        //create
        Uni_1ToM_FK u = new Uni_1ToM_FK();
        u.setName("u");
        List<EntityC_U1MFK> cs = new ArrayList<EntityC_U1MFK>();
        EntityC_U1MFK c = new EntityC_U1MFK();
        c.setName("c");
        cs.add(c);
        u.setEntityCs(cs);
        em.persist(u);
        em.persist(c);
        em.getTransaction().begin();
        em.getTransaction().commit();

        //update
        em.getTransaction().begin();
        cs = u.getEntityCs();
        u.setName("newName");
        EntityC_U1MFK c1 = new EntityC_U1MFK();
        c1.setName("c1");
        cs.add(c1);
        em.persist(c1);
        em.getTransaction().commit();
        
        // update by removing a c and then add this c to a new u
        em.getTransaction().begin();
        EntityC_U1MFK c2 = cs.remove(0);
        
        Uni_1ToM_FK u2 = new Uni_1ToM_FK();
        u2.setName("u2");
        List<EntityC_U1MFK> cs2 = new ArrayList<EntityC_U1MFK>();
        cs2.add(c2);
        u2.setEntityCs(cs2);
        em.persist(u2);
        em.getTransaction().commit();
        em.clear();
        
        //query
        Query q = em.createQuery("SELECT u FROM Uni_1ToM_FK u where u.name = 'newName'");
        Uni_1ToM_FK u1 = (Uni_1ToM_FK)q.getSingleResult();
        assertEquals(u, u1);
        em.clear();

        //find
        long id = u1.getId();
        Uni_1ToM_FK findU1 = em.find(Uni_1ToM_FK.class, id);
        assertEquals(findU1, u1);
        
        //remove
        em.getTransaction().begin();
        em.remove(findU1);
        em.getTransaction().commit();
        em.clear();
    }
    
    // default
    public void crudUni1MJT(EntityManager em) {
        Uni_1ToM_JT u = new Uni_1ToM_JT();
        u.setName("u");
        List<EntityC> cs = new ArrayList<EntityC>();
        EntityC c = new EntityC();
        c.setName("c");
        cs.add(c);
        u.setEntityCs(cs);
        em.persist(u);
        em.persist(c);
        em.getTransaction().begin();
        em.getTransaction().commit();
        
        //update
        em.getTransaction().begin();
        cs = u.getEntityCs();
        u.setName("newName");
        EntityC c1 = new EntityC();
        c1.setName("c1");
        cs.add(c1);
        em.persist(c1);
        em.getTransaction().commit();
        em.clear();
        
        //query
        Query q = em.createQuery("SELECT u FROM Uni_1ToM_JT u");
        Uni_1ToM_JT u1 = (Uni_1ToM_JT)q.getSingleResult();
        assertEquals(u, u1);
        em.clear();

        //find
        long id = u1.getId();
        Uni_1ToM_JT u2 = em.find(Uni_1ToM_JT.class, id);
        assertEquals(u, u2);
        
        //remove
        em.getTransaction().begin();
        em.remove(u2);
        em.getTransaction().commit();
        em.clear();
    }
    
    //default
    public void crudBi1MFK(EntityManager em) {
        Bi_1ToM_FK b = new Bi_1ToM_FK();
        b.setName("b");
        List<EntityC_B1MFK> cs = new ArrayList<EntityC_B1MFK>();
        EntityC_B1MFK c = new EntityC_B1MFK();
        c.setName("c");
        c.setBi1mfk(b);
        cs.add(c);
        b.setEntityCs(cs);
        em.persist(b);
        em.persist(c);
        em.getTransaction().begin();
        em.getTransaction().commit();
        
        //update
        em.getTransaction().begin();
        cs = b.getEntityCs();
        b.setName("newName");
        EntityC_B1MFK c1 = new EntityC_B1MFK();
        c1.setName("c1");
        cs.add(c1);
        c1.setBi1mfk(b);
        em.persist(c1);
        em.getTransaction().commit();
        em.clear();
        
        //query
        Query q = em.createQuery("SELECT b FROM Bi_1ToM_FK b");
        Bi_1ToM_FK b1 = (Bi_1ToM_FK)q.getSingleResult();
        assertEquals(b, b1);
        em.clear();

        //find
        long id = b1.getId();
        Bi_1ToM_FK b2 = em.find(Bi_1ToM_FK.class, id);
        assertEquals(b, b2);
        
        //remove
        em.getTransaction().begin();
        em.remove(b2);
        em.getTransaction().commit();
        em.clear();
    }

    public void crudBi1MJT(EntityManager em) {
        Bi_1ToM_JT b = new Bi_1ToM_JT();
        b.setName("b");
        List<EntityC_B1MJT> cs = new ArrayList<EntityC_B1MJT>();
        EntityC_B1MJT c = new EntityC_B1MJT();
        c.setName("c");
        c.setBi1mjt(b);
        cs.add(c);
        b.setEntityCs(cs);
        em.persist(b);
        em.persist(c);
        em.getTransaction().begin();
        em.getTransaction().commit();

        //update
        em.getTransaction().begin();
        cs = b.getEntityCs();
        b.setName("newName");
        EntityC_B1MJT c1 = new EntityC_B1MJT();
        c1.setName("c1");
        cs.add(c1);
        c1.setBi1mjt(b);
        em.persist(c1);
        em.getTransaction().commit();
        em.clear();
        
        //query
        Query q = em.createQuery("SELECT b FROM Bi_1ToM_JT b where b.name = 'newName'");
        Bi_1ToM_JT b1 = (Bi_1ToM_JT)q.getSingleResult();
        assertEquals(b, b1);
        em.clear();

        //query
        q = em.createQuery("SELECT c FROM EntityC_B1MJT c");
        List<EntityC_B1MJT> cs1 = q.getResultList();
        assertEquals(2, cs1.size());
        em.clear();
                
        //find
        long id = b1.getId();
        Bi_1ToM_JT b2 = em.find(Bi_1ToM_JT.class, id);
        assertEquals(b, b2);
        
        //remove
        em.getTransaction().begin();
        em.remove(b2);
        em.getTransaction().commit();
        em.clear();
    }
    
    public void testOneToManyMapRelation() {
        List<Class<?>> types = new ArrayList<Class<?>>();
        types.add(EntityC_U1M_Map_FK.class);
        types.add(Uni_1ToM_Map_FK.class);
        types.add(EntityC_B1M_Map_JT.class);
        types.add(Bi_1ToM_Map_JT.class);
        types.add(EntityC_U1M_Map_RelKey_FK.class);
        types.add(Uni_1ToM_Map_RelKey_FK.class);
        types.add(EntityC.class);
        types.add(EntityC_B1M_Map_RelKey_JT.class);
        types.add(Bi_1ToM_Map_RelKey_JT.class);
        OpenJPAEntityManagerFactorySPI emf = createEMF2_0(types);
        EntityManager em = emf.createEntityManager();
        
        try {
            // trigger table creation
            em.getTransaction().begin();
            em.getTransaction().commit();
            assertSQLFragnments(sql, "CREATE TABLE C_U1M_Map_FK", "Uni1MFK_ID", "KEY0");
            assertSQLFragnments(sql, "CREATE TABLE Bi1M_Map_JT_C", "B_ID", "C_ID");
            assertSQLFragnments(sql, "CREATE TABLE C_U1M_Map_RelKey_FK", "Uni1MFK_ID");
            assertSQLFragnments(sql, "CREATE TABLE Bi1M_Map_RelKey_JT_C", "B_ID", "C_ID");
            crudUni1MMapFK(em);
            crudBi1MMapJT(em);
            crudUni1MMapRelKeyFK(em);
            crudBi1MMapRelKeyJT(em);
        } catch (Exception e) {
            e.printStackTrace();
            fail("OneToMany mapping failed with exception message: " + e.getMessage());
        } finally {
            em.close();
            emf.close();            
        }
    }

    public void crudUni1MMapFK(EntityManager em) {
        //create
        Uni_1ToM_Map_FK u = new Uni_1ToM_Map_FK();
        u.setName("u");
        Map<String, EntityC_U1M_Map_FK> cs = new HashMap<String, EntityC_U1M_Map_FK>();
        EntityC_U1M_Map_FK c1 = new EntityC_U1M_Map_FK();
        c1.setName("c1");
        cs.put(c1.getName(), c1);
        EntityC_U1M_Map_FK c2 = new EntityC_U1M_Map_FK();
        c2.setName("c2");
        cs.put(c2.getName(), c2);
        u.setEntityCs(cs);
        
        em.persist(u);
        em.persist(c1);
        em.persist(c2);
        em.getTransaction().begin();
        em.getTransaction().commit();

        //update by adding a new C
        cs = u.getEntityCs();
        u.setName("newName");
        EntityC_U1M_Map_FK c3 = new EntityC_U1M_Map_FK();
        c3.setName("c3");
        cs.put(c3.getName(), c3);
        em.persist(c3);

        em.getTransaction().begin();
        em.getTransaction().commit();
        
        // update by removing a c and then add this c to a new u
        em.getTransaction().begin();
        EntityC_U1M_Map_FK c4 = cs.remove("c1");
        
        Uni_1ToM_Map_FK u2 = new Uni_1ToM_Map_FK();
        u2.setName("u2");
        Map<String, EntityC_U1M_Map_FK> cs2 = new HashMap<String, EntityC_U1M_Map_FK>();
        cs2.put(c4.getName(), c4);
        u2.setEntityCs(cs2);
        em.persist(u2);
        em.getTransaction().commit();
        em.clear();
        
        //query
        Query q = em.createQuery("SELECT u FROM Uni_1ToM_Map_FK u where u.name='newName'");
        Uni_1ToM_Map_FK u1 = (Uni_1ToM_Map_FK)q.getSingleResult();
        assertEquals(u, u1);
        em.clear();

        //find
        long id = u1.getId();
        Uni_1ToM_Map_FK findU = em.find(Uni_1ToM_Map_FK.class, id);
        assertEquals(u, findU);
        
        //remove
        em.getTransaction().begin();
        em.remove(findU);
        em.getTransaction().commit();
    }
    
    public void crudBi1MMapJT(EntityManager em) {
        Bi_1ToM_Map_JT b = new Bi_1ToM_Map_JT();
        b.setName("b");
        Map<String, EntityC_B1M_Map_JT> cs = new HashMap<String, EntityC_B1M_Map_JT>();
        EntityC_B1M_Map_JT c = new EntityC_B1M_Map_JT();
        c.setName("c");
        c.setBi1mjt(b);
        cs.put(c.getName(), c);
        b.setEntityCs(cs);
        em.persist(b);
        em.persist(c);
        em.getTransaction().begin();
        em.getTransaction().commit();

        //update
        em.getTransaction().begin();
        cs = b.getEntityCs();
        b.setName("newName");
        EntityC_B1M_Map_JT c1 = new EntityC_B1M_Map_JT();
        c1.setName("c1");
        cs.put(c1.getName(), c1);
        c1.setBi1mjt(b);
        em.persist(c1);
        em.getTransaction().commit();
        em.clear();
        
        //query
        Query q = em.createQuery("SELECT b FROM Bi_1ToM_Map_JT b");
        Bi_1ToM_Map_JT b1 = (Bi_1ToM_Map_JT)q.getSingleResult();
        assertEquals(b, b1);
        em.clear();

        // query the owner
        q = em.createQuery("SELECT c FROM EntityC_B1M_Map_JT c");
        List<EntityC_B1M_Map_JT> cs1 = q.getResultList();
        assertEquals(2, cs.size());
        em.clear();
        
        //find
        long id = b1.getId();
        Bi_1ToM_Map_JT b2 = em.find(Bi_1ToM_Map_JT.class, id);
        assertEquals(b, b2);
        
        //remove
        em.getTransaction().begin();
        em.remove(b2);
        em.getTransaction().commit();
    }
    
    public void crudUni1MMapRelKeyFK(EntityManager em) {
        //create
        Uni_1ToM_Map_RelKey_FK u = new Uni_1ToM_Map_RelKey_FK();
        u.setName("u");
        Map<EntityC, EntityC_U1M_Map_RelKey_FK> cs = new HashMap<EntityC, EntityC_U1M_Map_RelKey_FK>();
        EntityC_U1M_Map_RelKey_FK c1 = new EntityC_U1M_Map_RelKey_FK();
        c1.setName("c1");
        EntityC cKey1 = new EntityC();
        cKey1.setName("cKey1");
        cs.put(cKey1, c1);
        EntityC_U1M_Map_RelKey_FK c2 = new EntityC_U1M_Map_RelKey_FK();
        c2.setName("c2");
        EntityC cKey2 = new EntityC();
        cKey2.setName("cKey2");
        cs.put(cKey2, c1);
        cs.put(cKey2, c2);
        u.setEntityCs(cs);
        em.persist(u);
        em.persist(c1);
        em.persist(c2);
        em.persist(cKey1);
        em.persist(cKey2);
        em.getTransaction().begin();
        em.getTransaction().commit();

        //update by adding a new C
        em.getTransaction().begin();
        cs = u.getEntityCs();
        u.setName("newName");
        EntityC_U1M_Map_RelKey_FK c3 = new EntityC_U1M_Map_RelKey_FK();
        c3.setName("c3");
        EntityC cKey3 = new EntityC();
        cKey3.setName("cKey3");
        cs.put(cKey3, c3);
        em.persist(c3);
        em.persist(cKey3);
        em.getTransaction().commit();
        
        // update by removing a c and then add this c to a new u
        em.getTransaction().begin();
        EntityC_U1M_Map_RelKey_FK c4 = cs.remove(cKey1);
        
        Uni_1ToM_Map_RelKey_FK u2 = new Uni_1ToM_Map_RelKey_FK();
        u2.setName("u2");
        Map<EntityC, EntityC_U1M_Map_RelKey_FK> cs2 = new HashMap<EntityC, EntityC_U1M_Map_RelKey_FK>();
        cs2.put(cKey1, c4);
        u2.setEntityCs(cs2);
        em.persist(u2);
        em.getTransaction().commit();
        em.clear();
        
        //query
        Query q = em.createQuery("SELECT u FROM Uni_1ToM_Map_RelKey_FK u where u.name='newName'");
        Uni_1ToM_Map_RelKey_FK u1 = (Uni_1ToM_Map_RelKey_FK)q.getSingleResult();
        assertEquals(u, u1);
        em.clear();

        //find
        long id = u1.getId();
        Uni_1ToM_Map_RelKey_FK findU = em.find(Uni_1ToM_Map_RelKey_FK.class, id);
        assertEquals(u, findU);
        
        //remove
        em.getTransaction().begin();
        em.remove(findU);
        em.getTransaction().commit();
    }

    public void crudBi1MMapRelKeyJT(EntityManager em) {
        Bi_1ToM_Map_RelKey_JT b = new Bi_1ToM_Map_RelKey_JT();
        b.setName("b");
        Map<EntityC, EntityC_B1M_Map_RelKey_JT> cs = new HashMap<EntityC, EntityC_B1M_Map_RelKey_JT>();
        EntityC_B1M_Map_RelKey_JT c = new EntityC_B1M_Map_RelKey_JT();
        c.setName("c");
        c.setBi1mjt(b);
        EntityC cKey = new EntityC();
        cKey.setName("cKey");
        cs.put(cKey, c);
        b.setEntityCs(cs);
        em.persist(b);
        em.persist(c);
        em.persist(cKey);
        em.getTransaction().begin();
        em.getTransaction().commit();

        //update
        em.getTransaction().begin();
        cs = b.getEntityCs();
        b.setName("newName");
        EntityC_B1M_Map_RelKey_JT c1 = new EntityC_B1M_Map_RelKey_JT();
        c1.setName("c1");
        EntityC cKey1 = new EntityC();
        cKey1.setName("cKey1");
        cs.put(cKey1, c1);
        c1.setBi1mjt(b);
        em.persist(c1);
        em.persist(cKey1);
        em.getTransaction().commit();
        em.clear();
        
        //query
        Query q = em.createQuery("SELECT b FROM Bi_1ToM_Map_RelKey_JT b");
        Bi_1ToM_Map_RelKey_JT b1 = (Bi_1ToM_Map_RelKey_JT)q.getSingleResult();
        assertEquals(b, b1);
        em.clear();

        // query the owner
        q = em.createQuery("SELECT c FROM EntityC_B1M_Map_RelKey_JT c where c.name = 'c'");
        EntityC_B1M_Map_RelKey_JT newC = (EntityC_B1M_Map_RelKey_JT)q.getSingleResult();
        assertEquals(newC, c);
        em.clear();
        
        //find
        long id = b1.getId();
        Bi_1ToM_Map_RelKey_JT b2 = em.find(Bi_1ToM_Map_RelKey_JT.class, id);
        assertEquals(b, b2);
        
        //remove
        em.getTransaction().begin();
        em.remove(b2);
        em.getTransaction().commit();
    }

    public void testUniManyToOneUsingJoinTable() {
        List<Class<?>> types = new ArrayList<Class<?>>();
        types.add(EntityC.class);
        types.add(Uni_MTo1_JT.class);
        OpenJPAEntityManagerFactorySPI emf = createEMF2_0(types);
        EntityManager em = emf.createEntityManager();
        
        try {
            // trigger table creation
            em.getTransaction().begin();
            em.getTransaction().commit();
            assertSQLFragnments(sql, "CREATE TABLE UniM1JT_C", "U_ID", "C_ID");
            crudUniM1JT(em);
        } catch (Exception e) {
            e.printStackTrace();
            fail("ManyToOne mapping failed with exception message: " + e.getMessage());
        } finally {
            em.close();
            emf.close();            
        }
    }
    
    public void crudUniM1JT(EntityManager em) {
        //create
        Uni_MTo1_JT u = new Uni_MTo1_JT();
        u.setName("u");

        Uni_MTo1_JT u1 = new Uni_MTo1_JT();
        u1.setName("u1");

        EntityC c1 = new EntityC();
        c1.setName("c1");
        u.setEntityC(c1);
        u1.setEntityC(c1);
        
        em.persist(u);
        em.persist(u1);
        em.persist(c1);
        em.getTransaction().begin();
        em.getTransaction().commit();

        //update by changing the many-to-one value 
        em.getTransaction().begin();
        u.setName("u_new");
        EntityC c3 = new EntityC();
        c3.setName("c3");
        u.setEntityC(c3);
        em.persist(c3);
        em.getTransaction().commit();
        
        // update be removing the many-to-one value
        em.getTransaction().begin();
        u.setEntityC(null);
        em.getTransaction().commit();
        
        //query
        Query q = em.createQuery("SELECT u FROM Uni_MTo1_JT u where u.name='u_new'");
        Uni_MTo1_JT queryU = (Uni_MTo1_JT)q.getSingleResult();
        assertEquals(u, queryU);
        em.clear();

        //find
        long id = u1.getId();
        Uni_MTo1_JT findU = em.find(Uni_MTo1_JT.class, id);
        assertEquals(u1, findU);
        
        //remove
        em.getTransaction().begin();
        em.remove(findU);
        em.getTransaction().commit();
    }

    public void testOneToOneUsingJoinTable() {
        List<Class<?>> types = new ArrayList<Class<?>>();
        types.add(EntityC_B11JT.class);
        types.add(EntityC_U11JT.class);
        types.add(Bi_1To1_JT.class);
        types.add(Uni_1To1_JT.class);
        OpenJPAEntityManagerFactorySPI emf = createEMF2_0(types);
        EntityManager em = emf.createEntityManager();
        
        try {
            // trigger table creation
            em.getTransaction().begin();
            em.getTransaction().commit();
            assertSQLFragnments(sql, "CREATE TABLE Bi11JT_C", "B_ID", "C_ID");
            assertSQLFragnments(sql, "CREATE TABLE Uni11JT_C", "U_ID", "C_ID");
            crudBi11JT(em);
            crudUni11JT(em);
        } catch (Exception e) {
            e.printStackTrace();
            fail("OneToOne mapping failed with exception message: " + e.getMessage());
        } finally {
            em.close();
            emf.close();            
        }
    }

    public void crudUni11JT(EntityManager em) {
        Uni_1To1_JT u = new Uni_1To1_JT();
        u.setName("uni1mjt");

        EntityC_U11JT c1 = new EntityC_U11JT();
        c1.setName("c1");
        u.setEntityC(c1);

        em.persist(u);
        em.persist(c1);
        em.getTransaction().begin();
        em.getTransaction().commit();
        
        //update by setting to a new C
        em.getTransaction().begin();
        u.setName("uni1mjt_new");
        EntityC_U11JT newC = new EntityC_U11JT();
        newC.setName("newC");
        u.setEntityC(newC);
        em.persist(newC);
        em.getTransaction().commit();
        
        // update by setting to null
        em.getTransaction().begin();
        u.setEntityC(null);
        em.getTransaction().commit();
        em.clear();
        
        //query
        Query q = em.createQuery("SELECT u FROM Uni_1To1_JT u where u.name = 'uni1mjt_new'");
        Uni_1To1_JT u1 = (Uni_1To1_JT)q.getSingleResult();
        assertEquals(u, u1);
        em.clear();

        //find
        long id = u1.getId();
        Uni_1To1_JT findU1 = em.find(Uni_1To1_JT.class, id);
        assertEquals(u, findU1);
        
        //remove
        em.getTransaction().begin();
        em.remove(findU1);
        em.getTransaction().commit();
    }

    public void crudBi11JT(EntityManager em) {
        Bi_1To1_JT b = new Bi_1To1_JT();
        b.setName("bi11fk");
        
        EntityC_B11JT c = new EntityC_B11JT();
        c.setName("c");
        b.setEntityC(c);
        //c.setBi11jt(b);

        em.persist(b);
        em.persist(c);
        em.getTransaction().begin();
        em.getTransaction().commit();

        // update by removing a c 
        em.getTransaction().begin();
        b.setEntityC(null);
        em.getTransaction().commit();

        //update
        em.getTransaction().begin();
        b.setName("newName");
        EntityC_B11JT c1 = new EntityC_B11JT();
        c1.setName("c1");
        b.setEntityC(c1);
        //c1.setBi11jt(b);
        em.persist(c1);
        em.getTransaction().commit();
        
        //query
        Query q = em.createQuery("SELECT u FROM Bi_1To1_JT u");
        Bi_1To1_JT b1 = (Bi_1To1_JT)q.getSingleResult();
        assertEquals(b, b1);
        em.clear();

        // query
        q = em.createQuery("SELECT c FROM EntityC_B11JT c");
        List<EntityC_B11JT> cs1 = q.getResultList();
        assertEquals(2, cs1.size());
        em.clear();
        
        //find
        long id = b1.getId();
        Bi_1To1_JT b2 = em.find(Bi_1To1_JT.class, id);
        assertEquals(b, b2);
        
        //remove
        em.getTransaction().begin();
        em.remove(b2);
        em.getTransaction().commit();
    }

    private OpenJPAEntityManagerFactorySPI createEMF2_0(List<Class<?>> types) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("openjpa.jdbc.JDBCListeners", 
                new JDBCListener[] { 
                    this.new Listener() 
                });
        map.put("openjpa.jdbc.SynchronizeMappings", 
            "buildSchema(ForeignKeys=true,SchemaAction='drop,add')");

        map.put(PERSISTENT_CLASS_LIST, types);
        return (OpenJPAEntityManagerFactorySPI)
                createContainerEMF("persistence_2_0",
                    "org/apache/openjpa/persistence/compat/" +
                    "persistence_2_0.xml", map);
    }
    
    void assertSQLFragnments(List<String> list, String... keys) {
        if (SQLSniffer.matches(list, keys))
            return;
        fail("None of the following " + sql.size() + " SQL \r\n" + 
                toString(sql) + "\r\n contains all keys \r\n"
                + toString(Arrays.asList(keys)));
    }

    public String toString(List<String> list) {
        StringBuffer buf = new StringBuffer();
        for (String s : list)
            buf.append(s).append("\r\n");
        return buf.toString();
    }

    public class Listener extends AbstractJDBCListener {
        @Override
        public void beforeExecuteStatement(JDBCEvent event) {
            if (event.getSQL() != null && sql != null) {
                sql.add(event.getSQL());
                sqlCount++;
            }
        }
    }
}
