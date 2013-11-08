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

import org.apache.openjpa.persistence.AnnotationPersistenceMetaDataSerializer;
import org.apache.openjpa.persistence.PersistenceStrategy;
import org.apache.openjpa.persistence.AnnotationBuilder;
import org.apache.openjpa.jdbc.meta.QueryResultMapping;
import org.apache.openjpa.jdbc.meta.MappingRepository;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.ClassMappingInfo;
import org.apache.openjpa.jdbc.meta.DiscriminatorMappingInfo;
import org.apache.openjpa.jdbc.meta.MappingInfo;
import org.apache.openjpa.jdbc.meta.SequenceMapping;
import org.apache.openjpa.jdbc.meta.ValueMappingInfo;
import org.apache.openjpa.jdbc.meta.strats.FlatClassStrategy;
import org.apache.openjpa.jdbc.meta.strats.VerticalClassStrategy;
import org.apache.openjpa.jdbc.meta.strats.FullClassStrategy;
import org.apache.openjpa.jdbc.meta.strats.EnumValueHandler;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.schema.*;
import org.apache.openjpa.jdbc.schema.Unique;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.SequenceMetaData;
import org.apache.openjpa.meta.MetaDataModes;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.sql.Types;
import java.lang.annotation.Annotation;

import serp.util.Strings;

import javax.persistence.TemporalType;
import javax.persistence.EnumType;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.SecondaryTable;
import javax.persistence.Inheritance;
import javax.persistence.DiscriminatorValue;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.Enumerated;
import javax.persistence.UniqueConstraint;
import javax.persistence.TableGenerator;
import javax.persistence.JoinColumns;
import javax.persistence.JoinColumn;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.EntityResult;
import javax.persistence.FieldResult;
import javax.persistence.ColumnResult;

//@todo: javadocs

/**
 * Serializes persistence mappings as annotations.
 *
 * @since 1.0.0
 * @author Steve Kim
 * @author Gokhan Ergul
 * @nojavadoc
 */
