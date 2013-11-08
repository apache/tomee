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
package org.apache.openjpa.persistence.entityoperation;


import org.apache.openjpa.persistence.entityoperation.common.apps.
        CascadesEntity;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

/**
 * <p>Test EJB persistence cascade options.</p>
 *
 * @author Abe White
 */
public class TestCascades extends AbstractTestCase {

    public TestCascades(String name) {
        super(name, "entityopcactusapp");
    }

    public void setUp() {
        deleteAll(CascadesEntity.class);
    }

    public void testNoCascadePersist ()
     {
         CascadesEntity ent = new CascadesEntity ();
         CascadesEntity rel = new CascadesEntity ();
         ent.setNone (rel);
         ent.getNoneCollection ().add (rel);

         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persist (ent);
         assertTrue (em.isPersistent (ent));
         assertFalse (em.isPersistent (rel));
         rollbackTx(em);
         endEm(em);
     }


     public void testCascadePersistIsImmediate ()
     {
         CascadesEntity ent = new CascadesEntity ();
         CascadesEntity rel1 = new CascadesEntity ();
         CascadesEntity rel2 = new CascadesEntity ();
         ent.setAll (rel1);
         ent.getAllCollection ().add (rel2);

         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persist (ent);
         assertTrue (em.isPersistent (ent));
         assertTrue (em.isPersistent (rel1));
         assertTrue (em.isPersistent (rel2));
         rollbackTx(em);
         endEm(em);
     }


     public void testNoCascadePersistFlushWithDeletedCausesException ()
     {
         CascadesEntity rel = new CascadesEntity ();
         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persist (rel);
         endTx(em);
         long id = rel.getId ();
         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         rel = em.find (CascadesEntity.class, id);
         assertNotNull (rel);
         CascadesEntity ent = new CascadesEntity ();
         ent.setNone (rel);
         startTx(em);
         em.remove (rel);
         em.persist (ent);
         try
         {
             endTx(em);
             fail ("Allowed flush with deleted object in non-cascade-persist "
                 + "relation field");
         }
         catch (RuntimeException re)
         {
         }
         catch (Exception e)
         {}

         assertTrue (!em.getTransaction().isActive ());
         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         rel = em.find (CascadesEntity.class, id);
         assertNotNull (rel);
         ent = new CascadesEntity ();
         ent.getNoneCollection ().add (rel);
         startTx(em);
         em.remove (rel);
         em.persist (ent);
         try
         {
             endTx(em);
             fail ("Allowed flush with deleted object in non-cascade-persist "
                 + "relation field");
         }
         catch (RuntimeException re)
         {
         }
         catch (Exception re)
         {
         }
         assertTrue (!em.getTransaction().isActive ());

         endEm(em);
     }


     public void testCascadePersistFlushWithDeleted ()
     {
         CascadesEntity rel = new CascadesEntity ();
         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persist (rel);
         endTx(em);
         long id = rel.getId ();
         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         rel = em.find (CascadesEntity.class, id);
         assertNotNull (rel);
         CascadesEntity ent = new CascadesEntity ();
         ent.setAll (rel);
         startTx(em);
         em.remove (rel);
         em.persist (ent);
         endTx(em);
         assertTrue (!em.getTransaction().isActive ());
         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         rel = em.find (CascadesEntity.class, id);
         assertNotNull (rel);
         ent = new CascadesEntity ();
         ent.getAllCollection ().add (rel);
         startTx(em);
         em.remove (rel);
         em.persist (ent);
         endTx(em);
         assertTrue (!em.getTransaction().isActive ());

         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         rel = em.find (CascadesEntity.class, id);
         assertNotNull (rel);
         endEm(em);
     }


     public void testNoCascadePersistFlushWithTransientCausesException ()
     {
         CascadesEntity ent = new CascadesEntity ();
         CascadesEntity rel = new CascadesEntity ();
         ent.setNone (rel);

         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persist (ent);
         try
         {
             endTx(em);
             fail ("Allowed flush with transient object in non-cascade-persist "
                 + "relation field");
         }
         catch (RuntimeException re)
         {
         }
         catch (Exception re)
         {
         }


         assertTrue (!em.getTransaction().isActive ());
         endEm(em);

         ent = new CascadesEntity ();
         rel = new CascadesEntity ();
         ent.getNoneCollection ().add (rel);

         em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persist (ent);
         try
         {
             endTx(em);
             fail ("Allowed flush with transient object in non-cascade-persist "
                 + "relation field");
         }
         catch (RuntimeException re)
         {
         }
         catch (Exception re)
         {
         }
         assertTrue (!em.getTransaction().isActive ());
         endEm(em);
     }


