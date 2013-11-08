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

import java.util.List;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.kernel.JDBCStore;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.FieldStrategy;
import org.apache.openjpa.jdbc.meta.JavaSQLTypes;
import org.apache.openjpa.jdbc.meta.RelationId;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.jdbc.meta.ValueMappingInfo;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.ForeignKey;
import org.apache.openjpa.kernel.DetachedValueStateManager;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.MetaDataException;
import org.apache.openjpa.util.UserException;

/**
 * Helper methods for relation mappings.
 *
 * @author Abe White
 */
public class RelationStrategies {

    private static final Localizer _loc = Localizer.forPackage
        (RelationStrategies.class);

    /**
     * Return an exception indicating that we cannot join to the given relation.
     */
    public static MetaDataException unjoinable(ValueMapping vm) {
        return new MetaDataException(_loc.get("cant-join", vm));
    }

    /**
     * Return an exception indicating that the relation cannot be loaded
     * because it has independent subclasses and does not represent a full oid.
     */
    public static MetaDataException unloadable(ValueMapping vm) {
        return new MetaDataException(_loc.get("cant-load", vm));
    }

    /**
     * Return an exception indicating that the relation is invalid
     * because it has is based on an inverse foreign key and has independent
     * subclasses.
     */
    public static MetaDataException uninversable(ValueMapping vm) {
        return new MetaDataException(_loc.get("cant-inverse", vm));
    }

    /**
     * Return the given object as its foreign key values.
     *
     * @see FieldStrategy#toDataStoreValue
     */
    public static Object toDataStoreValue(ValueMapping vm, Object val,
        JDBCStore store) {
        ClassMapping rel;
        if (val == null) {
            ClassMapping[] clss = vm.getIndependentTypeMappings();
            rel = (clss.length > 0) ? clss[0] : vm.getTypeMapping();
        } else if (val.getClass() == vm.getType())
            rel = vm.getTypeMapping(); // common case
        else {
            rel = vm.getMappingRepository().getMapping(val.getClass(),
                store.getContext().getClassLoader(), true);
        }
        if (!rel.isMapped())
            throw new UserException(_loc.get("unmapped-datastore-value", 
                rel.getDescribedType()));

        Column[] cols;
        if (vm.getJoinDirection() == ValueMapping.JOIN_INVERSE)
            cols = rel.getPrimaryKeyColumns();
        else
            cols = vm.getForeignKey(rel).getPrimaryKeyColumns();
        return rel.toDataStoreValue(val, cols, store);
    }

    /**
     * Map a logical foreign key to an unmapped base class relation.
     */
    public static void mapRelationToUnmappedPC(ValueMapping vm,
        String name, boolean adapt) {
        mapRelationToUnmappedPC(vm, DBIdentifier.newColumn(name), adapt);
    }

    public static void mapRelationToUnmappedPC(ValueMapping vm,
        DBIdentifier name, boolean adapt) {
        if (vm.getTypeMapping().getIdentityType() == ClassMapping.ID_UNKNOWN)
            throw new MetaDataException(_loc.get("rel-to-unknownid", vm));

        ValueMappingInfo vinfo = vm.getValueInfo();
        Column[] tmplates = newUnmappedPCTemplateColumns(vm, name);
        vm.setColumns(vinfo.getColumns(vm, name, tmplates,
            vm.getFieldMapping().getTable(), adapt));
        vm.setColumnIO(vinfo.getColumnIO());
    }

