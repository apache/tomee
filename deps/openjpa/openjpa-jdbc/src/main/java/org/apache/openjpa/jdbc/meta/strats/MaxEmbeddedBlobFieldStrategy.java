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

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.Row;
import org.apache.openjpa.jdbc.sql.SQLExceptions;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.MetaDataException;

/**
 * Mapping for serialized fields on a dictionary that has a maximum embedded
 * BLOB size.
 *
 * @author Abe White
 * @nojavadoc
 * @since 0.4.0
 */
public class MaxEmbeddedBlobFieldStrategy
    extends MaxEmbeddedLobFieldStrategy {

    private static final Localizer _loc = Localizer.forPackage
        (MaxEmbeddedBlobFieldStrategy.class);

    private int _maxSize = 0;

    protected int getExpectedJavaType() {
        return JavaTypes.OBJECT;
    }

    protected void update(OpenJPAStateManager sm, Row row)
        throws SQLException {
        byte[] b = (byte[]) sm.getImplData(field.getIndex());
        if (b == null || (b.length > _maxSize && !field.getColumns()[0].isNotNull()))
            row.setNull(field.getColumns()[0], true);
        else {
            sm.setImplData(field.getIndex(), null);
            DBDictionary.SerializedData dat =
                new DBDictionary.SerializedData(b);
            row.setObject(field.getColumns()[0], dat);
        }
    }

    protected Boolean isCustom(OpenJPAStateManager sm, JDBCStore store) {
        // have we already stored our serialized data?
        byte[] b = (byte[]) sm.getImplData(field.getIndex());
        if (b == null) {
            Object o = sm.fetch(field.getIndex());
            if (o == null)
                return Boolean.FALSE;

            // serialize field value
            DBDictionary dict = field.getMappingRepository().getDBDictionary();
            try {
                b = dict.serialize(o, store);
            } catch (SQLException se) {
                throw SQLExceptions.getStore(se, dict);
            }

            // set in impl data so that we don't have to re-serialize on store
            sm.setImplData(field.getIndex(), b);
        }
        return (b.length > _maxSize) ? null : Boolean.FALSE;
    }

    protected void putData(OpenJPAStateManager sm, ResultSet rs,
        DBDictionary dict)
        throws SQLException {
        byte[] b = (byte[]) sm.setImplData(field.getIndex(), null);
        Blob blob = rs.getBlob(1);
        dict.putBytes(blob, b);
    }

    public void map(boolean adapt) {
        if (!field.isSerialized())
            throw new MetaDataException(_loc.get("not-serialized", field));
        super.map(adapt);
    }

    public void initialize() {
        DBDictionary dict = field.getMappingRepository().getDBDictionary();
        _maxSize = dict.maxEmbeddedBlobSize;
        field.setUsesImplData(Boolean.TRUE);
    }
    
    protected Object getValue(OpenJPAStateManager sm) {
        byte[] b = (byte[]) sm.getImplData(field.getIndex());
        if (b == null || (b.length > _maxSize && !field.getColumns()[0].isNotNull()))
            return null;
        sm.setImplData(field.getIndex(), null);
        DBDictionary.SerializedData dat = new DBDictionary.SerializedData(b);
        return dat;
    }    
}