     public void testNoCascadePersistFlushWithPersistent ()
     {
         CascadesEntity ent = new CascadesEntity ();
         CascadesEntity rel = new CascadesEntity ();
         ent.setNone (rel);

         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persist (ent);
         assertFalse(em.isPersistent (rel));
         em.persist (rel);
         endTx(em);
         long id = rel.getId ();
         endEm(em);

         ent = new CascadesEntity ();
         rel = new CascadesEntity ();
         ent.getNoneCollection ().add (rel);

         em = (OpenJPAEntityManager)currentEntityManager();
         assertNotNull (em.find (CascadesEntity.class, id));
         startTx(em);
         em.persist (ent);
         assertFalse (em.isPersistent (rel));
         em.persist (rel);
         endTx(em);
         id = rel.getId ();
         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         assertNotNull (em.find (CascadesEntity.class, id));
         endEm(em);
     }


     public void testCascadePersistFlushWithTransient ()
     {
         CascadesEntity ent = new CascadesEntity ();
         CascadesEntity rel = new CascadesEntity ();

         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persist (ent);
         ent.setAll (rel);
         assertFalse (em.isPersistent (rel));
         endTx(em);
         long id = rel.getId ();
         endEm(em);

         ent = new CascadesEntity ();
         rel = new CascadesEntity ();

         em = (OpenJPAEntityManager)currentEntityManager();
         assertNotNull (em.find (CascadesEntity.class, id));
         startTx(em);
         em.persist (ent);
         ent.getAllCollection ().add (rel);
         assertFalse (em.isPersistent (rel));
         endTx(em);
         id = rel.getId ();
         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         assertNotNull (em.find (CascadesEntity.class, id));
         endEm(em);
     }


     public void testCascadePersistFlushWithPersistent ()
     {
         CascadesEntity ent = new CascadesEntity ();
         CascadesEntity rel = new CascadesEntity ();
         ent.setAll (rel);

         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persist (ent);
         assertTrue (em.isPersistent (rel));
         endTx(em);
         long id = rel.getId ();
         endEm(em);

         ent = new CascadesEntity ();
         rel = new CascadesEntity ();
         ent.getAllCollection ().add (rel);

         em = (OpenJPAEntityManager)currentEntityManager();
         assertNotNull (em.find (CascadesEntity.class, id));
         startTx(em);
         em.persist (ent);
         assertTrue (em.isPersistent (rel));
         endTx(em);
         id = rel.getId ();
         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         assertNotNull (em.find (CascadesEntity.class, id));
         endEm(em);
     }


     public void testCascadeCircleThroughPersistent ()
     {
         CascadesEntity ent = new CascadesEntity ();
         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persist (ent);
         endTx(em);
         long id = ent.getId ();
         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         ent = em.find (CascadesEntity.class, id);
         CascadesEntity top = new CascadesEntity ();
         top.setAll (ent);
         CascadesEntity rel = new CascadesEntity ();

         startTx(em);
         ent.setAll (rel);
         rel.setAll (top);
         em.persist (top);
         assertTrue (em.isPersistent (top));
         assertTrue (em.isPersistent (ent));
         assertTrue (em.isPersistent (rel));
         rollbackTx(em);
         endEm(em);
     }


