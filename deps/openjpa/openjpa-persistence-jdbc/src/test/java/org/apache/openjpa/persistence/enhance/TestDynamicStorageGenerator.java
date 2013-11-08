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
package org.apache.openjpa.persistence.enhance;

import java.util.Date;

import org.apache.openjpa.persistence.common.utils.AbstractTestCase;
import org.apache.openjpa.enhance.DynamicStorage;
import org.apache.openjpa.enhance.DynamicStorageGenerator;
import org.apache.openjpa.meta.JavaTypes;

public class TestDynamicStorageGenerator
    extends AbstractTestCase {

    public TestDynamicStorageGenerator(String s) {
        super(s, "enhancecactusapp");
    }

    public void testStorage()
        throws Exception {

        DynamicStorageGenerator gen = new DynamicStorageGenerator();
        int[] types = new int[]{
            JavaTypes.BOOLEAN,
            JavaTypes.BYTE,
            JavaTypes.CHAR,
            JavaTypes.INT,
            JavaTypes.SHORT,
            JavaTypes.LONG,
            JavaTypes.FLOAT,
            JavaTypes.DOUBLE,
            JavaTypes.STRING,
            JavaTypes.OBJECT
        };
        DynamicStorage storage = gen.generateStorage(types,
            "org.apache.openjpa.enhance.Test");
        storage = storage.newInstance();

        storage.setBoolean(0, true);
        storage.setByte(1, (byte) 1);
        storage.setChar(2, 'f');
        storage.setInt(3, 3);
        storage.setShort(4, (short) 4);
        storage.setLong(5, 5);
        storage.setFloat(6, (float) 6.6);
        storage.setDouble(7, 7.7);
        storage.setObject(8, "field8");
        Date date = new Date();
        storage.setObject(9, date);

        assertTrue(storage.getBoolean(0));
        assertEquals(1, storage.getByte(1));
        assertEquals('f', storage.getChar(2));
        assertEquals(3, storage.getInt(3));
        assertEquals(4, storage.getShort(4));
        assertEquals(5, storage.getLong(5));
        assertTrue(6.59 < storage.getFloat(6) && 6.61 > storage.getFloat(6));
        assertTrue(7.69 < storage.getDouble(7)
            && 7.71 > storage.getDouble(7));
        assertEquals("field8", storage.getObject(8));
        assertEquals(date, storage.getObject(9));
    }
}
