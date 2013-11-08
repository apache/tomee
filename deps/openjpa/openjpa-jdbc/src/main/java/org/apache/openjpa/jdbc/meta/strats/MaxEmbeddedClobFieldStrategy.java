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

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.Row;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.MetaDataException;

/**
 * Mapping for CLOB string on a dictionary that has a maximum embedded
 * CLOB size.
 *
 * @author Abe White
 * @nojavadoc
 * @since 0.4.0
 */
public class MaxEmbeddedClobFieldStrategy
    extends MaxEmbeddedLobFieldStrategy {

    private static final Localizer _loc = Localizer.forPackage
        (MaxEmbeddedClobFieldStrategy.class);

    private int _maxSize = 0;

    protected int getExpectedJavaType() {
        return JavaTypes.STRING;
    }

    protected void update(OpenJPAStateManager sm, Row row)
        throws SQLException {
        String s = (String) getValue(sm);
        if (s == null)
            row.setNull(field.getColumns()[0], true);
        else
            row.setString(field.getColumns()[0], s);
    }

    protected Boolean isCustom(OpenJPAStateManager sm, JDBCStore store) {
        String s = sm.fetchString(field.getIndex());
        return (s != null && s.length() > _maxSize) ? null : Boolean.FALSE;
    }

    protected void putData(OpenJPAStateManager sm, ResultSet rs,
        DBDictionary dict)
        throws SQLException {
        Clob clob = rs.getClob(1);
        dict.putString(clob, sm.fetchString(field.getIndex()));
    }

    public void map(boolean adapt) {
        if (field.getTypeCode() != JavaTypes.STRING)
            throw new MetaDataException(_loc.get("not-clobstring", field));
        super.map(adapt);
    }

    public void initialize() {
        DBDictionary dict = field.getMappingRepository().getDBDictionary();
        _maxSize = dict.maxEmbeddedClobSize;
    }
    
    protected Object getValue(OpenJPAStateManager sm) {
        String s = sm.fetchString(field.getIndex());
        if (s == null || (s.length() > _maxSize && !field.getColumns()[0].isNotNull())) {
            return null;
        }
        return s;
    }    
}
