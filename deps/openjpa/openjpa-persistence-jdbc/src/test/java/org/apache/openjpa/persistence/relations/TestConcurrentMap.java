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
package org.apache.openjpa.persistence.relations;

import org.apache.openjpa.persistence.test.SingleEMTestCase;

public class TestConcurrentMap extends SingleEMTestCase {
	public void setUp() {
        setUp(ConcurrentEntityLeft.class, ConcurrentEntityRight.class,
              CLEAR_TABLES);
    }

    public void testConcurrentMap001() {
        em.getTransaction().begin();
        
        ConcurrentEntityLeft left = new ConcurrentEntityLeft();
        left.setId(1);
        left.setStrData("Lefty");
        
        ConcurrentEntityRight right = new ConcurrentEntityRight();
        right.setId(1);
        right.setStrData("Poncho");
        
        em.persist(left);
        em.persist(right);
        
        left.setRightEntity(right);
        right.getLeftEntityMap().put(left.getStrData(), left);
        
        em.getTransaction().commit();
    }
}
