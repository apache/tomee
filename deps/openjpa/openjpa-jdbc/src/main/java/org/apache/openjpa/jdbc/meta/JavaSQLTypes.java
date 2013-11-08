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
package org.apache.openjpa.jdbc.meta;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;

import org.apache.openjpa.meta.JavaTypes;

/**
 * Java SQL type constants.
 *
 * @author Abe White
 */
public class JavaSQLTypes
    extends JavaTypes {

    // constants for the sql types that aren't directly supported by
    // OpenJPA; make sure these don't conflict with our standard metadata types
    public static final int SQL_ARRAY = 1000;
    public static final int ASCII_STREAM = 1001;
    public static final int BINARY_STREAM = 1002;
    public static final int BLOB = 1003;
    public static final int BYTES = 1004;
    public static final int CHAR_STREAM = 1005;
    public static final int CLOB = 1006;
    public static final int SQL_DATE = 1007;
    public static final int SQL_OBJECT = 1008;
    public static final int REF = 1009;
    public static final int TIME = 1010;
    public static final int TIMESTAMP = 1011;
    public static final int JDBC_DEFAULT = 1012;

    private static final Byte ZERO_BYTE = Byte.valueOf((byte) 0);
    private static final Character ZERO_CHAR = Character.valueOf((char) 0);
    private static final Double ZERO_DOUBLE = Double.valueOf(0d);
    private static final Float ZERO_FLOAT = Float.valueOf(0f);
    private static final Short ZERO_SHORT = Short.valueOf((short) 0);
    private static final BigDecimal ZERO_BIGDECIMAL = new BigDecimal(0d);

    private static final Byte NONZERO_BYTE = new Byte((byte) 1);
    private static final Character NONZERO_CHAR = Character.valueOf((char) 'a');
    private static final Double NONZERO_DOUBLE = Double.valueOf(1d);
    private static final Float NONZERO_FLOAT = Float.valueOf(1f);
    private static final Short NONZERO_SHORT = Short.valueOf((short) 1);
    private static final BigInteger NONZERO_BIGINTEGER = new BigInteger("1");
    private static final BigDecimal NONZERO_BIGDECIMAL = new BigDecimal(1d);

    /**
     * Return the proper date typecode.
     */
    public static int getDateTypeCode(Class<?> dtype) {
        if (dtype == java.util.Date.class)
            return DATE;
        if (dtype == java.sql.Date.class)
            return SQL_DATE;
        if (dtype == Timestamp.class)
            return TIMESTAMP;
        if (dtype == Time.class)
            return TIME;
        return OBJECT;
    }

    /**
     * Return an empty value object for the given type code.
     */
    public static Object getEmptyValue(int type) {
        switch (type) {
            case JavaTypes.STRING:
                return "";
            case JavaTypes.BOOLEAN:
            case JavaTypes.BOOLEAN_OBJ:
                return Boolean.FALSE;
            case JavaTypes.BYTE:
            case JavaTypes.BYTE_OBJ:
                return ZERO_BYTE;
            case JavaTypes.CHAR:
            case JavaTypes.CHAR_OBJ:
                return ZERO_CHAR;
            case JavaTypes.DOUBLE:
            case JavaTypes.DOUBLE_OBJ:
                return ZERO_DOUBLE;
            case JavaTypes.FLOAT:
            case JavaTypes.FLOAT_OBJ:
                return ZERO_FLOAT;
            case JavaTypes.INT:
            case JavaTypes.INT_OBJ:
                return 0;
            case JavaTypes.LONG:
            case JavaTypes.LONG_OBJ:
                return 0L;
            case JavaTypes.SHORT:
            case JavaTypes.SHORT_OBJ:
                return ZERO_SHORT;
            case JavaTypes.BIGINTEGER:
                return BigInteger.ZERO;
            case JavaTypes.BIGDECIMAL:
            case JavaTypes.NUMBER:
                return ZERO_BIGDECIMAL;
            default:
                return null;
        }
    }

    /**
     * Return a non-empty value object for the given type code.
     */
    public static Object getNonEmptyValue(int type) {
        switch (type) {
            case JavaTypes.STRING:
                return "x";
            case JavaTypes.BOOLEAN:
            case JavaTypes.BOOLEAN_OBJ:
                return Boolean.TRUE;
            case JavaTypes.BYTE:
            case JavaTypes.BYTE_OBJ:
                return NONZERO_BYTE;
            case JavaTypes.CHAR:
            case JavaTypes.CHAR_OBJ:
                return NONZERO_CHAR;
            case JavaTypes.DOUBLE:
            case JavaTypes.DOUBLE_OBJ:
                return NONZERO_DOUBLE;
            case JavaTypes.FLOAT:
            case JavaTypes.FLOAT_OBJ:
                return NONZERO_FLOAT;
            case JavaTypes.INT:
            case JavaTypes.INT_OBJ:
                return 1;
            case JavaTypes.LONG:
            case JavaTypes.LONG_OBJ:
                return 1L;
            case JavaTypes.SHORT:
            case JavaTypes.SHORT_OBJ:
                return NONZERO_SHORT;
            case JavaTypes.BIGINTEGER:
                return NONZERO_BIGINTEGER;
            case JavaTypes.BIGDECIMAL:
            case JavaTypes.NUMBER:
                return NONZERO_BIGDECIMAL;
            default:
                return null;
        }
    }
}
