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
package org.apache.openjpa.jdbc.identifier;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.identifier.DBIdentifier.DBIdentifierType;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.NameSet;
import org.apache.openjpa.jdbc.schema.Schema;
import org.apache.openjpa.jdbc.schema.SchemaGroup;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.identifier.IdentifierConfiguration;
import org.apache.openjpa.lib.identifier.IdentifierRule;
import org.apache.openjpa.lib.identifier.IdentifierUtil;
import org.apache.openjpa.lib.identifier.IdentifierUtilImpl;

public class DBIdentifierUtilImpl extends IdentifierUtilImpl implements DBIdentifierUtil,
    Configurable {
    
    private JDBCConfiguration _conf = null;

    public DBIdentifierUtilImpl() {
    }
    
    public DBIdentifierUtilImpl(IdentifierConfiguration config) {
        super(config);
    }
    
    public DBIdentifier getValidColumnIdentifier(DBIdentifier name, Table table,
        int maxLen, boolean checkForUniqueness) {
        if (DBIdentifier.isNull(name)) {
            return name;
        }
        String rule = name.getType().name();
        maxLen = getMaxLen(rule, name, maxLen);

        DBIdentifier sName = DBIdentifier.removeLeading(name, IdentifierUtil.UNDERSCORE);
        return makeIdentifierValid(sName, table, maxLen, checkForUniqueness);
    }
    
    public DBIdentifier getValidForeignKeyIdentifier(DBIdentifier name, Table table, Table toTable, int maxLen) {
        if (DBIdentifier.isNull(name)) {
            return name;
        }
        String rule = name.getType().name();
        maxLen = getMaxLen(rule, name, maxLen);

        DBIdentifier sName = makeName(rule, name, table, "F");
        return makeIdentifierValid(sName, table.getSchema().getSchemaGroup(),
            maxLen, true);
    }

    
    public DBIdentifier getValidUniqueIdentifier(DBIdentifier name, Table table, int maxLen) {
        if (DBIdentifier.isNull(name)) {
            return name;
        }
        String rule = name.getType().name();
        maxLen = getMaxLen(rule, name, maxLen);

        DBIdentifier sName = makeName(rule, name, table, "U");
        return makeIdentifierValid(sName, table.getSchema().getSchemaGroup(),
            maxLen, true);
    }

    public DBIdentifier getValidIndexIdentifier(DBIdentifier name, Table table, int maxLen) {
        if (DBIdentifier.isNull(name)) {
            return name;
        }
        String rule = name.getType().name();
        maxLen = getMaxLen(rule, name, maxLen);

        DBIdentifier sName = makeName(rule, name, table, "I");
        return makeIdentifierValid(sName, table.getSchema().getSchemaGroup(),
            maxLen, true);
    }

    public DBIdentifier getValidSequenceIdentifier(DBIdentifier name, Schema schema, int maxLen) {
        if (DBIdentifier.isNull(name)) {
            return name;
        }
        String rule = name.getType().name();
        maxLen = getMaxLen(rule, name, maxLen);

        DBIdentifier sName = makeName(rule, name, "S");
        return makeIdentifierValid(sName, schema.getSchemaGroup(), maxLen, true);
    }

    public DBIdentifier getValidTableIdentifier(DBIdentifier name, Schema schema, int maxLen) {
        if (DBIdentifier.isNull(name)) {
            return name;
        }
        String rule = name.getType().name();
        maxLen = getMaxLen(rule, name, maxLen);

        DBIdentifier sName = makeName(rule, name, null);
        
        return makeIdentifierValid(sName, schema.getSchemaGroup(),
            maxLen, true);
    }

    public DBIdentifier makeNameValid(String name, NameSet set, int maxLen,
        int nameType, boolean checkForUniqueness) {
        DBIdentifierType id = DBIdentifierType.DEFAULT;
        switch (nameType) {
            case DBIdentifierUtil.TABLE:
                id = DBIdentifierType.TABLE;
                break;
            case DBIdentifierUtil.SEQUENCE:
                id = DBIdentifierType.SEQUENCE;
                break;
            case DBIdentifierUtil.COLUMN:
                id = DBIdentifierType.COLUMN;
                break;
        }
        return makeIdentifierValid(DBIdentifier.newIdentifier(name, id), set, maxLen, checkForUniqueness);
    }

    public DBIdentifier makeIdentifierValid(DBIdentifier sname, NameSet set, int maxLen,
        boolean checkForUniqueness) {
        DBIdentifier validName = sname;
        String rule = sname.getType().name();
        
        maxLen = getMaxLen(rule, validName, maxLen);

        int nameLen = validName.getName().length();
        if (nameLen > maxLen) {
            validName = DBIdentifier.truncate(validName, nameLen - maxLen);
            nameLen = validName.getName().length();
        }
        if (isReservedWord(rule, validName.getName())) {
            if (nameLen == maxLen)
                validName = DBIdentifier.truncate(validName, 1);
            validName = DBIdentifier.append(validName, "0");
            nameLen = validName.getName().length();
        }

        // now make sure the name is unique
        if (set != null && checkForUniqueness) {
            for (int version = 1, chars = 1; true; version++) {
                // for table names, we check for the table itself in case the
                // name set is lazy about schema reflection
                if (validName.getType() == DBIdentifierType.TABLE) {
                    if (!((SchemaGroup) set).isKnownTable(QualifiedDBIdentifier.getPath(validName)))
                        break;
                } else if (validName.getType() == DBIdentifierType.SEQUENCE) {
                    if (!((SchemaGroup) set).isKnownSequence(QualifiedDBIdentifier.getPath(validName)))
                        break;
                } else {
                    if (!set.isNameTaken(validName))
                        break;
                }

                // a single char for the version is probably enough, but might
                // as well be general about it...
                if (version > 1) {
                    validName = DBIdentifier.truncate(validName, chars);
                }
                if (version >= Math.pow(10, chars))
                    chars++;
                if (nameLen + chars > maxLen) {
                    validName = DBIdentifier.truncate(validName, nameLen + chars - maxLen);
                }
                validName = DBIdentifier.append(validName, Integer.toString(version)); 
                nameLen = validName.getName().length();
            }
        }
        
        if (validName.isDelimited()) {
            String delimCase = getIdentifierConfiguration().getDelimitedCase();
            if (delimCase.equals(CASE_LOWER)) {
                return DBIdentifier.toLower(validName,true);
            }
            else if (delimCase.equals(CASE_UPPER)) {
                return DBIdentifier.toUpper(validName,true);
            }
            else {
                return validName;
            }
        }
        return DBIdentifier.toUpper(validName);
    }

    /**
     *  Converts the name to a name which can be used within a SQL statement.  Uses
     *  the appropriate delimiters and separators. 
     *  @parm name a DBIdentifier
     */
    public String toDBName(DBIdentifier name) {
        return toDBName(name, true);
    }
    
    /**
     *  Converts the name to a name which can be used within a SQL statement.  Uses
     *  the appropriate delimiters and separators. 
     *  @parm name a DBIdentifier
     *  @param delimit If true, allows the name to be delimited, if necessary.  
     *  Otherwise, the identifier is not delimited.
     */
    public String toDBName(DBIdentifier name, boolean delimit) {
        if (DBIdentifier.isNull(name)) {
            return null;
        }
        if (getIdentifierConfiguration().getSupportsDelimitedIdentifiers() && delimit 
            && getIdentifierConfiguration().delimitAll() && !name.isDelimited()) {
            return delimit(name, true);
        }
        String rule = name.getType().name();
        if (name instanceof QualifiedDBIdentifier) {
            QualifiedDBIdentifier path = (QualifiedDBIdentifier)name;
            return convertFull(Normalizer.getNamingConfiguration(), rule, path.getName());
        }
        return convert(Normalizer.getNamingConfiguration(), rule, name.getName());
    }

    /**
     * Converts the identifier to a format appropriate for the configuration.
     * Delimits if necessary
     */
    public String toDBName(String name) {
        return toDBName(name, true);
    }

    /**
     * Converts the identifier to a format appropriate for the configuration using
     * the default naming rule.
     * @param delimit If false, do not delimit.  Otherwise, delimit if necessary.
     */
    public String toDBName(String name, boolean delimit) {
        return toDBName(getIdentifierConfiguration().getDefaultIdentifierRule().getName(), name, delimit);
    }

    /**
     * Converts the identifier to a format appropriate for the configuration using
     * the specified naming rule.
     * @param delimit If false, do not delimit.  Otherwise, delimit if necessary.
     */
    private String toDBName(String rule, String name, boolean delimit) {
        if (name == null) {
            return null;
        }
        if (getIdentifierConfiguration().getSupportsDelimitedIdentifiers() && delimit && 
            getIdentifierConfiguration().delimitAll() && !Normalizer.isDelimited(name)) {
            return delimit(rule, name, true);
        }
        return convert(Normalizer.getNamingConfiguration(), rule, name);
    }

    /**
     * Creates a new identifier of a given type based upon the name returned
     * from the database.  
     */
    public DBIdentifier fromDBName(String name, DBIdentifierType id) {
        if (name == null) {
            return DBIdentifier.NULL;
        }
        if (!getIdentifierConfiguration().getSupportsDelimitedIdentifiers()) {
            return DBIdentifier.newIdentifier(name, id);
        }
        String delimCase = getIdentifierConfiguration().getDelimitedCase();
        String nonDelimCase = getIdentifierConfiguration().getSchemaCase();
        String caseName = name;
        
        // If delimited and non-delimited case are the same, don't change 
        // case or try to determine whether delimiting is required.  Let the
        // normalizer figure it out using standard rules.
        if (delimCase.equals(nonDelimCase)) {
            return DBIdentifier.newIdentifier(name, id, false, false, !delimCase.equals(CASE_PRESERVE));
        }
        
        // Otherwise, try to determine whether to delimit based on an expected vs.
        // actual name comparison.
        if (delimCase.equals(CASE_PRESERVE)) {
            if (nonDelimCase.equals(CASE_LOWER)) {
                caseName = name.toLowerCase();
            } else {
                caseName = name.toUpperCase();
            }
        } else if (delimCase.equals(CASE_LOWER)) {
            if (nonDelimCase.equals(CASE_UPPER)) {
                caseName = name.toUpperCase();
            }
        } else if (delimCase.equals(CASE_UPPER)) {
            if (nonDelimCase.equals(CASE_LOWER)) {
                caseName = name.toLowerCase();
            }
        }
        
        boolean delimit = !caseName.equals(name) || getIdentifierConfiguration().delimitAll();
        return DBIdentifier.newIdentifier((delimit ? name : caseName), id, false, delimit, 
            !delimCase.equals(CASE_PRESERVE));
    }

    public DBIdentifier append(DBIdentifierType resultId, DBIdentifier...names) {
        if (names == null || names.length == 0) {
            return DBIdentifier.NULL;
        }
        DBIdentifier sName = DBIdentifier.newIdentifier("", resultId);
        for (DBIdentifier name : names) {
            DBIdentifier.append(sName, name.getName());
        }
        return sName;
    }
    
    public String appendColumns(Column[] columns) {
        if (columns == null || columns.length == 0) {
            return "";
        }
        if (columns.length == 1) {
            return toDBName(columns[0].getIdentifier());
        }
        StringBuilder colsb = new StringBuilder("");
        for (int i = 0; i < columns.length; i++) {
            colsb.append(toDBName(columns[i].getIdentifier()));
            if (i < (columns.length - 1)) {
                colsb.append(", ");
            }
        }
        return colsb.toString();
    }
    
    public String delimit(DBIdentifier name, boolean force) {
        String rule = name.getType().name();
        // If this is a compound path, each item must be delimited
        // separately
        if (name instanceof QualifiedDBIdentifier) {
            QualifiedDBIdentifier path = (QualifiedDBIdentifier)name;
            // Make sure this is a qualified path before delimiting
            // separately
            if (!((path.getType() == DBIdentifierType.COLUMN &&
                  path.isUnqualifiedColumn()) ||
                  (path.getType() != DBIdentifierType.COLUMN &&
                   path.isUnqualifiedObject()))) {
                DBIdentifier[] names = QualifiedDBIdentifier.splitPath(name);
                for (int i = 0; i < names.length; i++) {
                    DBIdentifier sName = names[i].getUnqualifiedName();
                    if (!sName.isDelimited()) {
                        String pRule = sName.getType().name();
                        names[i].setName(delimit(pRule, sName.getName(), force));
                    }
                }
                return QualifiedDBIdentifier.newPath(names).getName();
            }
        }
        return delimit(rule, name.getName(), force);
    }
    
    public String shorten(String name, int targetLength) {
        return DBDictionary.shorten(name, targetLength);
    }
    
    public DBIdentifier getGeneratedKeySequenceName(Column col, int maxLen) {
        DBIdentifier tname = col.getTableIdentifier();
        DBIdentifier cname = col.getIdentifier();
        int max = maxLen;
        int extraChars = -max + tname.getName().length() + 1 // <tname> + '_'
            + cname.getName().length() + 4; // <cname> + '_SEQ'
        String tsname = tname.getName();
        if (extraChars > 0) {
            // this assumes that tname is longer than extraChars
            tsname = tsname.substring(0, tsname.length() - extraChars);
        }
        return DBIdentifier.combine(DBIdentifierType.SEQUENCE, tsname, cname.getName(), "SEQ");
    }
    
    /**
     * Convert the specified schema name to a name that the database will
     * be able to understand in metadata operations.
     */
    public DBIdentifier convertSchemaCase(DBIdentifier name) {
        if (DBIdentifier.isNull(name))
            return DBIdentifier.NULL;

        DBIdentifier sName = name.clone();
        // Handle delimited string differently. Return unquoted name.
        String delimCase = getIdentifierConfiguration().getDelimitedCase();
        if (/* getNamingConfiguration().delimitAll() || */ name.isDelimited()) {
            if (CASE_UPPER.equals(delimCase)) {
                sName = DBIdentifier.toUpper(sName,true);
            }
            else if (CASE_LOWER.equals(delimCase)) {
                sName = DBIdentifier.toLower(sName,true);
            }
            
            return DBIdentifier.removeDelimiters(sName);
        }
        if (!getIdentifierConfiguration().delimitAll()) {
            // Not delimited, use the base schema case expected by the DB
            String schemaCase = getIdentifierConfiguration().getSchemaCase();
            if (CASE_LOWER.equals(schemaCase))
                return DBIdentifier.toLower(sName);
            if (CASE_PRESERVE.equals(schemaCase))
                return sName;
            return DBIdentifier.toUpper(sName);
        }
        return sName;
    }
    
    /**
     * Converts a column alias to use the appropriate delimiters
     */
    public String convertAlias(String alias) {
        if (!needsConversion(getIdentifierConfiguration())) {
            return alias;
        }

        String[] names = Normalizer.splitName(alias);
        if (names.length <= 1) {
            // Nothing to split
            return alias;
        }
        // Skip the the first name.  It is the alias (T0, T1, etc.)
        for (int i = 1; i < names.length; i++) {
            names[i] = toDBName(getIdentifierConfiguration().getDefaultIdentifierRule().toString(), names[i], true);
        }
        return joinNames(getIdentifierConfiguration().getDefaultIdentifierRule(), names);
    }

    private DBIdentifier makeName(String rule, DBIdentifier name, Table tbl, String prefix) {
        DBIdentifier sName = DBIdentifier.removeLeading(name, IdentifierUtil.UNDERSCORE);
        String tableName = tbl.getIdentifier().getName();
        int len = Math.min(tableName.length(), 7);
        
        // Combine the names using the normalized configuration.  
        String str = combineNames(Normalizer.getNamingConfiguration(), rule, 
            new String[] { prefix == null ? "" : prefix, 
            shorten(tableName, len), sName.getName() });
        sName.setName(str);
        return sName;
    }

    private DBIdentifier makeName(String rule, DBIdentifier name, String prefix) {
        DBIdentifier sName = DBIdentifier.removeLeading(name, IdentifierUtil.UNDERSCORE);
        if (!StringUtils.isEmpty(prefix)) {
            sName = DBIdentifier.preCombine(sName, prefix);
        }
        return sName;
    }

    private int getMaxLen(String rule, DBIdentifier name, int maxLen) {
        IdentifierConfiguration config = getIdentifierConfiguration();
        if (maxLen < 1) {
            IdentifierRule nrule = config.getIdentifierRule(rule);
            maxLen = nrule.getMaxLength();
        }
        // Subtract delimiter length if name is delimited or will be delimited
        if (config.delimitAll() || name.isDelimited()) {
            maxLen = maxLen - (config.getLeadingDelimiter().length() + config.getTrailingDelimiter().length());
        }
        
        return maxLen;
    }
    
    /**
     * System configuration.
     */
    public JDBCConfiguration getConfiguration() {
        return _conf;
    }

    @Override
    public void setConfiguration(Configuration conf) {
        _conf = (JDBCConfiguration)conf;
    }
}
