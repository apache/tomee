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
package org.apache.openjpa.conf;


import junit.framework.TestCase;
/**
 * Test basics of Specification object.
 * 
 * @author Pinaki Poddar
 *
 */
public class TestSpecification extends TestCase {
    public void testStaticConstruction() {
        Specification spec1 = new Specification("JPA 2.3");
        assertEquals("JPA", spec1.getName());
        assertEquals(2, spec1.getVersion());
        assertEquals("3", spec1.getMinorVersion());

        Specification spec2 = new Specification("JPA 1.1");
        assertEquals("JPA", spec2.getName());
        assertEquals(1, spec2.getVersion());
        assertEquals("1", spec2.getMinorVersion());
        
        Specification spec3 = new Specification("JDO 3.ED");
        assertEquals("JDO", spec3.getName());
        assertEquals(3, spec3.getVersion());
        assertEquals("ED", spec3.getMinorVersion());
        
        Specification spec4 = new Specification("JDO 3.5");
        assertEquals("JDO", spec4.getName());
        assertEquals(3, spec4.getVersion());
        assertEquals("5", spec4.getMinorVersion());
    }
    
    public void testEqualityByName() {
        Specification spec1 = new Specification("JPA 2.3");
        Specification spec2 = new Specification("JPA 1.0");
        Specification spec3 = new Specification("JDO 3.1");
        
        assertTrue(spec1.isSame(spec2));
        assertTrue(spec1.isSame("jpa"));
        assertTrue(spec1.isSame("JPA"));
        
     
        assertFalse(spec1.isSame(spec3));
    }
    
    public void testVersionCompare() {
        Specification spec1 = new Specification("JPA 1.1");
        Specification spec2 = new Specification("JPA 2.2");
        assertTrue(spec1.compareVersion(spec2) < 0);
        assertTrue(spec2.compareVersion(spec1) > 0);
        assertTrue(spec1.compareVersion(spec1) == 0);
    } 
}
