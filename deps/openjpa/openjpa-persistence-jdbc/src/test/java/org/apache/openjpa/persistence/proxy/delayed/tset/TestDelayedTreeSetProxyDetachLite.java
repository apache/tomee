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
package org.apache.openjpa.persistence.proxy.delayed.tset;

import java.util.Collection;

import javax.persistence.EntityManager;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.persistence.proxy.delayed.IDepartment;
import org.apache.openjpa.persistence.proxy.delayed.IEmployee;
import org.apache.openjpa.util.DelayedTreeSetProxy;

public class TestDelayedTreeSetProxyDetachLite extends TestDelayedTreeSetProxy {

    @Override
    public void setUp() {
        super.setUp(
                "openjpa.DetachState", "loaded(LiteAutoDetach=true,detachProxyFields=false)");
    }
    
    /*
     * Verify that a collection can be loaded post detachment
     */
    @Override
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
        assertTrue(emps instanceof DelayedTreeSetProxy);
        DelayedTreeSetProxy dep = (DelayedTreeSetProxy)emps;
        dep.setDirectAccess(true);
        assertEquals(0, dep.size());
        dep.setDirectAccess(false);
        assertNotNull(emps);
        // call contains and assert a select from the employee table
        // occurred that the expected entities are returned.
        resetSQL();
        assertTrue(emps.contains(e));
        e = getEmployee(emps,0);
        assertAnySQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
        resetSQL();
        assertEquals(1, emps.size());
        // Verify the delay load entity is detached
        assertTrue(e instanceof PersistenceCapable);
        PersistenceCapable pc = (PersistenceCapable)e;
        // LiteAutoDetach
        assertTrue(pc.pcGetStateManager() == null);
        // verify a second SQL was not issued to get the size
        assertNoneSQLAnyOrder("SELECT .* DC_EMPLOYEE .*");
    }
}
