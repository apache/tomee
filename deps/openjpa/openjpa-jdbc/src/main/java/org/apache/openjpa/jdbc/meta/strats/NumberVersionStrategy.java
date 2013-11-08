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

import java.util.HashMap;
import java.util.Map;

import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.meta.JavaTypes;

/**
 * Uses a version number for optimistic versioning.
 *
 * @author Abe White
 */
public class NumberVersionStrategy
    extends ColumnVersionStrategy {

    public static final String ALIAS = "version-number";

    private Number _initial = 1;
    private Integer _javaType = null;
    
    /**
     * Set the initial value for version column. Defaults to 1.
     */
    public void setInitialValue(int initial) {
        _initial = initial;
    }

    /**
     * Return the initial value for version column. Defaults to 1.
     */
    public int getInitialValue() {
        return _initial.intValue();
    }

    public String getAlias() {
        return ALIAS;
    }

    protected int getJavaType() {
        if (_javaType == null && vers.getClassMapping().getVersionFieldMapping() != null) {
            _javaType = Integer.valueOf(vers.getClassMapping().getVersionFieldMapping().getTypeCode());
        } else {
            return JavaTypes.INT;
        }

        return _javaType;
    }
    
    protected Object nextVersion(Object version) {
        if (version == null)
            return _initial;
        return ((Number) version).intValue() + 1;
    }

    public Map<Column,String> getBulkUpdateValues() {
        Column[] cols = vers.getColumns();
        Map<Column,String> map = new HashMap<Column,String>(cols.length);
        for (int i = 0; i < cols.length; i++)
            map.put(cols[i], cols[i].getName() + " + 1");
        return map;
    }
}
