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

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.inheritance.entities.EntityMapping;
import org.apache.openjpa.persistence.inheritance.entities.EntityMapping.
    InheritanceEntityMapping;
import org.apache.openjpa.persistence.inheritance.entities.testinterfaces.
    RootEntity;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestInheritanceWithMSCID extends SingleEMFTestCase {
    @SuppressWarnings("unchecked")
    public void setUp() {
        /*
         * All entities used by this test are defined in the enumeration
         * EntityMapping.InheritanceEntityMapping.
         */
        EntityMapping.InheritanceEntityMapping[] entityClassEnums = 
            EntityMapping.InheritanceEntityMapping.values();
        
        Class[] entityClassTypes = new Class[entityClassEnums.length];
        int idx = 0;
        for (EntityMapping.InheritanceEntityMapping eEnum : entityClassEnums) {
            entityClassTypes[idx] = eEnum.getEntityClass();
            idx++;
        }
        
        setUp((Object[]) entityClassTypes);
    }

    private InheritanceEntityMapping[][] allEntityGroups = { 
            EntityMapping.PIdJTIDMSC, 
            EntityMapping.PIdJTCDMSC,
            EntityMapping.PIdJTSDMSC,
            
            EntityMapping.PIdSTIDMSC, 
            EntityMapping.PIdSTCDMSC,
            EntityMapping.PIdSTSDMSC
    };
    
    /**
     * Verifies that each entity type in the inheritance structure can be
     * persisted.  Entities are not cleared out of the persistence context
     * (the L1 cache) when they are created.
     */
    @SuppressWarnings("unchecked")
    public void testPersistableWithEntitiesInL1Cache() {
        EntityManager em = emf.createEntityManager();
        
        for (InheritanceEntityMapping[] tEntities : allEntityGroups ) {
            int idx = 0;
            
            try {
                for (InheritanceEntityMapping tEntity : tEntities) {
                    RootEntity entity = (RootEntity) 
                        EntityMapping.createEntityObjectInstance(tEntity);
                    entity.updateId(new Integer(idx++));
                    entity.setRootEntityData("Root " + (idx - 1));
                    
                    em.getTransaction().begin();
                    em.persist(entity);
                    em.getTransaction().commit();
                }
            } catch (Exception e) {
                fail("Test failed with Exception\n" + e);
            }
            
            List<RootEntity> resultList = (List<RootEntity>) em.createQuery(
                    "SELECT e FROM " + tEntities[0].getEntityName() + 
                    " e ORDER BY e.id").getResultList();
            assertEquals(tEntities.length, resultList.size());
      
            idx = 0;
            for (Object obj : resultList) {
                RootEntity entity = (RootEntity) obj;
                Class<? extends RootEntity> actualType = entity.getClass();
                Class<? extends RootEntity> expectedType = 
                    tEntities[idx].getEntityClass();
                
                assertEquals(
                        "Assert Entity " + (idx + 1) + "is entity of type " + 
                        expectedType, expectedType, actualType);
                
                idx++;
            }
        }
        
        em.close();
    }
    
    /**
     * Verifies that each entity type in the inheritance structure can be
     * persisted.  Entities are cleared out of the persistence context
     * (the L1 cache) after they are created.
     */
    @SuppressWarnings("unchecked")
    public void testPersistableWithEntitiesClearedFromL1Cache() {
        EntityManager em = emf.createEntityManager();       
        
        for (InheritanceEntityMapping[] tEntities : allEntityGroups ) {
            int idx = 0;
            
            try {
                for (InheritanceEntityMapping tEntity : tEntities) {
                    RootEntity entity = (RootEntity) 
                        EntityMapping.createEntityObjectInstance(tEntity);
                    entity.updateId(new Integer(idx++));
                    entity.setRootEntityData("Root " + (idx - 1));
                    
                    em.getTransaction().begin();
                    em.persist(entity);
                    em.getTransaction().commit();
                    em.clear();
                }
            } catch (Exception e) {
                fail("Test failed with Exception\n" + e);
            }
            
            List<RootEntity>  resultList = (List<RootEntity>) em.createQuery(
                    "SELECT e FROM " + tEntities[0].getEntityName() + 
                    " e ORDER BY e.id").getResultList();
            assertEquals(tEntities.length, resultList.size());
      
            idx = 0;
            for (Object obj : resultList) {
                RootEntity entity = (RootEntity) obj;
                Class<? extends RootEntity> actualType = entity.getClass();
                Class<? extends RootEntity> expectedType = 
                    tEntities[idx].getEntityClass();
                
                assertEquals(
                        "Assert Entity " + (idx + 1) + "is entity of type " + 
                        expectedType, expectedType, actualType);
                
                idx++;
            }
        }
        
        em.close();
    }
    

    
    /*
     * The following tests exercise the following scenario:
     * 
     * Verify that all-inclusive SELECT against an entity that is a member
     * of an inheritance hierarchy will include all of its subclasses within
     * the query results as well.
     * 
     * Variations of this test include clearing the L1 cache after db 
     * population and between each Query check, and not clearing the L1 
     * cache.
     * 
     */
    
    private boolean[][] queryLogic001AcceptenceMapping = new boolean[][] {
            // RootEntity
            new boolean[] { true, true, true, true, true, true, true, true,
                    true },
            // Leaf A
            new boolean[] { false, true, false, false, false, false, false,
                    false, false },
            // EntityB
            new boolean[] { false, false, true, true, true, false, false,
                    false, false },
            // Leaf B1
            new boolean[] { false, false, false, true, false, false, false,
                    false, false },
            // Leaf B2
            new boolean[] { false, false, false, false, true, false, false,
                    false, false },
            // Leaf C
            new boolean[] { false, false, false, false, false, true, false,
                    false, false },
            // Entity D
            new boolean[] { false, false, false, false, false, false, true,
                    true, true },
            // Leaf D1
            new boolean[] { false, false, false, false, false, false, false,
                    true, false },
            // Leaf D2
            new boolean[] { false, false, false, false, false, false, false,
                    false, true } };

    @SuppressWarnings("unchecked")
    private void query001TestLogic(boolean clearL1Cache) {
        EntityManager em = emf.createEntityManager();         
            
        boolean[][] acceptenceMapping = queryLogic001AcceptenceMapping;
        
        for (InheritanceEntityMapping[] tEntities : allEntityGroups ) {
            // Populate Database
            int idx = 0;
            try {
                for (InheritanceEntityMapping tEntity : tEntities) {
                    RootEntity entity = (RootEntity) 
                        EntityMapping.createEntityObjectInstance(tEntity);
                    entity.updateId(new Integer(idx++));
                    entity.setRootEntityData("Root " + (idx - 1));
                    
                    em.getTransaction().begin();
                    em.persist(entity);
                    em.getTransaction().commit();
                    
                    if (clearL1Cache)
                        em.clear();
                }
            } catch (Exception e) {
                fail("Test failed with Exception\n" + e);
            }
            
            // Run through acceptance tests
            idx = 0;
            for (boolean[] acceptenceMap : acceptenceMapping) {
                int expectedQueryResultListSize = 0;
                for (boolean acceptence : acceptenceMap) {
                    if (acceptence)
                        expectedQueryResultListSize++;
                }
                String queryStr = "SELECT e FROM " + 
                    tEntities[idx].getEntityName() + " e";
                List<RootEntity> resultList = (List<RootEntity>)
                    em.createQuery(queryStr).getResultList();

                assertEquals(
                        "Assert the following query creates a result list " +
                        "with " + expectedQueryResultListSize + " entities: " +
                        "\"" + queryStr + "\".  ",
                        expectedQueryResultListSize, 
                        resultList.size());
                idx++;
                
                if (clearL1Cache)
                    em.clear();
            }        
        }

        em.close();
    }
    
    /**
     * Verify that all-inclusive SELECT against an entity that is a member of an
     * inheritance hierarchy will include all of its subclasses within the query
     * results as well.  Test will not clear the L1 cache after populating DB
     * and will not clear the L1 cache in between Query executions.
     */
    public void testQuery001WithL1() {
        query001TestLogic(false);
    }
    
    /**
     * Verify that all-inclusive SELECT against an entity that is a member of an
     * inheritance hierarchy will include all of its subclasses within the query
     * results as well.  Test will clear the L1 cache after populating DB
     * and will  clear the L1 cache in between Query executions.
     */
    public void testQuery001NoL1() {
        query001TestLogic(true);
    }
}
