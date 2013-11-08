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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ColumnIO;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.jdbc.schema.Index;
import org.apache.openjpa.jdbc.schema.Schemas;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.schema.Unique;
import org.apache.openjpa.jdbc.sql.Row;
import org.apache.openjpa.jdbc.sql.Select;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.meta.ValueMetaDataImpl;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.MetaDataException;

/**
 * Standalone {@link ValueMapping} implementation.
 *
 * @author Abe White
 * @since 0.4.0
 */
@SuppressWarnings("serial")
public class ValueMappingImpl
    extends ValueMetaDataImpl
    implements ValueMapping {

    private static final Localizer _loc = Localizer.forPackage
        (ValueMappingImpl.class);

    private ValueMappingInfo _info;
    private ValueHandler _handler = null;
    private ClassMapping[] _typeArr = null;

    private Column[] _cols = Schemas.EMPTY_COLUMNS;
    private ColumnIO _io = null;
    private ForeignKey _fk = null;
    private Map<ClassMapping,ForeignKey> _targetFKs = null;
    private Index _idx = null;
    private Unique _unq = null;
    private int _join = JOIN_FORWARD;
    private boolean _criteria = false;
    private int _poly = POLY_TRUE;

    /**
     * Constructor. Supply owning mapping.
     */
    public ValueMappingImpl(FieldMapping owner) {
        super(owner);
        _info = owner.getMappingRepository().newMappingInfo(this);
        _info.setUseClassCriteria(owner.getMappingRepository().
            getMappingDefaults().useClassCriteria());
    }
    
    /**
     * Constructor for deserialization.
     */
    protected ValueMappingImpl() {
        super();
    }

    public ValueMappingInfo getValueInfo() {
        return _info;
    }

    public ValueHandler getHandler() {
        return _handler;
    }

    public void setHandler(ValueHandler handler) {
        _handler = handler;
    }

    public MappingRepository getMappingRepository() {
        return (MappingRepository) getRepository();
    }

    public FieldMapping getFieldMapping() {
        return (FieldMapping) getFieldMetaData();
    }

    public ClassMapping getTypeMapping() {
        return (ClassMapping) getTypeMetaData();
    }

    public ClassMapping getDeclaredTypeMapping() {
        return (ClassMapping) getDeclaredTypeMetaData();
    }

    public ClassMapping getEmbeddedMapping() {
        return (ClassMapping) getEmbeddedMetaData();
    }

    public FieldMapping getValueMappedByMapping() {
        return (FieldMapping) getValueMappedByMetaData();
    }

    public Column[] getColumns() {
        if (_cols.length != 0)
            return _cols;
        if (_fk != null)
            return _fk.getColumns();
        if (getValueMappedBy() != null)
            return getValueMappedByMapping().getColumns();
        return _cols;
    }

    public void setColumns(Column[] cols) {
        if (cols == null)
            cols = Schemas.EMPTY_COLUMNS;
        _cols = cols;
    }

    public ColumnIO getColumnIO() {
        if (_cols.length == 0 && _fk == null && getValueMappedBy() != null)
            return getValueMappedByMapping().getColumnIO();
        return (_io == null) ? ColumnIO.UNRESTRICTED : _io;
    }

    public void setColumnIO(ColumnIO io) {
        _io = io;
    }

    public ForeignKey getForeignKey() {
        if (_fk == null && getValueMappedBy() != null)
            return getValueMappedByMapping().getForeignKey();
        return _fk;
    }

    public void setForeignKey(ForeignKey fk) {
        _fk = fk;
        if (fk == null)
            _join = JOIN_FORWARD;
    }

    public ForeignKey getForeignKey(ClassMapping target, int targetNumber) {
        if (_fk == null && getValueMappedBy() != null)
            return getValueMappedByMapping().getForeignKey(target);
        if (target == null)
            return _fk;
        ClassMapping embeddedMeta = (ClassMapping)getEmbeddedMetaData(); 
        if (embeddedMeta != null) {
            FieldMapping[] fields = embeddedMeta.getFieldMappings();
            int j = 0;
            for (int i = 0; i < fields.length; i++) {
                ValueMapping val = fields[i].getValueMapping(); 
                if (val.getDeclaredTypeMapping() == target)
                    if (targetNumber == j)
                    return val.getForeignKey();
                    else
                        j++;
            }
        }
        if (_fk == null && _cols.length == 0)
            return null;

        // always use least-derived joinable type
        for (ClassMapping sup = target; sup != null;
            sup = sup.getJoinablePCSuperclassMapping()) {
            if (sup == getTypeMetaData())
                return _fk;
            target = sup;
        }

        synchronized (this) {
            if (_targetFKs != null) {
                Object cachedFK = _targetFKs.get(target);
                if (cachedFK != null)
                    return (ForeignKey) cachedFK;
            } else
                _targetFKs = new HashMap<ClassMapping, ForeignKey>();

            ForeignKey newfk = (_join == JOIN_FORWARD)
                ? newForwardForeignKey(target) : newInverseForeignKey(target);
            _targetFKs.put(target, newfk);
            return newfk;
        }
    }
    public ForeignKey getForeignKey(ClassMapping target) {
        return getForeignKey(target, 0);
    }
    /**
     * Create a forward foreign key to the given target.
     */
    private ForeignKey newForwardForeignKey(ClassMapping target) {
        Table table;
        Column[] cols;
        if (_fk == null) {
            table = _cols[0].getTable();
            cols = _cols;
        } else {
            table = _fk.getTable();
            cols = _fk.getColumns();
        }

        // gather target cols before adding foreign key to table in case
        // there is an error while looking for a target col
        Column[] tcols = new Column[cols.length];
        for (int i = 0; i < cols.length; i++) {
            if (cols[i].getTargetField() != null)
                tcols[i] = getEquivalentColumn(cols[i], target,
                    cols[i].getTargetField());
            else if (_fk != null)
                tcols[i] = getEquivalentColumn(_fk.getPrimaryKeyColumn
                    (cols[i]).getIdentifier(), target, true);
            else if (!DBIdentifier.isNull(cols[i].getTargetIdentifier()))
                tcols[i] = getEquivalentColumn(cols[i].getTargetIdentifier(), target,
                    true);
            else
                tcols[i] = getEquivalentColumn(cols[i].getIdentifier(), target,
                    false);
        }

        ForeignKey newfk = table.addForeignKey();
        newfk.setJoins(cols, tcols);
        if (_fk != null) {
            cols = _fk.getConstantColumns();
            for (int i = 0; i < cols.length; i++)
                newfk.joinConstant(cols[i], _fk.getConstant(cols[i]));

            cols = _fk.getConstantPrimaryKeyColumns();
            for (int i = 0; i < cols.length; i++)
                newfk.joinConstant(_fk.getPrimaryKeyConstant(cols[i]),
                    getEquivalentColumn(cols[i].getIdentifier(), target, true));
        }
        return newfk;
    }

    /**
     * Return the given mapping's equivalent to the given column, using the
     * target field.
     */
    private Column getEquivalentColumn(Column col, ClassMapping target,
        String fieldName) {
        fieldName = fieldName.substring(fieldName.indexOf('.') + 1);
        FieldMapping field = target.getFieldMapping(fieldName);
        if (field == null)
            throw new MetaDataException(_loc.get("no-equiv-field",
                new Object[]{ this, target, fieldName, col }));

        Column[] cols = field.getColumns();
        if (cols.length != 1)
            throw new MetaDataException(_loc.get("bad-equiv-field",
                new Object[]{ this, target, fieldName, col }));

        return cols[0];
    }

    /**
     * Return the given mapping's equivalent of the given column.
     */
    private Column getEquivalentColumn(DBIdentifier colName, ClassMapping target,
        boolean explicit) {
        // if there was no explicit target, use single pk column
        if (!explicit) {
            for (ClassMapping cls = target; cls != null;
                cls = cls.getJoinablePCSuperclassMapping()) {
                if (cls.getTable() != null) {
                    if (cls.getPrimaryKeyColumns().length == 1)
                        return cls.getPrimaryKeyColumns()[0];
                    break;
                }
            }
        }

        Column ret;
        for (ClassMapping cls = target; cls != null;
            cls = cls.getJoinablePCSuperclassMapping()) {
            if (cls.getTable() != null) {
                ret = cls.getTable().getColumn(colName);
                if (ret != null)
                    return ret;
            }
        }

        throw new MetaDataException(_loc.get("no-equiv-col", this, target,
            colName));
    }

    /**
     * Return an inverse foreign key from the given related type to our table.
     */
    private ForeignKey newInverseForeignKey(ClassMapping target) {
        FieldMapping field = getFieldMapping();
        FieldMapping mapped = field.getMappedByMapping();
        if (mapped == null)
            throw new MetaDataException(_loc.get("cant-inverse", this));

        mapped = target.getFieldMapping(mapped.getIndex());
        if (mapped == null || mapped.getTypeCode() != JavaTypes.PC)
            throw new MetaDataException(_loc.get("no-equiv-mapped-by",
                this, target, field.getMappedBy()));
        return mapped.getForeignKey();
    }

    public int getJoinDirection() {
        if (_fk == null && getValueMappedBy() != null)
            return getValueMappedByMapping().getJoinDirection();
        return _join;
    }

    public void setJoinDirection(int direction) {
        _join = direction;
    }

    public void setForeignKey(Row row, OpenJPAStateManager rel, int targetNumber)
        throws SQLException {
        if (rel != null) {
            row.setForeignKey(getForeignKey((ClassMapping) rel.getMetaData(), targetNumber),
                _io, rel);
        }
        else if (_fk != null)
            row.setForeignKey(_fk, _io, null);
        else {
            for (int i = 0; i < _cols.length; i++) {
                if (_io == null || (row.getAction() == Row.ACTION_INSERT
                    && _io.isInsertable(i, true))
                    || (row.getAction() != Row.ACTION_INSERT
                    && _io.isUpdatable(i, true)))
                    row.setNull(_cols[i]);
            }
        }
    }
    public void setForeignKey(Row row, OpenJPAStateManager rel)
        throws SQLException {
        setForeignKey(row, rel, 0);
    }

    public void whereForeignKey(Row row, OpenJPAStateManager rel)
        throws SQLException {
        if (rel != null)
            row.whereForeignKey(getForeignKey((ClassMapping)
                rel.getMetaData()), rel);
        else if (_fk != null)
            row.whereForeignKey(_fk, null);
        else
            for (int i = 0; i < _cols.length; i++)
                row.whereNull(_cols[i]);
    }

    public ClassMapping[] getIndependentTypeMappings() {
        ClassMapping rel = getTypeMapping();
        if (rel == null)
            return ClassMapping.EMPTY_MAPPINGS;
        if (_poly != POLY_TRUE) {
            if (!rel.isMapped())
                return ClassMapping.EMPTY_MAPPINGS;
            if (_typeArr == null)
                _typeArr = new ClassMapping[]{ rel };
            return _typeArr;
        }
        return rel.getIndependentAssignableMappings();
    }

    public int getSelectSubclasses() {
        ClassMapping rel = getTypeMapping();
        if (rel == null || !rel.isMapped())
            return -1;

        switch (_poly) {
            case POLY_FALSE:
                return (_criteria) ? Select.SUBS_NONE : Select.SUBS_EXACT;
            case POLY_TRUE:
                ClassMapping[] assign = rel.getIndependentAssignableMappings();
                if (assign.length != 1 || assign[0] != rel)
                    return -1;
                // no break
            case POLY_JOINABLE:
                return (_criteria) ? Select.SUBS_JOINABLE
                    : Select.SUBS_ANY_JOINABLE;
            default:
                throw new InternalException();
        }
    }

    public Unique getValueUnique() {
        return _unq;
    }

    public void setValueUnique(Unique unq) {
        _unq = unq;
    }

    public Index getValueIndex() {
        return _idx;
    }

    public void setValueIndex(Index idx) {
        _idx = idx;
    }

    public boolean getUseClassCriteria() {
        if (_fk == null && getValueMappedBy() != null)
            return getValueMappedByMapping().getUseClassCriteria();
        return _criteria;
    }

    public void setUseClassCriteria(boolean criteria) {
        _criteria = criteria;
    }

    public int getPolymorphic() {
        return _poly;
    }

    public void setPolymorphic(int poly) {
        _poly = poly;
    }

    public void refSchemaComponents() {
        for (int i = 0; i < _cols.length; i++)
            _cols[i].ref();
        if (_fk != null) {
            _fk.ref();
            _fk.refColumns();
        }

        ClassMapping embed = getEmbeddedMapping();
        if (embed != null)
            embed.refSchemaComponents();
    }

    /**
     * @deprecated
     */
    public void mapConstraints(String name, boolean adapt) {
        mapConstraints(DBIdentifier.newConstraint(name), adapt);
    }

    public void mapConstraints(DBIdentifier name, boolean adapt) {
        _unq = _info.getUnique(this, name, adapt);
        _idx = _info.getIndex(this, name, adapt);
    }
    
    public void clearMapping() {
        _handler = null;
        _cols = Schemas.EMPTY_COLUMNS;
        _unq = null;
        _idx = null;
        _fk = null;
        _join = JOIN_FORWARD;
        _info.clear();
        setResolve(MODE_MAPPING | MODE_MAPPING_INIT, false);
    }

    public void syncMappingInfo() {
        if (getValueMappedBy() != null)
            _info.clear();
        else {
            _info.syncWith(this);
            ClassMapping embed = getEmbeddedMapping();
            if (embed != null)
                embed.syncMappingInfo();
        }
    }
    
    public void copy(ValueMetaData vmd) {
        super.copy(vmd);
        copyMappingInfo((ValueMapping)vmd);
    }

    public void copyMappingInfo(ValueMapping vm) {
        setValueMappedBy(vm.getValueMappedBy());
        setPolymorphic(vm.getPolymorphic());
        _info.copy(vm.getValueInfo());

        ClassMapping embed = vm.getEmbeddedMapping();
        if (embed != null && getEmbeddedMapping() != null) {
            FieldMapping[] tmplates = embed.getFieldMappings();
            FieldMapping[] fms = getEmbeddedMapping().getFieldMappings();
            if (tmplates.length == fms.length)
                for (int i = 0; i < fms.length; i++)
                    fms[i].copyMappingInfo(tmplates[i]);
        }
    }

    public boolean resolve(int mode) {
        int cur = getResolve();
        if (super.resolve(mode))
            return true;
        ClassMapping embed = getEmbeddedMapping();
        if (embed != null)
            embed.resolve(mode);
        if ((mode & MODE_MAPPING) != 0 && (cur & MODE_MAPPING) == 0)
            resolveMapping();
        if ((mode & MODE_MAPPING_INIT) != 0 && (cur & MODE_MAPPING_INIT) == 0)
            initializeMapping();
        return false;
    }

    /**
     * Setup mapping. Our handler will already have been set by our owning
     * field.
     */
    private void resolveMapping() {
        // mark mapped columns
        Column[] cols;
        int insertFlag;
        if (_fk != null) {
            cols = _fk.getColumns();
            insertFlag = Column.FLAG_FK_INSERT;
        } else {
            cols = getColumns();
            insertFlag = Column.FLAG_DIRECT_INSERT;
        }
        ColumnIO io = getColumnIO();
        for (int i = 0; i < cols.length; i++) {
            if (io.isInsertable(i, false))
                cols[i].setFlag(insertFlag, true);
            if (io.isUpdatable(i, false))
                cols[i].setFlag(insertFlag, true);
        }
    }

    /**
     * Prepare mapping for runtime use.
     */
    private void initializeMapping() {
        if (_fk == null)
            return;

        // if our fk cols are direct mapped by other values, make them
        // non-nullable
        Column[] cols = _fk.getColumns();
        for (int i = 0; i < cols.length; i++) {
            if (cols[i].getFlag(Column.FLAG_DIRECT_INSERT))
                newIO().setNullInsertable(i, false);
            if (cols[i].getFlag(Column.FLAG_DIRECT_UPDATE))
                newIO().setNullUpdatable(i, false);
        }

        // if anything maps our constant fk cols, make them read only
        int len = cols.length;
        cols = _fk.getConstantColumns();
        for (int i = 0; i < cols.length; i++) {
            if (cols[i].getFlag(Column.FLAG_DIRECT_INSERT)
                || cols[i].getFlag(Column.FLAG_FK_INSERT))
                newIO().setInsertable(len + i, false);
            if (cols[i].getFlag(Column.FLAG_DIRECT_UPDATE)
                || cols[i].getFlag(Column.FLAG_FK_UPDATE))
                newIO().setUpdatable(len + i, false);
        }
    }

    /**
     * Return the column I/O information, creating it if necessary.
     */
    private ColumnIO newIO() {
        if (_io == null)
            _io = new ColumnIO();
        return _io;
    }
}
