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
package org.apache.openjpa.meta;

import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestSecondaryTable extends SingleEMFTestCase {
    public void setUp() {
        setUp(Parent.class, PChild.class, PChildBi.class
        // Hard code to 2.0 p.xml value. If the p.xml is 1.0, this value will be changed to false, and the test
        // won't fail.
            , "openjpa.Compatibility", "NonDefaultMappingAllowed=true");
    }

    /**
     * Added for OPENJPA-2247.
     */
    public void testMappingInfo() {
        FieldMapping fm = getMapping(Parent.class).getFieldMapping("child");
        assertNotNull(fm);
        assertEquals("CHILD_REF", fm.getColumns()[0].getIdentifier().getName());

        fm = getMapping(Parent.class).getFieldMapping("childbi");
        assertNotNull(fm);
        assertEquals("CHILDBI_REF", fm.getColumns()[0].getIdentifier().getName());

        fm = getMapping(Parent.class).getFieldMapping("children");
        assertNotNull(fm);
        assertEquals("CHILDREN_REF", fm.getColumns()[0].getIdentifier().getName());
    }
}
