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

import java.lang.reflect.Modifier;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.DiscriminatorType;
import javax.persistence.EnumType;
import javax.persistence.InheritanceType;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.QualifiedDBIdentifier;
import org.apache.openjpa.jdbc.kernel.EagerFetchModes;
import org.apache.openjpa.jdbc.meta.ClassMapping;
import org.apache.openjpa.jdbc.meta.ClassMappingInfo;
import org.apache.openjpa.jdbc.meta.DiscriminatorMappingInfo;
import org.apache.openjpa.jdbc.meta.FieldMapping;
import org.apache.openjpa.jdbc.meta.FieldMappingInfo;
import org.apache.openjpa.jdbc.meta.MappingInfo;
import org.apache.openjpa.jdbc.meta.MappingRepository;
import org.apache.openjpa.jdbc.meta.QueryResultMapping;
import org.apache.openjpa.jdbc.meta.SequenceMapping;
import org.apache.openjpa.jdbc.meta.QueryResultMapping.PCResult;
import org.apache.openjpa.jdbc.meta.strats.EnumValueHandler;
import org.apache.openjpa.jdbc.meta.strats.FlatClassStrategy;
import org.apache.openjpa.jdbc.meta.strats.FullClassStrategy;
import org.apache.openjpa.jdbc.meta.strats.NoneClassStrategy;
import org.apache.openjpa.jdbc.meta.strats.VerticalClassStrategy;
import org.apache.openjpa.jdbc.schema.Column;
import org.apache.openjpa.jdbc.schema.Unique;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.meta.SourceTracker;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.persistence.XMLPersistenceMetaDataParser;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.MetaDataException;
import org.apache.openjpa.util.UserException;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import static org.apache.openjpa.persistence.jdbc.MappingTag.*;
/**
 * Custom SAX parser used by the system to parse persistence mapping files.
 *
 * @author Steve Kim
 * @nojavadoc
 */
