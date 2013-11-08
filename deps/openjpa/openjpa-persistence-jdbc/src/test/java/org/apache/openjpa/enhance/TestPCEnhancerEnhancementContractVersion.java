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
package org.apache.openjpa.enhance;

import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.log.NoneLogFactory.NoneLog;
import org.apache.openjpa.persistence.DummyPersistenceCapeable;
import org.apache.openjpa.persistence.Country;
import org.apache.openjpa.persistence.test.AbstractPersistenceTestCase;

public class TestPCEnhancerEnhancementContractVersion extends AbstractPersistenceTestCase {
    Log log = NoneLog.getInstance();

    protected void setUp() throws Exception {
        super.setUp();
        // Create to trigger static initializer to run.
        new Country();
        new DummyPersistenceCapeable();
    }
    public void testCurrentLevel() {
        assertFalse(PCEnhancer.checkEnhancementLevel(Country.class, log));
    }
    
    public void testDownLevel(){
        assertTrue(PCEnhancer.checkEnhancementLevel(DummyPersistenceCapeable.class, log));
    }
    public void testContact() {
        assertFalse(PCEnhancer.checkEnhancementLevel(null, log));
        assertFalse(PCEnhancer.checkEnhancementLevel(Country.class, null));
        try {
            PCEnhancer.checkEnhancementLevel(Object.class, log);
            fail("Should have got an IllegalArgumentException exception from " +
            		"org.apache.openjpa.enhance.PCEnhancer.checkEnhancementLevel");
        } catch (java.lang.IllegalStateException ile) {
            // expected
        }
    }

}