     public void testNoCascadeDelete ()
     {
         CascadesEntity ent = new CascadesEntity ();
         CascadesEntity rel = new CascadesEntity ();
         CascadesEntity depend = new CascadesEntity ();
         ent.setNone (rel);
         ent.setDependent (depend);
         ent.getNoneCollection ().add (rel);

         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persistAll (ent, rel, depend);
         endTx(em);
         long id = ent.getId ();
         long relId = rel.getId ();
         long dependId = depend.getId ();
         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         ent = em.find (CascadesEntity.class, id);
         rel = ent.getNone ();
         depend = ent.getDependent ();
         assertEquals (relId, rel.getId ());
         assertEquals (dependId, depend.getId ());
         assertEquals (1, ent.getNoneCollection ().size ());
         assertEquals (relId,
                 ent.getNoneCollection().iterator().next().getId());

         startTx(em);
         em.remove (ent);
         assertTrue (em.isRemoved (ent));
         assertFalse (em.isRemoved (rel));
         assertFalse (em.isRemoved (depend));
         endTx(em);
         assertFalse (em.isPersistent (ent));
         assertTrue (em.isPersistent (rel));
         assertFalse (em.isPersistent (depend));
         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         assertNull (em.find (CascadesEntity.class, id));
         assertNotNull (em.find (CascadesEntity.class, relId));
         assertNull (em.find (CascadesEntity.class, dependId));
         endEm(em);
     }


     public void testDeepCascadeDelete ()
     {
         CascadesEntity ent = new CascadesEntity ();
         CascadesEntity rel1 = new CascadesEntity ();
         CascadesEntity rel2 = new CascadesEntity ();
         CascadesEntity depend = new CascadesEntity ();
         CascadesEntity deep1 = new CascadesEntity ();
         CascadesEntity deep2 = new CascadesEntity ();
         CascadesEntity deep3 = new CascadesEntity ();
         ent.setAll (rel1);
         rel1.setAll (deep1);
         ent.getAllCollection ().add (rel2);
         rel2.getAllCollection ().add (deep2);
         ent.setDependent (depend);
         depend.setAll (deep3);

         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persistAll (ent, depend);
         endTx(em);
         long id = ent.getId ();
         long rel1Id = rel1.getId ();
         long rel2Id = rel2.getId ();
         long deep1Id = deep1.getId ();
         long deep2Id = deep2.getId ();
         long deep3Id = deep3.getId ();
         long dependId = depend.getId ();
         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         ent = em.find (CascadesEntity.class, id);
         rel1 = ent.getAll ();
         assertEquals (rel1Id, rel1.getId ());
         deep1 = rel1.getAll ();
         assertEquals (deep1Id, deep1.getId ());
         assertEquals (1, ent.getAllCollection ().size ());
         rel2 = ent.getAllCollection ().iterator ().next ();
         assertEquals (rel2Id, rel2.getId ());
         assertEquals (1, rel2.getAllCollection ().size ());
         deep2 = rel2.getAllCollection ().iterator ().next ();
         assertEquals (deep2Id, deep2.getId ());
         depend = ent.getDependent ();
         assertEquals (dependId, depend.getId ());
         deep3 = depend.getAll ();
         assertEquals (deep3Id, deep3.getId ());

         startTx(em);
         em.remove (ent);
         assertTrue (em.isRemoved (ent));
         assertTrue (em.isRemoved (rel1));
         assertTrue (em.isRemoved (rel2));
         assertTrue (em.isRemoved (deep1));
         assertTrue (em.isRemoved (deep2));
         assertFalse (em.isRemoved (depend));
         assertFalse (em.isRemoved (deep3));
         endTx(em);
         assertFalse (em.isPersistent (ent));
         assertFalse (em.isPersistent (rel1));
         assertFalse (em.isPersistent (rel2));
         assertFalse (em.isPersistent (deep1));
         assertFalse (em.isPersistent (depend));
         assertFalse (em.isPersistent (deep2));
         assertFalse(em.isPersistent (deep3));
         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         assertNull (em.find (CascadesEntity.class, id));
         assertNull (em.find (CascadesEntity.class, rel1Id));
         assertNull (em.find (CascadesEntity.class, rel2Id));
         assertNull (em.find (CascadesEntity.class, deep1Id));
         assertNull (em.find (CascadesEntity.class, deep2Id));
         assertNull (em.find (CascadesEntity.class, deep3Id));
         assertNull (em.find (CascadesEntity.class, dependId));
         endEm(em);
     }


