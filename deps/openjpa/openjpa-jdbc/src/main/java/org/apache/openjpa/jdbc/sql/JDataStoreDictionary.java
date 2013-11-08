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
package org.apache.openjpa.jdbc.sql;

import java.sql.SQLException;
import java.util.Arrays;

import org.apache.openjpa.jdbc.kernel.exps.FilterValue;
import org.apache.openjpa.util.OpenJPAException;
import org.apache.openjpa.util.ReferentialIntegrityException;

/**
 * Dictionary for Borland JDataStore
 */
public class JDataStoreDictionary
    extends DBDictionary {

    public JDataStoreDictionary() {
        platform = "Borland JDataStore";
        joinSyntax = SYNTAX_TRADITIONAL;

        supportsDeferredConstraints = false;
        allowsAliasInBulkClause = false;

        maxTableNameLength = 31;
        maxColumnNameLength = 31;
        maxIndexNameLength = 31;
        maxConstraintNameLength = 31;

        useGetStringForClobs = true;
        useSetStringForClobs = true;
        useGetBytesForBlobs = true;
        blobTypeName = "VARBINARY";
        clobTypeName = "VARCHAR";

        // it is possible to use a FOR UPDATE clause with JDataStore,
        // but the actual row won't wind up being locked
        supportsLockingWithDistinctClause = false;
        supportsQueryTimeout = false;

        // there is no build-in function for getting the last generated
        // key in JDataStore; using MAX will have to suffice
        supportsAutoAssign = true;
        lastGeneratedKeyQuery = "SELECT MAX({0}) FROM {1}";
        autoAssignClause = "AUTOINCREMENT";

        fixedSizeTypeNameSet.addAll(Arrays.asList(new String[]{
            "SHORT", "INT", "LONG", "DOUBLE PRECISION", "BOOLEAN",
        }));

        requiresSearchStringEscapeForLike = true;
        searchStringEscape = "";
    }

    @Override
    public void substring(SQLBuffer buf, FilterValue str, FilterValue start,
        FilterValue length) {
        buf.append("SUBSTRING(");
        str.appendTo(buf);
        buf.append(" FROM (");
        start.appendTo(buf);
        buf.append(") FOR (");
        if (length == null) {
            buf.append("CHAR_LENGTH(");
            str.appendTo(buf);
            buf.append(")");
        } else
            length.appendTo(buf);
        buf.append(" - (");
        start.appendTo(buf);
        buf.append(" - 1)))");
    }

    @Override
    public void indexOf(SQLBuffer buf, FilterValue str, FilterValue find,
        FilterValue start) {
        buf.append("(POSITION(");
        find.appendTo(buf);
        buf.append(" IN ");
        if (start != null)
            substring(buf, str, start, null);
        else
            str.appendTo(buf);
        buf.append(")");
        if (start != null) {
            buf.append(" - 1 + ");
            start.appendTo(buf);
        }
        buf.append(")");
    }

    @Override
    public OpenJPAException newStoreException(String msg, SQLException[] causes,
        Object failed) {
        OpenJPAException ke = super.newStoreException(msg, causes, failed);
        if (ke instanceof ReferentialIntegrityException
            && causes[0].getMessage().indexOf("Duplicate key value for") > -1) {
            ((ReferentialIntegrityException) ke).setIntegrityViolation
                (ReferentialIntegrityException.IV_UNIQUE);
        }
        return ke;
    }
}
