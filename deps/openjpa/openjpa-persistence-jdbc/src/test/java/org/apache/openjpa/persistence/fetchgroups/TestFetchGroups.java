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

package org.apache.openjpa.persistence.fetchgroups;

import java.util.HashSet;

import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.persistence.FetchPlan;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.test.SingleEMTestCase;

public class TestFetchGroups extends SingleEMTestCase {
    private static final int empPerMgr = 5;
    private static final int mgrCount = 3;
    private static final int empCount = mgrCount * empPerMgr;
    
    
    private HashSet<FGEmployee> employeeSet = new HashSet<FGEmployee>();
    private HashSet<FGManager> managerSet = new HashSet<FGManager>();
    
    private static final String empDescriptionFieldStr =
            "org.apache.openjpa.persistence.fetchgroups.FGEmployee.description";
    
    public void setUp() {
        super.setUp(CLEAR_TABLES, 
            FGManager.class, FGDepartment.class, FGEmployee.class, FGAddress.class);
        createEmployeeData();
    }
    
    /**
     * Verify the "default" fetch plan that models JPA's expected eager/lazy fetch load behaviors.
     */
    public void testDefaultFetchPlan001() {
        OpenJPAEntityManager em = emf.createEntityManager();
        FetchPlan fp = em.getFetchPlan();
        assertNotNull(fp);
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        
        FetchConfiguration fetchCfg = ((org.apache.openjpa.persistence.EntityManagerImpl) em)
                .getBroker()
                .getFetchConfiguration();
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        
        FGManager mgr = managerSet.iterator().next();
        assertNotNull(mgr);
        
        FGManager findMgr = em.find(FGManager.class, mgr.getId());
        em.close();
        
        assertEquals(mgr.getId(), findMgr.getId());
        assertEquals(mgr.getFirstName(), findMgr.getFirstName());
        assertEquals(mgr.getLastName(), findMgr.getLastName());
        assertNull(findMgr.getDescription()); // Should be lazy-loaded
    }
    
    /**
     * Verify that adding a FetchGroup to the fetch plan makes a normally JPA determined lazy loaded
     * field to behave as an eagerly loaded field.
     */
    public void testDefaultFetchPlan002() {
        OpenJPAEntityManager em = emf.createEntityManager();
        FetchPlan fp = em.getFetchPlan();
        assertNotNull(fp);
        
        fp.addFetchGroup("DescFetchGroup");
        assertNotNull(fp.getFetchGroups());
        assertEquals(2, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFetchGroups().contains("DescFetchGroup"));
        
        FetchConfiguration fetchCfg = ((org.apache.openjpa.persistence.EntityManagerImpl) em)
                .getBroker()
                .getFetchConfiguration();
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        FGManager mgr = managerSet.iterator().next();
        assertNotNull(mgr);
        
        FGManager findMgr = em.find(FGManager.class, mgr.getId());
        em.close();
        
        assertEquals(mgr.getId(), findMgr.getId());
        assertEquals(mgr.getFirstName(), findMgr.getFirstName());
        assertEquals(mgr.getLastName(), findMgr.getLastName());
        assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
    }
    
    /**
     * Verify that adding a field to the fetch plan makes a normally JPA determined lazy loaded
     * field to behave as an eagerly loaded field.
     */
    public void testDefaultFetchPlan003() {
        OpenJPAEntityManager em = emf.createEntityManager();
        FetchPlan fp = em.getFetchPlan();
        assertNotNull(fp);
        
        fp.addField(empDescriptionFieldStr);
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFields().contains(empDescriptionFieldStr));
        
        FetchConfiguration fetchCfg = ((org.apache.openjpa.persistence.EntityManagerImpl) em)
                .getBroker()
                .getFetchConfiguration();
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        FGManager mgr = managerSet.iterator().next();
        assertNotNull(mgr);
        
        FGManager findMgr = em.find(FGManager.class, mgr.getId());
        em.close();
        
