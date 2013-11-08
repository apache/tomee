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

import junit.framework.TestCase;

public class TestJavaTypes extends TestCase {
    TypesHolder _types = new TypesHolder();

    public void testIsPrimitiveDefault() {
        assertTrue(JavaTypes.isPrimitiveDefault(_types.getBoolean(), JavaTypes.BOOLEAN));
        assertTrue(JavaTypes.isPrimitiveDefault(_types.getChar(), JavaTypes.CHAR));
        assertTrue(JavaTypes.isPrimitiveDefault(_types.getDouble(), JavaTypes.DOUBLE));
        assertTrue(JavaTypes.isPrimitiveDefault(_types.getInt(), JavaTypes.INT));
        assertTrue(JavaTypes.isPrimitiveDefault(_types.getLong(), JavaTypes.LONG));
        assertTrue(JavaTypes.isPrimitiveDefault(_types.getShort(), JavaTypes.SHORT));
    }

    class TypesHolder {
        boolean _boolean;
        short _short;
        int _int;
        long _long;
        float _float;
        double _double;
        char _char;

        public Object getBoolean() {
            return _boolean;
        }

        public Object getShort() {
            return _short;
        }

        public Object getInt() {
            return _int;
        }

        public Object getLong() {
            return _long;
        }

        public Object getDouble() {
            return _double;
        }

        public Object getChar() {
            return _char;
        }
    }
}
