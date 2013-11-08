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
package org.apache.openjpa.persistence.merge;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.merge.model.Car;
import org.apache.openjpa.persistence.merge.model.Label;
import org.apache.openjpa.persistence.merge.model.Label2;
import org.apache.openjpa.persistence.merge.model.Make;
import org.apache.openjpa.persistence.merge.model.Model;
import org.apache.openjpa.persistence.merge.model.ShipPackage;
import org.apache.openjpa.persistence.merge.model.ShipPackage2;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestMergeDetachStateField extends SingleEMFTestCase {
    public void setUp() {
        setUp(Label.class, ShipPackage.class, 
        	  Label2.class, ShipPackage2.class,
        	  Car.class, Model.class, Make.class,
        	  CLEAR_TABLES);
    }

    /** 
     * Verify a merge graph is correct when an entity uses a detached state field.  When
     * a detached state field is used, the entity does not get populated with a DetachedStateManager
     * upon detachment.
     *  
     * ShipPackage: Has DetachedStateManager after detach.
     * Label: Detached state field - no DSM after detach.
     * Initial graph:  Label <--> ShipPackage
     * Merge: Label
     * Verify after merge:  Label' <--> ShipPackage'
     */
    public void testCascadeMergeDetachState() {
        EntityManager em = emf.createEntityManager();

        try {
            // Create simple bi-di graph
        	ShipPackage p = new ShipPackage();
            Label l = new Label(p);
            p.setLabel(l);
            
            // Persist
            em.getTransaction().begin();
            em.persist(l);
            em.getTransaction().commit();

            // Detach
            em.clear();
            assertFalse(em.contains(l));
            assertFalse(em.contains(p));
            assertFalse(em.contains(l.getPackage()));
            assertFalse(em.contains(p.getLabel()));
            
            em.getTransaction().begin();
            Label mergedLabel = em.merge(l);

            assertFalse(mergedLabel == l);
            assertFalse(p == mergedLabel.getPackage());
            // Assert that the bi-directional relationship points to the
            // newly merged entity
            assertTrue(mergedLabel == mergedLabel.getPackage().getLabel());
            assertFalse(l == mergedLabel.getPackage().getLabel());
            em.remove(mergedLabel);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                if (em.getTransaction().isActive())
                    em.getTransaction().rollback();
                em.close();
            }
        }
    }

    /** 
     * Verify a merge graph is correct when an entity uses a detached state field.  When
     * a detached state field is used, the entity does not get populated with a DetachedStateManager
     * upon detachment.  Same as testCascadeMergeDetachState, except merge is on ShipPackage2,
     * which contains the cascade instead of Label2.
     *  
     * ShipPackage: Has DetachedStateManager after detach.
     * Label: Detached state field - no DSM after detach.
     * Initial graph:  ShipPackage2 <--> Label2
     * Merge: ShipPackage2
     * Verify after merge: ShipPackage2' <-->  Label2' 
     */
    public void testCascadeMergeDetachState2() {
        EntityManager em = emf.createEntityManager();

        try {
            // Create simple bi-di graph
        	ShipPackage2 p = new ShipPackage2();
            Label2 l = new Label2(p);
            p.setLabel2(l);
            
            // Persist
            em.getTransaction().begin();
            em.persist(p);
            em.getTransaction().commit();

            // Detach
            em.clear();
            assertFalse(em.contains(l));
            assertFalse(em.contains(p));
            assertFalse(em.contains(l.getPackage2()));
            assertFalse(em.contains(p.getLabel2()));
            
            em.getTransaction().begin();
            ShipPackage2 mergedPackage = em.merge(p);

            assertFalse(mergedPackage == p);
            assertFalse(l == mergedPackage.getLabel2());
            // Assert that the bi-directional relationship points to the
            // newly merged entity
            assertTrue(mergedPackage == mergedPackage.getLabel2().getPackage2());
            assertFalse(p == mergedPackage.getLabel2().getPackage2());
            em.remove(mergedPackage);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                if (em.getTransaction().isActive())
                    em.getTransaction().rollback();
                em.close();
            }
        }
    }

    /** 
     * Verify a merge graph is correct when multiple entities of a complex
     * graph use a detached state field.  When a detached state field is used, 
     * the entity does not get populated with a DetachedStateManager upon 
     * detachment, but merge should still succeed.
     */
    public void testCascadeMergeDetachStateComplex() {
        EntityManager em = emf.createEntityManager();

        try {
        	Car c1 = new Car();
        	Car c2 = new Car();
        	Make mk1 = new Make();
        	ArrayList<Model> models = new ArrayList<Model>();
        	Model md1 = new Model();
        	models.add(md1);
        	Model md2 = new Model();
        	models.add(md2);

        	//populate bidirectional relationships
        	c1.setModel(md1);
        	c2.setModel(md2);        	
        	md1.setCar(c1);
        	md1.setMake(mk1);
        	md2.setCar(c2);
        	md1.setMake(mk1);
        	mk1.setModels(models);
        	            
            // Persist car1 - will cascade
            em.getTransaction().begin();
            em.persist(c1);
            em.getTransaction().commit();

            Object[] entities = new Object[] { c1, c2, mk1, md1, md2 };
            // detach all
            em.clear();
            // verify all entities are detached and references to them
            // are also detached.
            verifyDetached(em, entities);
            
            em.getTransaction().begin();
            // Merge model back in and verify all entities are newly merged entities
            Model mergedModel = em.merge(md1);
            assertFalse(mergedModel == md1);
            assertFalse(mergedModel.getMake() == mk1);
            List<Model> mds = mergedModel.getMake().getModels();
            assertTrue(mds.contains(mergedModel));
            assertFalse(c1 == mergedModel.getCar());
            assertTrue(mergedModel.getCar().getModel() == mergedModel);            
            em.remove(mergedModel);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                if (em.getTransaction().isActive())
                    em.getTransaction().rollback();
                em.close();
            }
        }
    }

	private void verifyDetached(EntityManager em, Object[] entities) {
        for (Object entity : entities) {
        	assertFalse(em.contains(entity));
        	if (entity instanceof Car) {
        		Car c = (Car)entity;
        		assertFalse(em.contains(c.getModel()));
        	}
        	else if (entity instanceof Make) {
        		Make m = (Make)entity;
        		List<Model> mds = m.getModels();
        		for (Model md : mds) {
        			assertFalse(em.contains(md));
        		}
        	}
        	else if (entity instanceof Model) {
        		Model m = (Model)entity;
        		assertFalse(em.contains(m.getCar()));
        		assertFalse(em.contains(m.getMake()));
        	}
        }
	}

}
