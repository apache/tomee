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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.kernel.exps.FilterValue;
import org.apache.openjpa.jdbc.schema.Sequence;

public class IngresDictionary extends DBDictionary {
    
    public IngresDictionary() {
        // Schema Data
        platform = "Ingres";
        driverVendor = "Ingres Corporation";
        maxColumnNameLength = 32;
        maxConstraintNameLength = 32;
        maxIndexNameLength = 32;
        maxTableNameLength = 32;
        supportsDeferredConstraints = false;
        schemaCase = DBDictionary.SCHEMA_CASE_LOWER;
        systemSchemas = "$ingres";
        supportsDefaultDeleteAction = false;
        supportsDefaultUpdateAction = false;

        // SQL
        validationSQL = "SELECT DATE('now')";
        rangePosition = DBDictionary.RANGE_POST_SELECT;
        supportsLockingWithDistinctClause = false;
        supportsLockingWithInnerJoin = false;
        supportsLockingWithMultipleTables = false;
        supportsLockingWithOuterJoin = false;
        supportsSelectEndIndex = true;
        supportsMultipleNontransactionalResultSets = false;
        allowsAliasInBulkClause = false;
        requiresCastForMathFunctions = true;
        requiresAliasForSubselect = true;

        // Functions
        stringLengthFunction = "LENGTH({0})";

        // Types
        binaryTypeName = "BYTE";
        bitTypeName = "TINYINT";
        blobTypeName = "LONG BYTE";
        booleanTypeName = "TINYINT";
        charTypeName = "CHAR";
        clobTypeName = "LONG NVARCHAR";
        javaObjectTypeName = "LONG BYTE";
        numericTypeName = "DECIMAL";
        doubleTypeName = "FLOAT";
        longVarcharTypeName = "LONG NVARCHAR";
        longVarbinaryTypeName = "LONG BYTE";
        varbinaryTypeName = "LONG BYTE";
        datePrecision = DBDictionary.NANO;

        // Schema Metadata
        supportsNullTableForGetIndexInfo = true;
        supportsNullTableForGetPrimaryKeys = true;
        tableTypes = "TABLE,VIEW,SYSTEM TABLE";
        requiresAutoCommitForMetaData = true;

        // Auto Increment
        maxAutoAssignNameLength = 32;
        supportsAutoAssign = false;
        autoAssignClause = null;
        autoAssignTypeName = null;
        sequenceSQL = "SELECT seq_name AS SEQUENCE_NAME, seq_owner AS SEQUENCE_SCHEMA FROM iisequences";
        sequenceNameSQL = "seq_name = ?";
        sequenceSchemaSQL = "seq_owner = ?";
        nextSequenceQuery = "SELECT NEXT VALUE FOR {0}";

        systemTables =
            "iiaccess, iialt_columns, iiattribute, iiaudit, iiaudittables, "
                + "iicdbid_idx, iicolumns, iiconstraint_indexes, " 
                + "iiconstraints, iidatabase, iidatabase_info, iidb_comments, " 
                + "iidb_subcomments, iidbcapabilities, iidbconstants, " 
                + "iidbdepends, iidbid_idx, iidbms_comment, iidbpriv, " 
                + "iidbprivileges, iiddb_netcost, iiddb_nodecosts, iidefault, " 
                + "iidefaultidx, iidevices, iidistcol, iidistcols, " 
                + "iidistscheme, iidistschemes, iidistval, "
                + "iievent, iievents, iiextend, iiextend_info, "
                + "iiextended_relation, iifile_info, iigw06_attribute, "
                + "iigw06_relation, iigw07_attribute, iigw07_index, "
                + "iigw07_relation, iihistogram, iihistograms, iiindex, "
                + "iiindex_columns, iiindexes, iiingres_tables, iiintegrities, "
                + "iiintegrity, iiintegrityidx, iikey, iikey_columns, iikeys, "
                + "iilocation_info, iilocations, iilog_help, iilpartitions, "
                + "iimulti_locations, iiocolumns, iiotables, iipartname, "
                + "iipermits, iiphysical_columns, iiphysical_tables, iipriv, "
                + "iiprivlist, iiproc_access, iiproc_params, iiprocedure, "
                + "iiprocedure_parameter, iiprocedures, iiprofile, iiprofiles, "
                + "iiprotect, iiqrytext, iirange, iiref_constraints, "
                + "iiregistrations, iirel_idx, iirelation, iirole, "
                + "iirolegrant, iirolegrants, iiroles, iirule, iiruleidx, "
                + "iiruleidx1, iirules, iischema, iischemaidx, iisecalarm, "
                + "iisectype, iisecurity_alarms, iisecurity_state, "
                + "iisecuritystate, iisequence, iisequences, "
                + "iisession_privileges, iistar_cdbinfo, iistar_cdbs, "
                + "iistatistics, iistats, iisynonym, iisynonyms, iitables, "
                + "iitree, iiuser, iiusergroup, iiusers, iiviews, "
                + "iixdbdepends, iixevent, iixpriv, iixprocedure, "
                + "iixsynonym, ii_abfclasses, ii_abfdependencies, "
                + "ii_abfobjects, ii_app_cntns_comp, ii_app_cntns_comp_index, "
                + "ii_applications, ii_atttype, ii_client_dep_mod, "
                + "ii_components, ii_databases, ii_dbd_acl, "
                + "ii_dbd_identifiers, ii_dbd_locations, ii_dbd_rightslist, "
                + "ii_dbd_table_char, ii_defaults, ii_dependencies, "
                + "ii_dependencies_index, ii_dependencies_index2, "
                + "ii_dict_modules, ii_domains, ii_encoded_forms, "
                + "ii_encodings, ii_entities, ii_entities_index, ii_enttype, "
                + "ii_fields, " + "ii_forms, ii_framevars, ii_gropts, "
                + "ii_id, ii_incl_apps, ii_joindefs, ii_joinspecs, "
                + "ii_key_info, ii_key_map, ii_limits, ii_locks, "
                + "ii_longremarks, ii_menuargs, ii_objects, "
                + "ii_objects_index, ii_qbfnames, ii_rcommands, "
                + "ii_rel_cncts_ent, ii_reltype, ii_reports, "
                + "ii_sequence_values, ii_sqlatts, ii_sqltables, "
                + "ii_srcobj_encoded, ii_stored_bitmaps, ii_stored_strings, "
                + "ii_trim, ii_vqjoins, ii_vqtabcols, ii_vqtables";
        fixedSizeTypeNames =
            "INTEGER,INTEGER1,INTEGER2,INTEGER4,INTEGER8,TINYINT,SMALLINT,"
                + "BIGINT,FLOAT,FLOAT4,FLOAT8,REAL,DATE,INGRESDATE,ANSIDATE";
        reservedWords =
            "ABORT,BYREF,CALLPROC,COMMENT,COPY,DEFINE,DISABLE,DO,ELSEIF,"
                + "ENABLE,ENDIF,ENDLOOP,ENDWHILE,EXCLUDING,IF,IMPORT,INDEX,"
                + "INTEGRITY,MESSAGE,MODIFY,PERMIT,RAISE,REFERENCING,REGISTER,"
                + "RELOCATE,REMOVE,REPEAT,RETURN,SAVE,SAVEPOINT,UNTIL,WHILE";
        
        /* Don't allow "tid" as a column name; this is an internal column */
        invalidColumnWordSet.add("tid");
    }