     public void testCircularCascadeDelete ()
     {
         CascadesEntity ent = new CascadesEntity ();
         CascadesEntity rel = new CascadesEntity ();
         ent.setAll (rel);
         ent.getAllCollection ().add (rel);
         rel.setAll (ent);
         rel.getAllCollection ().add (ent);

         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persist (ent);
         endTx(em);
         long id = ent.getId ();
         long relId = rel.getId ();
         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         ent = em.find (CascadesEntity.class, id);
         rel = ent.getAll ();
         assertEquals (relId, rel.getId ());
         assertEquals (rel, ent.getAllCollection ().iterator ().next ());
         assertEquals (ent, rel.getAllCollection ().iterator ().next ());

         startTx(em);
         em.remove (ent);
         assertTrue (em.isRemoved (ent));
         assertTrue (em.isRemoved (rel));
         endTx(em);
         assertFalse (em.isPersistent (ent));
         assertFalse (em.isPersistent (rel));
         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         assertNull (em.find (CascadesEntity.class, id));
         assertNull (em.find (CascadesEntity.class, relId));
         endEm(em);
     }


     public void testNoCascadeRefresh ()
     {
         CascadesEntity ent = new CascadesEntity ();
         CascadesEntity rel = new CascadesEntity ();
         ent.setNone (rel);
         ent.getNoneCollection ().add (rel);

         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persistAll (ent, rel);
         endTx(em);
         long id = ent.getId ();
         long relId = rel.getId ();
         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         ent = em.find (CascadesEntity.class, id);
         rel = ent.getNone ();
         assertEquals (relId, rel.getId ());

         startTx(em);
         assertNull (ent.getDependent ());
         assertNull (rel.getDependent ());
         ent.setDependent (new CascadesEntity ());
         rel.setDependent (new CascadesEntity ());
         em.persist (ent.getDependent ());
         em.persist (rel.getDependent ());
         em.refresh (ent);
         assertNull (ent.getDependent ());
         assertNotNull (rel.getDependent ());
         endTx(em);
         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         assertNull (em.find (CascadesEntity.class, id).getDependent ());
         assertNotNull (em.find (CascadesEntity.class, relId).getDependent ());
         endEm(em);
     }


     public void testCircularCascadeRefresh ()
     {
         CascadesEntity ent = new CascadesEntity ();
         CascadesEntity rel = new CascadesEntity ();
         ent.setAll (rel);
         rel.setAll (ent);

         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persist (ent);
         endTx(em);
         long id = ent.getId ();
         long relId = rel.getId ();
         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         ent = em.find (CascadesEntity.class, id);
         rel = ent.getAll ();
         assertEquals (relId, rel.getId ());
         assertEquals (ent, rel.getAll ());

         startTx(em);
         assertNull (ent.getDependent ());
         assertNull (rel.getDependent ());
         ent.setDependent (new CascadesEntity ());
         rel.setDependent (new CascadesEntity ());
         em.persist (ent.getDependent ());
         em.persist (rel.getDependent ());
         em.refresh (ent);
         assertNull (ent.getDependent ());
         assertNull (rel.getDependent ());
         endTx(em);
         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         ent = em.find (CascadesEntity.class, id);
         assertEquals (relId, ent.getAll ().getId ());
         assertNull (ent.getDependent ());
         assertNull (em.find (CascadesEntity.class, relId).getDependent ());
         endEm(em);

         ent = new CascadesEntity ();
         rel = new CascadesEntity ();
         CascadesEntity deep = new CascadesEntity ();
         ent.getAllCollection ().add (rel);
         rel.getAllCollection ().add (ent);
         rel.getAllCollection ().add (deep);

         em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persist (ent);
         endTx(em);
         id = ent.getId ();
         relId = rel.getId ();
         long deepId = deep.getId ();
         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         ent = em.find (CascadesEntity.class, id);
         rel = ent.getAllCollection ().iterator ().next ();
         assertEquals (relId, rel.getId ());
         assertEquals (2, rel.getAllCollection ().size ());
         deep = null;
         for (CascadesEntity elem : rel.getAllCollection ())
             if (elem != ent)
                 deep = elem;
         assertEquals (deepId, deep.getId ());

         startTx(em);
         assertNull (ent.getDependent ());
         assertNull (rel.getDependent ());
         assertNull (deep.getDependent ());
         ent.setDependent (new CascadesEntity ());
         ent.getAllCollection ().add (new CascadesEntity ());
         rel.setDependent (new CascadesEntity ());
         deep.setDependent (new CascadesEntity ());
         em.persistAll (ent.getAllCollection ());
         em.persist (ent.getDependent ());
         em.persist (rel.getDependent ());
         em.persist (deep.getDependent ());
         em.refresh (ent);
         assertNull (ent.getDependent ());
         assertEquals (1, ent.getAllCollection ().size ());
         assertTrue (ent.getAllCollection ().contains (rel));
         assertNull (rel.getDependent ());
         assertEquals (2, rel.getAllCollection ().size ());
         assertTrue (rel.getAllCollection ().contains (ent));
         assertTrue (rel.getAllCollection ().contains (deep));
         assertNull (deep.getDependent ());
         endTx(em);
         endEm(em);

         em = (OpenJPAEntityManager)currentEntityManager();
         ent = em.find (CascadesEntity.class, id);
         assertEquals (1, ent.getAllCollection ().size ());
         assertEquals (relId, ent.getAllCollection ().iterator ().next ().
             getId ());
         assertNull (ent.getDependent ());
         assertNull (em.find (CascadesEntity.class, relId).getDependent ());
         assertNull (em.find (CascadesEntity.class, deepId).getDependent ());
         endEm(em);
     }


