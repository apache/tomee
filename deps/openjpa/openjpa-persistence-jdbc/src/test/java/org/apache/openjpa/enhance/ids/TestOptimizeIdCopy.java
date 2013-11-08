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
package org.apache.openjpa.enhance.ids;

import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Verifies the use of the openjpa.OptimizeIdCopy configuration parameter.  This parameter
 * changes the behavior of the enhancer and thus, must be set before enhancement occurs.  There
 * is special enhancement task in main/ant/enhancer.xml to ensure this value is set
 * during the enhancement process for the entities used by this test.
 */
public class TestOptimizeIdCopy extends SingleEMFTestCase {
    
    @Override
    public void setUp() {
        setUp(Device.class,Hardware.class,Software.class, CLEAR_TABLES);
    }
    
    /*
     * Verifies that constructor-based Id optimization occurs during Id copy. Asserts
     * only the proper/expected public constructor is called during the id copy operation.
     */
    public void testIdOptimization() {
        EntityManager em = emf.createEntityManager();
        
        // Add a software entity
        Software sw = new Software();
        int id = new Random().nextInt();
        sw.setIdInt(id);
        sw.setIdInteger(10);
        sw.setIdString("StringIdVal");
        
        em.getTransaction().begin();
        em.persist(sw);
        em.getTransaction().commit();
        em.clear();
        
        TypedQuery<Software> swq = em.createQuery("select sw from Software sw", Software.class);
        List<Software> swl = swq.getResultList();
        assertTrue("Software result list > 0", swl.size() > 0);
        // Id copy optimization should have used the 3rd constructor
        assertFalse("First constructor was not used", SoftwareId.usedConstructor[0]);
        assertFalse("Second constructor was not used", SoftwareId.usedConstructor[1]);
        assertTrue("Third (correct) constructor was used", SoftwareId.usedConstructor[2]);
        em.close();
    }
    
    /*
     * Verifies that constructor based optimization functions even if parms
     * are different than field order
     */
    public void testIdOptimizationConstructorOutOfOrder() {
        EntityManager em = emf.createEntityManager();
        
        Hardware hw = new Hardware();
        String id = "Model" + (new Random().nextInt());
        hw.setModel("Model" + id);
        hw.setSerial("123XYZ");
        
        em.getTransaction().begin();
        em.persist(hw);
        em.getTransaction().commit();
        em.clear();
        
        TypedQuery<Hardware> hwq = em.createQuery("select hw from Hardware hw", Hardware.class);
        List<Hardware> hwl = hwq.getResultList();
        assertTrue("Hardware result list > 0", hwl.size() > 0);
        // Id copy optimization should have used the first constructor
        assertTrue("First (correct) constructor was used", HardwareId.usedConstructor[0]);
        assertFalse("Second constructor was not used", HardwareId.usedConstructor[1]);
        em.close();
    }
    
    /*
     * Verifies that classes without a proper constructor do not get optimized
     */
    public void testNoOptimization() {
        EntityManager em = emf.createEntityManager();
        
        int id = new Random().nextInt();
        Device d = new Device();
        d.setId(id);
        d.setType(10);
        
        em.getTransaction().begin();
        em.persist(d);
        em.getTransaction().commit();
        em.clear();

        TypedQuery<Device> dq = em.createQuery("select d from Device d", Device.class);
        List<Device> dl = dq.getResultList();
        assertTrue("Device result list > 0", dl.size() > 0);
        // Only the first, default constructor should have been called
        assertTrue("First (default) constructor was used", DeviceId.usedConstructor[0]);
        assertFalse("Second constructor was not used", DeviceId.usedConstructor[1]);
        assertFalse("Third constructor was not used", DeviceId.usedConstructor[2]);
        em.close();
    }
}