public class XMLPersistenceMappingParser
    extends XMLPersistenceMetaDataParser {

    private static final Map<String, MappingTag> _elems =
        new HashMap<String, MappingTag>();

    static {
        _elems.put("association-override", ASSOC_OVERRIDE);
        _elems.put("attribute-override", ATTR_OVERRIDE);
        _elems.put("collection-table", COLLECTION_TABLE);
        _elems.put("column", COL);
        _elems.put("columns", COLS);
        _elems.put("column-name", COLUMN_NAME);
        _elems.put("column-result", COLUMN_RESULT);
        _elems.put("data-store-id-column", DATASTORE_ID_COL);
        _elems.put("delimited-identifiers", DELIMITED_IDS);
        _elems.put("discriminator-column", DISCRIM_COL);
        _elems.put("discriminator-value", DISCRIM_VAL);
        _elems.put("entity-result", ENTITY_RESULT);
        _elems.put("enumerated", ENUMERATED);
        _elems.put("field-result", FIELD_RESULT);
        _elems.put("foreign-key", FK);
        _elems.put("fk_column-names", FK_COL_NAMES);
        _elems.put("fk_column_name", FK_COL_NAME);
        _elems.put("inheritance", INHERITANCE);
        _elems.put("index", INDEX);
        _elems.put("join-column", JOIN_COL);
        _elems.put("inverse-join-column", COL);
        _elems.put("join-table", JOIN_TABLE);
        _elems.put("map-key-enumerated", MAP_KEY_ENUMERATED);
        _elems.put("map-key-column", MAP_KEY_COL);
        _elems.put("map-key-join-column", MAP_KEY_JOIN_COL);
        _elems.put("map-key-temporal", MAP_KEY_TEMPORAL);
        _elems.put("name", NAME);
        _elems.put("order-column", ORDER_COLUMN);
        _elems.put("primary-key-join-column", PK_JOIN_COL);
        _elems.put("secondary-table", SECONDARY_TABLE);
        _elems.put("sql-result-set-mapping", SQL_RESULT_SET_MAPPING);
        _elems.put("table", TABLE);
        _elems.put("table-generator", TABLE_GEN);
        _elems.put("temporal", TEMPORAL);
        _elems.put("unique-constraint", UNIQUE);
        _elems.put("version-columns", VERSION_COLS);
        _elems.put("version-column", VERSION_COL);
    }

    private static final Localizer _loc = Localizer.forPackage
        (XMLPersistenceMappingParser.class);

    private String _override = null;
    private String _schema = null;
    private String _colTable = null;
    private String _secondaryTable = null;
    private List<Column> _cols = null;
    private List<Column> _joinCols = null;
    private List<Column> _supJoinCols = null;
    private boolean _lob = false;
    private TemporalType _temporal = null;
    private EnumSet<UniqueFlag> _unique = EnumSet.noneOf(UniqueFlag.class);
    private DiscriminatorType _discType;
    private Column _discCol;
    private int _resultIdx = 0;
    private final DBDictionary _dict;
    
    // ForeignKey info
    private Attributes _foreignKeyAttributes = null;
    private List<String> _columnNamesList = null;
    private String[] _columnNames = {};
    
    private List<Column> _versionColumnsList = null;

    private final Map<Class<?>, ArrayList<DeferredEmbeddableOverrides>> 
        _deferredMappings = new HashMap<Class<?>, 
             ArrayList<DeferredEmbeddableOverrides>>();
    
    /**
     * Constructor; supply configuration.
     */
    public XMLPersistenceMappingParser(JDBCConfiguration conf) {
        super(conf);
        _dict = conf.getDBDictionaryInstance();
    }

    @Override
    protected void reset() {
        super.reset();
        clearColumnInfo();
        clearClassInfo();
        clearSecondaryTableInfo();
        _override = null;
        _schema = null;
        _resultIdx = 0;
    }

    @Override
    protected Object startSystemMappingElement(String name, Attributes attrs)
        throws SAXException {
        MappingTag tag = _elems.get(name);
        if (tag == null) {
            if ("schema".equals(name))
                return name;
            return null;
        }

        boolean ret;
        switch (tag) {
            case TABLE_GEN:
                ret = startTableGenerator(attrs);
                break;
            case SQL_RESULT_SET_MAPPING:
                ret = startSQLResultSetMapping(attrs);
                break;
            case ENTITY_RESULT:
                ret = startEntityResult(attrs);
                break;
            case FIELD_RESULT:
                ret = startFieldResult(attrs);
                break;
            case COLUMN_RESULT:
                ret = startColumnResult(attrs);
                break;
            default:
                ret = false;
        }
        return (ret) ? tag : null;
    }

    @Override
    protected void endSystemMappingElement(String name)
        throws SAXException {
        MappingTag tag = _elems.get(name);
        if (tag == null) {
            if ("schema".equals(name))
                _schema = currentText();
            return;
        }

        switch (tag) {
            case SQL_RESULT_SET_MAPPING:
                endSQLResultSetMapping();
                break;
            case ENTITY_RESULT:
                endEntityResult();
                break;
        }
    }

    @Override
    protected Object startClassMappingElement(String name, Attributes attrs)
        throws SAXException {
        MappingTag tag = _elems.get(name);
        if (tag == null)
            return null;

        boolean ret;
        switch (tag) {
            case TABLE:
                ret = startTable(attrs);
                break;
            case SECONDARY_TABLE:
                ret = startSecondaryTable(attrs);
                break;
            case DISCRIM_COL:
                parseDiscriminatorColumn(attrs);
                _discCol = parseColumn(attrs);
                ret = true;
                break;
            case DISCRIM_VAL:
                ret = true;
                break;
            case INHERITANCE:
                ret = startInheritance(attrs);
                break;
            case ASSOC_OVERRIDE:
            case ATTR_OVERRIDE:
                ret = startAttributeOverride(attrs);
                break;
            case PK_JOIN_COL:
                ret = startPrimaryKeyJoinColumn(attrs);
                break;
            case COL:
                ret = startColumn(attrs);
                break;
            case COLS:
                ret = true;
                break;
            case JOIN_COL:
                ret = startJoinColumn(attrs);
                break;
            case JOIN_TABLE:
                ret = startJoinTable(attrs);
                break;
            case TABLE_GEN:
                ret = startTableGenerator(attrs);
                break;
            case UNIQUE:
                ret = startUniqueConstraint(attrs);
                break;
            case NAME:
                ret = true;
                break;
            case TEMPORAL:
            case ENUMERATED:
            case MAP_KEY_ENUMERATED:
            case MAP_KEY_TEMPORAL:
                ret = true;
                break;
            case SQL_RESULT_SET_MAPPING:
                ret = startSQLResultSetMapping(attrs);
                break;
            case ENTITY_RESULT:
                ret = startEntityResult(attrs);
                break;
            case FIELD_RESULT:
                ret = startFieldResult(attrs);
                break;
            case COLUMN_RESULT:
                ret = startColumnResult(attrs);
                break;
            case COLUMN_NAME:
                ret = true;
                break;
            case COLLECTION_TABLE:
                ret = startCollectionTable(attrs);
                break;
            case MAP_KEY_COL:
                ret = startMapKeyColumn(attrs);
                break;
            case MAP_KEY_JOIN_COL:
                ret = startMapKeyJoinColumn(attrs);
                break;
            case DATASTORE_ID_COL:
                ret = startDatastoreIdCol(attrs);
                break;
            case INDEX:
                ret = startIndex(attrs);
                break;
            case FK:
                ret = startForeignKey(attrs);
                break;
            case FK_COL_NAMES:
                ret = startFKColumnNames(attrs);
                break;
            case FK_COL_NAME:
                ret = true;
                break;
            case VERSION_COLS:
                ret = startVersionColumns(attrs);
                break;
            case VERSION_COL:
                ret = startVersionColumn(attrs);
                break;
            default:
                ret = false;
        }
        return (ret) ? tag : null;
    }

    private boolean endName() {
        String name = this.currentText();
        if (StringUtils.isNotEmpty(name)) {
            Object current = currentElement();
            if (current instanceof Unique) {
                Unique unq = (Unique)current;
                unq.setIdentifier(DBIdentifier.newConstraint(name, delimit()));
            }
        }
            
        return true;
    }

    @Override
    protected void endClassMappingElement(String name)
        throws SAXException {
        MappingTag tag = _elems.get(name);
        if (tag == null)
            return;

        switch (tag) {
            case SECONDARY_TABLE:
                endSecondaryTable();
                break;
            case DISCRIM_VAL:
                endDiscriminatorValue();
                break;
            case ASSOC_OVERRIDE:                
            case ATTR_OVERRIDE:
                endAttributeOverride();
                break;
            case JOIN_TABLE:
                endJoinTable();
                break;
            case TEMPORAL:
                endTemporal();
                break;
            case MAP_KEY_TEMPORAL:
                endMapKeyTemporal();
                break;
            case ENUMERATED:
                endEnumerated();
                break;
            case MAP_KEY_ENUMERATED:
                endMapKeyEnumerated();
                break;
            case SQL_RESULT_SET_MAPPING:
                endSQLResultSetMapping();
                break;
            case ENTITY_RESULT:
                endEntityResult();
                break;
            case UNIQUE:
                endUniqueConstraint();
                break;
            case COLUMN_NAME:
                endColumnName();
                break;
            case TABLE_GEN:
            	endTableGenerator();
            	break;
            case NAME:
                endName();
                break;
            case FK:
                endForeignKey();
                break;
            case FK_COL_NAMES:
                endFKColumnNames();
                break;
            case FK_COL_NAME:
                endFKColumnName();
                break;
            case VERSION_COLS:
                endVersionColumns();
                break;
        }
    }

    @Override
    protected void startClassMapping(ClassMetaData meta, boolean mappedSuper,
        Attributes attrs)
        throws SAXException {
        if (mappedSuper)
            ((ClassMapping) meta).getMappingInfo().setStrategy
                (NoneClassStrategy.ALIAS);
    }

    @Override
    protected void endClassMapping(ClassMetaData meta)
        throws SAXException {
        ClassMapping cm = (ClassMapping) meta;
        if (_schema != null)
            cm.getMappingInfo().setSchemaIdentifier(DBIdentifier.newSchema(_schema, delimit()));

        if (_supJoinCols != null)
            cm.getMappingInfo().setColumns(_supJoinCols);

        if (_discCol != null) {
            DiscriminatorMappingInfo dinfo = cm.getDiscriminator()
                    .getMappingInfo();
            switch (_discType) {
                case CHAR:
                    _discCol.setJavaType(JavaTypes.CHAR);
                    cm.getDiscriminator().setJavaType(JavaTypes.CHAR);
                    break;
                case INTEGER:
                    _discCol.setJavaType(JavaTypes.INT);
                    cm.getDiscriminator().setJavaType(JavaTypes.INT);
                    break;
                default:
                    _discCol.setJavaType(JavaTypes.STRING);
                    cm.getDiscriminator().setJavaType(JavaTypes.STRING);
                    break;
            }
            dinfo.setColumns(Arrays.asList(new Column[]{ _discCol }));
        }
        clearClassInfo();
    }

    /**
     * Clear cached class mapping info.
     */
    private void clearClassInfo() {
        _supJoinCols = null;
        _discCol = null;
        _discType = null;
    }

    /**
     * Start tracking secondary table information and columns
     */
    private boolean startSecondaryTable(Attributes attrs)
        throws SAXException {
        _secondaryTable = toTableIdentifier(attrs.getValue("schema"),
            attrs.getValue("name")).getName();
        ((ClassMapping)currentElement()).getMappingInfo()
        	.addSecondaryTable(DBIdentifier.newTable(_secondaryTable));
        return true;
    }

    /**
     * Set the secondary table information back to the owning class mapping.
     */
    private void endSecondaryTable() {
        ClassMapping cm = (ClassMapping) currentElement();
        ClassMappingInfo info = cm.getMappingInfo();
        info.setSecondaryTableJoinColumns(DBIdentifier.newTable(_secondaryTable, delimit()), _joinCols);
        clearSecondaryTableInfo();
    }

    /**
     * Clear cached secondary table info.
     */
    private void clearSecondaryTableInfo() {
        _joinCols = null;
        _secondaryTable = null;
    }

    /**
     * Parse table-generator.
     */
    private boolean startTableGenerator(Attributes attrs) {
        String name = attrs.getValue("name");
        Log log = getLog();
        if (log.isTraceEnabled())
            log.trace(_loc.get("parse-gen", name));
        if (getRepository().getCachedSequenceMetaData(name) != null
            && log.isWarnEnabled())
            log.warn(_loc.get("override-gen", name));

        SequenceMapping seq = (SequenceMapping) getRepository().
            addSequenceMetaData(name);
        seq.setSequencePlugin(SequenceMapping.IMPL_VALUE_TABLE);
        seq.setTableIdentifier(toTableIdentifier(attrs.getValue("schema"),
            attrs.getValue("table")));
        seq.setPrimaryKeyColumnIdentifier(DBIdentifier.newColumn(attrs.getValue("pk-column-name"), delimit())); 
        seq.setSequenceColumnIdentifier(DBIdentifier.newColumn(attrs.getValue("value-column-name"), delimit())); 
        seq.setPrimaryKeyValue(attrs.getValue("pk-column-value"));
        String val = attrs.getValue("initial-value");
        if (val != null)
            seq.setInitialValue(Integer.parseInt(val));
        val = attrs.getValue("allocation-size");
        if (val != null)
            seq.setAllocate(Integer.parseInt(val));

        Object cur = currentElement();
        Object scope = (cur instanceof ClassMetaData)
            ? ((ClassMetaData) cur).getDescribedType() : null;
        seq.setSource(getSourceFile(), scope, SourceTracker.SRC_XML);
        Locator locator = getLocation().getLocator();
        if (locator != null) {
            seq.setLineNumber(locator.getLineNumber());
            seq.setColNumber(locator.getColumnNumber());
        }
        pushElement(seq);
        return true;
    }
    
    private void endTableGenerator() {
    	popElement();
    }

    /**
     * Parse inheritance.
     */
    private boolean startInheritance(Attributes attrs) {
        String val = attrs.getValue("strategy");
        if (val == null)
            return true;

        ClassMapping cm = (ClassMapping) currentElement();
        ClassMappingInfo info = cm.getMappingInfo();
        switch (Enum.valueOf(InheritanceType.class, val)) {
            case SINGLE_TABLE:
                info.setHierarchyStrategy(FlatClassStrategy.ALIAS);
                break;
            case JOINED:
                info.setHierarchyStrategy(VerticalClassStrategy.ALIAS);
                break;
            case TABLE_PER_CLASS:
                info.setHierarchyStrategy(FullClassStrategy.ALIAS);
                break;
        }
        return true;
    }

    /**
     * Parse discriminator-value.
     */
    private void endDiscriminatorValue() {
        String val = currentText();
        if (StringUtils.isEmpty(val))
            return;

        ClassMapping cm = (ClassMapping) currentElement();
        cm.getDiscriminator().getMappingInfo().setValue(val);

        if (Modifier.isAbstract(cm.getDescribedType().getModifiers())
                && getLog().isInfoEnabled()) {
            getLog().info(
                    _loc.get("discriminator-on-abstract-class", cm
                            .getDescribedType().getName()));
        }
    }

    /**
     * Parse temporal.
     */
    private void endTemporal() {
        String temp = currentText();
        if (!StringUtils.isEmpty(temp))
            _temporal = Enum.valueOf(TemporalType.class, temp);
    }

    /**
     * Parse temporal.
     */
    private void endMapKeyTemporal() {
        String temp = currentText();
        TemporalType _mapKeyTemporal = null;
        if (!StringUtils.isEmpty(temp))
            _mapKeyTemporal = Enum.valueOf(TemporalType.class, temp);
        FieldMapping fm = (FieldMapping) currentElement();
        List<Column> cols = fm.getKeyMapping().getValueInfo().getColumns();
        if (cols.isEmpty()) {
            cols = Arrays.asList(new Column[]{ new Column() });
            fm.getKeyMapping().getValueInfo().setColumns(cols);
        }

        Column col = (Column) cols.get(0);
        switch (_mapKeyTemporal) {
            case DATE:
                col.setType(Types.DATE);
                break;
            case TIME:
                col.setType(Types.TIME);
                break;
            case TIMESTAMP:
                col.setType(Types.TIMESTAMP);
                break;
        }
    }

    /**
     * Parse enumerated.
     */
    private void endEnumerated() {
        String text = currentText();
        if (StringUtils.isEmpty(text))
            return;
        EnumType type = Enum.valueOf(EnumType.class, text);

        FieldMapping fm = (FieldMapping) currentElement();
        String strat = EnumValueHandler.class.getName() + "(StoreOrdinal="
            + String.valueOf(type == EnumType.ORDINAL) + ")";
        if (fm.isElementCollection())
            fm.getElementMapping().getValueInfo().setStrategy(strat);
        else
            fm.getValueInfo().setStrategy(strat);
    }

    /**
     * Parse map-key-enumerated.
     */
    private void endMapKeyEnumerated() {
        String text = currentText();
        if (StringUtils.isEmpty(text))
            return;
        EnumType type = Enum.valueOf(EnumType.class, text);

        FieldMapping fm = (FieldMapping) currentElement();
        String strat = EnumValueHandler.class.getName() + "(StoreOrdinal="
            + String.valueOf(type == EnumType.ORDINAL) + ")";
        fm.getKeyMapping().getValueInfo().setStrategy(strat);
    }

    @Override
    protected boolean startLob(Attributes attrs)
        throws SAXException {
        if (super.startLob(attrs)) {
            _lob = true;
            return true;
        }
        return false;
    }

    /**
     * Extend to clear annotation mapping info.
     */
    @Override
    protected void startFieldMapping(FieldMetaData field, Attributes attrs)
        throws SAXException {
        super.startFieldMapping(field, attrs);
        if (getAnnotationParser() != null) {
            FieldMapping fm = (FieldMapping) field;
            fm.getMappingInfo().clear();
            fm.getValueInfo().clear();
            fm.getElementMapping().getValueInfo().clear();
            fm.getKeyMapping().getValueInfo().clear();
        }
    }

    /**
     * Extend to set the columns.
     */
    @Override
    protected void endFieldMapping(FieldMetaData field)
        throws SAXException {
        // setup columns with cached lob and temporal info
        FieldMapping fm = (FieldMapping) field;
        if (_lob || _temporal != null) {
            int typeCode = fm.isElementCollection() ? fm.getElement().getDeclaredTypeCode() : 
                fm.getDeclaredTypeCode();
            Class<?> type = fm.isElementCollection() ? fm.getElement().getDeclaredType() : fm.getDeclaredType();  
            if (_cols == null) {
                _cols = new ArrayList<Column>(1);
                _cols.add(new Column());
            }
            for (Column col : _cols) {
                if (_lob && (typeCode == JavaTypes.STRING
                    || type == char[].class
                    || type == Character[].class)) {
                    col.setSize(-1);
                    col.setType(Types.CLOB);
                } else if (_lob)
                    col.setType(Types.BLOB);
                else {
                    switch (_temporal) {
                        case DATE:
                            col.setType(Types.DATE);
                            break;
                        case TIME:
                            col.setType(Types.TIME);
                            break;
                        case TIMESTAMP:
                            col.setType(Types.TIMESTAMP);
                            break;
                    }
                }
            }
        }

        if (_cols != null) {
            switch (fm.getDeclaredTypeCode()) {
                case JavaTypes.ARRAY:
                    Class<?> type = fm.getDeclaredType();
                    if (type == byte[].class || type == Byte[].class
                        || type == char[].class || type == Character[].class ) {
                        fm.getValueInfo().setColumns(_cols);
                        break;
                    }
                    // else no break
                case JavaTypes.COLLECTION:
                    if (!fm.getValue().isSerialized()) {
                        fm.getElementMapping().getValueInfo().setColumns(_cols);
                    } else  {
                        fm.getValueInfo().setColumns(_cols);
                    }
                    break;
                case JavaTypes.MAP:
                    fm.getElementMapping().getValueInfo().setColumns(_cols);
                    break;
                default:
                    fm.getValueInfo().setColumns(_cols);
            }
            if (_colTable != null)
                fm.getMappingInfo().setTableIdentifier(DBIdentifier.newTable(_colTable, delimit()));
            setUnique(fm);
        }
        clearColumnInfo();
    }

    /**
     * Set unique for field.
     */
    private void setUnique(FieldMapping fm) {
        setUnique(fm, _unique);
    }

    private void setUnique(FieldMapping fm, EnumSet<UniqueFlag> unique) {
        if (unique.size() == 2) // i.e. TRUE & FALSE
            getLog().warn(_loc.get("inconsist-col-attrs", fm));
        else if (unique.contains(UniqueFlag.TRUE))
            fm.getValueInfo().setUnique(new Unique());
    }

    /**
     * Clear field level column information.
     */
    private void clearColumnInfo() {
        _cols = null;
        _joinCols = null;
        _colTable = null;
        _lob = false;
        _temporal = null;
        _unique.clear();
    }

    /**
     * Parse attribute-override.
     */
    private boolean startAttributeOverride(Attributes attr) {
        _override = attr.getValue("name");
        return true;
    }

    /**
     * Set attribute override into proper mapping.
     */
    private void endAttributeOverride()
        throws SAXException {
        Object elem = currentElement();
        FieldMapping fm = null;
        if (elem instanceof ClassMapping)
            fm = getAttributeOverride((ClassMapping) elem);
        else {
            FieldMapping basefm = (FieldMapping) elem;
            
            fm = getAttributeOverrideForEmbeddable(basefm, _override, false);
            if (fm == null) {
                DeferredEmbeddableOverrides dfm = 
                    getDeferredFieldMappingInfo(
                        AnnotationPersistenceMappingParser.
                        getEmbeddedClassType(basefm, _override),
                        basefm, _override, true);
                dfm._defCols = _cols;
                dfm._defTable = DBIdentifier.newTable(_colTable, delimit());
                dfm._attrName = _override;
                dfm._unique = _unique;
            }
        }
        if (fm != null && _cols != null) {
            fm.getValueInfo().setColumns(_cols);
            if (_colTable != null)
                fm.getMappingInfo().setTableIdentifier(DBIdentifier.newTable(_colTable, delimit()));
            setUnique(fm);
        }
        clearColumnInfo();
        _override = null;
    }

    /**
     * Return the proper override.
     */
    private FieldMapping getAttributeOverride(ClassMapping cm) {
        FieldMapping sup = (FieldMapping) cm.getDefinedSuperclassField
            (_override);
        if (sup == null)
            sup = (FieldMapping) cm.addDefinedSuperclassField(_override,
                Object.class, Object.class);
        return sup;
    }

    /**
     * Return the proper override.
     */
    private FieldMapping getAttributeOverrideForEmbeddable(FieldMapping fm, 
        String attrName, boolean mustExist) 
    throws SAXException {
        return AnnotationPersistenceMappingParser.getEmbeddedFieldMapping(fm, 
            attrName, mustExist);
    }

    /**
     * Parse table.
     */
    private boolean startTable(Attributes attrs)
        throws SAXException {
        ClassMapping mapping = (ClassMapping) currentElement();
        if (mapping.isAbstract())
            throw new UserException(_loc.get("table-not-allowed", mapping));
        DBIdentifier table = toTableIdentifier(attrs.getValue("schema"),
            attrs.getValue("name"));
        if (!DBIdentifier.isNull(table))
            mapping.getMappingInfo().setTableIdentifier(table);
        return true;
    }

    /**
     * Parse join-table.
     */
    private boolean startJoinTable(Attributes attrs)
        throws SAXException {
        DBIdentifier sTable = toTableIdentifier(attrs.getValue("schema"),
            attrs.getValue("name"));
        if (!DBIdentifier.isNull(sTable)) {
            Object elem = currentElement();
            FieldMapping fm = null;
            if (elem instanceof FieldMapping) {
                fm = (FieldMapping) elem; 
                if (_override != null) {
                    FieldMapping basefm = (FieldMapping) elem;
                    fm = getAttributeOverrideForEmbeddable(basefm, 
                        _override, false);
                    if (fm == null) {
                        DeferredEmbeddableOverrides dfm = 
                            getDeferredFieldMappingInfo(
                                AnnotationPersistenceMappingParser.
                                getEmbeddedClassType(basefm, _override),
                                basefm, _override, true);
                        dfm._defTable = sTable.clone();
                        dfm._attrName = _override;
                    }
                }
            } else if (elem instanceof ClassMapping) {
                ClassMapping cm = (ClassMapping) elem;
                fm = getAttributeOverride(cm);
            }
            if (fm != null)
                fm.getMappingInfo().setTableIdentifier(sTable);
        }
        return true;
    }

    /**
     * Set the join table information back.
     */
    private void endJoinTable() throws SAXException {
        Object elem = currentElement();
        FieldMapping fm = null;
        if (elem instanceof FieldMapping) {
            fm = (FieldMapping) elem;
            if (_override != null) {
                FieldMapping basefm = (FieldMapping) elem;
                fm = getAttributeOverrideForEmbeddable(basefm, _override, 
                    false);
                if (fm == null) {
                    DeferredEmbeddableOverrides dfm = 
                        getDeferredFieldMappingInfo(
                            AnnotationPersistenceMappingParser.
                            getEmbeddedClassType(basefm, _override),
                            basefm, _override, true);
                    dfm._defCols = _cols;
                    dfm._defElemJoinCols = _joinCols;
                    dfm._attrName = _override;
                }
            }
        } else if (elem instanceof ClassMapping){
            ClassMapping cm = (ClassMapping) elem;
            fm = getAttributeOverride(cm);
        }

        if (fm != null) {
            if (_joinCols != null)
                fm.getMappingInfo().setColumns(_joinCols);
            if (_cols != null)
                fm.getElementMapping().getValueInfo().setColumns(_cols);
        }
        clearColumnInfo();
    }

    /**
     * Parse primary-key-join-column.
     */
    private boolean startPrimaryKeyJoinColumn(Attributes attrs)
        throws SAXException {
        Column col = parseColumn(attrs);
        col.setFlag(Column.FLAG_PK_JOIN, true);
        // pk join columns on fields act as field cols
        if (currentElement() instanceof FieldMapping) {
            if (_cols == null)
                _cols = new ArrayList<Column>(3);
            _cols.add(col);
        } else if (currentParent() == SECONDARY_TABLE) {
            // pk join columns in secondary table acts as join cols
            if (_joinCols == null)
                _joinCols = new ArrayList<Column>(3);
            _joinCols.add(col);
        } else {
            // must be pk join cols from this class to superclass
            if (_supJoinCols == null)
                _supJoinCols = new ArrayList<Column>(3);
            _supJoinCols.add(col);
        }
        return true;
    }

    /**
     * Parse join-column.
     */
    private boolean startJoinColumn(Attributes attrs)
        throws SAXException {
        // only join cols in a join table join field table to class table;
        // others act as data fk cols
        Object currentParent = currentParent();
        if (currentParent == COLLECTION_TABLE) {
            FieldMapping fm = (FieldMapping) peekElement();
            Column col = parseColumn(attrs);
            List<Column> colList = fm.getMappingInfo().getColumns();
            if (colList.isEmpty()) {
                colList = new ArrayList<Column>();
                fm.getMappingInfo().setColumns(colList);
            }
            colList.add(col);
            fm.getMappingInfo().setColumns(colList);
            return true;
        }
        
        if (currentParent != JOIN_TABLE)
            return startColumn(attrs);

        if (_joinCols == null)
            _joinCols = new ArrayList<Column>(3);
        _joinCols.add(parseColumn(attrs));
        return true;
    }

    /**
     * Parse column.
     */
    private boolean startColumn(Attributes attrs)
        throws SAXException {
        Column col = parseColumn(attrs);
        Object obj = peekElement();
        if (obj instanceof FieldMapping) {
            FieldMapping fm = (FieldMapping)obj;
            // a collection of basic types
            // the column is in a separate table
            if (fm.isElementCollection() &&
                !fm.getElementMapping().isEmbedded()) {
                List<Column> list = fm.getElementMapping().getValueInfo().getColumns();
                if (list.size() == 0) {
                    list = new ArrayList<Column>();
                    fm.getElementMapping().getValueInfo().setColumns(list);
                }
                list.add(col);
                return true;
            }
        }
        if (_cols == null)
            _cols = new ArrayList<Column>(3);
        _cols.add(col);
        return true;
    }

    /**
     * Parse map-key-column.
     */
    private boolean startMapKeyColumn(Attributes attrs)
        throws SAXException {
        FieldMapping fm = (FieldMapping) peekElement();
        Column col = parseColumn(attrs);
        MappingInfo info = fm.getKeyMapping().getValueInfo();
        List<Column> cols = new ArrayList<Column>();
        cols.add(col);
        info.setColumns(cols);
        return true;
    }

    /**
     * Parse map-key-join-column.
     */
    private boolean startMapKeyJoinColumn(Attributes attrs)
    throws SAXException {
        boolean retVal = startMapKeyColumn(attrs);
        // check if name is not set, set it to default: the
        // concatenation of the name of the referencing property
        // or field name, "-", "KEY"
        FieldMapping fm = (FieldMapping) peekElement();
        MappingInfo info = fm.getKeyMapping().getValueInfo();
        List<Column> cols = info.getColumns();
        Column col = cols.get(0);
        if (DBIdentifier.isNull(col.getIdentifier())) {
            col.setIdentifier(DBIdentifier.newColumn(fm.getName() + "_" + "KEY", delimit()));
        }

        return retVal;
    }
    
    /**
     * Create a column with the given attributes.
     */
    private Column parseColumn(Attributes attrs)
        throws SAXException {
        Column col = new Column();
        String val = attrs.getValue("name");
        if (val != null)
            col.setIdentifier(DBIdentifier.newColumn(val, delimit()));
        val = attrs.getValue("referenced-column-name");
        if (val != null) {
            setTargetIdentifier(col, val);
        }
        val = attrs.getValue("column-definition");
        if (val != null)
            col.setTypeIdentifier(DBIdentifier.newColumnDefinition(val));
        val = attrs.getValue("precision");
        if (val != null)
            col.setSize(Integer.parseInt(val));
        val = attrs.getValue("length");
        if (val != null)
            col.setSize(Integer.parseInt(val));
        val = attrs.getValue("scale");
        if (val != null)
            col.setDecimalDigits(Integer.parseInt(val));
        val = attrs.getValue("nullable");
        if (val != null)
            col.setNotNull("false".equals(val));
        val = attrs.getValue("insertable");
        if (val != null)
            col.setFlag(Column.FLAG_UNINSERTABLE, "false".equals(val));
        val = attrs.getValue("updatable");
        if (val != null)
            col.setFlag(Column.FLAG_UNUPDATABLE, "false".equals(val));

        val = attrs.getValue("unique");
        if (val != null)
            _unique.add(Enum.valueOf(UniqueFlag.class, val.toUpperCase()));
        val = attrs.getValue("table");
        if (val != null) {
            if (_colTable != null && !_colTable.equals(val))
                throw getException(_loc.get("second-inconsist",
                    currentElement()));
            _colTable = val;
        }
        return col;
    }

    /**
     * Sets reference column name of the given column taking into account
     * that the given reference name that begins with a single quote represents
     * special meaning of a constant join column and hence not to be delimited.  
     * @param col
     * @param refColumnName
     * @see <a href="http://issues.apache.org/jira/browse/OPENJPA-1979">OPENJPA-1979</a>
     */
    private static final char SINGLE_QUOTE = '\'';
    protected void setTargetIdentifier(Column col, String refColumnName) {
    	if (refColumnName.charAt(0) == SINGLE_QUOTE) {
    		col.setTargetIdentifier(DBIdentifier.newConstant(refColumnName));
    	} else {
    		col.setTargetIdentifier(DBIdentifier.newColumn(refColumnName, delimit()));
    	}
    }
    
    /**
     * Parse collectionTable.
     */
    private boolean startCollectionTable(Attributes attrs)
        throws SAXException {
        FieldMapping fm = (FieldMapping) peekElement();

        FieldMappingInfo info = fm.getMappingInfo();
        DBIdentifier ctbl = parseCollectionTable(attrs);
        info.setTableIdentifier(ctbl);
        return true;
    }

    private DBIdentifier parseCollectionTable(Attributes attrs) {
        String tVal = attrs.getValue("name");
        String sVal = attrs.getValue("schema");
        return toTableIdentifier(sVal, tVal); 
    }

    /**
     * Form a qualified table name from a schema and table name.
     */
    private DBIdentifier toTableIdentifier(String schema, String table) {
        DBIdentifier sName = DBIdentifier.newSchema(schema, delimit());
        DBIdentifier tName = DBIdentifier.newTable(table, delimit());
        if (DBIdentifier.isEmpty(tName) || DBIdentifier.isEmpty(sName)) {
            return tName;
        }
        return QualifiedDBIdentifier.newPath(sName, tName);
    }


    /**
     * Start processing <code>sql-result-set-mapping</code> node.
     * Pushes the {@link QueryResultMapping} onto the stack as current element.
     */
    private boolean startSQLResultSetMapping(Attributes attrs) {
        String name = attrs.getValue("name");
        Log log = getLog();
        if (log.isTraceEnabled())
            log.trace(_loc.get("parse-sqlrsmapping", name));

        MappingRepository repos = (MappingRepository) getRepository();
        QueryResultMapping result = repos.getCachedQueryResultMapping
            (null, name);
        if (result != null && log.isWarnEnabled())
            log.warn(_loc.get("override-sqlrsmapping", name,
                currentLocation()));

        result = repos.addQueryResultMapping(null, name);
        result.setListingIndex(_resultIdx++);
        addComments(result);

        Object cur = currentElement();
        Object scope = (cur instanceof ClassMetaData)
            ? ((ClassMetaData) cur).getDescribedType() : null;
        result.setSource(getSourceFile(), scope, SourceTracker.SRC_XML);
        Locator locator = getLocation().getLocator();
        if (locator != null) {
            result.setLineNumber(locator.getLineNumber());
            result.setColNumber(locator.getColumnNumber());
        }
        pushElement(result);
        return true;
    }

    private void endSQLResultSetMapping()
        throws SAXException {
        popElement();
    }

    /**
     * Start processing <code>entity-result</code> node.
     * Pushes the {@link QueryResultMapping.PCResult}
     * onto the stack as current element.
     */
    private boolean startEntityResult(Attributes attrs)
        throws SAXException {
        Class<?> entityClass = classForName(attrs.getValue("entity-class"));
        String discriminator = DBIdentifier.newColumn(attrs.getValue("discriminator-column"), delimit()).getName(); 

        QueryResultMapping parent = (QueryResultMapping) currentElement();
        QueryResultMapping.PCResult result = parent.addPCResult(entityClass);
        if (!StringUtils.isEmpty(discriminator))
            result.addMapping(PCResult.DISCRIMINATOR, discriminator);
        pushElement(result);
        return true;
    }

    private void endEntityResult()
        throws SAXException {
        popElement();
    }

    /**
     * Process a <code>field-result</code> node.
     */
    private boolean startFieldResult(Attributes attrs)
        throws SAXException {
        String fieldName = attrs.getValue("name");
        String columnName = DBIdentifier.newColumn(attrs.getValue("column"), delimit()).getName();

        QueryResultMapping.PCResult parent = (QueryResultMapping.PCResult)
            currentElement();
        parent.addMapping(fieldName, columnName);
        return true;
    }

    /**
     * Process a <code>column-result</code> node.
     */
    private boolean startColumnResult(Attributes attrs)
        throws SAXException {
        QueryResultMapping parent = (QueryResultMapping) currentElement();
        parent.addColumnResult(attrs.getValue("name"));
        return true;
    }

    /** 
     * Starts processing &lt;unique-constraint&gt; provided the tag occurs
     * within a ClassMapping element and <em>not</em> within a secondary
     * table. 
     * Pushes the Unique element in the stack.
     */
    private boolean startUniqueConstraint(Attributes attrs) 
        throws SAXException {
        Unique unique = new Unique();

        DBIdentifier name = DBIdentifier.newConstraint(attrs.getValue("name"), delimit());
        if (!DBIdentifier.isEmpty(name)) {
            unique.setIdentifier(name);
        }

        pushElement(unique);
        return true;
    }
    
    /**
     * Ends processing &lt;unique-constraint&gt; provided the tag occurs
     * within a ClassMapping element and <em>not</em> within a secondary
     * table. The stack is popped and the Unique element is added to the
     * ClassMappingInfo. 
     */
    private void endUniqueConstraint() {
        Unique unique = (Unique) popElement();
        Object ctx = currentElement();
        DBIdentifier tableName = DBIdentifier.newTable("?");
        if (ctx instanceof ClassMapping) {
        	ClassMappingInfo info = ((ClassMapping) ctx).getMappingInfo();
        	tableName = (_secondaryTable == null) 
        		? info.getTableIdentifier() : DBIdentifier.newTable(_secondaryTable, delimit());
        	info.addUnique(tableName, unique);
        } else if (ctx instanceof FieldMapping) {// JoinTable
        	FieldMappingInfo info = ((FieldMapping)ctx).getMappingInfo();
        	info.addJoinTableUnique(unique);
        } else if (ctx instanceof SequenceMapping) {
        	SequenceMapping seq = (SequenceMapping)ctx;
        	unique.setTableIdentifier(seq.getTableIdentifier());
        	Column[] uniqueColumns = unique.getColumns();
        	DBIdentifier[] columnNames = new DBIdentifier[uniqueColumns.length];
        	int i = 0;
        	for (Column uniqueColumn : uniqueColumns)
        		columnNames[i++] = uniqueColumn.getIdentifier().clone();
        	seq.setUniqueColumnsIdentifier(columnNames);
        	if (!DBIdentifier.isEmpty(unique.getIdentifier())) {
        	    seq.setUniqueConstraintIdentifier(unique.getIdentifier());
        	}
        } else {
        	throw new InternalException();
        }
    }
    
    /**
     * Ends processing &lt;column-name&gt; tag by adding the column name in
     * the current Unique element that resides in the top of the stack.
     */
    private boolean endColumnName() {
        Object current = currentElement();
        if (current instanceof Unique) {
            Unique unique = (Unique) current;
            Column column = new Column();
            column.setIdentifier(DBIdentifier.newColumn(this.currentText(), delimit()));
            unique.addColumn(column);
            return true;
        }
        return false;
    }
    
    /**
     * Track unique column settings.
	 */
	private static enum UniqueFlag
	{
		TRUE,
		FALSE
	}
	
	private void parseDiscriminatorColumn(Attributes attrs) { 
	    String val = attrs.getValue("discriminator-type");
        if (val != null) {
            _discType = Enum.valueOf(DiscriminatorType.class, val);
        }
        else {
            _discType = DiscriminatorType.STRING;
        }
            
	}  
    
    /**
     * Process OrderColumn.
     */
    protected boolean startOrderColumn(Attributes attrs)
        throws SAXException {
        Column col = parseOrderColumn(attrs);
        Object obj = peekElement();
        if (obj instanceof FieldMapping) {
            FieldMapping fm = (FieldMapping)obj;
            fm.getMappingInfo().setOrderColumn(col);

        }
        return true;
    }
    /**
     * Create an order column with the given attributes.
     */
    private Column parseOrderColumn(Attributes attrs)
        throws SAXException {

        Column col = new Column();
        String val = attrs.getValue("name");
        if (val != null)
            col.setIdentifier(DBIdentifier.newColumn(val, delimit()));
        val = attrs.getValue("column-definition"); 
        if (val != null)
            col.setTypeIdentifier(DBIdentifier.newColumnDefinition(val));
        val = attrs.getValue("precision");
        if (val != null)
            col.setSize(Integer.parseInt(val));
        val = attrs.getValue("length");
        if (val != null)
            col.setSize(Integer.parseInt(val));
        val = attrs.getValue("scale");
        if (val != null)
            col.setDecimalDigits(Integer.parseInt(val));
        val = attrs.getValue("nullable");
        if (val != null)
            col.setNotNull("false".equals(val));
        val = attrs.getValue("insertable");
        if (val != null)
            col.setFlag(Column.FLAG_UNINSERTABLE, "false".equals(val));
        val = attrs.getValue("updatable");
        if (val != null)
            col.setFlag(Column.FLAG_UNUPDATABLE, "false".equals(val));
        
        return col;
    }
    
    /**
     * Process all deferred embeddable overrides for a given class.  
     * This should only occur after the embeddable is mapped.
     * 
     * @param embedType  embeddable class 
     * @param access class level access for embeddable
     * @throws SAXException 
     */
    @Override
    protected void applyDeferredEmbeddableOverrides(Class<?> cls) 
        throws SAXException {
        ArrayList<DeferredEmbeddableOverrides> defMappings = 
            _deferredMappings.get(cls);
        if (defMappings == null)
            return;
        
        for (DeferredEmbeddableOverrides defMap : defMappings) {
            FieldMapping fm = (FieldMapping)defMap._fm;
            if (defMap == null)
                return;
            fm = getAttributeOverrideForEmbeddable(fm, defMap._attrName, true);
            // Apply column, table, and unique overrides
            if (defMap._defCols != null) {
                fm.getValueInfo().setColumns(defMap._defCols);
                if (!DBIdentifier.isNull(defMap._defTable))
                    fm.getMappingInfo().setTableIdentifier(defMap._defTable);
                setUnique(fm, defMap._unique);
            }
            // Apply Join column and element join columns overrides overrides
            if (defMap._defJoinCols != null)
                fm.getMappingInfo().setColumns(defMap._defJoinCols);
            if (defMap._defElemJoinCols != null)
                fm.getElementMapping().getValueInfo().setColumns(
                    defMap._defElemJoinCols);
        }
        // Clean up after applying mappings
        defMappings.clear();
        _deferredMappings.remove(cls);
    }

    /*
     * Defer overrides for the specified field mapping
     */
    private void deferEmbeddableOverrides(
        Class<?> cls, DeferredEmbeddableOverrides defMap) {
        ArrayList<DeferredEmbeddableOverrides> defMappings = 
            _deferredMappings.get(cls);
        if (defMappings == null) {
            defMappings = new ArrayList<DeferredEmbeddableOverrides>();
            _deferredMappings.put(cls, defMappings);
        }
        defMappings.add(defMap);
    }
    
    /*
     * Clean up any deferred mappings
     */
    @Override
    protected void clearDeferredMetaData() {
        super.clearDeferredMetaData();
        _deferredMappings.clear();
    }
    
    /*
     * Get embeddable overrides for the specified field mapping.  If create
     * is true, create a new override if one does not exist.
     */
    private DeferredEmbeddableOverrides 
        getDeferredFieldMappingInfo(Class<?> cls, FieldMapping fm, 
            String attrName, boolean create) {

        ArrayList<DeferredEmbeddableOverrides> defMappings = 
            _deferredMappings.get(cls);
        
        if (defMappings == null && create) {
            defMappings = new ArrayList<DeferredEmbeddableOverrides>();
            _deferredMappings.put(cls, defMappings);
        }
        DeferredEmbeddableOverrides dfm = 
            findDeferredMapping(cls, fm, attrName);

        if (dfm == null & create) {
            dfm = new DeferredEmbeddableOverrides(fm, attrName);
            deferEmbeddableOverrides(cls, dfm);
        }
        return dfm;            
    }

    /*
     * Find deferred mappings for the given class, fm, and attr name
     */
    private DeferredEmbeddableOverrides findDeferredMapping(Class<?> cls, 
        FieldMapping fm, String attrName) {
        ArrayList<DeferredEmbeddableOverrides> defMappings = 
            _deferredMappings.get(cls);
        if (defMappings == null)
            return null;
        
        for (DeferredEmbeddableOverrides dfm : defMappings) {
            if (dfm != null && dfm._fm == fm && 
                attrName.equals(dfm._attrName))
                return dfm;
        }
        return null;
    }

    /**
     * Process all deferred embeddables using an unknown access type.
     */
    protected void addDeferredEmbeddableMetaData() {
        super.addDeferredEmbeddableMetaData();
        if (_deferredMappings.size() > 0) {
            Set<Class<?>> keys = _deferredMappings.keySet();
            Class<?>[] classes = keys.toArray(new Class[keys.size()]);
            for (int i = 0; i < classes.length; i++) {
                try {
                    applyDeferredEmbeddableOverrides(classes[i]);
                } catch (Exception e) {
                    throw new MetaDataException(_loc.get("no-embeddable-metadata", classes[i].getName()), e);
                }
            }
        }
        
    }    
    
    // Inner class for storing override information
    class DeferredEmbeddableOverrides {
        DeferredEmbeddableOverrides(FieldMapping fm, String attrName) {
            _fm = fm;
            _attrName = attrName;
            _defTable = DBIdentifier.NULL;
        }
        private FieldMapping _fm;
        private List<Column> _defCols;
        private List<Column> _defElemJoinCols;
        private List<Column> _defJoinCols;
        private DBIdentifier _defTable;
        private String _attrName;
        private EnumSet<UniqueFlag> _unique;
    }  
    
    @Override
    protected boolean startDelimitedIdentifiers() {
        JDBCConfiguration conf = (JDBCConfiguration) getConfiguration();
        DBDictionary dict = conf.getDBDictionaryInstance();
        dict.setDelimitIdentifiers(true);
        return true;
    }
    
    @Override
    protected String normalizeSequenceName(String seqName) {
        if (StringUtils.isEmpty(seqName)) {
            return seqName;
        }
        return DBIdentifier.newSequence(seqName, delimit()).getName();
    }
    
    @Override
    protected String normalizeSchemaName(String schName) {
        if (StringUtils.isEmpty(schName)) {
            return schName;
        }
        return DBIdentifier.newSchema(schName, delimit()).getName();
    }

    @Override
    protected String normalizeCatalogName(String catName) {
        if (StringUtils.isEmpty(catName)) {
            return catName;
        }
        return DBIdentifier.newCatalog(catName, delimit()).getName();
    }

    private boolean delimit() {
        return _dict.getDelimitIdentifiers();
    }
    
    /**
     * Translate the fetch mode enum value to the internal OpenJPA constant.
     */
    private static int toEagerFetchModeConstant(String mode) {
        if(mode.equals("NONE"))
            return EagerFetchModes.EAGER_NONE;
        else if (mode.equals("JOIN"))
            return EagerFetchModes.EAGER_JOIN;
        else if (mode.equals("PARALLEL"))
            return EagerFetchModes.EAGER_PARALLEL;
        else
            throw new InternalException();
    }
    
    private boolean startDatastoreIdCol(Attributes attrs)
        throws SAXException {
        
        ClassMapping cm = (ClassMapping) peekElement();
        
        Column col = new Column();
        String name = attrs.getValue("name");
        if (!StringUtils.isEmpty(name));
            col.setIdentifier(DBIdentifier.newColumn(name, delimit()));
        String columnDefinition= attrs.getValue("column-definition");
        if (!StringUtils.isEmpty(columnDefinition))
            col.setTypeIdentifier(DBIdentifier.newColumnDefinition(columnDefinition));
        int precision = Integer.parseInt(attrs.getValue("precision"));
        if (precision != 0)
            col.setSize(precision);
        col.setFlag(Column.FLAG_UNINSERTABLE, !Boolean.parseBoolean(attrs.getValue("insertable")));
        col.setFlag(Column.FLAG_UNUPDATABLE, !Boolean.parseBoolean(attrs.getValue("updatable")));
        cm.getMappingInfo().setColumns(Arrays.asList(new Column[]{ col }));
        
        return true;
    }
    
    private boolean startIndex(Attributes attrs)
        throws SAXException {
        
        FieldMapping fm = (FieldMapping) peekElement();
        
        parseIndex(fm.getValueInfo(),
            attrs.getValue("name"),
            Boolean.parseBoolean(attrs.getValue("enabled")),
            Boolean.parseBoolean(attrs.getValue("unique")));
        
        return true;
    }
    
    private void parseIndex(MappingInfo info, String name,
        boolean enabled, boolean unique) {
        if (!enabled) {
            info.setCanIndex(false);
            return;
        }

        org.apache.openjpa.jdbc.schema.Index idx =
            new org.apache.openjpa.jdbc.schema.Index();
        if (!StringUtils.isEmpty(name))
            idx.setIdentifier(DBIdentifier.newConstraint(name, delimit()));
        idx.setUnique(unique);
        info.setIndex(idx);
    }
    
    private boolean startForeignKey(Attributes attrs) 
        throws SAXException {
        
        _foreignKeyAttributes = attrs;
        
        return true;
    }
    
    private void endForeignKey() {
        if (_foreignKeyAttributes == null) {
            throw new InternalException();
        }
        
        boolean implicit = Boolean.parseBoolean(_foreignKeyAttributes.getValue("implicit"));
        
        FieldMapping fm = (FieldMapping) peekElement();
        MappingInfo info = fm.getValueInfo();
        
        String name = _foreignKeyAttributes.getValue("name");
        boolean enabled = Boolean.parseBoolean(_foreignKeyAttributes.getValue("enabled"));
        boolean deferred = Boolean.parseBoolean(_foreignKeyAttributes.getValue("deferred"));
        boolean specified = Boolean.parseBoolean(_foreignKeyAttributes.getValue("specified"));
        String deleteActionString = _foreignKeyAttributes.getValue("delete-action");
        String updateActionString = _foreignKeyAttributes.getValue("update-action");
        int deleteAction = toForeignKeyInt(deleteActionString);
        int updateAction = toForeignKeyInt(updateActionString);
        
        if (!implicit) {
            parseForeignKey(info, name,
                enabled,
                deferred, deleteAction,
                updateAction);
        }
        else {
            info.setImplicitRelation(true);
            assertDefault(name, enabled, deferred, specified, updateAction, deleteAction);
        }
        
        _columnNamesList = null;
    }
    
    private void parseForeignKey(MappingInfo info, String name, boolean enabled,
        boolean deferred, int deleteAction, int updateAction) {
        
        if (!enabled) {
            info.setCanForeignKey(false);
            return;
        }

        org.apache.openjpa.jdbc.schema.ForeignKey fk =
            new org.apache.openjpa.jdbc.schema.ForeignKey();
        if (!StringUtils.isEmpty(name))
            fk.setIdentifier(DBIdentifier.newForeignKey(name, delimit()));
        fk.setDeferred(deferred);
        fk.setDeleteAction(deleteAction);
        fk.setUpdateAction(updateAction);
        info.setForeignKey(fk);
        
    }
    
    
    private int toForeignKeyInt(String action) {
        if (action.equals("RESTRICT")) {
            return org.apache.openjpa.jdbc.schema.ForeignKey.
                    ACTION_RESTRICT;
        }
        else if (action.equals("CASCADE")) {
            return org.apache.openjpa.jdbc.schema.ForeignKey.ACTION_CASCADE;
        }
        else if (action.equals("NULL")) {
            return org.apache.openjpa.jdbc.schema.ForeignKey.ACTION_NULL;
        }
        else if (action.equals("DEFAULT")) {
            return org.apache.openjpa.jdbc.schema.ForeignKey.ACTION_DEFAULT;
        }
        else {
            throw new InternalException();
        }
        
    }
    
    private void assertDefault(String name, boolean enabled, boolean deferred, boolean specified,
        int updateAction, int deleteAction) {
        boolean isDefault = StringUtils.isEmpty(name) 
            && enabled 
            && !deferred 
                && deleteAction == org.apache.openjpa.jdbc.schema.ForeignKey.ACTION_RESTRICT
                && updateAction == org.apache.openjpa.jdbc.schema.ForeignKey.ACTION_RESTRICT
            && _columnNames.length == 0
            && specified;
        if (!isDefault)
            throw new UserException(_loc.get("implicit-non-default-fk", _cls,
                getSourceFile()).getMessage());
    }
    
    private boolean startFKColumnNames(Attributes attrs) 
        throws SAXException {
        _columnNamesList = new ArrayList<String>();
        return true;
    }
    
    private void endFKColumnNames() {
        if (_columnNamesList.size() > 0) {
            _columnNames = _columnNamesList.toArray(_columnNames);
            _columnNamesList.removeAll(_columnNamesList);
        }
    }
    
    private void endFKColumnName() {
        _columnNamesList.add(currentText());
    }
    
    private boolean startVersionColumns(Attributes attrs)
        throws SAXException {
        
        _versionColumnsList = new ArrayList<Column>();
        
        return true;
    }
    
    private void endVersionColumns() {
        if (_versionColumnsList == null) {
            throw new InternalException();
        }
        
        if (_versionColumnsList.size() > 0) {
            ClassMapping cm = (ClassMapping)peekElement();
            cm.getVersion().getMappingInfo().setColumns(_versionColumnsList);
            _versionColumnsList= null;
        }
    }
    
    private boolean startVersionColumn(Attributes attrs)
            throws SAXException {
            
        Column col = AnnotationPersistenceMappingParser.newColumn(attrs.getValue("name"),
            Boolean.parseBoolean(attrs.getValue("nullable")),
            Boolean.parseBoolean(attrs.getValue("insertable")),
            Boolean.parseBoolean(attrs.getValue("updatable")),
            attrs.getValue("columnDefinition"),
            Integer.parseInt(attrs.getValue("length")),
            Integer.parseInt(attrs.getValue("precision")),
            Integer.parseInt(attrs.getValue("scale")), 
            attrs.getValue("table"), 
            delimit());
        
        _versionColumnsList.add(col);
        
        return true;
        }

    @Override
    protected void parseEagerFetchModeAttr(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
        
        FieldMapping fm = (FieldMapping) fmd;
        String eagerFetchMode = attrs.getValue("eager-fetch-mode");
        if (!StringUtils.isEmpty(eagerFetchMode)) {
            if (eagerFetchMode.equalsIgnoreCase("NONE")) {
                fm.setEagerFetchMode(EagerFetchModes.EAGER_NONE);
            } else if (eagerFetchMode.equalsIgnoreCase("JOIN")) {
                fm.setEagerFetchMode(EagerFetchModes.EAGER_JOIN);
            } else if (eagerFetchMode.equalsIgnoreCase("PARALLEL")) {
                fm.setEagerFetchMode(EagerFetchModes.EAGER_PARALLEL);
            }
        }
    }

    @Override
    protected void parseElementClassCriteriaAttr(FieldMetaData fmd, Attributes attrs)
        throws SAXException {
        
        String elementClassCriteriaString = attrs.getValue("element-class-criteria");
        if (!StringUtils.isEmpty(elementClassCriteriaString)) {
            FieldMapping fm = (FieldMapping) fmd;
            boolean elementClassCriteria = Boolean.parseBoolean(elementClassCriteriaString);
            fm.getElementMapping().getValueInfo().setUseClassCriteria(elementClassCriteria);
        }
    }

    @Override
    protected void parseStrategy(FieldMetaData fmd, Attributes attrs) {
        String strategy = attrs.getValue("strategy");
        if (!StringUtils.isEmpty(strategy)) {
            ((FieldMapping) fmd).getMappingInfo().setStrategy(strategy);
        }
    }

    @Override
    protected boolean startExtendedClass(String elem, Attributes attrs) 
            throws SAXException {
        ClassMapping mapping = (ClassMapping) currentElement();
        
        String strategy = attrs.getValue("strategy");
        if (!StringUtils.isEmpty(strategy))
            mapping.getMappingInfo().setStrategy(strategy);
        
        String versionStrat = attrs.getValue("version-strategy");
        if (!StringUtils.isEmpty(versionStrat))
            mapping.getVersion().getMappingInfo().setStrategy(versionStrat);
        
        String discrimStrat = attrs.getValue("discriminator-strategy");
        if (!StringUtils.isEmpty(discrimStrat))
            mapping.getDiscriminator().getMappingInfo().setStrategy(discrimStrat);
        
        String subclassFetchMode = attrs.getValue("subclass-fetch-mode");
        if (!StringUtils.isEmpty(subclassFetchMode))
            mapping.setSubclassFetchMode(toEagerFetchModeConstant(subclassFetchMode));
        
        return true;
    }
}
