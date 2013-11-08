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
package org.apache.openjpa.persistence.jdbc;

import org.apache.openjpa.jdbc.identifier.Normalizer;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.Discriminator;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.MappingDefaultsImpl;
import org.apache.openjpa.jdbc.meta.ValueMapping;
import org.apache.openjpa.jdbc.meta.ValueMappingImpl;
import org.apache.openjpa.jdbc.meta.Version;
import org.apache.openjpa.jdbc.meta.strats.FlatClassStrategy;
import org.apache.openjpa.jdbc.meta.strats.MultiColumnVersionStrategy;
import org.apache.openjpa.jdbc.meta.strats.NoneDiscriminatorStrategy;
import org.apache.openjpa.jdbc.meta.strats.NoneVersionStrategy;
import org.apache.openjpa.jdbc.meta.strats.NumberVersionStrategy;
import org.apache.openjpa.jdbc.meta.strats.SubclassJoinDiscriminatorStrategy;
import org.apache.openjpa.jdbc.meta.strats.ValueMapDiscriminatorStrategy;
import org.apache.openjpa.jdbc.meta.strats.VerticalClassStrategy;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Schema;
import org.apache.openjpa.jdbc.schema.Table;
import org.apache.openjpa.jdbc.sql.JoinSyntaxes;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import serp.util.Strings;

/**
 * Supplies default mapping information in accordance with JPA spec.
 *
 * @author Steve Kim
 * @author Abe White
 * @nojavadoc
 */