    /* (non-Javadoc)
     * @see org.apache.openjpa.jdbc.sql.DBDictionary#connectedConfiguration(java.sql.Connection)
     */
    public void connectedConfiguration(Connection conn) throws SQLException {
        super.connectedConfiguration(conn);
        
        if (isVersion9_2orLater(conn))
            supportsSelectStartIndex = true;
    }
    
    boolean isVersion9_2orLater(Connection conn) throws SQLException
    {
        DatabaseMetaData meta = conn.getMetaData();
        
        int dbMajorVersion = meta.getDatabaseMajorVersion();
        int dbMinorVersion = meta.getDatabaseMinorVersion();
        
        if ((dbMajorVersion == 9 && dbMinorVersion >= 2) ||
                dbMajorVersion > 9)
            return true;
        else
            return false;
    }

    /**
     * Implementation of appendSelectRange for Ingres - uses "SELECT FIRST n"
     * syntax. Not implemented in superclass.
     * 
     * @see org.apache.openjpa.jdbc.sql.DBDictionary#appendSelectRange(
     *      org.apache.openjpa.jdbc.sql.SQLBuffer, long, long, boolean)
     * 
     * @param buf
     *            - SQL building buffer.
     * @param start
     *            - Ignored, not supported by Ingres. Throws an
     * @param end
     *            - The number of records to return.
     * @param subselect
     *            - Is the SQL query part of a subselect statement?
     */
    protected void appendSelectRange(SQLBuffer buf, long start, long end,
        boolean subselect) {
        if (!supportsSelectStartIndex && start > 0) 
            throw new IllegalArgumentException(
                "Ingres does not support start indexes for Select Ranges");
        
        if (start > 0 && start != Long.MAX_VALUE && !subselect)
            buf.append(" OFFSET ").append(Long.toString(start));
        
        if (end > 0 && end != Long.MAX_VALUE && !subselect) 
            buf.append(" FETCH NEXT ").append(Long.toString(end)).append(" ROWS ONLY");
    }
    