    /**
     * Create template columns for a logical foreign key to an unmapped base
     * class relation.
     */
    private static Column[] newUnmappedPCTemplateColumns(ValueMapping vm,
        DBIdentifier name) {
        ClassMapping rel = vm.getTypeMapping();
        if (rel.getIdentityType() == ClassMapping.ID_DATASTORE) {
            Column col = new Column();
            col.setIdentifier(name);
            col.setJavaType(JavaTypes.LONG);
            col.setRelationId(true);
            return new Column[]{ col };
        }

        FieldMapping[] pks = rel.getPrimaryKeyFieldMappings();
        Column[] cols = new Column[pks.length];
        for (int i = 0; i < pks.length; i++) {
            cols[i] = mapPrimaryKey(vm, pks[i]);
            if (cols.length == 1)
                cols[i].setIdentifier(name);
            else if (DBIdentifier.isNull(cols[i].getIdentifier())) {
                DBIdentifier sName = DBIdentifier.combine(cols[i].getIdentifier(), pks[i].getName());
                cols[i].setIdentifier(sName);
            }
            else {
                DBIdentifier sName = DBIdentifier.combine(cols[i].getIdentifier(), cols[i].getName());
                cols[i].setIdentifier(sName);
            }
            cols[i].setTargetField(pks[i].getName());
            cols[i].setRelationId(true);
        }
        return cols;
    }

    /**
     * Create a default column for the given primary key field. Uses the
     * user's raw mapping info if given. Only supports simple field types.
     * The column name will be set to the name of the related primary key
     * column, if any.
     */
    private static Column mapPrimaryKey(ValueMapping vm, FieldMapping pk) {
        List cols = pk.getValueInfo().getColumns();
        if (cols.size() > 1)
            throw new MetaDataException(_loc.get("bad-unmapped-rel", vm, pk));

        Column tmplate = null;
        if (cols.size() == 1)
            tmplate = (Column) cols.get(0);

        Column col = new Column();
        switch (pk.getTypeCode()) {
            case JavaTypes.BOOLEAN:
            case JavaTypes.BOOLEAN_OBJ:
            case JavaTypes.BYTE:
            case JavaTypes.BYTE_OBJ:
            case JavaTypes.CHAR:
            case JavaTypes.CHAR_OBJ:
            case JavaTypes.DOUBLE:
            case JavaTypes.DOUBLE_OBJ:
            case JavaTypes.FLOAT:
            case JavaTypes.FLOAT_OBJ:
            case JavaTypes.INT:
            case JavaTypes.INT_OBJ:
            case JavaTypes.LONG:
            case JavaTypes.LONG_OBJ:
            case JavaTypes.NUMBER:
            case JavaTypes.SHORT:
            case JavaTypes.SHORT_OBJ:
            case JavaTypes.STRING:
            case JavaTypes.BIGINTEGER:
            case JavaTypes.BIGDECIMAL:
                col.setJavaType(pk.getTypeCode());
                break;
            case JavaTypes.DATE:
                col.setJavaType(JavaSQLTypes.getDateTypeCode(pk.getType()));
                break;
            default:
                throw new MetaDataException(
                    _loc.get("bad-unmapped-rel", vm, pk));
        }

        if (tmplate != null) {
            col.setIdentifier(tmplate.getIdentifier());
            col.setType(tmplate.getType());
            col.setTypeName(tmplate.getTypeName());
            col.setSize(tmplate.getSize());
            col.setDecimalDigits(tmplate.getDecimalDigits());
        }
        return col;
    }

    /**
     * Return the state manager for the given instance, using a detached
     * state manager if the instnace is not managed.
     */
    public static OpenJPAStateManager getStateManager(Object obj,
        StoreContext ctx) {
        if (obj == null)
            return null;
        OpenJPAStateManager sm = ctx.getStateManager(obj);
        if (sm == null) // must be detached
            return new DetachedValueStateManager(obj, ctx);
        return sm;
    }
    
    /**
     * Affirms if all of the given columns represent a {@linkplain RelationId relationship identifier}. 
     */
    public static boolean isRelationId(Column[] cols) {
        if (cols == null || cols.length == 0)
            return false;
        for (int i = 0; i < cols.length; i++) {
            if (!cols[i].isRelationId())
                return false;
        }
        return true;
    }
    
    /**
     * Affirms if all of the foreign key columns represent a {@linkplain RelationId relationship identifier}.
     */
    public static boolean isRelationId(ForeignKey fk) {
        return fk != null && isRelationId(fk.getColumns());
    }

}