     public void testNoCascadeAttachClean ()
     {
         CascadesEntity ent = new CascadesEntity ();
         CascadesEntity rel = new CascadesEntity ();
         ent.setName ("ent");
         rel.setName ("rel");
         ent.setNone (rel);
         ent.getNoneCollection ().add (rel);

         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persistAll (ent, rel);
         endTx(em);
         long id = ent.getId ();
         long relId = rel.getId ();
         endEm(em);

         assertEquals ("ent", ent.getName ());
         assertEquals ("rel", rel.getName ());
         assertEquals (rel, ent.getNone ());
         assertEquals (rel, ent.getNoneCollection ().iterator ().next ());

         em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         ent = em.merge (ent);
         assertTrue (!em.isDirty (ent));
         assertEquals ("ent", ent.getName ());
         assertEquals (id, ent.getId ());
         assertTrue (ent.getNone () != rel);
         rel = ent.getNone ();
         assertNotNull (rel);
         assertTrue (!em.isDirty (rel));
         assertEquals (1, ent.getNoneCollection ().size ());
         assertEquals (rel, ent.getNoneCollection ().iterator ().next ());

         assertTrue (em.isPersistent (rel));
         assertEquals (relId, rel.getId ());
         assertEquals ("rel", rel.getName ());
         endTx(em);
         endEm(em);
     }


     public void testCascadeAttachClean ()
     {
         CascadesEntity ent = new CascadesEntity ();
         CascadesEntity rel1 = new CascadesEntity ();
         CascadesEntity rel2 = new CascadesEntity ();
         ent.setName ("ent");
         rel1.setName ("rel1");
         ent.setAll (rel1);
         rel2.setName ("rel2");
         ent.getAllCollection ().add (rel2);

         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persist (ent);
         endTx(em);
         long id = ent.getId ();
         long rel1Id = rel1.getId ();
         long rel2Id = rel2.getId ();
         endEm(em);

         assertEquals ("ent", ent.getName ());
         assertEquals ("rel1", rel1.getName ());
         assertEquals ("rel2", rel2.getName ());
         assertEquals (rel1, ent.getAll ());
         assertEquals (rel2, ent.getAllCollection ().iterator ().next ());

         em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         ent = em.merge (ent);
         assertTrue (!em.isDirty (ent));
         assertEquals ("ent", ent.getName ());
         assertEquals (id, ent.getId ());
         assertTrue (rel1 != ent.getAll ());
         rel1 = ent.getAll ();
         assertTrue (!em.isDirty (rel1));
         assertEquals (1, ent.getAllCollection ().size ());
         rel2 = ent.getAllCollection ().iterator ().next ();
         assertTrue (!em.isDirty (rel2));

         assertTrue (em.isPersistent (rel1));
         assertEquals (rel1Id, rel1.getId ());
         assertEquals ("rel1", rel1.getName ());
         assertTrue (em.isPersistent (rel2));
         assertEquals (rel2Id, rel2.getId ());
         assertEquals ("rel2", rel2.getName ());
         endTx(em);
         endEm(em);
     }


