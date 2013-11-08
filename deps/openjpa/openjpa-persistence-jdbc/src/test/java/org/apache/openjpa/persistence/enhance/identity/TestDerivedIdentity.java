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
package org.apache.openjpa.persistence.enhance.identity;

import javax.persistence.EntityManager;

import org.apache.openjpa.persistence.ArgumentException;
import org.apache.openjpa.persistence.PersistenceException;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestDerivedIdentity extends SingleEMFTestCase {
    public void setUp() throws Exception {
        super.setUp(DROP_TABLES, Dependent5a.class, Employee5.class, EmployeeId5.class);
    }

    public void testIncorrectJoinColumnAnnotation() {
        try {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            em.getTransaction().commit();
        } catch (PersistenceException e) {
            fail("Wrong exception");
        } catch (ArgumentException e) {
            //Correct exception is thrown:
            //"org.apache.openjpa.persistence.enhance.identity.Dependent5a.emp" defines a target of "xFIRSTNAME" 
            //for column "FIRSTNAME", but that target does not exist in table "Employee5".
        }
        
        
    }

}
