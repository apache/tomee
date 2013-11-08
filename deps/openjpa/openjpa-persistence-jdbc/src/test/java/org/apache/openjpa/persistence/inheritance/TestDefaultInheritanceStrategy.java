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
package org.apache.openjpa.persistence.inheritance;

import java.lang.reflect.Method;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.Discriminator;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.inheritance.entity.AbstractClass;
import org.apache.openjpa.persistence.inheritance.entity.BaseClass;
import org.apache.openjpa.persistence.inheritance.entity.BaseClass2;
import org.apache.openjpa.persistence.inheritance.entity.BaseClass3;
import org.apache.openjpa.persistence.inheritance.entity.BaseClass4;
import org.apache.openjpa.persistence.inheritance.entity.BaseClass5;
import org.apache.openjpa.persistence.inheritance.entity.BaseClass6;
import org.apache.openjpa.persistence.inheritance.entity.ManagedIface;
import org.apache.openjpa.persistence.inheritance.entity.ManagedIface2;
import org.apache.openjpa.persistence.inheritance.entity.MappedSuper;
import org.apache.openjpa.persistence.inheritance.entity.MidClass;
import org.apache.openjpa.persistence.inheritance.entity.MidClass2;
import org.apache.openjpa.persistence.inheritance.entity.MidClass3;
import org.apache.openjpa.persistence.inheritance.entity.SubclassA;
import org.apache.openjpa.persistence.inheritance.entity.SubclassB;
import org.apache.openjpa.persistence.inheritance.entity.SubclassC;
import org.apache.openjpa.persistence.inheritance.entity.SubclassD;
import org.apache.openjpa.persistence.inheritance.entity.SubclassE;
import org.apache.openjpa.persistence.inheritance.entity.ImplClassA;
import org.apache.openjpa.persistence.inheritance.entity.SubclassF;
import org.apache.openjpa.persistence.inheritance.entity.SubclassG;
import org.apache.openjpa.persistence.inheritance.entity.SubclassH;
import org.apache.openjpa.persistence.inheritance.entity.SubclassI;
import org.apache.openjpa.persistence.inheritance.entity.SubclassJ;
import org.apache.openjpa.persistence.inheritance.entity.SubclassK;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * This test verifies that OpenJPA uses a single-table inheritance
 * strategy and default discriminator column if no inheritance strategy 
 * is defined.
 * 
 * OpenJPA JIRA: {@link http://issues.apache.org/jira/browse/OPENJPA-670}

 * @author Jeremy Bauer
 *
 */
public class TestDefaultInheritanceStrategy 
    extends SingleEMFTestCase {
    
    public void setUp() {
        setUp(BaseClass.class, SubclassA.class, SubclassB.class,
            SubclassC.class, MappedSuper.class, SubclassD.class,
            BaseClass2.class, MidClass.class, SubclassE.class,
            ManagedIface.class, ImplClassA.class,
            ManagedIface2.class, BaseClass3.class, SubclassF.class,
            BaseClass4.class, SubclassG.class,
            BaseClass5.class, MidClass2.class, SubclassH.class,
            AbstractClass.class, SubclassI.class, SubclassJ.class,
            BaseClass6.class, SubclassK.class,
            "openjpa.jdbc.FinderCache", "true",
            CLEAR_TABLES);
    }

    private Class[] classArray(Class... classes) {
        return classes;
    }
    
    /**
     * This variation tests a default simple class hierarchy with no inheritance
     * or discriminator column annotations defined.  
     */
    public void testSimpleDefaultInheritance() {
        EntityManager em = emf.createEntityManager();
        
        // Create some entities
        SubclassA sca = new SubclassA();
        sca.setId(0);
        sca.setName("SubclassABaseClassName0");
        sca.setClassAName("SubclassAName0");

        SubclassA sca2 = new SubclassA();
        sca2.setId(1);
        sca2.setName("SubclassABaseClassName1");
        sca2.setClassAName("SubclassAName1");

        SubclassB scb = new SubclassB();
        scb.setId(2);
        scb.setName("SubclassBBaseClassName");
        scb.setClassBName("SubclassBName");

        BaseClass b = new BaseClass();
        b.setName("BaseClassName");
        b.setId(3);

        em.getTransaction().begin();
        em.persist(sca);
        em.persist(sca2);
        em.persist(scb);
        em.persist(b);
        em.getTransaction().commit();

        // test entity type discriminator queries for polymorphic results
        em.clear();
        String query = "select a from BaseClass a where TYPE(a) = BaseClass";
        List rs = em.createQuery(query).getResultList();
        assertTrue(rs.get(0) instanceof BaseClass);
        query = "select a from BaseClass a where TYPE(a) = SubclassA";
        rs = em.createQuery(query).getResultList();
        assertTrue(rs.get(0) instanceof SubclassA);

        em.clear();
        
        verifyDtypeColumnEntriesAndMapping(em, "BaseClass", 4, BaseClass.class);
                
        verifyInheritanceQueryResult(em, "SubclassA", 
            classArray(SubclassA.class), 0, 1);

        verifyInheritanceQueryResult(em, "SubclassB", 
            classArray(SubclassB.class), 2);

        verifyInheritanceQueryResult(em, "BaseClass", 
            classArray(BaseClass.class), 0, 1, 2, 3);

        em.close();
    }

    /**
     * This variation ensures that a mapped superclass does not cause the
     * production of a discriminator column.
     */
    public void testMappedSuperclass() {
        EntityManager em = emf.createEntityManager();

        // Add two entities, each extending the same mapped interface
        em.getTransaction().begin();
        SubclassC sc = new SubclassC();
        sc.setId(1010);
        sc.setName("SubclassCMappedSuperName");
        sc.setClassCName("SubclassCName");

        SubclassD sd = new SubclassD();
        sd.setId(2020);
        sd.setName("SubclassDMappedSuperName");
        sd.setClassDName("SubclassDName");
        
        em.persist(sc);
        em.persist(sd);
        em.getTransaction().commit();
        
        em.clear();
        
        SubclassD sd2 =em.find(SubclassD.class, 2020);
        assertEquals(2020, sd2.getId());
                
        // The subclasses should not contain a discriminator column
        verifyNoDypeColumn(em, "SubclassC");
        verifyNoDypeColumn(em, "SubclassD");

        // Query the subclass entities.  Make sure the counts are correct and
        // the result is castable to the mapped sc.
        verifyInheritanceQueryResult(em, "SubclassC", 
                classArray(SubclassC.class, MappedSuper.class), 1010);

        verifyInheritanceQueryResult(em, "SubclassD", 
                classArray(SubclassD.class, MappedSuper.class), 2020);
                
        em.close();
    }

    /**
     * This variation ensures that a 3-level inheritance hierarchy uses 
     * a discriminator column at the root class level.
     */
    public void testTwoLevelInheritance() {
        EntityManager em = emf.createEntityManager();

        // Add two entities, each extending the same mapped interface
        em.getTransaction().begin();
        SubclassE sc = new SubclassE();
        sc.setId(0);
        sc.setName("SubclassEBaseClassName");
        sc.setMidClassName("SubclassEMidClassName");
        sc.setClassEName("SubclassCName");

        MidClass mc = new MidClass();
        mc.setId(1);
        mc.setName("MidClassBaseClassName");
        mc.setMidClassName("MidClassName");

        BaseClass2 b2 = new BaseClass2();
        b2.setName("BaseClass2Name");
        b2.setId(2);
        
        em.persist(sc);
        em.persist(mc);
        em.persist(b2);
        em.getTransaction().commit();

        // test entity type discriminator queries for polymorphic results
        em.clear();
        String query = "select a from BaseClass2 a where TYPE(a) = MidClass";
        List rs = em.createQuery(query).getResultList();
        for (int i = 0; i < rs.size(); i++)
        assertTrue(rs.get(i) instanceof MidClass);

        query = "select a from BaseClass2 a where TYPE(a) = SubclassE";
        rs = em.createQuery(query).getResultList();
        for (int i = 0; i < rs.size(); i++)
            assertTrue(rs.get(i) instanceof SubclassE);   

        query = "select a from BaseClass2 a where TYPE(a) = BaseClass2";
        rs = em.createQuery(query).getResultList();
        for (int i = 0; i < rs.size(); i++)
            assertTrue(rs.get(i) instanceof BaseClass2);

        em.clear();

        // Verify that baseclass2 contains a discriminator column
        verifyDtypeColumnEntriesAndMapping(em, "BaseClass2", 3, 
                BaseClass2.class);
        
        // Verify that the subclass tables do not contain a discriminator column
        verifyNoDypeColumn(em, "MidClass");
        verifyNoDypeColumn(em, "SubclassE");

        // Query the subclass tables.  Make sure the counts are correct and
        // the result is castable to the mapped sc.
        verifyInheritanceQueryResult(em, "SubclassE", 
                classArray(SubclassE.class, MidClass.class, BaseClass2.class),
                0);

        verifyInheritanceQueryResult(em, "MidClass", 
                classArray(MidClass.class), 0, 1);

        verifyInheritanceQueryResult(em, "BaseClass2", 
                classArray(BaseClass2.class), 0, 1, 2);

        em.close();
    }

    /**
     * This variation verifies that an entity with a managed interface
     * does not use a discriminator column.
     */
    public void testManagedInterface() {
        OpenJPAEntityManager em = emf.createEntityManager();

        // Add some entities
        em.getTransaction().begin();
        ManagedIface mif = em.createInstance(ManagedIface.class);
        mif.setIntFieldSup(10);
                
        ImplClassA ica = new ImplClassA();
        ica.setImplClassAName("ImplClassAName");
        ica.setIntFieldSup(11);
        
        em.persist(mif);
        em.persist(ica);        
        em.getTransaction().commit();

        em.clear();

        // Verify that the iface table does not contain a discriminator column
        verifyNoDypeColumn(em, "ManagedIface");

        // Verify that the impl table does not contain a discriminator column
        verifyNoDypeColumn(em, "ImplClassA");
        
        // Query the subclass tables.  Make sure the counts are correct and
        // the result is castable to the entity and interface types.
        verifyInheritanceQueryResult(em, "ImplClassA", 
                classArray(ImplClassA.class, ManagedIface.class), ica.getId());

        // Query the interface2 table.  Make sure the count is correct and
        // the result is castable to the interface type.
        verifyInheritanceQueryResult(em, "ManagedIface", 
                classArray(ManagedIface.class), mif.getId(),
                ica.getId());
        
        em.close();
    }

    /**
     * This variation verifies that an entity with managed interface
     * and a superclass DOES use a discriminator column.
     */
    public void testManagedInterfaceAndBase() {
        OpenJPAEntityManager em = emf.createEntityManager();

        // Add some entities
        em.getTransaction().begin();
        ManagedIface2 mif2 = em.createInstance(ManagedIface2.class);
        mif2.setIntFieldSup(12);
                
        SubclassF scf = new SubclassF();
        scf.setClassFName("SubclassFName");
        scf.setIntFieldSup(13);

        BaseClass3 bc3 = new BaseClass3();
        bc3.setName("BaseClass3");

        em.persist(mif2);
        em.persist(scf);  
        em.persist(bc3);
        em.getTransaction().commit();

        em.clear();

        // Verify that the iface table does not contain a discriminator column
        verifyNoDypeColumn(em, "ManagedIface2");

        // Verify that the subclass table does not contain a discriminator
        // column
        verifyNoDypeColumn(em, "SubclassF");

        // Verify that the base class does contain a discriminator column
        verifyDtypeColumnEntriesAndMapping(em, "BaseClass3", 2, 
            BaseClass3.class);

        // Query the subclass table.  Make sure the counts are correct and
        // the result is castable to the entity and interface types.
        verifyInheritanceQueryResult(em, "SubclassF", 
            classArray(SubclassF.class, ManagedIface2.class, BaseClass3.class),
            scf.getId());
        
        // Query the base class table.  Make sure the counts are correct and
        // the result is castable to the entity and interface types.
        verifyInheritanceQueryResult(em, "BaseClass3", 
            classArray(BaseClass3.class),
            scf.getId(), bc3.getId());

        // Query the interface2 table.  Make sure the count is correct and
        // the result is castable to the interface type.
        verifyInheritanceQueryResult(em, "ManagedIface2", 
                classArray(ManagedIface2.class),
                scf.getId(), mif2.getId());        
        em.close();
    }
    
    /**
     * This variation tests a default simple class hierarchy with a inheritance
     * annotation defined on the subclass.  
     */
    public void testSubclassSpecifiedInheritance() {
        EntityManager em = emf.createEntityManager();
        
        // Create some entities
        SubclassG scg = new SubclassG();
        scg.setId(0);
        scg.setName("SubclassGBaseClass4Name");
        scg.setClassGName("SubclassGName");

        BaseClass4 b = new BaseClass4();
        b.setName("BaseClass4Name");
        b.setId(1);

        em.getTransaction().begin();
        em.persist(scg);
        em.persist(b);
        em.getTransaction().commit();

        em.clear();
        
        verifyDtypeColumnEntriesAndMapping(em, "BaseClass4", 2, 
            BaseClass4.class);

        // Verify that the subclass table does not contain a discriminator
        // column
        verifyNoDypeColumn(em, "SubclassG");
        
        // Run queries for each type. They should return only those values
        // which match their respective types.  This will not work for single
        // table inheritance unless a discriminator column is defined.
        verifyInheritanceQueryResult(em, "SubclassG", 
            classArray(SubclassG.class, BaseClass4.class), 0);

        verifyInheritanceQueryResult(em, "BaseClass4", 
                classArray(BaseClass4.class), 0, 1);
        
        em.close();
    }    
    
    /**
     * This variation tests a default inheritance hierarchy with circular
     * relationships:
     *    BaseClass5 has rel to SubclassH
     *    MidClass2 extends BaseClass5 inherits rel to SubclassH
     *    SubClassH extends MidClass2 has rel to BaseClass5
     */    
    public void testCircularInheritedRelationships() {
        EntityManager em = emf.createEntityManager();

        // Create and persist some related & inherited entities
        SubclassH sch = new SubclassH();
        sch.setId(1);
        sch.setClassHName("SubclassHName");
        sch.setName("SubclassHBaseClass5Name");
        sch.setMidClass2Name("SubclassHMidClass2Name");
    
        BaseClass5 bc5 = new BaseClass5();
        bc5.setId(2);
        bc5.setName("BaseClass5Name");
        bc5.setSubclassh(sch);
        
        sch.setBaseclass5(bc5);
        
        MidClass2 mc2 = new MidClass2();
        mc2.setId(3);
        mc2.setMidClass2Name("MidClass2Name");
        mc2.setName("MidClass2BaseClass5Name");
        mc2.setSubclassh(sch);
        
        em.getTransaction().begin();
        em.persist(sch);
        em.persist(bc5);
        em.persist(mc2);
        em.getTransaction().commit();
        
        em.clear();

        verifyDtypeColumnEntriesAndMapping(em, "BaseClass5", 3, 
                BaseClass5.class);

        // Verify that the midclass table does not contain a discriminator
        // column
        verifyNoDypeColumn(em, "MidClass2");

        // Verify that the subclass table does not contain a discriminator
        // column
        verifyNoDypeColumn(em, "SubclassH");
        
        // Run queries for each type. They should return only those values
        // which match their respective types.  This will not work for single
        // table inheritance unless a discriminator column is defined.
        verifyInheritanceQueryResult(em, "SubclassH", 
            classArray(SubclassH.class, MidClass2.class, BaseClass5.class),
            1);

        verifyInheritanceQueryResult(em, "MidClass2", 
                classArray(MidClass2.class, BaseClass5.class),
                1, 3);

        verifyInheritanceQueryResult(em, "BaseClass5", 
                classArray(BaseClass5.class),
                1, 2, 3);
        
        em.clear();
        
        // Validate entity relationships
        sch = em.find(SubclassH.class, 1);
        assertEquals(sch.getName(),"SubclassHBaseClass5Name");
        assertEquals(sch.getMidClass2Name(), "SubclassHMidClass2Name");
        // SubclassH has relationship to BaseClass5
        assertEquals(sch.getBaseclass5().getId(), 2);

        bc5 = em.find(BaseClass5.class, 3);
        assertEquals(bc5.getName(),"MidClass2BaseClass5Name");
        // BaseClass5 has relationship to SubclassH through MidClass2
        assertEquals(bc5.getSubclassh().getId(), 1);        
        
        bc5 = em.find(BaseClass5.class, 2);
        assertEquals(bc5.getName(),"BaseClass5Name");
        // BaseClass5 has relationship to SubclassH
        assertEquals(bc5.getSubclassh().getId(), 1);        

        mc2 = em.find(MidClass2.class, 3);
        assertEquals(mc2.getName(),"MidClass2BaseClass5Name");
        assertEquals(mc2.getMidClass2Name(), "MidClass2Name");
        // MidClass2 has relationship to SubclassH
        assertEquals(bc5.getSubclassh().getId(), 1);        

        em.close();
    }
    
    /**
     * This variation verifies default inheritance with an abstract
     * entity.
     */
    public void testAbstractEntityInheritance() {
        EntityManager em = emf.createEntityManager();
       
        SubclassI sci = new SubclassI();
        sci.setId(1);
        sci.setClassIName("SubclassIName");
        sci.setName("SubclassIBaseClassName");
        
        SubclassJ scj = new SubclassJ();
        scj.setId(2);
        scj.setClassJName("SubclassJName");
        scj.setName("SubclassJBaseClassName");
        
        em.getTransaction().begin();
        em.persist(sci);
        em.persist(scj);    
        em.getTransaction().commit();
        
        em.clear();

        verifyDtypeColumnEntriesAndMapping(em, "AbstractClass", 2, 
                AbstractClass.class);

        // Verify that the midclass table does not contain a discriminator
        // column
        verifyNoDypeColumn(em, "SubclassI");

        // Verify that the subclass table does not contain a discriminator
        // column
        verifyNoDypeColumn(em, "SubclassJ");        
        
        // Run queries for each type. They should return only those values
        // which match their respective types.  This will not work for single
        // table inheritance unless a discriminator column is defined.
        verifyInheritanceQueryResult(em, "AbstractClass", 
            classArray(AbstractClass.class), 1, 2);

        verifyInheritanceQueryResult(em, "SubclassI", 
                classArray(AbstractClass.class, SubclassI.class), 1);

        verifyInheritanceQueryResult(em, "SubclassJ", 
                classArray(AbstractClass.class, SubclassJ.class), 2);        
        
        em.close();
    }
    
    /**
     * This variation verifies that default inheritance is used when
     * there is a non-entity superclass in the mix:
     *   non-entity MidClass3 extends BaseClass6
     *   SubClassJ extends MidClass3
     */
    public void testMidNonEntityInheritance() {
        EntityManager em = emf.createEntityManager();
        
        SubclassK sck = new SubclassK();
        sck.setId(1);
        sck.setClassKName("SubclassKName");
        sck.setMidClass3Name("SubclassKMidClass3Name");
        sck.setName("SubclassKBaseClass6Name");
                
        BaseClass6 bk6 = new BaseClass6();
        bk6.setId(3);
        bk6.setName("BaseClass6Name");
        
        em.getTransaction().begin();
        em.persist(sck);
        em.persist(bk6);
        em.getTransaction().commit();
        
        em.clear();
        
        verifyDtypeColumnEntriesAndMapping(em, "BaseClass6", 2, 
                BaseClass6.class);

        // Verify that the subclass table does not contain a discriminator
        // column
        verifyNoDypeColumn(em, "SubclassK");           

        // Run queries for each type. They should return only those values
        // which match their respective types.  This will not work for single
        // table inheritance unless a discriminator column is defined.
        verifyInheritanceQueryResult(em, "BaseClass6", 
            classArray(BaseClass6.class), 1, 3);

        verifyInheritanceQueryResult(em, "SubclassK", 
                classArray(BaseClass6.class, MidClass3.class, SubclassK.class),
                1);        

        em.close();        
    }
    
    public void testFinder() {
        EntityManager em = emf.createEntityManager();
        SubclassK sck = new SubclassK();
        sck.setId(479);
        sck.setClassKName("SubclassKName");
        sck.setMidClass3Name("SubclassKMidClass3Name");
        sck.setName("SubclassKBaseClass6Name");
                
        BaseClass6 bk6 = new BaseClass6();
        bk6.setId(302);
        bk6.setName("BaseClass6Name");
        
        SubclassI sci = new SubclassI();
        sci.setId(109);
        sci.setClassIName("SubclassIName");
        sci.setName("SubclassIBaseClassName");
        
        SubclassJ scj = new SubclassJ();
        scj.setId(238);
        scj.setClassJName("SubclassJName");
        scj.setName("SubclassJBaseClassName");

        em.getTransaction().begin();
        em.persist(sck);
        em.persist(bk6);
        em.persist(sci);
        em.persist(scj);
        em.getTransaction().commit();
        
        em.clear();

        verifyInheritanceFinderResult(em, SubclassK.class, 479);
        verifyInheritanceFinderResult(em, BaseClass6.class, 479, 302);
        verifyInheritanceFinderResult(em, SubclassI.class, 109);
        verifyInheritanceFinderResult(em, SubclassJ.class, 238);
        em.close();
    }

    /**
     * Verifies that a table contains the specified number of entries
     * in its DTYPE (default discriminator) column.
     * @param em  Entity nanager
     * @param table  Name of the table to query
     * @param entries  Expected column entry count
     * @param baseClass Class mapping to verify
     */
    private void verifyDtypeColumnEntriesAndMapping(EntityManager em, 
        String table, int entries, Class baseClass) {
        try {
            Query qry = em.createNativeQuery("SELECT DTYPE FROM " + table);
            List vals = qry.getResultList();
            assertTrue("Query should have returned " + entries + " values", 
                vals.size() == entries);
        }
        catch (Exception e) {
            fail("Exception querying DTYPE column: " + e.getMessage());
        }        
        
        // Check the discriminator column of the class mapping.  
        ClassMapping cm = getMapping(baseClass);
        Discriminator d = cm.getDiscriminator();
        Column[] cols = d.getColumns();
        assertTrue("Discriminator should use DTYPE column", 
            (cols != null && cols.length == 1 && 
            cols[0].getName().equals("DTYPE"))); 
    }

    /**
     * Verifies that a table does not contain a DTYPE column.
     * @param em  Entity manager
     * @param table Name of the table to query
     */
    private void verifyNoDypeColumn(EntityManager em, String table) {
        try {
            Query qry = em.createNativeQuery("SELECT DTYPE FROM " + table);
            qry.getResultList();
            fail("Expected exception.  DTYPE column should not exist on " + 
                table);
        }
        catch (Exception e) {
            // Expected exception
        }        
    }    

    /**
     * Verifies the resulting entity count and expected entity ids from a 
     * simple entity query.  This method requires a "getId" method on the
     * entity type in order to work properly.
     * 
     * @param em entity manager
     * @param entity entity name
     * @param entityType entity class
     * @param expectedValues variable list of expected integral id values.
     */
    private void verifyInheritanceQueryResult(EntityManager em, String entity,
        Class[] types, int... expectedValues) {
    	String jpql = "SELECT e FROM " + entity + " e";
        Query qry = em.createQuery(jpql);
        List col = qry.getResultList();
        assertTrue("Query should return " + expectedValues.length + " entities",
            col.size() == expectedValues.length);
        int count = 0;
        for (int i = 0; i < col.size(); i++) {
            Object ent = col.get(i);
            // If a list of supers or interfaces is provided, make sure
            // the returned type is an instance of those types
            if (types != null) {
                for (int j = 0; j < types.length; j++ )
                    assertTrue(types[j].isInstance(ent));
            }
            int id = -1;
            try {
                Method mth = ent.getClass().getMethod("getId", (Class[])null);
                id = (Integer)mth.invoke(ent, (Object[])null);
            } catch (Exception e) {
                fail("Caught unexepcted exception getting entity id: " 
                    + e.getMessage());
            }
            for (int j = 0; j < expectedValues.length; j++)
                if (expectedValues[j] == id)                   
                    count++;
        }
        assertTrue("Returned unexpected entities " + col + " for " + jpql, 
                count == expectedValues.length);
    }    
    
    private void verifyInheritanceFinderResult(EntityManager em, 
        Class entityType, int... ids) {
        for (int j = 0; j < 2; j++) {
            em.clear();
            for (int id : ids) {
                Object pc = em.find(entityType, id);
                assertTrue(entityType.isAssignableFrom(pc.getClass()));
            }
        }
    }    

}