public class AnnotationPersistenceMappingSerializer
    extends AnnotationPersistenceMetaDataSerializer {

    private static final int TYPE_RESULTMAP = TYPE_QUERY + 1;

    private List<QueryResultMapping> _results = null;
    private boolean _sync = false;

    private Map<QueryResultMapping, List<AnnotationBuilder>> _rsmAnnos = null;
    
    /**
     * Constructor. Supply configuration.
     */
    public AnnotationPersistenceMappingSerializer(JDBCConfiguration conf) {
        super(conf);
    }

    /**
     * Whether to automatically synchronize mapping info with data available
     * from mapped components before serialization. Defaults to false.
     */
    public boolean getSyncMappingInfo() {
        return _sync;
    }

    /**
     * Whether to automatically synchronize mapping info with data available
     * from mapped components before serialization. Defaults to false.
     */
    public void setSyncMappingInfo(boolean sync) {
        _sync = sync;
    }

    /**
     * Adds the given result set mapping to local cache.
     */
    public void addQueryResultMapping(QueryResultMapping meta) {
        if (_results == null)
            _results = new ArrayList<QueryResultMapping>();
        _results.add(meta);
    }

    /**
     * Removes given result set mapping from the local cache.
     */
    public boolean removeQueryResultMapping(QueryResultMapping meta) {
        return _results != null && _results.remove(meta);
    }

    @Override
    public void addAll(MetaDataRepository repos) {
        super.addAll(repos);
        for (QueryResultMapping res : ((MappingRepository) repos)
            .getQueryResultMappings())
            addQueryResultMapping(res);
    }

    @Override
    public boolean removeAll(MetaDataRepository repos) {
        boolean removed = super.removeAll(repos);
        for (QueryResultMapping res : ((MappingRepository) repos)
            .getQueryResultMappings())
            removed |= removeQueryResultMapping(res);
        return removed;
    }

    @Override
    public void clear() {
        super.clear();
        if (_results != null)
            _results.clear();
    }

    /**
     * Add an annotation builder to list of builders for the specified
     * class metadata.
     */
    protected void addAnnotation(AnnotationBuilder ab, QueryResultMapping meta)
    {
        if (_rsmAnnos == null)
            _rsmAnnos = new HashMap<QueryResultMapping,
                    List<AnnotationBuilder>>();
        List<AnnotationBuilder> list = _rsmAnnos.get(meta);
        if (list == null) {
            list = new ArrayList<AnnotationBuilder>();
            _rsmAnnos.put(meta, list);
        }
        list.add(ab);
    }

    /**
     * Creates an an annotation builder for the specified class metadata
     * and adds it to list of builders.
     */
    protected AnnotationBuilder addAnnotation(
        Class<? extends Annotation> annType, QueryResultMapping meta) {
        AnnotationBuilder ab = newAnnotationBuilder(annType);
        if (meta == null)
            return ab;
        addAnnotation(ab, meta);
        return ab;
    }


    @Override
    protected void serializeClass(ClassMetaData meta) {
        if (_sync && isMappingMode() && meta instanceof ClassMapping) {
            // sync if resolved and mapped
            ClassMapping cls = (ClassMapping) meta;
            if ((cls.getResolve() & MetaDataModes.MODE_MAPPING) != 0 &&
                cls.isMapped()) {
                cls.syncMappingInfo();
                cls.getDiscriminator().syncMappingInfo();
                cls.getVersion().syncMappingInfo();
                FieldMapping[] fields;
                if (cls.getEmbeddingMetaData() == null)
                    fields = cls.getDefinedFieldMappings();
                else
                    fields = cls.getFieldMappings();
                for (FieldMapping f : fields)
                    f.syncMappingInfo();
            }
        }
        super.serializeClass(meta);
    }

    @Override
    protected void serializeClassMappingContent(ClassMetaData mapping) {
        ClassMapping cls = (ClassMapping) mapping;
        ClassMappingInfo info = cls.getMappingInfo();
        AnnotationBuilder abTable = addAnnotation(Table.class, mapping);
        serializeTable(info.getTableName(), Strings
            .getClassName(mapping.getDescribedType()), null,
            info.getUniques(info.getTableName()), abTable);
        serializeColumns(info, ColType.PK_JOIN, null, abTable, cls);
        for (String second : info.getSecondaryTableNames()) {
            AnnotationBuilder abSecTable =
                addAnnotation(SecondaryTable.class, mapping);
            serializeTable(second, null, info, info.getUniques(second),
                    abSecTable);
        }
    }

    @Override
    protected void serializeInheritanceContent(ClassMetaData mapping) {
        ClassMapping cls = (ClassMapping) mapping;
        ClassMappingInfo info = cls.getMappingInfo();
        DiscriminatorMappingInfo dinfo = cls.getDiscriminator()
            .getMappingInfo();
        String strat = info.getHierarchyStrategy();
        if (null == strat)
            return;
        String itypecls = Strings.getClassName(InheritanceType.class);
        AnnotationBuilder abInheritance =
            addAnnotation(Inheritance.class, mapping);
        if (FlatClassStrategy.ALIAS.equals(strat))
            abInheritance.add("strategy", itypecls + ".SINGLE_TABLE");
        else if (VerticalClassStrategy.ALIAS.equals(strat))
            abInheritance.add("strategy", itypecls + ".JOINED");
        else if (FullClassStrategy.ALIAS.equals(strat))
            abInheritance.add("strategy", itypecls + ".TABLE_PER_CLASS");
        if (dinfo.getValue() != null) {
            AnnotationBuilder abDiscVal =
                addAnnotation(DiscriminatorValue.class, mapping);
            abDiscVal.add(null, dinfo.getValue());
        }
        AnnotationBuilder abDiscCol =
            addAnnotation(DiscriminatorColumn.class, mapping);
        serializeColumns(dinfo, ColType.DISC, null, abDiscCol, null);
    }

    /**
     * Serialize table optionally listing primary-key-joins stored
     * in the given {@link org.apache.openjpa.jdbc.meta.ClassMappingInfo}.
     */
    private void serializeTable(String table, String defaultName,
        ClassMappingInfo secondaryInfo, Unique[] uniques,
        AnnotationBuilder ab) {
        List<Column> cols = null;
        if (secondaryInfo != null)
            cols = (List<Column>) secondaryInfo.getSecondaryTableJoinColumns
                (table);

        boolean print = (cols != null && cols.size() > 0) ||
            (uniques != null && uniques.length > 0);
        if (table != null
            && (defaultName == null || !defaultName.equals(table))) {
            print = true;
            int index = table.indexOf('.');
            if (index < 0)
                ab.add("name", table);
            else {
                Map<String, ClassMetaData> classMetaData = getClassMetaData();
                Object[] keySet = null; 
                if(classMetaData != null)
                {
                    keySet = classMetaData.keySet().toArray();
                }
                if((keySet != null) && (keySet.length > 0) && classMetaData.get(keySet[0]).getUseSchemaElement())
                {
                    ab.add("schema", table.substring(0, index));
                }
                ab.add("name", table.substring(index + 1));
            }
        }
        if (print) {
            if (cols != null) {
                for (Column col : cols)
                    serializeColumn(col, ColType.PK_JOIN,
                        null, false, ab, null);
            }
            if (uniques != null) {
                for (Unique unique: uniques) {
                    AnnotationBuilder abUniqueConst =
                        newAnnotationBuilder(UniqueConstraint.class);
                    serializeUniqueConstraint(unique, abUniqueConst);
                    ab.add("uniqueConstraints", abUniqueConst);
                }
            }
        }
    }

    @Override
    protected boolean serializeAttributeOverride(FieldMetaData fmd,
        FieldMetaData orig) {
        if (orig == null || fmd == orig)
            return false;

        FieldMapping field = (FieldMapping) fmd;
        FieldMapping field2 = (FieldMapping) orig;
        if (field.getMappingInfo().hasSchemaComponents()
            || field2.getMappingInfo().hasSchemaComponents())
            return true;

        ValueMappingInfo info = field.getValueInfo();
        List<Column> cols = (List<Column>) info.getColumns();
        if (cols == null || cols.size() == 0)
            return false;
        ValueMappingInfo info2 = field2.getValueInfo();
        List<Column> cols2 = (List<Column>) info2.getColumns();
        if (cols2 == null || cols2.size() != cols.size())
            return true;
        if (cols.size() != 1)
            return true;

        Column col;
        Column col2;
        for (int i = 0; i < cols.size(); i++) {
            col = cols.get(i);
            col2 = cols2.get(i);
            if (!StringUtils.equals(col.getName(), col2.getName()))
                return true;
            if (!StringUtils.equals(col.getTypeName(), col2.getTypeName()))
                return true;
            if (col.getSize() != col2.getSize())
                return true;
            if (col.getDecimalDigits() != col2.getDecimalDigits())
                return true;
            if (col.getFlag(Column.FLAG_UNINSERTABLE)
                != col2.getFlag(Column.FLAG_UNINSERTABLE))
                return true;
            if (col.getFlag(Column.FLAG_UNUPDATABLE)
                != col2.getFlag(Column.FLAG_UNUPDATABLE))
                return true;
        }
        return false;
    }

    @Override
    protected void serializeAttributeOverrideMappingContent(FieldMetaData fmd,
        FieldMetaData orig, AnnotationBuilder ab) {
        FieldMapping fm = (FieldMapping) fmd;
        serializeColumns(fm.getValueInfo(), ColType.COL, fm.getMappingInfo()
            .getTableName(), ab, fmd);
    }

    @Override
    protected PersistenceStrategy getStrategy(FieldMetaData fmd) {
        PersistenceStrategy strat = super.getStrategy(fmd);
        FieldMapping field = (FieldMapping) fmd;
        switch (strat) {
            case MANY_MANY:
                // we can differentiate a one-many by the fact that there is no
                // secondary table join, or the fk is unique
                if (field.getMappedBy() == null
                    && (field.getMappingInfo().getJoinDirection()
                    == MappingInfo.JOIN_NONE
                    || field.getElementMapping().getValueInfo().getUnique()
                    != null))
                    return PersistenceStrategy.ONE_MANY;
                break;
            case MANY_ONE:
                // inverse join cols or unique fk?
                if (field.getValueInfo().getJoinDirection()
                    == MappingInfo.JOIN_INVERSE
                    || field.getValueInfo().getUnique() != null)
                    return PersistenceStrategy.ONE_ONE;

                // scan for primary-key-join-column
                List<Column> cols = field.getValueInfo().getColumns();
                boolean pkJoin = cols != null && cols.size() > 0;
                for (int i = 0; pkJoin && i < cols.size(); i++)
                    pkJoin = cols.get(i).getFlag(Column.FLAG_PK_JOIN);
                if (pkJoin)
                    return PersistenceStrategy.ONE_ONE;
                break;
        }
        return strat;
    }

    @Override
    protected void serializeFieldMappingContent(FieldMetaData fmd,
        PersistenceStrategy strategy, AnnotationBuilder ab) {
        if (fmd.getMappedBy() != null)
            return;

        // while I'd like to do auto detection based on join directions, etc.
        // the distinguished column / table / etc names forces our hand
        // esp for OpenJPA custom mappings.
        FieldMapping field = (FieldMapping) fmd;
        switch (strategy) {
            case ONE_ONE:
            case MANY_ONE:
                serializeColumns(field.getValueInfo(), ColType.JOIN,
                    field.getMappingInfo().getTableName(), null, fmd);
                return;
            case ONE_MANY:
                if (field.getMappingInfo().getJoinDirection() ==
                    MappingInfo.JOIN_NONE) {
                    serializeColumns(field.getElementMapping().getValueInfo(),
                        ColType.JOIN, null, null, fmd);
                    return;
                }
                // else no break
            case MANY_MANY:
                if (field.getMappingInfo().hasSchemaComponents()
                    || field.getElementMapping().getValueInfo()
                    .hasSchemaComponents()) {
                    AnnotationBuilder abJoinTbl =
                        addAnnotation(JoinTable.class, fmd);
                    String table = field.getMappingInfo().getTableName();
                    if (table != null) {
                        int index = table.indexOf('.');
                        if (index < 0)
                            abJoinTbl.add("name", table);
                        else {
                            abJoinTbl.add("schema", table.substring(0, index));
                            abJoinTbl.add("name", table.substring(index + 1));
                        }
                    }
                    serializeColumns(field.getMappingInfo(),
                        ColType.JOIN, null, abJoinTbl, null);
                    serializeColumns(field.getElementMapping().getValueInfo(),
                        ColType.INVERSE, null, abJoinTbl, null);
                }
                return;
        }

        serializeColumns(field.getValueInfo(), ColType.COL,
            field.getMappingInfo().getTableName(), null, fmd);
        if (strategy == PersistenceStrategy.BASIC && isLob(field)) {
            addAnnotation(Lob.class, fmd);
        }
        TemporalType temporal = getTemporal(field);
        if (temporal != null) {
            addAnnotation(Temporal.class, fmd).
                add(null, temporal);
        }

        EnumType enumType = getEnumType(field);
        if (enumType != null && enumType != EnumType.ORDINAL) {
            addAnnotation(Enumerated.class, fmd).
                add(null, enumType);
        }
    }

    /**
     * Determine if the field is a lob.
     */
    private boolean isLob(FieldMapping field) {
        for (Column col : (List<Column>) field.getValueInfo().getColumns())
            if (col.getType() == Types.BLOB || col.getType() == Types.CLOB)
                return true;
        return false;
    }

    /**
     * Return field's temporal type.
     */
    private TemporalType getTemporal(FieldMapping field) {
        if (field.getDeclaredTypeCode() != JavaTypes.DATE
            && field.getDeclaredTypeCode() != JavaTypes.CALENDAR)
            return null;

        DBDictionary dict = ((JDBCConfiguration) getConfiguration())
            .getDBDictionaryInstance();
        int def = dict.getJDBCType(field.getTypeCode(), false);
        for (Column col : (List<Column>) field.getValueInfo().getColumns()) {
            if (col.getType() == def)
                continue;
            switch (col.getType()) {
                case Types.DATE:
                    return TemporalType.DATE;
                case Types.TIME:
                    return TemporalType.TIME;
                case Types.TIMESTAMP:
                    return TemporalType.TIMESTAMP;
            }
        }
        return null;
    }

    /**
     * Return enum type for the field.
     */
    protected EnumType getEnumType(FieldMapping field) {
        if (field.getDeclaredTypeCode() != JavaTypes.OBJECT)
            return null;
        if (!(field.getHandler() instanceof EnumValueHandler))
            return null;
        return ((EnumValueHandler) field.getHandler()).getStoreOrdinal()
            ? EnumType.ORDINAL : EnumType.STRING;
    }

    /**
     * Serialize the columns in the given mapping info.
     */
    private void serializeColumns(MappingInfo info, ColType type,
        String secondary, AnnotationBuilder ab, Object meta) {
        List<Column> cols = (List<Column>) info.getColumns();
        if (cols == null)
            return;
        AnnotationBuilder abContainer = ab;
        if (cols.size() > 1) {
            Class grpType = type.getColumnGroupAnnotationType();
            if (null != grpType) {
                AnnotationBuilder abGrp = newAnnotationBuilder(grpType);
                if (null == ab)
                    addAnnotation(abGrp, meta);
                else
                    ab.add(null, abGrp);
                abContainer = abGrp;
            }
        }
        for (Column col : cols)
            serializeColumn(col, type, secondary,
                info.getUnique() != null, abContainer, meta);
    }

    /**
     * Serialize a single column.
     */
    private void serializeColumn(Column col, ColType type, String secondary,
        boolean unique, AnnotationBuilder ab, Object meta) {
        FieldMetaData fmd = meta instanceof FieldMetaData ?
            (FieldMetaData) meta : null;
        AnnotationBuilder abCol = newAnnotationBuilder(
            type.getColumnAnnotationType());
        if (col.getName() != null && (null == fmd ||
            !col.getName().equalsIgnoreCase(fmd.getName())))
            abCol.add("name", col.getName());
        if (col.getTypeName() != null)
            abCol.add("columnDefinition", col.getTypeName());
        if (col.getTarget() != null
            && (type == ColType.JOIN || type == ColType.INVERSE
            || type == ColType.PK_JOIN))
            abCol.add("referencedColumnName", col.getTarget());
        if (type == ColType.COL || type == ColType.JOIN
            || type == ColType.PK_JOIN) {
            if (unique)
                abCol.add("unique", true);
            if (col.isNotNull())
                abCol.add("nullable", false);
            if (col.getFlag(Column.FLAG_UNINSERTABLE))
                abCol.add("insertable", false);
            if (col.getFlag(Column.FLAG_UNUPDATABLE))
                abCol.add("updatable", false);
            if (secondary != null)
                abCol.add("table", secondary);

            if (type == ColType.COL) {
                if (col.getSize() > 0 && col.getSize() != 255)
                    abCol.add("length", col.getSize());
                if (col.getDecimalDigits() != 0)
                    abCol.add("scale", col.getDecimalDigits());
            }
        }

        if (type != ColType.COL || abCol.hasComponents()) {
            if (null != ab) {
                String key = null;
                if (ab.getType() == JoinTable.class) {
                    switch(type) {
                        case JOIN:
                            key = "joinColumns";
                            break;
                        case INVERSE:
                            key = "inverseJoinColumns";
                            break;
                    }
                }
                ab.add(key, abCol);
            } else {
                addAnnotation(abCol, meta);
            }                
        }
    }

    private void serializeUniqueConstraint(Unique unique,
        AnnotationBuilder ab) {
        StringBuilder sb = new StringBuilder();
        Column[] columns = unique.getColumns();
        for (Column column:columns) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(column.getName());
        }
        if (columns.length > 1)
            sb.insert(0, "{").append("}");
        ab.add("columnNames", sb.toString());
        if (StringUtils.isNotEmpty(unique.getName())) {
            ab.add("name", unique.getName());
        }
    }

    @Override
    protected SerializationComparator newSerializationComparator() {
        return new AnnotationPersistenceMappingSerializer.
            MappingSerializationComparator();
    }

    @Override
    protected void addSystemMappingElements(Collection toSerialize) {
        if (isQueryMode())
            toSerialize.addAll(getQueryResultMappings(null));
    }

    @Override
    protected int type(Object o) {
        int type = super.type(o);
        if (type == -1 && o instanceof QueryResultMapping)
            return TYPE_RESULTMAP;
        return type;
    }

    /**
     * Return the result set mappings for the given scope.
     */
    private List<QueryResultMapping> getQueryResultMappings(ClassMetaData cm) {
        if (_results == null || _results.isEmpty())
            return (List<QueryResultMapping>) Collections.EMPTY_LIST;

        List<QueryResultMapping> result = null;
        for (int i = 0; i < _results.size(); i++) {
            QueryResultMapping element = _results.get(i);
            if ((cm == null && element.getSourceScope() != null) || (cm != null
                && element.getSourceScope() != cm.getDescribedType()))
                continue;

            if (result == null)
                result = new ArrayList<QueryResultMapping>(_results.size() - i);
            result.add(element);
        }
        return (result == null)
            ? (List<QueryResultMapping>) Collections.EMPTY_LIST : result;
    }

    @Override
    protected void serializeSystemMappingElement(Object obj) {
        if (obj instanceof QueryResultMapping)
            serializeQueryResultMapping((QueryResultMapping) obj, null);
    }

    @Override
    protected void serializeQueryMappings(ClassMetaData meta) {
        for (QueryResultMapping res : getQueryResultMappings(meta))
            serializeQueryResultMapping(res, meta);
    }

    /**
     * Serialize given result set mapping.
     */
    private void serializeQueryResultMapping(QueryResultMapping meta,
        ClassMetaData clsmeta) {
        AnnotationBuilder ab = addAnnotation(SqlResultSetMapping.class, meta);
        if (null != clsmeta)
            addAnnotation(ab, clsmeta);
        ab.add("name", meta.getName());
        for (QueryResultMapping.PCResult pc : meta.getPCResults()) {
            AnnotationBuilder abEntRes =
                newAnnotationBuilder(EntityResult.class);
            ab.add("entities", abEntRes);
            abEntRes.add("entityClass", pc.getCandidateType());
            Object discrim = pc.getMapping(pc.DISCRIMINATOR);
            if (discrim != null)
                abEntRes.add("discriminatorColumn", discrim.toString());

            for (String path : pc.getMappingPaths()) {
                AnnotationBuilder abFldRes =
                    newAnnotationBuilder(FieldResult.class);
                abEntRes.add("fields", abFldRes);
                abFldRes.add("name", path);
                abFldRes.add("column", pc.getMapping(path).toString());
            }
        }
        for (Object col : meta.getColumnResults()) {
            AnnotationBuilder abColRes =
                newAnnotationBuilder(ColumnResult.class);
            abColRes.add("name", col.toString());
        }
    }

    @Override
    protected void serializeSequence(SequenceMetaData meta) {
        if (SequenceMapping.IMPL_VALUE_TABLE.equals(meta.getSequencePlugin())) {
            super.serializeSequence(meta);
            return;
        }

        AnnotationBuilder abTblGen = addAnnotation(TableGenerator.class, meta);
        SequenceMapping seq = (SequenceMapping) meta;
        abTblGen.add("name", seq.getName());
        String table = seq.getTable();
        if (table != null) {
            int dotIdx = table.indexOf('.');
            if (dotIdx == -1)
                abTblGen.add("table", table);
            else {
                abTblGen.add("table", table.substring(dotIdx + 1));
                abTblGen.add("schema", table.substring(0, dotIdx));
            }
        }
        if (!StringUtils.isEmpty(seq.getPrimaryKeyColumn()))
            abTblGen.add("pkColumnName", seq.getPrimaryKeyColumn());
        if (!StringUtils.isEmpty(seq.getSequenceColumn()))
            abTblGen.add("valueColumnName", seq.getSequenceColumn());
        if (!StringUtils.isEmpty(seq.getPrimaryKeyValue()))
            abTblGen.add("pkColumnValue", seq.getPrimaryKeyValue());
        if (seq.getAllocate() != 50 && seq.getAllocate() != -1)
            abTblGen.add("allocationSize", seq.getAllocate() + "");
        if (seq.getInitialValue() != 0 && seq.getInitialValue() != -1)
            abTblGen.add("initialValue", seq.getInitialValue() + "");
    }

    /**
     * Column types serialized under different names.
     */
    private static enum ColType {

        COL,
        JOIN,
        INVERSE,
        PK_JOIN,
        DISC;

        private Class<? extends Annotation> getColumnAnnotationType() {
            switch(this) {
                case COL:
                    return javax.persistence.Column.class;
                case JOIN:
                case INVERSE:
                    return JoinColumn.class;
                case PK_JOIN:
                    return PrimaryKeyJoinColumn.class;
                case DISC:
                    return DiscriminatorColumn.class;
            }
            return null;
        }

        private Class<? extends Annotation> getColumnGroupAnnotationType() {
            switch(this) {
                case JOIN:
                case INVERSE:
                    return JoinColumns.class;
                case PK_JOIN:
                    return PrimaryKeyJoinColumns.class;
            }
            return null;
        }

    }

    /**
     * Extends {@link SerializationComparator} for store-specific tags such
     * as &lt;sql-result-set-mapping&gt;.
     *
     * @author Pinaki Poddar
     */
    protected class MappingSerializationComparator
        extends SerializationComparator {

        protected int compareUnknown(Object o1, Object o2) {
            if (!(o1 instanceof QueryResultMapping))
                return super.compareUnknown(o1, o2);

            QueryResultMapping res1 = (QueryResultMapping) o1;
            QueryResultMapping res2 = (QueryResultMapping) o2;

            // system scope before class scope
            Object scope1 = res1.getSourceScope();
            Object scope2 = res2.getSourceScope();
            if (scope1 == null && scope2 != null)
                return -1;
            if (scope1 != null && scope2 == null)
                return 1;

            // compare on listing index, or if none/same, use name
            int listingIndex1 = res1.getListingIndex();
            int listingIndex2 = res2.getListingIndex();
            if (listingIndex1 != listingIndex2)
                return listingIndex1 - listingIndex2;
            return res1.getName ().compareTo (res2.getName ());
		}
	}
}
