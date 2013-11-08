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
package org.apache.openjpa.persistence.jdbc.mapping;

import java.sql.Types;

import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.persistence.test.SingleEMFTestCase;

public class TestPrecisionMapping extends SingleEMFTestCase {
    
    private static final String[] _DOUBLE_FIELDS =
            { "primDbl", "dbl" };
    private static final String _BIG_DECIMAL_FIELD = "bigDecimal";
    
    public void setUp() { 
        setUp(PrecisionTestEntity.class);
    }
    
    public void testUnspecified() {
        testDoubleMapping("", Types.DOUBLE,0,0);
        testBigDecimalMapping("", Types.NUMERIC, 0, 0);
    }
    
    public void testPrecisionOnly() {
        testDoubleMapping("Precis", Types.NUMERIC, 10, 0);
        testBigDecimalMapping("Precis", Types.NUMERIC, 10, 0);
    }

    public void testScaleOnly() {
        testDoubleMapping("Scale", Types.NUMERIC, 0, 10);
        testBigDecimalMapping("Scale", Types.NUMERIC, 0, 10);
    }
    
    public void testPrecisionAndScale() { 
        testDoubleMapping("PrecisScale", Types.NUMERIC,10,10);
        testBigDecimalMapping("PrecisScale", Types.NUMERIC, 10, 10);
    }
    
    private void testBigDecimalMapping(String fieldSuffix, int expectedType,
            int expectedPrecision, int expectedScale) {
        ClassMapping mapping = getMapping(PrecisionTestEntity.class);
        FieldMapping fm =
                mapping.getFieldMapping(_BIG_DECIMAL_FIELD + fieldSuffix);

        Column[] cols = fm.getColumns();
        assertEquals(1, cols.length);
        assertEquals(expectedType, cols[0].getType());
        assertEquals(expectedPrecision, cols[0].getSize());
        assertEquals(expectedScale, cols[0].getDecimalDigits());
    }
    
    private void testDoubleMapping(String fieldSuffix, int expectedType,
            int expectedPrecision, int expectedScale) {
        ClassMapping mapping = getMapping(PrecisionTestEntity.class);
        FieldMapping fm; 
        
        for(String s : _DOUBLE_FIELDS) {
            fm = mapping.getFieldMapping(s + fieldSuffix);
            
            Column[] cols = fm.getColumns();
            assertEquals(1, cols.length);
            assertEquals(expectedType ,cols[0].getType()); 
            assertEquals(expectedPrecision, cols[0].getSize());
            assertEquals(expectedScale, cols[0].getDecimalDigits());
        }
    }
}
