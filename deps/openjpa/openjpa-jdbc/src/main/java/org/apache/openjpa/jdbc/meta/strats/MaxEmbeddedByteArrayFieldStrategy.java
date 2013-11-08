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

import java.lang.reflect.Array;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.JavaSQLTypes;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.jdbc.sql.Joins;
import org.apache.openjpa.jdbc.sql.Result;
import org.apache.openjpa.jdbc.sql.Row;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.MetaDataException;

/**
 * Mapping for byte array fields on a dictionary that has a maximum embedded
 * BLOB size.
 *
 * @author Abe White
 * @nojavadoc
 * @since 0.4.0
 */
public class MaxEmbeddedByteArrayFieldStrategy
    extends MaxEmbeddedLobFieldStrategy {

    private static final Localizer _loc = Localizer.forPackage
        (MaxEmbeddedByteArrayFieldStrategy.class);

    private int _maxSize = 0;

    protected int getExpectedJavaType() {
        return JavaSQLTypes.BYTES;
    }

    protected void update(OpenJPAStateManager sm, Row row)
        throws SQLException {
        byte[] b = (byte[]) getValue(sm);
         if (b == null)
            row.setBytes(field.getColumns()[0], null);
        else
            row.setBytes(field.getColumns()[0], b);
    }

    protected Boolean isCustom(OpenJPAStateManager sm, JDBCStore store) {
        Object val = sm.fetchObject(field.getIndex());
        return (val != null && Array.getLength(val) > _maxSize) ? null
            : Boolean.FALSE;
    }

    protected void putData(OpenJPAStateManager sm, ResultSet rs,
        DBDictionary dict)
        throws SQLException {
        Blob blob = rs.getBlob(1);
        dict.putBytes(blob, PrimitiveWrapperArrays.toByteArray(sm.fetchObject
            (field.getIndex())));
    }

    protected Object load(Column col, Result res, Joins joins)
        throws SQLException {
        return PrimitiveWrapperArrays.toObjectValue(field,
            res.getBytes(col, joins));
    }

    public void map(boolean adapt) {
        if (field.getType() != byte[].class && field.getType() != Byte[].class)
            throw new MetaDataException(_loc.get("not-bytes", field));
        super.map(adapt);
    }

    public void initialize() {
        DBDictionary dict = field.getMappingRepository().getDBDictionary();
        _maxSize = dict.maxEmbeddedBlobSize;
    }
    
    protected Object getValue(OpenJPAStateManager sm) {
        byte[] b = PrimitiveWrapperArrays.toByteArray(sm.fetchObject
                (field.getIndex()));
        if (b == null || (b.length > _maxSize && !field.getColumns()[0].isNotNull()))
            return null;
        return b;
    }    
}
