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

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Array;
import java.sql.Clob;
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
 * Mapping for CLOB character array on a dictionary that has a maximum
 * embedded CLOB size.
 *
 * @author Abe White
 * @nojavadoc
 * @since 0.4.0
 */
public class MaxEmbeddedCharArrayFieldStrategy
    extends MaxEmbeddedLobFieldStrategy {

    private static final Localizer _loc = Localizer.forPackage
        (MaxEmbeddedCharArrayFieldStrategy.class);

    private int _maxSize = 0;

    protected int getExpectedJavaType() {
        return JavaSQLTypes.CHAR_STREAM;
    }

    protected void update(OpenJPAStateManager sm, Row row)
        throws SQLException {
        char[] c = PrimitiveWrapperArrays.
            toCharArray(sm.fetchObject(field.getIndex()));
        if (c == null || c.length > _maxSize)
            row.setNull(field.getColumns()[0], true);
        else
            row.setCharacterStream(field.getColumns()[0],
                new CharArrayReader(c), c.length);
    }

    protected Boolean isCustom(OpenJPAStateManager sm, JDBCStore store) {
        Object val = sm.fetchObject(field.getIndex());
        return (val != null && Array.getLength(val) > _maxSize) ? null
            : Boolean.FALSE;
    }

    protected void putData(OpenJPAStateManager sm, ResultSet rs,
        DBDictionary dict)
        throws SQLException {
        Clob clob = rs.getClob(1);
        dict.putChars(clob, PrimitiveWrapperArrays.
            toCharArray(sm.fetchObject(field.getIndex())));
    }

    protected Object load(Column col, Result res, Joins joins)
        throws SQLException {
        Reader reader = res.getCharacterStream(col, joins);
        if (reader == null)
            return null;

        try {
            CharArrayWriter writer = new CharArrayWriter();
            for (int c; (c = reader.read()) != -1;)
                writer.write(c);
            return PrimitiveWrapperArrays.toObjectValue(field,
                writer.toCharArray());
        } catch (IOException ioe) {
            throw new SQLException(ioe.toString());
        }
    }

    public void map(boolean adapt) {
        if (field.getType() != char[].class
            && field.getType() != Character[].class)
            throw new MetaDataException(_loc.get("not-chars", field));
        super.map(adapt);
    }

    public void initialize() {
        DBDictionary dict = field.getMappingRepository().getDBDictionary();
        _maxSize = dict.maxEmbeddedClobSize;
    }
    
    protected Object getValue(OpenJPAStateManager sm) {
        char[] c = PrimitiveWrapperArrays.
        toCharArray(sm.fetchObject(field.getIndex()));
        if (c == null || c.length > _maxSize)
            return null;
        else
            return c;
    }
}
