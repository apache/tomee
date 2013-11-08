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

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EnumType;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.ClassMappingInfo;
import org.apache.openjpa.jdbc.meta.DiscriminatorMappingInfo;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.MappingInfo;
import org.apache.openjpa.jdbc.meta.MappingRepository;
import org.apache.openjpa.jdbc.meta.QueryResultMapping;
import org.apache.openjpa.jdbc.meta.SequenceMapping;
import org.apache.openjpa.jdbc.meta.ValueMappingImpl;
import org.apache.openjpa.jdbc.meta.ValueMappingInfo;
import org.apache.openjpa.jdbc.meta.strats.EnumValueHandler;
import org.apache.openjpa.jdbc.meta.strats.FlatClassStrategy;
import org.apache.openjpa.jdbc.meta.strats.FullClassStrategy;
import org.apache.openjpa.jdbc.meta.strats.VerticalClassStrategy;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Unique;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import static org.apache.openjpa.meta.MetaDataModes.MODE_MAPPING;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.meta.SequenceMetaData;
import org.apache.openjpa.persistence.PersistenceStrategy;
import org.apache.openjpa.persistence.XMLPersistenceMetaDataSerializer;
import serp.util.Strings;

/**
 * Serializes persistence mapping to XML.
 *
 * @since 0.4.0
 * @author Steve Kim
 * @nojavadoc
 */