     public void testNoCascadeAttachDirtyFields ()
     {
         CascadesEntity ent = new CascadesEntity ();
         CascadesEntity rel = new CascadesEntity ();
         ent.setName ("ent");
         rel.setName ("rel");
         ent.setNone (rel);
         ent.getNoneCollection ().add (rel);

         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persistAll (ent, rel);
         endTx(em);
         long id = ent.getId ();
         long relId = rel.getId ();
         endEm(em);

         assertEquals ("ent", ent.getName ());
         assertEquals ("rel", rel.getName ());
         assertEquals (rel, ent.getNone ());
         assertEquals (rel, ent.getNoneCollection ().iterator ().next ());
         rel.setName ("foo");

         em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         ent = em.merge (ent);
         assertTrue (!em.isDirty (ent));
         assertEquals ("ent", ent.getName ());
         assertEquals (id, ent.getId ());
         assertTrue (ent.getNone () != rel);
         rel = ent.getNone ();
         assertNotNull (rel);
         assertTrue (!em.isDirty (rel));
         assertEquals (relId, rel.getId ());
         assertEquals (1, ent.getNoneCollection ().size ());
         assertEquals (rel, ent.getNoneCollection ().iterator ().next ());

         assertTrue (em.isPersistent (rel));
         assertEquals (relId, rel.getId ());
         assertEquals ("rel", rel.getName ());
         endTx(em);
         endEm(em);
     }


     public void testCascadeAttachDirtyFields ()
     {
         CascadesEntity ent = new CascadesEntity ();
         CascadesEntity rel1 = new CascadesEntity ();
         CascadesEntity rel2 = new CascadesEntity ();
         ent.setName ("ent");
         rel1.setName ("rel1");
         ent.setAll (rel1);
         rel2.setName ("rel2");
         ent.getAllCollection ().add (rel2);

         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persist (ent);
         endTx(em);
         long id = ent.getId ();
         long rel1Id = rel1.getId ();
         long rel2Id = rel2.getId ();
         endEm(em);

         assertEquals ("ent", ent.getName ());
         assertEquals ("rel1", rel1.getName ());
         assertEquals ("rel2", rel2.getName ());
         assertEquals (rel1, ent.getAll ());
         assertEquals (rel2, ent.getAllCollection ().iterator ().next ());
         rel1.setName ("foo");
         rel2.setName ("bar");

         em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         ent = em.merge (ent);
         assertEquals ("ent", ent.getName ());
         assertTrue (!em.isDirty (ent));
         assertEquals (id, ent.getId ());
         assertTrue (rel1 != ent.getAll ());
         rel1 = ent.getAll ();
         assertTrue (em.isDirty (rel1));
         assertEquals (1, ent.getAllCollection ().size ());
         rel2 = ent.getAllCollection ().iterator ().next ();
         assertTrue (em.isDirty (rel2));

         assertTrue (em.isPersistent (rel1));
         assertEquals (rel1Id, rel1.getId ());
         assertEquals ("foo", rel1.getName ());
         assertTrue (em.isPersistent (rel2));
         assertEquals (rel2Id, rel2.getId ());
         assertEquals ("bar", rel2.getName ());
         endTx(em);
         endEm(em);
     }


     public void testNoCascadeAttachDirtyRelations ()
     {
         CascadesEntity ent = new CascadesEntity ();
         CascadesEntity rel = new CascadesEntity ();
         CascadesEntity other = new CascadesEntity ();
         ent.setName ("ent");
         rel.setName ("rel");
         other.setName ("other");
         ent.setNone (rel);
         ent.getNoneCollection ().add (rel);

         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persistAll (ent, rel, other);
         endTx(em);
         long id = ent.getId ();
         long relId = rel.getId ();
         long otherId = other.getId ();
         endEm(em);

         assertEquals ("ent", ent.getName ());
         assertEquals ("rel", rel.getName ());
         assertEquals ("other", other.getName ());
         assertEquals (rel, ent.getNone ());
         assertEquals (rel, ent.getNoneCollection ().iterator ().next ());
         other.setName ("foo");
         ent.setNone (other);
         ent.getNoneCollection ().remove (rel);
         ent.getNoneCollection ().add (other);

         em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         ent = em.merge (ent);
         assertTrue (em.isDirty (ent));
         assertEquals ("ent", ent.getName ());
         assertEquals (id, ent.getId ());
         assertTrue (ent.getNone () != rel);
         assertTrue (ent.getNone () != other);
         other = ent.getNone ();
         assertNotNull (other);
         assertTrue (!em.isDirty (other));
         assertEquals (otherId, other.getId ());
         assertEquals (1, ent.getNoneCollection ().size ());
         assertEquals (other, ent.getNoneCollection ().iterator ().next ());

         assertTrue (em.isPersistent (other));
         assertFalse (em.isPersistent (rel));
         assertEquals (otherId, other.getId ());
         assertEquals ("other", other.getName ());
         endTx(em);
         endEm(em);
     }