public class PersistenceMappingDefaults
    extends MappingDefaultsImpl {

    private boolean _prependFieldNameToJoinTableInverseJoinColumns = true;

    public PersistenceMappingDefaults() {
        setDefaultMissingInfo(true);
        setStoreEnumOrdinal(true);
        setOrderLists(false);
        setAddNullIndicator(false);
        setDiscriminatorColumnName("DTYPE");
    }

    /**
     * Whether to prepend the field name to the default name of inverse join
     * columns within join tables.  Defaults to true per spec, but set to false
     * for compatibility with older versions of OpenJPA.
     */
    public boolean getPrependFieldNameToJoinTableInverseJoinColumns() {
        return _prependFieldNameToJoinTableInverseJoinColumns;
    }

    /**
     * Whether to prepend the field name to the default name of inverse join
     * columns within join tables.  Defaults to true per spec, but set to false
     * for compatibility with older versions of OpenJPA.
     */
    public void setPrependFieldNameToJoinTableInverseJoinColumns(boolean val) {
        _prependFieldNameToJoinTableInverseJoinColumns = val;
    }

    @Override
    public Object getStrategy(Version vers, boolean adapt) {
        Object strat = super.getStrategy(vers, adapt);
        ClassMapping cls = vers.getClassMapping();
        if (strat != null || cls.getJoinablePCSuperclassMapping() != null
            || cls.getVersionField() != null)
            return strat;

        int nColumn = vers.getMappingInfo().getColumns().size();
        switch (nColumn) {
            case 0 : return NoneVersionStrategy.getInstance();
            case 1 : return new NumberVersionStrategy();
            default: return new MultiColumnVersionStrategy();
        }
    }

    @Override
    public Object getStrategy(Discriminator disc, boolean adapt) {
        Object strat = super.getStrategy(disc, adapt);
        ClassMapping cls = disc.getClassMapping();
        if (strat != null || cls.getJoinablePCSuperclassMapping() != null
            || disc.getMappingInfo().getValue() != null)
            return strat;

        // don't use a column-based discriminator approach unless user has set
        // a column explicitly or is using flat inheritance explicitly
        if (!disc.getMappingInfo().getColumns().isEmpty())
            return new ValueMapDiscriminatorStrategy();

        ClassMapping base = cls;
        while (base.getMappingInfo().getHierarchyStrategy() == null
            && base.getPCSuperclassMapping() != null)
            base = base.getPCSuperclassMapping();

        strat = base.getMappingInfo().getHierarchyStrategy();
        if (FlatClassStrategy.ALIAS.equals(strat))
            return new ValueMapDiscriminatorStrategy();
        if (VerticalClassStrategy.ALIAS.equals(strat)
            && dict.joinSyntax != JoinSyntaxes.SYNTAX_TRADITIONAL)
            return new SubclassJoinDiscriminatorStrategy();
        return NoneDiscriminatorStrategy.getInstance();
    }

    @Override
    public String getTableName(ClassMapping cls, Schema schema) {
        if (cls.getTypeAlias() != null)
            return cls.getTypeAlias();
        return Strings.getClassName(cls.getDescribedType()).replace('$', '_');
    }

    @Override
    public String getTableName(FieldMapping fm, Schema schema) {
        return getTableIdentifier(fm, schema).getName();
    }

    @Override
    public DBIdentifier getTableIdentifier(FieldMapping fm, Schema schema) {
        // base name is table of defining type + '_'
        ClassMapping clm = fm.getDefiningMapping();
        Table table = getTable(clm);
        
        DBIdentifier sName = DBIdentifier.NULL;
        if (fm.isElementCollection()) 
            sName = DBIdentifier.newTable(clm.getTypeAlias());
        else 
            sName = table.getIdentifier();
        
        // if this is an assocation table, spec says to suffix with table of
        // the related type. spec doesn't cover other cases; we're going to
        // suffix with the field name
        ClassMapping rel = fm.getElementMapping().getTypeMapping();
        boolean assoc = rel != null && rel.getTable() != null
            && fm.getTypeCode() != JavaTypes.MAP;
        DBIdentifier sName2 = DBIdentifier.NULL;
        if (assoc) {
            sName2 = rel.getTable().getIdentifier();
        }
        else {
            sName2 = DBIdentifier.newTable(fm.getName().replace('$', '_'));
        }
        
        sName = DBIdentifier.combine(sName, sName2.getName());
        
        return sName;
    }
    
    private Table getTable(ClassMapping clm) {
        Table table = clm.getTable();
        if (table == null) {
            ValueMappingImpl value =
                    (ValueMappingImpl)clm.getEmbeddingMetaData();
            if (value == null)
                return table;
            FieldMetaData field = value.getFieldMetaData();
            clm = (ClassMapping)field.getDefiningMetaData();
            return getTable(clm);
        }
        return table;
    }

    @Override
    public void populateJoinColumn(FieldMapping fm, Table local, Table foreign,
        Column col, Object target, int pos, int cols) {
        // only use spec defaults with column targets
        if (!(target instanceof Column))
            return;

        // if this is a bidi relation, prefix with inverse field name, else
        // prefix with owning entity name
        FieldMapping[] inverses = fm.getInverseMappings();
        DBIdentifier sName = DBIdentifier.NULL;
        if (inverses.length > 0)
            sName = DBIdentifier.newColumn(inverses[0].getName());
        else
            sName = DBIdentifier.newColumn(fm.getDefiningMapping().getTypeAlias());
        DBIdentifier targetName = ((Column) target).getIdentifier();
        DBIdentifier tempName = DBIdentifier.NULL;
        if ((sName.length() + targetName.length()) >= dict.maxColumnNameLength)
            tempName = DBIdentifier.truncate(sName, dict.maxColumnNameLength
                    - targetName.length() - 1);
        // suffix with '_' + target column
        if (DBIdentifier.isNull(tempName))
            tempName = sName;
        sName = DBIdentifier.combine(tempName, targetName.getName());
        sName = dict.getValidColumnName(sName, foreign);
        col.setIdentifier(sName);
    }

    @Override
    public void populateForeignKeyColumn(ValueMapping vm, String name,
        Table local, Table foreign, Column col, Object target, boolean inverse,
        int pos, int cols) {
         populateForeignKeyColumn(vm, DBIdentifier.newColumn(name), local,
            foreign, col, target, inverse, pos, cols);
    }

    public void populateForeignKeyColumn(ValueMapping vm, DBIdentifier sName,
        Table local, Table foreign, Column col, Object target, boolean inverse,
        int pos, int cols) {
        boolean elem = vm == vm.getFieldMapping().getElement()
            && vm.getFieldMapping().getTypeCode() != JavaTypes.MAP;

        // if this is a non-inverse collection element key, it must be in
        // a join table: if we're not prepending the field name, leave the
        // default
        if (!_prependFieldNameToJoinTableInverseJoinColumns && !inverse && elem)
            return;

        // otherwise jpa always uses <field>_<pkcol> for column name, even
        // when only one col
        if (target instanceof Column) {
            if (DBIdentifier.isNull(sName)) {
                sName = col.getIdentifier();
            } else {
                if (elem)
                    sName = DBIdentifier.newColumn(vm.getFieldMapping().getName());
                if (isRemoveHungarianNotation())
                    sName = DBIdentifier.newColumn(Normalizer.removeHungarianNotation(sName.getName()));
                sName = sName.combine(sName, ((Column)target).getIdentifier().getName());

                // No need to check for uniqueness.
                sName = dict.getValidColumnName(sName, local, false);
            }
            col.setIdentifier(sName);
        }
    }

    @Override
    public void populateColumns(Version vers, Table table, Column[] cols) {
        // check for version field and use its name as column name
        FieldMapping fm = vers.getClassMapping().getVersionFieldMapping();
        if (fm != null && cols.length == 1)
            cols[0].setIdentifier(DBIdentifier.newColumn(fm.getName()));
        else
            super.populateColumns(vers, table, cols);
    }
}
