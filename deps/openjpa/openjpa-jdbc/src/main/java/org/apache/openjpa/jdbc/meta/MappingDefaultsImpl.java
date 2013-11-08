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
package org.apache.openjpa.jdbc.meta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.identifier.Normalizer;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.meta.strats.UntypedPCValueHandler;
import org.apache.openjpa.jdbc.meta.strats.EnumValueHandler;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Index;
import org.apache.openjpa.jdbc.schema.Schema;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.schema.Unique;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.identifier.IdentifierUtil;
import org.apache.openjpa.meta.JavaTypes;
import serp.util.Strings;

/**
 * Default implementation of {@link MappingDefaults}.
 *
 * @author Abe White
 */
public class MappingDefaultsImpl
    implements MappingDefaults, Configurable {

    protected transient DBDictionary dict = null;
    private String _baseClassStrategy = null;
    private String _subclassStrategy = null;
    private String _versionStrategy = null;
    private String _discStrategy = null;
    private final Map _fieldMap = new HashMap();
    private boolean _defMissing = false;
    private boolean _classCriteria = false;
    private int _joinFKAction = ForeignKey.ACTION_NONE;
    private int _fkAction = ForeignKey.ACTION_NONE;
    private boolean _defer = false;
    private boolean _indexFK = true;
    private boolean _indexDisc = true;
    private boolean _indexVers = false;
    private boolean _orderLists = true;
    private boolean _addNullInd = false;
    private boolean _ordinalEnum = false;
    private boolean _stringifyUnmapped = false;
    private DBIdentifier _dsIdName = DBIdentifier.NULL;
    private DBIdentifier _versName = DBIdentifier.NULL;
    private DBIdentifier _discName = DBIdentifier.NULL;
    private DBIdentifier _orderName = DBIdentifier.NULL;
    private DBIdentifier _nullIndName = DBIdentifier.NULL;
    private boolean _removeHungarianNotation = false;

    public boolean isRemoveHungarianNotation() {
        return _removeHungarianNotation;
    }

    public void setRemoveHungarianNotation(boolean removeHungarianNotation) {
        this._removeHungarianNotation = removeHungarianNotation;
    }

    /**
     * Default base class strategy alias.
     */
    public String getBaseClassStrategy() {
        return _baseClassStrategy;
    }

    /**
     * Default base class strategy alias.
     */
    public void setBaseClassStrategy(String baseClassStrategy) {
        _baseClassStrategy = baseClassStrategy;
    }

    /**
     * Default subclass strategy alias.
     */
    public String getSubclassStrategy() {
        return _subclassStrategy;
    }

    /**
     * Default subclass strategy alias.
     */
    public void setSubclassStrategy(String subclassStrategy) {
        _subclassStrategy = subclassStrategy;
    }

    /**
     * Default version strategy alias.
     */
    public String getVersionStrategy() {
        return _versionStrategy;
    }

    /**
     * Default version strategy alias.
     */
    public void setVersionStrategy(String versionStrategy) {
        _versionStrategy = versionStrategy;
    }

    /**
     * Default discriminator strategy alias.
     */
    public String getDiscriminatorStrategy() {
        return _discStrategy;
    }

    /**
     * Default discriminator strategy alias.
     */
    public void setDiscriminatorStrategy(String discStrategy) {
        _discStrategy = discStrategy;
    }

    /**
     * Property string mapping field type names to value handler or field
     * mapping class names. For auto-configuration.
     */
    public void setFieldStrategies(String fieldMapString) {
        Properties props = Configurations.parseProperties(fieldMapString);
        if (props != null)
            _fieldMap.putAll(props);
    }

    /**
     * Association of a field value type name with the handler or strategy
     * class name.
     */
    public void setFieldStrategy(String valueType, String handlerType) {
        if (handlerType == null)
            _fieldMap.remove(valueType);
        else
            _fieldMap.put(valueType, handlerType);
    }

    /**
     * Association of a field value type name with the handler or strategy
     * class name.
     */
    public String getFieldStrategy(String valueType) {
        return (String) _fieldMap.get(valueType);
    }

    /**
     * Whether to store enums as the ordinal value rather than the enum name.
     * Defaults to false.
     */
    public boolean getStoreEnumOrdinal() {
        return _ordinalEnum;
    }

    /**
     * Whether to store enums as the ordinal value rather than the enum name.
     * Defaults to false.
     */
    public void setStoreEnumOrdinal(boolean ordinal) {
        _ordinalEnum = ordinal;
    }

    /**
     * Whether to store a relation to an unmapped class by stringifying the
     * oid of the related object, rather than storing primary key values.
     */
    public boolean getStoreUnmappedObjectIdString() {
        return _stringifyUnmapped;
    }

    /**
     * Whether to store a relation to an unmapped class by stringifying the
     * oid of the related object, rather than storing primary key values.
     */
    public void setStoreUnmappedObjectIdString(boolean stringify) {
        _stringifyUnmapped = stringify;
    }

    /**
     * Default foreign key action for join keys. Defaults to logical keys.
     */
    public int getJoinForeignKeyDeleteAction() {
        return _joinFKAction;
    }

    /**
     * Default foreign key action for join keys. Defaults to logical keys.
     */
    public void setJoinForeignKeyDeleteAction(int joinFKAction) {
        _joinFKAction = joinFKAction;
    }

    /**
     * Default foreign key action name for join keys. Used in auto
     * configuration.
     */
    public void setJoinForeignKeyDeleteAction(String joinFKAction) {
        _joinFKAction = ForeignKey.getAction(joinFKAction);
    }

    /**
     * Default foreign key action for relation keys. Defaults to logical keys.
     */
    public int getForeignKeyDeleteAction() {
        return _fkAction;
    }

    /**
     * Default foreign key action for relation keys. Defaults to logical keys.
     */
    public void setForeignKeyDeleteAction(int fkAction) {
        _fkAction = fkAction;
    }

    /**
     * Default foreign key action name for relation keys. Used in auto
     * configuration.
     */
    public void setForeignKeyDeleteAction(String fkAction) {
        _fkAction = ForeignKey.getAction(fkAction);
    }

    /**
     * Whether to index logical foreign keys by default. Defaults to true.
     */
    public boolean getIndexLogicalForeignKeys() {
        return _indexFK;
    }

    /**
     * Whether to index logical foreign keys by default. Defaults to true.
     */
    public void setIndexLogicalForeignKeys(boolean indexFK) {
        _indexFK = indexFK;
    }

    /**
     * Whether to index discriminator columns by default. Defaults to true.
     */
    public boolean getIndexDiscriminator() {
        return _indexDisc;
    }

    /**
     * Whether to index discriminator columns by default. Defaults to true.
     */
    public void setIndexDiscriminator(boolean indexDisc) {
        _indexDisc = indexDisc;
    }

    /**
     * Whether to index version columns by default. Defaults to true.
     */
    public boolean getIndexVersion() {
        return _indexVers;
    }

    /**
     * Whether to index version columns by default. Defaults to true.
     */
    public void setIndexVersion(boolean indexVers) {
        _indexVers = indexVers;
    }

    /**
     * Whether to order lists and arrays using a dedicated ordering column
     * by default.
     */
    public boolean getOrderLists() {
        return _orderLists;
    }

    /**
     * Whether to order lists and arrays using a dedicated ordering column
     * by default.
     */
    public void setOrderLists(boolean orderLists) {
        _orderLists = orderLists;
    }

    /**
     * Whether to add a synthetic null indicator column to embedded mappings
     * by default.
     */
    public boolean getAddNullIndicator() {
        return _addNullInd;
    }

    /**
     * Whether to add a synthetic null indicator column to embedded mappings
     * by default.
     */
    public void setAddNullIndicator(boolean addNullInd) {
        _addNullInd = addNullInd;
    }

    /**
     * Whether to defer constraints by default. Defaults to false.
     */
    public boolean getDeferConstraints() {
        return _defer;
    }

    /**
     * Whether to defer constraints by default. Defaults to false.
     */
    public void setDeferConstraints(boolean defer) {
        _defer = defer;
    }

    /**
     * Default base name for datastore identity columns, or null to the
     * mapping's built-in name.
     * @deprecated
     */
    public String getDataStoreIdColumnName() {
        return getDataStoreIdColumnIdentifier().getName();
    }

    public DBIdentifier getDataStoreIdColumnIdentifier() {
        return _dsIdName == null ? DBIdentifier.NULL : _dsIdName;
    }

    /**
     * Default base name for datastore identity columns, or null to the
     * mapping's built-in name.
     * @deprecated
     */
    public void setDataStoreIdColumnName(String dsIdName) {
        setDataStoreIdColumnIdentifier(DBIdentifier.newColumn(dsIdName));
    }

    public void setDataStoreIdColumnIdentifier(DBIdentifier dsIdName) {
        _dsIdName = dsIdName;
    }

    /**
     * Default base name for version identity columns, or null to the mapping's
     * built-in name.
     * @deprecated
     */
    public String getVersionColumnName() {
        return getVersionColumnIdentifier().getName();
    }

    public DBIdentifier getVersionColumnIdentifier() {
        return _versName == null ? DBIdentifier.NULL : _versName;
    }

    /**
     * Default base name for version identity columns, or null to the mapping's
     * built-in name.
     * @deprecated
     */
    public void setVersionColumnName(String versName) {
        setVersionColumnIdentifier(DBIdentifier.newColumn(versName));
    }

    public void setVersionColumnIdentifier(DBIdentifier versName) {
        _versName = versName;
    }

    /**
     * Default base name for discriminator columns, or null to the mapping's
     * built-in name.
     * @deprecated
     */
    public String getDiscriminatorColumnName() {
        return getDiscriminatorColumnIdentifier().getName();
    }

    public DBIdentifier getDiscriminatorColumnIdentifier() {
        return _discName == null ? DBIdentifier.NULL : _discName;
    }

    /**
     * Default base name for discriminator columns, or null to the mapping's
     * built-in name.
     * @deprecated
     */
    public void setDiscriminatorColumnName(String discName) {
        setDiscriminatorColumnIdentifier(DBIdentifier.newColumn(discName));
    }

    public void setDiscriminatorColumnIdentifier(DBIdentifier discName) {
        _discName = discName;
    }

    /**
     * Default base name for order columns, or null to the mapping's
     * built-in name.
     * @deprecated
     */
    public String getOrderColumnName() {
        return getOrderColumnIdentifier().getName();
    }

    public DBIdentifier getOrderColumnIdentifier() {
        return _orderName == null ? DBIdentifier.NULL : _orderName;
    }

    /**
     * Default base name for order columns, or null to the mapping's
     * built-in name.
     * @deprecated
     */
    public void setOrderColumnName(String orderName) {
        setOrderColumnIdentifier(DBIdentifier.newColumn(orderName));
    }

    public void setOrderColumnIdentifier(DBIdentifier orderName) {
        _orderName = orderName;
    }

    /**
     * Default base name for null indicator columns, or null to the mapping's
     * built-in name.
     * @deprecated
     */
    public String getNullIndicatorColumnName() {
        return getNullIndicatorColumnIdentifier().getName();
    }

    public DBIdentifier getNullIndicatorColumnIdentifier() {
        return _nullIndName == null ? DBIdentifier.NULL : _nullIndName;
    }

    /**
     * Default base name for null indicator columns, or null to the mapping's
     * built-in name.
     * @deprecated
     */
    public void setNullIndicatorColumnName(String nullIndName) {
        setNullIndicatorColumnIdentifier(DBIdentifier.newColumn(nullIndName));
    }
    
    public void setNullIndicatorColumnIdentifier(DBIdentifier nullIndName) {
        _nullIndName = nullIndName;
    }
    
    public boolean defaultMissingInfo() {
        return _defMissing;
    }

    public void setDefaultMissingInfo(boolean defMissing) {
        _defMissing = defMissing;
    }

    public boolean useClassCriteria() {
        return _classCriteria;
    }

    public void setUseClassCriteria(boolean classCriteria) {
        _classCriteria = classCriteria;
    }

    public Object getStrategy(ClassMapping cls, boolean adapt) {
        if (adapt || defaultMissingInfo())
            return (cls.getMappedPCSuperclassMapping() == null)
                ? _baseClassStrategy : _subclassStrategy;
        return null;
    }

    public Object getStrategy(Version vers, boolean adapt) {
        ClassMapping cls = vers.getClassMapping();
        if ((adapt || defaultMissingInfo())
            && cls.getJoinablePCSuperclassMapping() == null
            && cls.getVersionField() == null)
            return _versionStrategy;
        return null;
    }

    public Object getStrategy(Discriminator disc, boolean adapt) {
        ClassMapping cls = disc.getClassMapping();
        if ((adapt || defaultMissingInfo())
            && cls.getJoinablePCSuperclassMapping() == null
            && disc.getMappingInfo().getValue() == null)
            return _discStrategy;
        return null;
    }

    public Object getStrategy(ValueMapping vm, Class<?> type, boolean adapt) {
        Object ret = _fieldMap.get(type.getName());
        if (ret != null)
            return ret;
        if (_stringifyUnmapped && vm.getTypeMapping() != null
            && !vm.getTypeMapping().isMapped())
            return UntypedPCValueHandler.getInstance();
        if (type.isEnum() && !vm.isSerialized()) {
            EnumValueHandler enumHandler = new EnumValueHandler();
            enumHandler.setStoreOrdinal(_ordinalEnum);
            return enumHandler;
        }
        return null;
    }

    /**
     * Provides a default value for the given Discriminator. 
     * 
     * <P>
     * The type of the object returned relies on the javaType field being set on
     * the Discriminator which is provided.
     * <TABLE border="2"> 
     * <TH>JavaType
     * <TH>Default value
     * <TBODY>
     * <TR><TD>{@link JavaTypes#INT}<TD> The hashcode of the entity name</TR>
     * <TR><TD>{@link JavaTypes#CHAR}<TD>The first character of the entity name
     * </TR>
     * <TR><TD>{@link JavaTypes#STRING}<TD>The entity name</TR>
     * </TBODY>
     * </TABLE>
     * 
     * @param disc The discriminator that needs a default value
     * @param adapt 
     * 
     * @return A new object containing the generated Discriminator value.
     */
    public Object getDiscriminatorValue(Discriminator disc, boolean adapt) {
        if (!adapt && !defaultMissingInfo())
            return null;

        // WARNING: CHANGING THIS WILL INVALIDATE EXISTING DATA IF DEFAULTING
        // MISSING MAPPING INFO
        
        String alias = Strings.getClassName(disc.getClassMapping()
                .getTypeAlias());
        
        switch (disc.getJavaType()) {
            case JavaTypes.INT:
                return Integer.valueOf(alias.hashCode());
            case JavaTypes.CHAR:
                return Character.valueOf(alias.charAt(0)); 
            case JavaTypes.STRING:
            default:
                return alias;
        }
    }

    public String getTableName(ClassMapping cls, Schema schema) {
        String name = Strings.getClassName(cls.getDescribedType()).
            replace(IdentifierUtil.DOLLAR_CHAR, IdentifierUtil.UNDERSCORE_CHAR);
        if (!_defMissing)
            name = dict.getValidTableName(name, schema);
        return name;
    }

    public DBIdentifier getTableIdentifier(ClassMapping cls, Schema schema) {
        return DBIdentifier.newTable(getTableName(cls, schema));
    }

    public String getTableName(FieldMapping fm, Schema schema) {
        return getTableIdentifier(fm, schema).getName();
    }

    public DBIdentifier getTableIdentifier(FieldMapping fm, Schema schema) {
        DBIdentifier sName = DBIdentifier.newTable(fm.getName());
        Table table = fm.getDefiningMapping().getTable();
        if (table != null) {
            DBIdentifier tableName = DBIdentifier.truncate(table.getIdentifier(),5);
            sName = DBIdentifier.append(tableName, fm.getName());
        }
        if (!_defMissing)
            sName = dict.getValidTableName(sName, schema);
        return sName;
    }

    public void populateDataStoreIdColumns(ClassMapping cls, Table table,
        Column[] cols) {
        for (int i = 0; i < cols.length; i++) {
            if (!DBIdentifier.isNull(_dsIdName) && cols.length == 1)
                cols[i].setIdentifier(_dsIdName);
            else if (!DBIdentifier.isNull(_dsIdName))
                cols[i].setIdentifier(DBIdentifier.append(_dsIdName, Integer.toString(i)));
            correctName(table, cols[i]);
        }
    }

    /**
     * Correct the given column's name.
     */
    protected void correctName(Table table, Column col) {
        if (!_defMissing || _removeHungarianNotation)
        {
            DBIdentifier name = col.getIdentifier();
            if (_removeHungarianNotation)
                name = DBIdentifier.removeHungarianNotation(name);
            DBIdentifier correctedName = dict.getValidColumnName(name, table);
            col.setIdentifier(correctedName);
            table.addCorrectedColumnName(correctedName, true);
        }
    }

    protected String removeHungarianNotation(String columnName) {
        return Normalizer.removeHungarianNotation(columnName);
    }

    public void populateColumns(Version vers, Table table, Column[] cols) {
        for (int i = 0; i < cols.length; i++) {
            if (!DBIdentifier.isNull(_versName) && cols.length == 1)
                cols[i].setIdentifier(_versName);
            else if (!DBIdentifier.isNull(_versName)) {
                if (i == 0) {
                    cols[i].setIdentifier(_versName);
                } else {
                    cols[i].setIdentifier(DBIdentifier.append(_versName, Integer.toString(i)));
                }
            } else if (!DBIdentifier.isNull(_versName))
                cols[i].setIdentifier(DBIdentifier.append(_versName, Integer.toString(i)));
            correctName(table, cols[i]);
        }
    }

    public void populateColumns(Discriminator disc, Table table,
        Column[] cols) {
        for (int i = 0; i < cols.length; i++) {
            if (!DBIdentifier.isNull(_discName) && cols.length == 1)
                cols[i].setIdentifier(_discName);
            else if (!DBIdentifier.isNull(_discName))
                cols[i].setIdentifier(DBIdentifier.append(_discName, Integer.toString(i)));
            correctName(table, cols[i]);
        }
    }

    public void populateJoinColumn(ClassMapping cm, Table local, Table foreign,
        Column col, Object target, int pos, int cols) {
        correctName(local, col);
    }

    public void populateJoinColumn(FieldMapping fm, Table local, Table foreign,
        Column col, Object target, int pos, int cols) {
        correctName(local, col);
    }

    /**
     * @deprecated
     */
    public void populateForeignKeyColumn(ValueMapping vm, String name,
        Table local, Table foreign, Column col, Object target, boolean inverse,
        int pos, int cols) {
        populateForeignKeyColumn(vm, DBIdentifier.newColumn(name), local, foreign, col,
            target, inverse, pos, cols);
    }

    public void populateForeignKeyColumn(ValueMapping vm, DBIdentifier name,
        Table local, Table foreign, Column col, Object target, boolean inverse,
        int pos, int cols) {
        if (cols == 1)
            col.setIdentifier(name);
        else if (target instanceof Column)
            col.setIdentifier(DBIdentifier.combine(name,((Column) target).getIdentifier().getName()));
        correctName(local, col);
    }

    public void populateColumns(ValueMapping vm, String name, Table table,
        Column[] cols) {
        populateColumns(vm, DBIdentifier.newColumn(name), table, cols);
    }

    public void populateColumns(ValueMapping vm, DBIdentifier name, Table table,
        Column[] cols) {
        for (int i = 0; i < cols.length; i++)
            correctName(table, cols[i]);
    }

    public boolean populateOrderColumns(FieldMapping fm, Table table,
        Column[] cols) {
        for (int i = 0; i < cols.length; i++) {
            if (!DBIdentifier.isNull(_orderName) && cols.length == 1)
                cols[i].setIdentifier(_orderName);
            else if (!DBIdentifier.isNull(_orderName))
                cols[i].setIdentifier(DBIdentifier.append(_orderName, Integer.toString(i)));
            correctName(table, cols[i]);
        }
        return _orderLists && (JavaTypes.ARRAY == fm.getTypeCode()
            || List.class.isAssignableFrom(fm.getType()));
    }

    /**
     * @deprecated
     */
    public boolean populateNullIndicatorColumns(ValueMapping vm, String name,
        Table table, Column[] cols) {
        return populateNullIndicatorColumns(vm, DBIdentifier.newColumn(name), table, cols);
    }

    public boolean populateNullIndicatorColumns(ValueMapping vm, DBIdentifier name,
        Table table, Column[] cols) {
        for (int i = 0; i < cols.length; i++) {
            if (!DBIdentifier.isNull(_nullIndName) && cols.length == 1)
                cols[i].setIdentifier(_nullIndName);
            else if (!DBIdentifier.isNull(_nullIndName))
                cols[i].setIdentifier(DBIdentifier.append(_nullIndName, Integer.toString(i)));
            correctName(table, cols[i]);
        }
        return _addNullInd;
    }

    public ForeignKey getJoinForeignKey(ClassMapping cls, Table local,
        Table foreign) {
        if (_joinFKAction == ForeignKey.ACTION_NONE)
            return null;
        ForeignKey fk = new ForeignKey();
        fk.setDeleteAction(_joinFKAction);
        fk.setDeferred(_defer);
        return fk;
    }

    public ForeignKey getJoinForeignKey(FieldMapping fm, Table local,
        Table foreign) {
        if (_joinFKAction == ForeignKey.ACTION_NONE)
            return null;
        ForeignKey fk = new ForeignKey();
        fk.setDeleteAction(_joinFKAction);
        fk.setDeferred(_defer);
        return fk;
    }

    /**
     * @deprecated
     */
    public ForeignKey getForeignKey(ValueMapping vm, String name, Table local,
        Table foreign, boolean inverse) {
        return getForeignKey(vm, DBIdentifier.newForeignKey(name), local, foreign, inverse);
    }
        
    public ForeignKey getForeignKey(ValueMapping vm, DBIdentifier name, Table local,
        Table foreign, boolean inverse) {
        if (_fkAction == ForeignKey.ACTION_NONE)
            return null;
        ForeignKey fk = new ForeignKey();
        fk.setDeleteAction(_fkAction);
        fk.setDeferred(_defer);
        return fk;
    }

    public Index getJoinIndex(FieldMapping fm, Table table, Column[] cols) {
        if (!_indexFK || fm.getJoinForeignKey() == null
            || !fm.getJoinForeignKey().isLogical())
            return null;
        if (areAllPrimaryKeyColumns(cols))
            return null;

        Index idx = new Index();
        idx.setIdentifier(getIndexName(DBIdentifier.NULL, table, cols));
        return idx;
    }

    /**
     * Return whether all the given columns are primary key columns.
     */
    protected boolean areAllPrimaryKeyColumns(Column[] cols) {
        for (int i = 0; i < cols.length; i++)
            if (!cols[i].isPrimaryKey())
                return false;
        return true;
    }

    /**
     * Generate an index name.
     * @deprecated
     */
    protected String getIndexName(String name, Table table, Column[] cols) {
        return getIndexName(DBIdentifier.newIndex(name), table, cols).getName();
    }

    protected DBIdentifier getIndexName(DBIdentifier name, Table table, Column[] cols) {
        // always use dict for index names because no spec mandates them
        // based on defaults
        DBIdentifier sName = name;
        if (DBIdentifier.isNull(sName))
            sName = cols[0].getIdentifier();

        if (_removeHungarianNotation)
            sName = DBIdentifier.removeHungarianNotation(sName);

        return dict.getValidIndexName(sName, table);
    }

    /**
     * @deprecated
     */
    public Index getIndex(ValueMapping vm, String name, Table table,
        Column[] cols) {
        return getIndex(vm, DBIdentifier.newIndex(name), table, cols);
    }

    public Index getIndex(ValueMapping vm, DBIdentifier name, Table table,
        Column[] cols) {
        if (!_indexFK || vm.getForeignKey() == null
            || !vm.getForeignKey().isLogical())
            return null;
        if (areAllPrimaryKeyColumns(cols))
            return null;

        Index idx = new Index();
        idx.setIdentifier(getIndexName(name, table, cols));
        return idx;
    }

    public Index getIndex(Version vers, Table table, Column[] cols) {
        if (!_indexVers)
            return null;
        Index idx = new Index();
        idx.setIdentifier(getIndexName(_versName, table, cols));
        return idx;
    }

    public Index getIndex(Discriminator disc, Table table, Column[] cols) {
        if (!_indexDisc)
            return null;
        Index idx = new Index();
        idx.setIdentifier(getIndexName(_discName, table, cols));
        return idx;
    }

    public Unique getJoinUnique(FieldMapping fm, Table table, Column[] cols) {
        return null;
    }

    /**
     * @deprecated
     */
    public Unique getUnique(ValueMapping vm, String name, Table table,
        Column[] cols) {
        return null;
    }

    public Unique getUnique(ValueMapping vm, DBIdentifier name, Table table,
        Column[] cols) {
        return null;
    }

    /**
     * @deprecated
     */
    public String getPrimaryKeyName(ClassMapping cm, Table table) {
        return null;
    }

    public DBIdentifier getPrimaryKeyIdentifier(ClassMapping cm, Table table) {
        return DBIdentifier.NULL;
    }

    public void installPrimaryKey(FieldMapping fm, Table table) {
    }

    ///////////////////////////////
    // Configurable implementation
    ///////////////////////////////

    public void setConfiguration(Configuration conf) {
        dict = ((JDBCConfiguration) conf).getDBDictionaryInstance();
    }

    public void startConfiguration() {
    }

    public void endConfiguration() {
    }
}