     public void testCascadeAttachDirtyRelations ()
     {
         CascadesEntity ent = new CascadesEntity ();
         CascadesEntity rel1 = new CascadesEntity ();
         CascadesEntity rel2 = new CascadesEntity ();
         CascadesEntity other1 = new CascadesEntity ();
         CascadesEntity other2 = new CascadesEntity ();
         ent.setName ("ent");
         rel1.setName ("rel1");
         ent.setAll (rel1);
         rel2.setName ("rel2");
         ent.getAllCollection ().add (rel2);
         other1.setName ("other1");
         other2.setName ("other2");

         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persistAll (ent, other1, other2);
         endTx(em);
         long id = ent.getId ();
         long rel1Id = rel1.getId ();
         long rel2Id = rel2.getId ();
         long other1Id = other1.getId ();
         long other2Id = other2.getId ();
         endEm(em);

         assertEquals ("ent", ent.getName ());
         assertEquals ("rel1", rel1.getName ());
         assertEquals ("rel2", rel2.getName ());
         assertEquals (rel1, ent.getAll ());
         assertEquals (rel2, ent.getAllCollection ().iterator ().next ());
         assertEquals ("other1", other1.getName ());
         other1.setName ("foo");
         assertEquals ("other2", other2.getName ());
         other2.setName ("bar");
         ent.setAll (other1);
         ent.getAllCollection ().remove (rel2);
         ent.getAllCollection ().add (other2);

         em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         ent = em.merge (ent);
         assertEquals ("ent", ent.getName ());
         assertTrue (em.isDirty (ent));
         assertEquals (id, ent.getId ());
         assertTrue (rel1 != ent.getAll ());
         assertTrue (other1 != ent.getAll ());
         other1 = ent.getAll ();
         assertTrue (em.isDirty (other1));
         assertEquals (1, ent.getAllCollection ().size ());
         other2 = ent.getAllCollection ().iterator ().next ();
         assertTrue (em.isDirty (other2));

         assertTrue (em.isPersistent (other1));
         assertEquals (other1Id, other1.getId ());
         assertEquals ("foo", other1.getName ());
         assertTrue (em.isPersistent (other2));
         assertEquals (other2Id, other2.getId ());
         assertEquals ("bar", other2.getName ());
         endTx(em);
         endEm(em);
     }


     public void testNoCascadeReferenceIsPreLoadedReference ()
     {
         CascadesEntity ent = new CascadesEntity ();
         CascadesEntity rel = new CascadesEntity ();
         CascadesEntity other = new CascadesEntity ();
         ent.setName ("ent");
         rel.setName ("rel");
         other.setName ("other");
         ent.setNone (rel);
         ent.getNoneCollection ().add (rel);

         OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         em.persistAll (ent, rel, other);
         endTx(em);
         long otherId = other.getId ();
         endEm(em);

         ent.setNone (other);
         ent.getNoneCollection ().remove (rel);
         ent.getNoneCollection ().add (other);

         em = (OpenJPAEntityManager)currentEntityManager();
         startTx(em);
         other = em.find (CascadesEntity.class, otherId);
         ent = em.merge (ent);
         assertEquals (other, ent.getNone ());
         assertEquals (other, ent.getNoneCollection ().iterator ().next ());
         endTx(em);
         endEm(em);
     }

