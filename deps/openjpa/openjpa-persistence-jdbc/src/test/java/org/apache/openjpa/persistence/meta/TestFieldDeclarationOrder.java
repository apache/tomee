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
package org.apache.openjpa.persistence.meta;


import org.apache.openjpa.persistence.meta.common.apps.FieldOrderPC;
import org.apache.openjpa.persistence.meta.common.apps.FieldOrderPCSubclass;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.persistence.JPAFacadeHelper;

public class TestFieldDeclarationOrder
    extends AbstractTestCase {

    public TestFieldDeclarationOrder(String test) {
        super(test, "metacactusapp");
    }

    public void testSubclass() {
        ClassMetaData meta = JPAFacadeHelper
            .getMetaData(getEmf(),
                FieldOrderPCSubclass.class);
        FieldMetaData[] fmds = meta.getFieldsInListingOrder();

        assertEquals(11, fmds.length);
        assertEquals("firstField", fmds[0].getName());
        assertEquals("secondField", fmds[1].getName());
        assertEquals("thirdField", fmds[2].getName());
        assertEquals("unmanagedField", fmds[3].getName());
        assertEquals("intField", fmds[4].getName());
        assertEquals("oneToOneField", fmds[5].getName());
        assertEquals("sub1", fmds[6].getName());
        assertEquals("sub2", fmds[7].getName());
        assertEquals("sub3", fmds[8].getName());
        assertEquals("unmanagedSubField", fmds[9].getName());
        assertEquals("undeclaredSubField", fmds[10].getName());
    }

    public void testSuperclass() {
        ClassMetaData meta = JPAFacadeHelper
            .getMetaData(getEmf(),
                FieldOrderPC.class);
        FieldMetaData[] fmds = meta.getFieldsInListingOrder();

        assertEquals(6, fmds.length);
        assertEquals("firstField", fmds[0].getName());
        assertEquals("secondField", fmds[1].getName());
        assertEquals("thirdField", fmds[2].getName());
        assertEquals("unmanagedField", fmds[3].getName());
        assertEquals("intField", fmds[4].getName());
        assertEquals("oneToOneField", fmds[5].getName());
    }
}
