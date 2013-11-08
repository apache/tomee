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
package org.apache.openjpa.jdbc.schema;

import java.sql.Types;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.identifier.Normalizer;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;

/**
 * Helper class to deal with schemas.
 *
 * @author Abe White
 * @nojavadoc
 */
public class Schemas {

    public static final Column[] EMPTY_COLUMNS = new Column[0];
    public static final ForeignKey[] EMPTY_FOREIGN_KEYS = new ForeignKey[0];
    public static final Index[] EMPTY_INDEXES = new Index[0];
    public static final Unique[] EMPTY_UNIQUES = new Unique[0];
    public static final Object[] EMPTY_VALUES = new Object[0];

    /**
     * Return the schema name that should be used for new tables, or null if
     * none.
     * @deprecated
     */
    public static String getNewTableSchema(JDBCConfiguration conf) {
        return getNewTableSchemaIdentifier(conf).getName();
    }

    public static DBIdentifier getNewTableSchemaIdentifier(JDBCConfiguration conf) {
        if (conf.getSchema() != null)
            return DBIdentifier.newSchema(conf.getSchema());

        String[] schemas = conf.getSchemasList();
        if (schemas.length == 0)
            return DBIdentifier.NULL;
        String[] names = Normalizer.splitName(schemas[0]);
        if (names.length == 0 || StringUtils.isEmpty(names[0])) {
            return DBIdentifier.NULL;
        }
        return DBIdentifier.newSchema(names[0]);
    }

    /**
     * Return the SQL type name for the given {@link Types} constant.
     */
    public static String getJDBCName(int type) {
        switch (type) {
            case Types.ARRAY:
                return "array";
            case Types.BIGINT:
                return "bigint";
            case Types.BINARY:
                return "binary";
            case Types.BIT:
                return "bit";
            case Types.BLOB:
                return "blob";
            case Types.CHAR:
                return "char";
            case Types.CLOB:
                return "clob";
            case Types.DATE:
                return "date";
            case Types.DECIMAL:
                return "decimal";
            case Types.DISTINCT:
                return "distinct";
            case Types.DOUBLE:
                return "double";
            case Types.FLOAT:
                return "float";
            case Types.INTEGER:
                return "integer";
            case Types.JAVA_OBJECT:
                return "java_object";
            case Types.LONGVARBINARY:
                return "longvarbinary";
            case Types.LONGVARCHAR:
                return "longvarchar";
            case Types.NULL:
                return "null";
            case Types.NUMERIC:
                return "numeric";
            case Types.OTHER:
                return "other";
            case Types.REAL:
                return "real";
            case Types.REF:
                return "ref";
            case Types.SMALLINT:
                return "smallint";
            case Types.STRUCT:
                return "struct";
            case Types.TIME:
                return "time";
            case Types.TIMESTAMP:
                return "timestamp";
            case Types.TINYINT:
                return "tinyint";
            case Types.VARBINARY:
                return "varbinary";
            case Types.VARCHAR:
                return "varchar";
            default:
                return "unknown(" + type + ")";
        }
    }

    /**
     * Return the {@link Types} constant for the given SQL type name.
     */
    public static int getJDBCType(String name) {
        if ("array".equalsIgnoreCase(name))
            return Types.ARRAY;
        if ("bigint".equalsIgnoreCase(name))
            return Types.BIGINT;
        if ("binary".equalsIgnoreCase(name))
            return Types.BINARY;
        if ("bit".equalsIgnoreCase(name))
            return Types.BIT;
        if ("blob".equalsIgnoreCase(name))
            return Types.BLOB;
        if ("char".equalsIgnoreCase(name))
            return Types.CHAR;
        if ("clob".equalsIgnoreCase(name))
            return Types.CLOB;
        if ("date".equalsIgnoreCase(name))
            return Types.DATE;
        if ("decimal".equalsIgnoreCase(name))
            return Types.DECIMAL;
        if ("distinct".equalsIgnoreCase(name))
            return Types.DISTINCT;
        if ("double".equalsIgnoreCase(name))
            return Types.DOUBLE;
        if ("float".equalsIgnoreCase(name))
            return Types.FLOAT;
        if ("integer".equalsIgnoreCase(name))
            return Types.INTEGER;
        if ("java_object".equalsIgnoreCase(name))
            return Types.JAVA_OBJECT;
        if ("longvarbinary".equalsIgnoreCase(name))
            return Types.LONGVARBINARY;
        if ("longvarchar".equalsIgnoreCase(name))
            return Types.LONGVARCHAR;
        if ("null".equalsIgnoreCase(name))
            return Types.NULL;
        if ("numeric".equalsIgnoreCase(name))
            return Types.NUMERIC;
        if ("other".equalsIgnoreCase(name))
            return Types.OTHER;
        if ("real".equalsIgnoreCase(name))
            return Types.REAL;
        if ("ref".equalsIgnoreCase(name))
            return Types.REF;
        if ("smallint".equalsIgnoreCase(name))
            return Types.SMALLINT;
        if ("struct".equalsIgnoreCase(name))
            return Types.STRUCT;
        if ("time".equalsIgnoreCase(name))
            return Types.TIME;
        if ("timestamp".equalsIgnoreCase(name))
            return Types.TIMESTAMP;
        if ("tinyint".equalsIgnoreCase(name))
            return Types.TINYINT;
        if ("varbinary".equalsIgnoreCase(name))
            return Types.VARBINARY;
        if ("varchar".equalsIgnoreCase(name))
            return Types.VARCHAR;
        if (name == null || name.toLowerCase().startsWith("unknown"))
            return Types.OTHER;
        throw new IllegalArgumentException("name = " + name);
    }

    /**
     * Return the java type for the given SQL type from {@link Types}.
     */
    public static Class<?> getJavaType(int type, int size, int decimals) {
        switch (type) {
            case Types.CHAR:
                if (size == 1)
                    return char.class;
                // no break
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.CLOB:
                return String.class;
            case Types.BIT:
                return boolean.class;
            case Types.TINYINT:
                return byte.class;
            case Types.SMALLINT:
                return short.class;
            case Types.INTEGER:
                return int.class;
            case Types.BIGINT:
                return long.class;
            case Types.REAL:
            case Types.FLOAT:
                return float.class;
            case Types.DOUBLE:
            case Types.NUMERIC:
                return double.class;
            case Types.DECIMAL:
                // oracle uses this for everything, so look at size and decimals
                if (decimals == 0 && size < 10)
                    return int.class;
                else if (decimals == 0)
                    return long.class;
                return double.class;
                // ### return a BigDecimal if the size if out of double range?
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return Date.class;
            default:
                return Object.class;
        }
    }
}