public class XMLPersistenceMappingSerializer
    extends XMLPersistenceMetaDataSerializer {

    private static final int TYPE_RESULTMAP = TYPE_QUERY + 1;

    private static final Map<ColType, String> _names;

    static {
        _names = new EnumMap<ColType, String>(ColType.class);
        _names.put(ColType.COL, "column");
        _names.put(ColType.JOIN, "join-column");
        _names.put(ColType.INVERSE, "inverse-join-column");
        _names.put(ColType.PK_JOIN, "primary-key-join-column");
        _names.put(ColType.DISC, "discriminator-column");
    }

    private List<QueryResultMapping> _results = null;
    private boolean _sync = false;

    /**
     * Constructor. Supply configuration.
     */
    public XMLPersistenceMappingSerializer(JDBCConfiguration conf) {
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

    protected void addCommments(Object obj)
        throws SAXException {
        if (isMappingMode() && !isMetaDataMode()) {
            if (obj instanceof ClassMapping)
                obj = ((ClassMapping) obj).getMappingInfo();
            else if (obj instanceof FieldMapping)
                obj = ((FieldMapping) obj).getMappingInfo();
        }
        super.addComments(obj);
    }

    @Override
    protected void serializeClass(ClassMetaData meta, boolean access)
        throws SAXException {
        if (_sync && isMappingMode() && meta instanceof ClassMapping) {
            // sync if resolved and mapped
            ClassMapping cls = (ClassMapping) meta;
            if ((cls.getResolve() & MODE_MAPPING) != 0 && cls.isMapped()) {
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
        super.serializeClass(meta, access);
    }

    @Override
    protected void serializeClassMappingContent(ClassMetaData mapping)
        throws SAXException {
        ClassMapping cls = (ClassMapping) mapping;
        ClassMappingInfo info = cls.getMappingInfo();
        serializeTable(info.getTableName(), "table", Strings
            .getClassName(mapping.getDescribedType()), null, 
            info.getUniques(info.getTableName()));
        for (String second : info.getSecondaryTableNames())
            serializeTable(second, "secondary-table", null, info,
                    info.getUniques(second));
        serializeColumns(info, ColType.PK_JOIN, null);
    }

    @Override
    protected void serializeInheritanceContent(ClassMetaData mapping)
        throws SAXException {
        ClassMapping cls = (ClassMapping) mapping;
        ClassMappingInfo info = cls.getMappingInfo();
        DiscriminatorMappingInfo dinfo = cls.getDiscriminator()
            .getMappingInfo();
        String strat = info.getHierarchyStrategy();
        if (FlatClassStrategy.ALIAS.equals(strat))
            addAttribute("strategy", "SINGLE_TABLE");
        else if (VerticalClassStrategy.ALIAS.equals(strat))
            addAttribute("strategy", "JOINED");
        else if (FullClassStrategy.ALIAS.equals(strat))
            addAttribute("strategy", "TABLE_PER_CLASS");
        if (strat != null) {
            startElement("inheritance");
            endElement("inheritance");
        }
        if (dinfo.getValue() != null) {
            startElement("discriminator-value");
            addText(dinfo.getValue());
            endElement("discriminator-value");
        }
        serializeColumns(dinfo, ColType.DISC, null);
    }

    /**
     * Serialize table optionally listing primary-key-joins stored
     * in the given {@link ClassMappingInfo}.
     */
    private void serializeTable(String table, String elementName,
        String defaultName, ClassMappingInfo secondaryInfo, Unique[] uniques)
        throws SAXException {
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
                addAttribute("name", table);
            else {
                Map<String, ClassMetaData> classMetaData = getClassMetaData();
                Object[] keySet = null; 
                if(classMetaData != null)
                {
                    keySet = classMetaData.keySet().toArray();
                }
                if((keySet != null) && (keySet.length > 0) && classMetaData.get(keySet[0]).getUseSchemaElement())
                {
                    addAttribute("schema", table.substring(0, index));
                }
                addAttribute("name", table.substring(index + 1));
            }
        }
        if (print) {
            startElement(elementName);
            if (cols != null) {
                for (Column col : cols)
                    serializeColumn(col, ColType.PK_JOIN, null, false);
            }
            if (uniques != null)
                for (Unique unique: uniques)
                    serializeUniqueConstraint(unique);
            endElement(elementName);
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
        FieldMetaData orig)
        throws SAXException {
        FieldMapping fm = (FieldMapping) fmd;
        serializeColumns(fm.getValueInfo(), ColType.COL, fm.getMappingInfo()
            .getTableName());
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
        PersistenceStrategy strategy)
        throws SAXException {
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
                    field.getMappingInfo().getTableName());
                return;
            case ONE_MANY:
                if (field.getMappingInfo().getJoinDirection() ==
                    MappingInfo.JOIN_NONE) {
                    serializeColumns(field.getElementMapping().getValueInfo(),
                        ColType.JOIN, null);
                    return;
                }
                // else no break
            case MANY_MANY:
                if (field.getMappingInfo().hasSchemaComponents()
                    || field.getElementMapping().getValueInfo()
                    .hasSchemaComponents()) {
                    String table = field.getMappingInfo().getTableName();
                    if (table != null) {
                        int index = table.indexOf('.');
                        if (index < 0)
                            addAttribute("name", table);
                        else {
                            addAttribute("schema", table.substring(0, index));
                            addAttribute("name", table.substring(index + 1));
                        }
                    }
                    startElement("join-table");
                    serializeColumns(field.getMappingInfo(), ColType.JOIN,
                        null);
                    serializeColumns(field.getElementMapping().getValueInfo(),
                        ColType.INVERSE, null);
                    endElement("join-table");
                }
                return;
            case ELEM_COLL:
                if (field.getMappingInfo().hasSchemaComponents()
                    || field.getElementMapping().getValueInfo()
                    .hasSchemaComponents()) {
                    String table = field.getMappingInfo().getTableName();
                    if (table != null) {
                        int index = table.indexOf('.');
                        if (index < 0)
                            addAttribute("name", table);
                        else {
                            addAttribute("schema", table.substring(0, index));
                            addAttribute("name", table.substring(index + 1));
                        }
                    }
                    startElement("collection-table");
                    ValueMappingImpl elem =
                            (ValueMappingImpl) field.getElement();
                    serializeColumns(elem.getValueInfo(), ColType.COL,
                            null);
                    endElement("collection-table");
                }
                return;
        }

        serializeColumns(field.getValueInfo(), ColType.COL,
            field.getMappingInfo().getTableName());
        if (strategy == PersistenceStrategy.BASIC && isLob(field)) {
            startElement("lob");
            endElement("lob");
        }
        TemporalType temporal = getTemporal(field);
        if (temporal != null) {
            startElement("temporal");
            addText(temporal.toString());
            endElement("temporal");
        }

        EnumType enumType = getEnumType(field);
        if (enumType != null && enumType != EnumType.ORDINAL) {
            startElement("enumerated");
            addText(enumType.toString());
            endElement("enumerated");
        }
    }

    /**
     * Serialize order column.
     */
    protected void serializeOrderColumn(FieldMetaData fmd)
        throws SAXException {
        FieldMapping field = (FieldMapping) fmd;
        Column orderCol = field.getOrderColumn();
        if (orderCol != null) {
            if (orderCol.getName() != null)
                addAttribute("name", orderCol.getName());
            if (orderCol.isNotNull())
                addAttribute("nullable", "false");
            if (orderCol.getFlag(Column.FLAG_UNINSERTABLE))
                addAttribute("insertable", "false");
            if (orderCol.getFlag(Column.FLAG_UNUPDATABLE))
                addAttribute("updatable", "false");
            if (orderCol.getTypeName() != null)
                addAttribute("column-definition", orderCol.getTypeName());
            startElement("order-column");
            endElement("order-column");
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
        String secondary)
        throws SAXException {
        List<Column> cols = (List<Column>) info.getColumns();
        if (cols == null)
            return;
        for (Column col : cols)
            serializeColumn(col, type, secondary, info.getUnique() != null);
    }

    /**
     * Serialize a single column.
     */
    private void serializeColumn(Column col, ColType type, String secondary,
        boolean unique)
        throws SAXException {
        if (col.getName() != null)
            addAttribute("name", col.getName());
        if (col.getTypeName() != null)
            addAttribute("column-definition", col.getTypeName());
        if (col.getTarget() != null
            && (type == ColType.JOIN || type == ColType.INVERSE
            || type == ColType.PK_JOIN))
            addAttribute("referenced-column-name", col.getTarget());
        if (type == ColType.COL || type == ColType.JOIN
            || type == ColType.PK_JOIN) {
            if (unique)
                addAttribute("unique", "true");
            if (col.isNotNull())
                addAttribute("nullable", "false");
            if (col.getFlag(Column.FLAG_UNINSERTABLE))
                addAttribute("insertable", "false");
            if (col.getFlag(Column.FLAG_UNUPDATABLE))
                addAttribute("updatable", "false");
            if (secondary != null)
                addAttribute("table", secondary);

            if (type == ColType.COL) {
                if (col.getSize() > 0 && col.getSize() != 255)
                    addAttribute("length", col.getSize() + "");
                if (col.getDecimalDigits() != 0)
                    addAttribute("scale", col.getDecimalDigits() + "");
            }
        }
        if (type != ColType.COL || getAttributes().getLength() > 0) {
            String name = col.getFlag(Column.FLAG_PK_JOIN) ? _names
                .get(ColType.PK_JOIN) : _names.get(type);
            startElement(name);
            endElement(name);
        }
    }

    private void serializeUniqueConstraint(Unique unique) throws SAXException {
        if (StringUtils.isNotEmpty(unique.getName())) {
            addAttribute("name", unique.getName());
        }
        startElement("unique-constraint");
        Column[] columns = unique.getColumns();
        for (Column column:columns) {
            startElement("column-name");
            addText(column.getName());
            endElement("column-name");
        }
        endElement("unique-constraint");
    }
    
    @Override
    protected SerializationComparator newSerializationComparator() {
        return new MappingSerializationComparator();
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
    protected void serializeSystemMappingElement(Object obj)
        throws SAXException {
        if (obj instanceof QueryResultMapping)
            serializeQueryResultMapping((QueryResultMapping) obj);
    }

    @Override
    protected void serializeQueryMappings(ClassMetaData meta)
        throws SAXException {
        for (QueryResultMapping res : getQueryResultMappings(meta))
            serializeQueryResultMapping(res);
    }

    /**
     * Serialize given result set mapping.
     */
    private void serializeQueryResultMapping(QueryResultMapping meta)
        throws SAXException {
        if (!getSerializeAnnotations()
            && meta.getSourceType() == meta.SRC_ANNOTATIONS)
            return;

        addAttribute("name", meta.getName());
        startElement("sql-result-set-mapping");
        for (QueryResultMapping.PCResult pc : meta.getPCResults()) {
            addAttribute("entity-class", pc.getCandidateType().getName());
            Object discrim = pc.getMapping(pc.DISCRIMINATOR);
            if (discrim != null)
                addAttribute("discriminator-column", discrim.toString());

            startElement("entity-result");
            for (String path : pc.getMappingPaths()) {
                addAttribute("name", path);
                addAttribute("column", pc.getMapping(path).toString());
                startElement("field-result");
                endElement("field-result");
            }
            endElement("entity-result");
        }
        for (Object col : meta.getColumnResults()) {
            addAttribute("name", col.toString());
            startElement("column-result");
            endElement("column-result");
        }
        endElement("sql-result-set-mapping");
    }

    @Override
    protected void serializeSequence(SequenceMetaData meta)
        throws SAXException {
        if (!getSerializeAnnotations()
            && meta.getSourceType() == meta.SRC_ANNOTATIONS)
            return;
        if (SequenceMapping.IMPL_VALUE_TABLE.equals(meta.getSequencePlugin())) {
            super.serializeSequence(meta);
            return;
        }

        SequenceMapping seq = (SequenceMapping) meta;
        addAttribute("name", seq.getName());
        String table = seq.getTable();
        if (table != null) {
            int dotIdx = table.indexOf('.');
            if (dotIdx == -1)
                addAttribute("table", table);
            else {
                addAttribute("table", table.substring(dotIdx + 1));
                addAttribute("schema", table.substring(0, dotIdx));
            }
        }
        if (!StringUtils.isEmpty(seq.getPrimaryKeyColumn()))
            addAttribute("pk-column-name", seq.getPrimaryKeyColumn());
        if (!StringUtils.isEmpty(seq.getSequenceColumn()))
            addAttribute("value-column-name", seq.getSequenceColumn());
        if (!StringUtils.isEmpty(seq.getPrimaryKeyValue()))
            addAttribute("pk-column-value", seq.getPrimaryKeyValue());
        if (seq.getAllocate() != 50 && seq.getAllocate() != -1)
            addAttribute("allocation-size", seq.getAllocate() + "");
        if (seq.getInitialValue() != 0 && seq.getInitialValue() != -1)
            addAttribute("initial-value", seq.getInitialValue() + "");
        startElement("table-generator");
        endElement("table-generator");
    }

    /**
     * Column types serialized under different names.
     */
    private static enum ColType {

        COL,
        JOIN,
        INVERSE,
        PK_JOIN,
        DISC
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
