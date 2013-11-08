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

import java.lang.reflect.Method;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.Exceptions;
import org.apache.openjpa.util.MetaDataException;

/**
 * Value handler for JDK1.5 enum field types.
 *
 * @nojavadoc
 */
@SuppressWarnings("serial")
public class EnumValueHandler
    extends AbstractValueHandler {

    private Enum<?>[] _vals = null;
    private boolean _ordinal = false;
    private static final Localizer _loc = Localizer.forPackage(EnumValueHandler.class);

    /**
     * Whether to store the enum value as its ordinal.
     */
    public boolean getStoreOrdinal() {
        return _ordinal;
    }

    /**
     * Whether to store the enum value as its ordinal.
     */
    public void setStoreOrdinal(boolean ordinal) {
        _ordinal = ordinal;
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
        // all enum classes have a static method called 'values()'
        // that returns an array of all the enum values
        try {
            Method m = vm.getType().getMethod("values", (Class[]) null);
            _vals = (Enum[]) m.invoke(null, (Object[]) null);
        } catch (Exception e) {
            
            throw new MetaDataException(_loc.get("not-enum-field", 
                    vm.getFieldMapping().getFullName(true), Exceptions.toClassName(vm.getType()))).setCause(e);
        }

        Column col = new Column();
        col.setIdentifier(name);
        if (_ordinal)
            col.setJavaType(JavaTypes.SHORT);
        else {
            // look for the longest enum value name; use 20 as min length to
            // leave room for future long names
            int len = 20;
            for (int i = 0; i < _vals.length; i++)
                len = Math.max(_vals[i].name().length(), len);

            col.setJavaType(JavaTypes.STRING);
            col.setSize(len);
        }
        return new Column[]{ col };
    }

    public boolean isVersionable() {
        return true;
    }

    public Object toDataStoreValue(ValueMapping vm, Object val, JDBCStore store) {
        if (val == null)
            return null;
        if (_ordinal)
            return Integer.valueOf(((Enum) val).ordinal());
        return ((Enum) val).name();
    }

    public Object toObjectValue(ValueMapping vm, Object val) {
        if (val == null)
            return null;
        if (_ordinal)
            return _vals[((Number) val).intValue()];
        return Enum.valueOf(vm.getType(), (String) val);
    }
}