    public void testNoCascadeNewCausesException() {
        CascadesEntity ent = new CascadesEntity();
        CascadesEntity rel = new CascadesEntity();
        ent.setNone(rel);

        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        em.persistAll(ent, rel);
        endTx(em);
        endEm(em);

        CascadesEntity other = new CascadesEntity();
        ent.setNone(other);

        em = (OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        try {
            ent = em.merge(ent);
            fail("Allowed merge of new instance in non-cascading relation.");
        }
        catch (RuntimeException re) {
            if (isActiveTx(em))
                rollbackTx(em);
        }
        endEm(em);

        ent = new CascadesEntity();
        em = (OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        em.persist(ent);
        endTx(em);
        endEm(em);

        other = new CascadesEntity();
        ent.getNoneCollection().add(other);

        em = (OpenJPAEntityManager) currentEntityManager();
        startTx(em);
        try {
            ent = em.merge(ent);
            fail("Allowed merge of new instance in non-cascading relation.");
        }
        catch (RuntimeException re) {
            if (isActiveTx(em))
                rollbackTx(em);
        }
        endEm(em);
    }

	public void testCascadeNewPersisted ()
	{
		CascadesEntity ent = new CascadesEntity ();
		CascadesEntity rel1 = new CascadesEntity ();
		CascadesEntity rel2 = new CascadesEntity ();
		ent.setName ("ent");
		rel1.setName ("rel1");
		ent.setAll (rel1);
		rel2.setName ("rel2");
		ent.getAllCollection ().add (rel2);

        OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
		startTx(em);
		em.persist (ent);
		endTx(em);
		endEm(em);

		CascadesEntity other1 = new CascadesEntity ();
		CascadesEntity other2 = new CascadesEntity ();
		other1.setName ("other1");
		other2.setName ("other2");

		ent.setAll (other1);
		ent.getAllCollection ().remove (rel2);
		ent.getAllCollection ().add (other2);

		em = (OpenJPAEntityManager)currentEntityManager();
		startTx(em);
		ent = em.merge (ent);
		assertTrue (em.isDirty (ent));
		assertTrue (rel1 != ent.getAll ());
		assertTrue (other1 != ent.getAll ());
		other1 = ent.getAll ();
		assertEquals ("other1", other1.getName ());
		assertTrue (em.isNewlyPersistent (other1));
		assertEquals (1, ent.getAllCollection ().size ());
		other2 = ent.getAllCollection ().iterator ().next ();
		assertEquals ("other2", other2.getName ());
		assertTrue (em.isNewlyPersistent (other2));
		endTx(em);
		endEm(em);
	}


	public void testCascadesDeleteNonPersistent ()
	{
		CascadesEntity all = new CascadesEntity ();
		CascadesEntity none = new CascadesEntity ();
		CascadesEntity manyAll = new CascadesEntity ();
		CascadesEntity manyNone = new CascadesEntity ();
        OpenJPAEntityManager em = (OpenJPAEntityManager)currentEntityManager();
		startTx(em);
		em.persist (all);
		em.persist (none);
		em.persist (manyAll);
		em.persist (manyNone);
		endTx(em);
		long allId = all.getId ();
		long noneId = none.getId ();
		long manyAllId = manyAll.getId ();
		long manyNoneId = manyNone.getId ();
		endEm(em);

		em = (OpenJPAEntityManager) currentEntityManager();
		startTx(em);
		CascadesEntity ent = new CascadesEntity ();
		ent.setAll (em.find (CascadesEntity.class, allId));
		ent.setNone (em.find (CascadesEntity.class, noneId));
        ent.getAllCollection().add(em.find(CascadesEntity.class, manyAllId));
        ent.getNoneCollection().add(em.find(CascadesEntity.class, manyNoneId));
		em.remove (ent);
		assertTrue (em.isRemoved (ent.getAll ()));
		assertFalse (em.isRemoved (ent.getNone ()));
		for (CascadesEntity rel : ent.getAllCollection ())
		assertTrue (em.isRemoved (rel));
		for (CascadesEntity rel : ent.getNoneCollection ())
			assertFalse (em.isRemoved (rel));
		assertFalse (em.contains (ent));
		endTx(em);
		endEm(em);

		em = (OpenJPAEntityManager) currentEntityManager();
		assertNull (em.find (CascadesEntity.class, allId));
		assertNotNull (em.find (CascadesEntity.class, noneId));
		assertNull (em.find (CascadesEntity.class, manyAllId));
		assertNotNull (em.find (CascadesEntity.class, manyNoneId));
		endEm(em);
	}
}
