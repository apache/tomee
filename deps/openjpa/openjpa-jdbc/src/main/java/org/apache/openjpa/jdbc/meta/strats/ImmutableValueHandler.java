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
package org.apache.openjpa.jdbc.meta.strats;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.JavaSQLTypes;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.meta.JavaTypes;

/**
 * Handler for simple type and string values.
 *
 * @nojavadoc
 */
public class ImmutableValueHandler
    extends AbstractValueHandler {

    private static final ImmutableValueHandler _instance =
        new ImmutableValueHandler();

    /**
     * Singleton instance.
     */
    public static ImmutableValueHandler getInstance() {
        return _instance;
    }

    /**
     * @deprecated
     */
    public Column[] map(ValueMapping vm, String name, ColumnIO io,
        boolean adapt) {
        DBDictionary dict = vm.getMappingRepository().getDBDictionary();
        DBIdentifier colName = DBIdentifier.newColumn(name, dict != null ? dict.delimitAll() : false);
        return map(vm, colName, io, adapt);
    }

    public Column[] map(ValueMapping vm, DBIdentifier name, ColumnIO io,
        boolean adapt) {
        Column col = new Column();
        col.setIdentifier(name);
        if (vm.getTypeCode() == JavaTypes.DATE)
            col.setJavaType(JavaSQLTypes.getDateTypeCode(vm.getType()));
        else
            col.setJavaType(vm.getTypeCode());
        return new Column[]{ col };
    }

    public boolean isVersionable(ValueMapping vm) {
        switch (vm.getTypeCode()) {
            case JavaTypes.BOOLEAN:
            case JavaTypes.BYTE:
            case JavaTypes.CHAR:
            case JavaTypes.INT:
            case JavaTypes.LONG:
            case JavaTypes.SHORT:
            case JavaTypes.BOOLEAN_OBJ:
            case JavaTypes.BYTE_OBJ:
            case JavaTypes.CHAR_OBJ:
            case JavaTypes.INT_OBJ:
            case JavaTypes.LONG_OBJ:
            case JavaTypes.SHORT_OBJ:
            case JavaTypes.STRING:
            case JavaTypes.DATE:
            case JavaTypes.BIGINTEGER:
            case JavaTypes.LOCALE:
                return true;
            default:
                return false;
        }
    }

    public Object toDataStoreValue(ValueMapping vm, Object val,
        JDBCStore store) {
        if (val != null)
            return val;

        FieldMapping field = vm.getFieldMapping();
        if (field.getNullValue() != FieldMapping.NULL_DEFAULT)
            return null;

        Column[] cols = vm.getColumns();
        if (cols[0].getDefaultString() != null)
            return null;

        // honor the user's null-value=default
        return JavaSQLTypes.getEmptyValue(vm.getTypeCode());
    }
}