    /**
     * Implementation of empty method in DBDictionary.  Returns SQL to find the sequences defined in the database.
     */
    @Override
    protected String getSequencesSQL(String schemaName, String sequenceName) {
        return getSequencesSQL(DBIdentifier.newSchema(schemaName), DBIdentifier.newSequence(sequenceName));
    }

    @Override
    protected String getSequencesSQL(DBIdentifier schemaName, DBIdentifier sequenceName) {
        StringBuilder buf = new StringBuilder();
        buf.append(sequenceSQL);
        if (!DBIdentifier.isNull(schemaName) || !DBIdentifier.isNull(sequenceName))
            buf.append(" WHERE ");
        if (!DBIdentifier.isNull(schemaName)) {
            buf.append(sequenceSchemaSQL);
            if (sequenceName != null)
                buf.append(" AND ");
        }
        if (!DBIdentifier.isNull(sequenceName))
            buf.append(sequenceNameSQL);
        return buf.toString();
    }
    
    /**
     * Overrides DBDictionary's newSequence method; trims the sequence name.
     * 
     * @see org.apache.openjpa.jdbc.sql.DBDictionary#newSequence(ResultSet)
     */
    @Override
    protected Sequence newSequence(ResultSet sequenceMeta) throws SQLException {
        Sequence seq = super.newSequence(sequenceMeta);
        seq.setIdentifier(DBIdentifier.trim(seq.getIdentifier()));
        return seq;
    }
    
    /**
     * Invoke Ingres' IndexOf Function (Find the first index of a string in
     * another string, starting at a given index).
     * 
     * @see org.apache.openjpa.jdbc.sql.DBDictionary#indexOf(
     *      org.apache.openjpa.jdbc.sql.SQLBuffer,
     *      org.apache.openjpa.jdbc.kernel.exps.FilterValue,
     *      org.apache.openjpa.jdbc.kernel.exps.FilterValue,
     *      org.apache.openjpa.jdbc.kernel.exps.FilterValue)
     * 
     * @param buf
     *            - the SQL Buffer to write the indexOf invocation to
     * @param str
     *            - a query value representing the target string
     * @param find
     *            - a query value representing the search string
     * @param start
     *            - a query value representing the start index, or null to 
     *            start at the beginning
     */
    @Override
    public void indexOf(SQLBuffer buf, FilterValue str, FilterValue find,
        FilterValue start) {
        buf.append("(POSITION((");
        find.appendTo(buf);
        buf.append(") IN (");

        if (start != null)
            substring(buf, str, start, null);
        else
            str.appendTo(buf);
        
        buf.append("))");

        if (start != null) {
            buf.append(" - 1 + ");
            start.appendTo(buf);
        }
        buf.append(")");
    }

    /**
     * @see org.apache.openjpa.jdbc.sql.DBDictionary#substring(
     *  org.apache.openjpa.jdbc.sql.SQLBuffer, 
     *  org.apache.openjpa.jdbc.kernel.exps.FilterValue, 
     *  org.apache.openjpa.jdbc.kernel.exps.FilterValue, 
     *  org.apache.openjpa.jdbc.kernel.exps.FilterValue)
     */
    @Override
    public void substring(SQLBuffer buf, FilterValue str, FilterValue start,
        FilterValue length) {
        buf.append(substringFunctionName).append("(");
        str.appendTo(buf);
        buf.append(", ");
        if (start.getValue() instanceof Number) {
            buf.append(Long.toString(toLong(start)));
        } else {
            buf.append("(CAST ((");
            start.appendTo(buf);
            buf.append(") AS INTEGER))");
        }
        if (length != null) {
            buf.append(", ");
            if (length.getValue() instanceof Number) {
                buf.append(Long.toString(toLong(length)));
            } else {
                buf.append("(CAST ((");
                length.appendTo(buf);
                buf.append(") AS INTEGER))");
            }
        }
        buf.append(")");
    }  
}
