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

import org.apache.openjpa.persistence.test.AllowFailure;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;
import org.apache.openjpa.util.ImplHelper;

@AllowFailure(message="excluded")
public class TestEnhancementConfiguration
    extends SingleEMFTestCase {

    @Override
    public void setUp() throws Exception {
        setUp("openjpa.RuntimeUnenhancedClasses", "unsupported",
            UnenhancedFieldAccess.class, CLEAR_TABLES);
    }
    
    public void testEnhancementConfiguration() {
        try {
            assertFalse(ImplHelper.isManagedType(emf.getConfiguration(),
                UnenhancedFieldAccess.class));
            emf.createEntityManager().close();
            fail("should not be possible to fully-initialize a system " +
                "that depends on unenhanced types but disables runtime" +
                "redefinition.");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains(
                "This configuration disallows runtime optimization"));
        }
    }
}
