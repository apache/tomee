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
package org.apache.openjpa.jira1100;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Tests that entity with unsupported version type can not be used.
 * Used to ignore such field silently before. 
 * <BR>
 * For more details refer
 * 
 *   <A HREF="https://issues.apache.org/jira/browse/OPENJPA-1100">OPENJPA-1100</A>
 *   
 * @author Pinaki Poddar
 *
 */
public class TestBadVersionField extends SingleEMFTestCase {
    public void setUp() {
        super.setUp(CLEAR_TABLES, Data.class);
    }
    
    public void testWrongVersionFieldNotSupported() {
        try {
            EntityManager em = emf.createEntityManager();
            fail("Expected to fail with unsupported Version field type");
        } catch (Exception ex) {
            
        }
    }
    
    /**
     * Declares a Version field of unsupported type.
     *  
     */
    @Entity
    @Table(name="BadVersionField")
    public class Data {
        @Id
        @GeneratedValue
        private long id;
        
        @Version
        private BigDecimal version;

        public long getId() {
            return id;
        }

        public BigDecimal getVersion() {
            return version;
        }
    }

}