        assertEquals(mgr.getId(), findMgr.getId());
        assertEquals(mgr.getFirstName(), findMgr.getFirstName());
        assertEquals(mgr.getLastName(), findMgr.getLastName());
        assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
    }
    
    
    /**
     * Verify the use of the "openjpa.FetchGroups" property when used to add a fetch group
     * to the default fetch plan.  Note when overriding that "default" must be included in the list.
     */
    public void testPctxDefaultFetchPlan001() {
        OpenJPAEntityManagerFactory emf2 = createNamedEMF(getPersistenceUnitName(), 
            FGManager.class, FGDepartment.class, FGEmployee.class, FGAddress.class, 
            "openjpa.FetchGroups", "default,DescFetchGroup");
        
        OpenJPAEntityManager em = emf2.createEntityManager();
        FetchPlan fp = em.getFetchPlan();
        assertNotNull(fp);
        assertNotNull(fp.getFetchGroups());
        assertEquals(2, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFetchGroups().contains("DescFetchGroup"));
        
        FetchConfiguration fetchCfg = ((org.apache.openjpa.persistence.EntityManagerImpl) em)
                .getBroker()
                .getFetchConfiguration();
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        FGManager mgr = managerSet.iterator().next();
        assertNotNull(mgr);
        
        FGManager findMgr = em.find(FGManager.class, mgr.getId());
        em.close();
        emf2.close();
        
        assertEquals(mgr.getId(), findMgr.getId());
        assertEquals(mgr.getFirstName(), findMgr.getFirstName());
        assertEquals(mgr.getLastName(), findMgr.getLastName());
        assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
    }
    
    /**
     * Verify the use of the "openjpa.FetchGroups" property - when a list not containing "default"
     * is provided, then the PCtx's default fetch plan should not include it.  This renders
     * fields normally eagerly loaded as per JPA rules to behave as lazy loaded fields.
     * 
     * Note that fetch groups are case sensitive, "default" != "Default".
     */
    public void testPctxDefaultFetchPlan002() {
        OpenJPAEntityManagerFactory emf2 = createNamedEMF(getPersistenceUnitName(), 
            FGManager.class, FGDepartment.class, FGEmployee.class, FGAddress.class, 
            "openjpa.FetchGroups", "Default,DescFetchGroup");
        
        OpenJPAEntityManager em = emf2.createEntityManager();
        FetchPlan fp = em.getFetchPlan();
        assertNotNull(fp);
        assertNotNull(fp.getFetchGroups());
        assertEquals(2, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("Default")); // Not the same as "default"
        assertTrue(fp.getFetchGroups().contains("DescFetchGroup"));
        
        FetchConfiguration fetchCfg = ((org.apache.openjpa.persistence.EntityManagerImpl) em)
                .getBroker()
                .getFetchConfiguration();
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        FGManager mgr = managerSet.iterator().next();
        assertNotNull(mgr);
        
        FGManager findMgr = em.find(FGManager.class, mgr.getId());
        em.close();
        emf2.close();
        
        assertEquals(mgr.getId(), findMgr.getId()); // Identity is always loaded
        assertNull(findMgr.getFirstName());
        assertNull(findMgr.getLastName());
        assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
    }
    
    /**
     * Test clearFetchGroups(), which removes all fetch groups from the fetch plan and reactivates
     * the "default" fetch plan.
     * 
     * Note that the method does not place "default" back in the list of active fetch groups, OPENJPA-2413
     * was opened to note this behavior.
     */
    public void testClearFetchGroups001() {
        OpenJPAEntityManagerFactory emf2 = createNamedEMF(getPersistenceUnitName(), 
            FGManager.class, FGDepartment.class, FGEmployee.class, FGAddress.class, 
            "openjpa.FetchGroups", "Default,DescFetchGroup");
        
        OpenJPAEntityManager em = emf2.createEntityManager();
        FetchPlan fp = em.getFetchPlan();
        assertNotNull(fp);
        assertNotNull(fp.getFetchGroups());
        assertEquals(2, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("Default")); // Not the same as "default"
        assertTrue(fp.getFetchGroups().contains("DescFetchGroup"));
        
        FetchConfiguration fetchCfg = ((org.apache.openjpa.persistence.EntityManagerImpl) em)
                .getBroker()
                .getFetchConfiguration();
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        fp.clearFetchGroups(); // OPENJPA-2413: now places "default" in the list of active fetch groups.
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        FGManager mgr = managerSet.iterator().next();
        assertNotNull(mgr);
        
        FGManager findMgr = em.find(FGManager.class, mgr.getId());
        em.close();
        emf2.close();
        
        assertEquals(mgr.getId(), findMgr.getId());
        assertEquals(mgr.getFirstName(), findMgr.getFirstName());
        assertEquals(mgr.getLastName(), findMgr.getLastName());
        assertNull(findMgr.getDescription()); // Should be lazy-loaded        
    }

    /**
     * The resetFetchGroups() method restores the fetch plan's active fetch plans to 
     * the PCtx's configured default.
     */
    public void testResetFetchGroups001() {
        OpenJPAEntityManagerFactory emf2 = createNamedEMF(getPersistenceUnitName(), 
            FGManager.class, FGDepartment.class, FGEmployee.class, FGAddress.class, 
            "openjpa.FetchGroups", "Default,DescFetchGroup");
        
        OpenJPAEntityManager em = emf2.createEntityManager();
        FetchPlan fp = em.getFetchPlan();
        assertNotNull(fp);
        assertNotNull(fp.getFetchGroups());
        assertEquals(2, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("Default")); // Not the same as "default"
        assertTrue(fp.getFetchGroups().contains("DescFetchGroup"));
        
        FetchConfiguration fetchCfg = ((org.apache.openjpa.persistence.EntityManagerImpl) em)
                .getBroker()
                .getFetchConfiguration();
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        
        // OPENJPA-2413: now places "default" in the list of active fetch groups.
        fp.clearFetchGroups();
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        // Reset to the PCtx default Fetch Plan
        fp.resetFetchGroups();
        assertNotNull(fp);
        assertNotNull(fp.getFetchGroups());
        assertEquals(2, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("Default")); // Not the same as "default"
        assertTrue(fp.getFetchGroups().contains("DescFetchGroup"));
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        // Verify that the PCtx default fetch plan was properly restored.  "default" should not be enabled
        // since it was not listed by openjpa.FetchGroups.
        FGManager mgr = managerSet.iterator().next();
        assertNotNull(mgr);
        
        FGManager findMgr = em.find(FGManager.class, mgr.getId());
        em.close();
        emf2.close();
        
        assertEquals(mgr.getId(), findMgr.getId()); // Identity is always loaded
//        assertNull(findMgr.getFirstName()); // Commented out, for OPENJPA-2420
//        assertNull(findMgr.getLastName());
        assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
    }
    
    /**
     * Baseline test for Finder Cache
     */
    public void testFinderCache001() {
        OpenJPAEntityManager em = emf.createEntityManager();
        
        FetchPlan fp = em.getFetchPlan();
        assertNotNull(fp);
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        
        FetchConfiguration fetchCfg = ((org.apache.openjpa.persistence.EntityManagerImpl) em)
                .getBroker()
                .getFetchConfiguration();
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        FGManager mgr = managerSet.iterator().next();
        assertNotNull(mgr);
        
        {
            // First find, to prime the Finder Cache
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }
        
        {
            // Second find, should rely on the finder cache to reuse generated SQL.
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }   
        
        em.close();
    }
    
    /**
     * Only SQL generated by the PCtx's default fetch plan should be used by the finder cache,
     * as it currently lacks the ability to distinguish fetch plan configuration in its key value.
     * The PCtx's default fetch plan is the normal plan not modified by the "openjpa.FetchGroups"
     * property.
     * 
     * In this variant, a find using the default fetch plan is first executed to prime the finder cache.
     * Finds operating under a modified fetch plan should not utilize sql stored in the finder cache.
     */
    public void testFinderCache002() {
        OpenJPAEntityManager em = emf.createEntityManager();
        
        FetchPlan fp = em.getFetchPlan();
        assertNotNull(fp);
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        
        FetchConfiguration fetchCfg = ((org.apache.openjpa.persistence.EntityManagerImpl) em)
                .getBroker()
                .getFetchConfiguration();
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        FGManager mgr = managerSet.iterator().next();
        assertNotNull(mgr);
        
        {
            // First find, to prime the Finder Cache
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }
        
        {
            // Second find, should rely on the finder cache to reuse generated SQL.
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }
        
        // Add a fetch group to the fetch plan and verify expected behavior
        fp.addFetchGroup("DescFetchGroup");
        assertNotNull(fp.getFetchGroups());
        assertEquals(2, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFetchGroups().contains("DescFetchGroup"));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            // Second find, should rely on the finder cache to reuse generated SQL.
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }
        
        // Remove the fetch group previously added, and verify expected behavior
        fp.removeFetchGroup("DescFetchGroup");
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fp.getFetchGroups().contains("DescFetchGroup"));
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }   
        
        // Add a fetch group to the fetch plan and verify expected behavior
        fp.addFetchGroup("DescFetchGroup");
        assertNotNull(fp.getFetchGroups());
        assertEquals(2, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFetchGroups().contains("DescFetchGroup"));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            // Second find, should rely on the finder cache to reuse generated SQL.
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }
        
        // Reset the fetch plan, and verify expected behavior
        fp.resetFetchGroups();
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fp.getFetchGroups().contains("DescFetchGroup"));
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }
        
        // Add a fetch group to the fetch plan and verify expected behavior
        fp.addFetchGroup("DescFetchGroup");
        assertNotNull(fp.getFetchGroups());
        assertEquals(2, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFetchGroups().contains("DescFetchGroup"));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            // Second find, should rely on the finder cache to reuse generated SQL.
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }
        
        // Clear all fetch groups, and verify expected behavior
        // OPENJPA-2413: now places "default" in the list of active fetch groups.
        fp.clearFetchGroups();
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fp.getFetchGroups().contains("DescFetchGroup"));
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }
        
        em.close();
    }
    
    /**
     * Only SQL generated by the PCtx's default fetch plan should be used by the finder cache,
     * as it currently lacks the ability to distinguish fetch plan configuration in its key value.
     * The PCtx's default fetch plan is the normal plan not modified by the "openjpa.FetchGroups"
     * property.
     * 
     * In this variant, a find using a modified fetch plan is first executed, which should not be added
     * to the finder cache.  
     */
    public void testFinderCache003() {
        OpenJPAEntityManager em = emf.createEntityManager();
        
        FetchPlan fp = em.getFetchPlan();
        assertNotNull(fp);
        
        fp.addFetchGroup("DescFetchGroup");
        assertNotNull(fp.getFetchGroups());
        assertEquals(2, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFetchGroups().contains("DescFetchGroup"));
        
        FetchConfiguration fetchCfg = ((org.apache.openjpa.persistence.EntityManagerImpl) em)
                .getBroker()
                .getFetchConfiguration();
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        
        FGManager mgr = managerSet.iterator().next();
        assertNotNull(mgr);
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }
        
        // Remove the "DescFetchGroup" fetch group, and verify expected behavior
        fp.removeFetchGroup("DescFetchGroup");
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fp.getFetchGroups().contains("DescFetchGroup"));
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }   
        
        // Restore the fetch group to the fetch plan and verify expected behavior
        fp.addFetchGroup("DescFetchGroup");
        assertNotNull(fp.getFetchGroups());
        assertEquals(2, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFetchGroups().contains("DescFetchGroup"));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            // Second find, should rely on the finder cache to reuse generated SQL.
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }
        
        // Remove the "DescFetchGroup" fetch group, and verify expected behavior
        fp.removeFetchGroup("DescFetchGroup");
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fp.getFetchGroups().contains("DescFetchGroup"));
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        } 
        
        // Restore the fetch group to the fetch plan and verify expected behavior
        fp.addFetchGroup("DescFetchGroup");
        assertNotNull(fp.getFetchGroups());
        assertEquals(2, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFetchGroups().contains("DescFetchGroup"));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            // Second find, should rely on the finder cache to reuse generated SQL.
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }
        
        // Reset the fetch plan, and verify expected behavior
        fp.resetFetchGroups();
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fp.getFetchGroups().contains("DescFetchGroup"));
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }
        
        // Restore the fetch group to the fetch plan and verify expected behavior
        fp.addFetchGroup("DescFetchGroup");
        assertNotNull(fp.getFetchGroups());
        assertEquals(2, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFetchGroups().contains("DescFetchGroup"));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            // Second find, should rely on the finder cache to reuse generated SQL.
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }
        
        // Clear all fetch groups, and verify expected behavior
        // OPENJPA-2413: now places "default" in the list of active fetch groups.
        fp.clearFetchGroups();
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fp.getFetchGroups().contains("DescFetchGroup"));
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }
        
        em.close();
    }
    
    /**
     * Only SQL generated by the PCtx's default fetch plan should be used by the finder cache,
     * as it currently lacks the ability to distinguish fetch plan configuration in its key value.
     * The PCtx's default fetch plan is modified by the "openjpa.FetchGroups" property.
     *  
     */
    public void testFinderCache004() {
        OpenJPAEntityManagerFactory emf2 = createNamedEMF(getPersistenceUnitName(), 
            FGManager.class, FGDepartment.class, FGEmployee.class, FGAddress.class, 
            "openjpa.FetchGroups", "default,DescFetchGroup");
        
        OpenJPAEntityManager em = emf2.createEntityManager();
        FetchPlan fp = em.getFetchPlan();
        assertNotNull(fp);
        assertNotNull(fp.getFetchGroups());
        assertEquals(2, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFetchGroups().contains("DescFetchGroup"));
        
        FetchConfiguration fetchCfg = ((org.apache.openjpa.persistence.EntityManagerImpl) em)
                .getBroker()
                .getFetchConfiguration();
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        FGManager mgr = managerSet.iterator().next();
        assertNotNull(mgr);
        
        {
            // First find, to prime the Finder Cache
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }
        
        {
            // Second find, should rely on the finder cache to reuse generated SQL.
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }
        
        // Remove a fetch group to the fetch plan and verify expected behavior
        fp.removeFetchGroup("DescFetchGroup");
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fp.getFetchGroups().contains("DescFetchGroup"));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            // Second find, should rely on the finder cache to reuse generated SQL.
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }
        
        // Restore the fetch group previously removed, and verify expected behavior
        fp.addFetchGroup("DescFetchGroup");
        assertNotNull(fp.getFetchGroups());
        assertEquals(2, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFetchGroups().contains("DescFetchGroup"));
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }   
        
        // Remove a fetch group to the fetch plan and verify expected behavior
        fp.removeFetchGroup("DescFetchGroup");
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fp.getFetchGroups().contains("DescFetchGroup"));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            // Second find, should rely on the finder cache to reuse generated SQL.
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }
        
        // Reset the fetch plan, and verify expected behavior
        fp.resetFetchGroups();
        assertNotNull(fp.getFetchGroups());
        assertEquals(2, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFetchGroups().contains("DescFetchGroup"));
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }
        
        // Clear all fetch groups, and verify expected behavior
        // OPENJPA-2413: now places "default" in the list of active fetch groups.
        fp.clearFetchGroups();
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fp.getFetchGroups().contains("DescFetchGroup"));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }
        
        em.close();
        emf2.close();
    }
    
    /**
     * Only SQL generated by the PCtx's default fetch plan should be used by the finder cache,
     * as it currently lacks the ability to distinguish fetch plan configuration in its key value.
     * The PCtx's default fetch plan is modified by the "openjpa.FetchGroups" property.
     * 
     * In this variant, a find using a modified fetch plan is first executed, which should not be added
     * to the finder cache.  
     */
    public void testFinderCache005() {
        OpenJPAEntityManagerFactory emf2 = createNamedEMF(getPersistenceUnitName(), 
            FGManager.class, FGDepartment.class, FGEmployee.class, FGAddress.class, 
            "openjpa.FetchGroups", "default,DescFetchGroup");
        
        OpenJPAEntityManager em = emf2.createEntityManager();
        FetchPlan fp = em.getFetchPlan();
        assertNotNull(fp);
        assertNotNull(fp.getFetchGroups());
        assertEquals(2, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFetchGroups().contains("DescFetchGroup"));
        
        FetchConfiguration fetchCfg = ((org.apache.openjpa.persistence.EntityManagerImpl) em)
                .getBroker()
                .getFetchConfiguration();
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        FGManager mgr = managerSet.iterator().next();
        assertNotNull(mgr);
        
        fp.removeFetchGroup("DescFetchGroup");
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fp.getFetchGroups().contains("DescFetchGroup"));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }
        
        // Restore the "DescFetchGroup" fetch group, and verify expected behavior
        fp.addFetchGroup("DescFetchGroup");
        assertNotNull(fp.getFetchGroups());
        assertEquals(2, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFetchGroups().contains("DescFetchGroup"));
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }   
        
        // Remove the "DescFetchGroup" fetch group, and verify expected behavior
        fp.removeFetchGroup("DescFetchGroup");
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fp.getFetchGroups().contains("DescFetchGroup"));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        } 
        
        // Reset the fetch plan, and verify expected behavior
        fp.resetFetchGroups();
        assertNotNull(fp.getFetchGroups());
        assertEquals(2, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFetchGroups().contains("DescFetchGroup"));
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }
        
        // Remove the "DescFetchGroup" fetch group, and verify expected behavior
        fp.removeFetchGroup("DescFetchGroup");
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fp.getFetchGroups().contains("DescFetchGroup"));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        } 
        
        // Clear all fetch groups, and verify expected behavior
        // OPENJPA-2413: now places "default" in the list of active fetch groups.
        fp.clearFetchGroups();
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fp.getFetchGroups().contains("DescFetchGroup"));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }
        
        em.close();
        emf2.close();
    }
    
    /**
     * Only SQL generated by the PCtx's default fetch plan should be used by the finder cache,
     * as it currently lacks the ability to distinguish fetch plan configuration in its key value.
     * The PCtx's default fetch plan is the normal plan not modified by the "openjpa.FetchGroups"
     * property.
     * 
     * In this variant, a find using the default fetch plan is first executed to prime the finder cache.
     * Finds operating under a modified fetch plan should not utilize sql stored in the finder cache.
     */
    public void testFinderCache006() {
        OpenJPAEntityManager em = emf.createEntityManager();
        
        FetchPlan fp = em.getFetchPlan();
        assertNotNull(fp);
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        
        FetchConfiguration fetchCfg = ((org.apache.openjpa.persistence.EntityManagerImpl) em)
                .getBroker()
                .getFetchConfiguration();
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        FGManager mgr = managerSet.iterator().next();
        assertNotNull(mgr);
        
        {
            // First find, to prime the Finder Cache
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }
        
        {
            // Second find, should rely on the finder cache to reuse generated SQL.
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }
        
        // Add a field to the fetch plan and verify expected behavior
        fp.addField(empDescriptionFieldStr);
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFields().contains(empDescriptionFieldStr));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            // Second find, should rely on the finder cache to reuse generated SQL.
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }
        
        // Remove the field previously added, and verify expected behavior
        fp.removeField(empDescriptionFieldStr);
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fp.getFetchGroups().contains("DescFetchGroup"));
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }   
        
        // Add a field to the fetch plan and verify expected behavior
        fp.addField(empDescriptionFieldStr);
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFields().contains(empDescriptionFieldStr));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            // Second find, should rely on the finder cache to reuse generated SQL.
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }
        
        // Reset the fetch groups, and verify expected behavior (note the reset doesn't remove added fields!)
        fp.resetFetchGroups();
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFields().contains(empDescriptionFieldStr));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }
        
        // Clear all fetch groups, and verify expected behavior (note the reset doesn't remove added fields!)
        // OPENJPA-2413: now places "default" in the list of active fetch groups.
        fp.clearFetchGroups();
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFields().contains(empDescriptionFieldStr));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }
        
        // Clear all fields, and verify expected behavior
        fp.clearFields();
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fp.getFields().contains(empDescriptionFieldStr));
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }
        
        em.close();
    }
    
    /**
     * Only SQL generated by the PCtx's default fetch plan should be used by the finder cache,
     * as it currently lacks the ability to distinguish fetch plan configuration in its key value.
     * The PCtx's default fetch plan is the normal plan not modified by the "openjpa.FetchGroups"
     * property.
     * 
     * In this variant, a find using a modified fetch plan is first executed, which should not be added
     * to the finder cache.  
     */
    public void testFinderCache007() {
        OpenJPAEntityManager em = emf.createEntityManager();
        
        FetchPlan fp = em.getFetchPlan();
        assertNotNull(fp);
        
        fp.addField(empDescriptionFieldStr);
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFields().contains(empDescriptionFieldStr));
        
        FetchConfiguration fetchCfg = ((org.apache.openjpa.persistence.EntityManagerImpl) em)
                .getBroker()
                .getFetchConfiguration();
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        
        FGManager mgr = managerSet.iterator().next();
        assertNotNull(mgr);
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }
        
        // Remove the field, and verify expected behavior
        fp.removeField(empDescriptionFieldStr);
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fp.getFetchGroups().contains("DescFetchGroup"));
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }   
        
        // Restore the field to the fetch plan and verify expected behavior
        fp.addField(empDescriptionFieldStr);
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFields().contains(empDescriptionFieldStr));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            // Second find, should rely on the finder cache to reuse generated SQL.
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }
        
        // Remove the "DescFetchGroup" fetch group, and verify expected behavior
        fp.removeField(empDescriptionFieldStr);
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fp.getFetchGroups().contains("DescFetchGroup"));
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        } 
        
        // Restore the field to the fetch plan and verify expected behavior
        fp.addField(empDescriptionFieldStr);
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFields().contains(empDescriptionFieldStr));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            // Second find, should rely on the finder cache to reuse generated SQL.
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }
        
        // Reset the fetch plan, and verify expected behavior (should not affect fields)
        fp.resetFetchGroups();
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fp.getFetchGroups().contains("DescFetchGroup"));
        assertTrue(fp.getFields().contains(empDescriptionFieldStr));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }
        
        // Clear all fetch groups, and verify expected behavior (should not affect fields)
        // OPENJPA-2413: now places "default" in the list of active fetch groups.
        fp.clearFetchGroups();
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertTrue(fp.getFields().contains(empDescriptionFieldStr));
        assertFalse(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertEquals(mgr.getDescription(), findMgr.getDescription()); // Should not be lazy-loaded
        }
        
        // Clear all fields, and verify expected behavior
        fp.clearFields();
        assertNotNull(fp.getFetchGroups());
        assertEquals(1, fp.getFetchGroups().size());
        assertTrue(fp.getFetchGroups().contains("default"));
        assertFalse(fp.getFields().contains(empDescriptionFieldStr));
        assertTrue(fetchCfg.isDefaultPUFetchGroupConfigurationOnly());
        
        {
            FGManager findMgr = em.find(FGManager.class, mgr.getId());
            em.clear();
            
            assertEquals(mgr.getId(), findMgr.getId());
            assertEquals(mgr.getFirstName(), findMgr.getFirstName());
            assertEquals(mgr.getLastName(), findMgr.getLastName());
            assertNull(findMgr.getDescription()); // Should be lazy-loaded
        }
        
        em.close();
    }
    
    private void createEmployeeData() {
        OpenJPAEntityManager em = emf.createEntityManager();
        
        employeeSet.clear();
        managerSet.clear();
        
        int empIdIndex = 1;
        
        em.getTransaction().begin();
        
        // Create Managers
        for (int i = 1; i < mgrCount; i++) {
            int id = empIdIndex++;
            
            FGAddress addr = createAddress(id);
            em.persist(addr);
            
            FGDepartment dept = createDepartment(id);
            em.persist(dept);
            
            FGManager mgr = new FGManager();
            mgr.setId(id);           
            mgr.setFirstName("First-" + id);
            mgr.setLastName("Last-" + id);
            mgr.setMData("MData-" + id);
            mgr.setRating("Rating-" + id);
            mgr.setDescription("Manager-" + id);
            mgr.setAddress(addr);
            mgr.setDept(dept);
            
            em.persist(mgr);
            
            managerSet.add(mgr);
        }
        
        // Create Employees
        for (int i = 1; i < empCount; i++) {
            int id = empIdIndex++;
            int mgrId = (id % empPerMgr) + 1;
            
            FGAddress addr = createAddress(id);
            em.persist(addr);
            
            FGDepartment dept = createDepartment(id);
            em.persist(dept);
            
            FGEmployee emp = new FGEmployee();
            emp.setId(id);           
            emp.setFirstName("First-" + id);
            emp.setLastName("Last-" + id);
            emp.setRating("Rating-" + id);
            emp.setDescription("Employee-" + id);
            emp.setAddress(addr);
            emp.setDept(dept);
            
            em.persist(emp);
            
            employeeSet.add(emp);
        }
        
        em.getTransaction().commit();
        
        em.close();
    }
    
    private FGAddress createAddress(int id) {
        FGAddress addr = new FGAddress();
        addr.setId(id);
        addr.setStreet("Street-" + id);
        addr.setCity("City-" + id);
        addr.setState("State-" + id);
        addr.setZip(id);
        
        return addr;
    }
   
    private FGDepartment createDepartment(int id) {
        FGDepartment dept = new FGDepartment();
        dept.setId(id);
        dept.setName("Department-" + id);
        
        return dept;
    }
}
