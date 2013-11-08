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
package org.apache.openjpa.persistence.hugariannotation;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

/**
 * Created by IntelliJ IDEA.
 * User: Ben
 * Date: 02-Nov-2007
 * Time: 21:36:36
 */
public class TestHungarianNotationRemoval extends SingleEMFTestCase {

    public void setUp() {
        setUp(HungarianNotationFieldDuplicates.class,
            HungarianNotationFields.class, OtherClass.class, CLEAR_TABLES,
            "openjpa.jdbc.MappingDefaults", "removeHungarianNotation=true");
    }

    public void testSimpleColumnNameTruncation() {
        ClassMapping cm = (ClassMapping) JPAFacadeHelper
            .getMetaData(emf, HungarianNotationFields.class);

        FieldMapping[] fieldMappings = cm.getFieldMappings();

        for (int i = 0; i < fieldMappings.length; i++) {
            final String name = fieldMappings[i].getColumns()[0].getName();

            // this one doesn't follow the rules
            if (fieldMappings[i].getName().equals("m_intFooBar7"))
                continue;

            assertTrue(
                "Failed to removed Hungarian Notation, resulting column name : "
                    + name, name.toUpperCase().startsWith("FOOBAR"));
        }
    }

    public void testCustomNameNotAltered() {
        ClassMapping cm = (ClassMapping) JPAFacadeHelper
            .getMetaData(emf, HungarianNotationFields.class);

        assertEquals("M_INTFOOBAR7_CUSTOM_NAME",
            cm.getFieldMapping("m_intFooBar7").getColumns()[0].getName());
    }

    /*
        pcl: This test currently fails. To make it work, we would need to
        change MappingDefaultsImpl.correctName() to take a ValueMapping as
        an argument, and do a two-pass algorithm to check for other fields
        that would turn into duplicates. Even doing this will not be
        foolproof, as a duplicate column might come from a subclass or an
        embedded class.
    public void testDuplicateColumnNameTruncation() {

        ClassMapping cm = (ClassMapping) JPAFacadeHelper
            .getMetaData(emf, HungarianNotationFieldDuplicates.class);

        for (FieldMapping fm : cm.getFieldMappings()) {
            String name = fm.getColumns()[0].getName();
            assertTrue(name.toUpperCase().endsWith("FOOBAR"));
            assertFalse(name.toUpperCase().startsWith("FOOBAR"));
        }
    }
    */
}

