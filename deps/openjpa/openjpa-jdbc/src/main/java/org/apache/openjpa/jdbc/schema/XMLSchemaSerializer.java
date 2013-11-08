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
package org.apache.openjpa.jdbc.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.lib.meta.XMLMetaDataSerializer;
import org.apache.openjpa.lib.util.Localizer;
import org.xml.sax.SAXException;

/**
 * Serializes {@link Schema}s to XML matching the document
 * type definition defined by the {@link XMLSchemaParser}. The serializer
 * actually works at the fine-grained table level to allow you to split
 * schemas among multiple files.
 *  Serializers are not thread safe.
 *
 * @author Abe White
 * @nojavadoc
 */
public class XMLSchemaSerializer
    extends XMLMetaDataSerializer
    implements SchemaSerializer {

    private static final Localizer _loc = Localizer.forPackage
        (XMLSchemaSerializer.class);

    private final Collection<Table> _tables = new TreeSet<Table>();
    private final Collection<Sequence> _seqs = new TreeSet<Sequence>();

    /**
     * Constructor. Supply configuration.
     */
    public XMLSchemaSerializer(JDBCConfiguration conf) {
        setLog(conf.getLog(JDBCConfiguration.LOG_SCHEMA));
    }

    public Table[] getTables() {
        return (Table[]) _tables.toArray(new Table[_tables.size()]);
    }

    public void addTable(Table table) {
        if (table != null)
            _tables.add(table);
    }

    public boolean removeTable(Table table) {
        return _tables.remove(table);
    }

    public Sequence[] getSequences() {
        return (Sequence[]) _seqs.toArray(new Sequence[_seqs.size()]);
    }

    public void addSequence(Sequence seq) {
        if (seq != null)
            _seqs.add(seq);
    }

    public boolean removeSequence(Sequence seq) {
        return _seqs.remove(seq);
    }

    public void addAll(Schema schema) {
        if (schema == null)
            return;
        Table[] tables = schema.getTables();
        for (int i = 0; i < tables.length; i++)
            addTable(tables[i]);
        Sequence[] seqs = schema.getSequences();
        for (int i = 0; i < seqs.length; i++)
            addSequence(seqs[i]);
    }

    public void addAll(SchemaGroup group) {
        if (group == null)
            return;
        Schema[] schemas = group.getSchemas();
        for (int i = 0; i < schemas.length; i++)
            addAll(schemas[i]);
    }

    public boolean removeAll(Schema schema) {
        if (schema == null)
            return false;

        boolean removed = false;
        Table[] tables = schema.getTables();
        for (int i = 0; i < tables.length; i++)
            removed |= removeTable(tables[i]);
        Sequence[] seqs = schema.getSequences();
        for (int i = 0; i < seqs.length; i++)
            removed |= removeSequence(seqs[i]);
        return removed;
    }

    public boolean removeAll(SchemaGroup group) {
        if (group == null)
            return false;

        boolean removed = false;
        Schema[] schemas = group.getSchemas();
        for (int i = 0; i < schemas.length; i++)
            removed |= removeAll(schemas[i]);
        return removed;
    }

    public void clear() {
        _tables.clear();
        _seqs.clear();
    }

    protected Collection getObjects() {
        if (_seqs.isEmpty())
            return _tables;
        if (_tables.isEmpty())
            return _seqs;
        List<Object> all = new ArrayList<Object>(_seqs.size() + _tables.size());
        all.addAll(_seqs);
        all.addAll(_tables);
        return all;
    }

    protected void serialize(Collection objs)
        throws SAXException {
        // group the objects by schema
        Map schemas = new HashMap();
        String schemaName;
        Collection schemaObjs;
        Object obj;
        for (Iterator itr = objs.iterator(); itr.hasNext();) {
            obj = itr.next();
            if (obj instanceof Table)
                schemaName = ((Table) obj).getSchemaName();
            else
                schemaName = ((Sequence) obj).getSchemaName();
            schemaObjs = (Collection) schemas.get(schemaName);
            if (schemaObjs == null) {
                schemaObjs = new LinkedList();
                schemas.put(schemaName, schemaObjs);
            }
            schemaObjs.add(obj);
        }

        startElement("schemas");
        Map.Entry entry;
        for (Iterator itr = schemas.entrySet().iterator(); itr.hasNext();) {
            entry = (Map.Entry) itr.next();
            serializeSchema((String) entry.getKey(), (Collection)
                entry.getValue());
        }
        endElement("schemas");
    }

    /**
     * Serializes the given objects together into the current schema.
     */
    private void serializeSchema(String name, Collection<?> objs)
        throws SAXException {
        if (objs.isEmpty())
            return;

        if (getLog().isTraceEnabled())
            getLog().trace(_loc.get("ser-schema", name));

        if (name != null)
            addAttribute("name", name);
        startElement("schema");

        // tables and seqs
        Object obj;
        for (Iterator<?> itr = objs.iterator(); itr.hasNext();) {
            obj = itr.next();
            if (obj instanceof Table)
                serializeTable((Table) obj);
            else
                serializeSequence((Sequence) obj);
        }

        endElement("schema");
    }

    /**
     * Serialize the given sequence.
     */
    private void serializeSequence(Sequence seq)
        throws SAXException {
        addAttribute("name", seq.getName());
        if (seq.getInitialValue() != 1)
            addAttribute("initial-value",
                String.valueOf(seq.getInitialValue()));
        if (seq.getIncrement() > 1)
            addAttribute("increment", String.valueOf(seq.getIncrement()));
        if (seq.getAllocate() > 1)
            addAttribute("allocate", String.valueOf(seq.getAllocate()));
        startElement("sequence");
        endElement("sequence");
    }

    /**
     * Serializes the given table.
     */
    private void serializeTable(Table table)
        throws SAXException {
        addAttribute("name", table.getName());
        startElement("table");

        // primary key
        PrimaryKey pk = table.getPrimaryKey();
        if (pk != null)
            serializePrimaryKey(pk);

        // columns
        Column[] cols = table.getColumns();
        for (int i = 0; i < cols.length; i++)
            serializeColumn(cols[i]);

        // foreign keys
        ForeignKey[] fks = table.getForeignKeys();
        for (int i = 0; i < fks.length; i++)
            serializeForeignKey(fks[i]);

        // indexes
        Index[] idxs = table.getIndexes();
        for (int i = 0; i < idxs.length; i++)
            serializeIndex(idxs[i]);

        // unique constraints
        Unique[] unqs = table.getUniques();
        for (int i = 0; i < unqs.length; i++)
            serializeUnique(unqs[i]);

        endElement("table");
    }

    /**
     * Serializes the given column.
     */
    private void serializeColumn(Column col)
        throws SAXException {
        addAttribute("name", col.getName());
        addAttribute("type", Schemas.getJDBCName(col.getType()));
        if (!StringUtils.isEmpty(col.getTypeName())
            && !col.getTypeName().equalsIgnoreCase
            (Schemas.getJDBCName(col.getType())))
            addAttribute("type-name", col.getTypeName());
        if (col.isNotNull())
            addAttribute("not-null", "true");
        if (col.isAutoAssigned())
            addAttribute("auto-assign", "true");
        if (col.getDefaultString() != null)
            addAttribute("default", col.getDefaultString());
        if (col.getSize() != 0)
            addAttribute("size", String.valueOf(col.getSize()));
        if (col.getDecimalDigits() != 0)
            addAttribute("decimal-digits", String.valueOf
                (col.getDecimalDigits()));
        startElement("column");
        endElement("column");
    }

    /**
     * Serializes the given primary key.
     */
    private void serializePrimaryKey(PrimaryKey pk)
        throws SAXException {
        if (pk.getName() != null)
            addAttribute("name", pk.getName());
        if (pk.isLogical())
            addAttribute("logical", "true");

        Column[] cols = pk.getColumns();
        if (cols.length == 1)
            addAttribute("column", cols[0].getName());
        startElement("pk");

        // columns
        if (cols.length > 1)
            for (int i = 0; i < cols.length; i++)
                serializeOn(cols[i]);

        endElement("pk");
    }

    /**
     * Serializes the given index.
     */
    private void serializeIndex(Index idx)
        throws SAXException {
        addAttribute("name", idx.getName());
        if (idx.isUnique())
            addAttribute("unique", "true");
        Column[] cols = idx.getColumns();
        if (cols.length == 1)
            addAttribute("column", cols[0].getName());
        startElement("index");

        // columns
        if (cols.length > 1)
            for (int i = 0; i < cols.length; i++)
                serializeOn(cols[i]);

        endElement("index");
    }

    /**
     * Serializes the given constraint.
     */
    private void serializeUnique(Unique unq)
        throws SAXException {
        if (unq.getName() != null)
            addAttribute("name", unq.getName());
        if (unq.isDeferred())
            addAttribute("deferred", "true");
        Column[] cols = unq.getColumns();
        if (cols.length == 1)
            addAttribute("column", cols[0].getName());
        startElement("unique");

        // columns
        if (cols.length > 1)
            for (int i = 0; i < cols.length; i++)
                serializeOn(cols[i]);

        endElement("unique");
    }

    /**
     * Serializes the given foreign key.
     */
    private void serializeForeignKey(ForeignKey fk)
        throws SAXException {
        if (fk.getName() != null)
            addAttribute("name", fk.getName());

        if (fk.isDeferred())
            addAttribute("deferred", "true");

        if (fk.getDeleteAction() != ForeignKey.ACTION_NONE)
            addAttribute("delete-action", ForeignKey.getActionName
                (fk.getDeleteAction()));
        if (fk.getUpdateAction() != ForeignKey.ACTION_NONE
            && fk.getUpdateAction() != ForeignKey.ACTION_RESTRICT)
            addAttribute("update-action", ForeignKey.getActionName
                (fk.getUpdateAction()));

        Column[] cols = fk.getColumns();
        Column[] pks = fk.getPrimaryKeyColumns();
        Column[] consts = fk.getConstantColumns();
        Column[] constsPK = fk.getConstantPrimaryKeyColumns();
        addAttribute("to-table", fk.getPrimaryKeyTable().getFullName());
        if (cols.length == 1 && consts.length == 0 && constsPK.length == 0)
            addAttribute("column", cols[0].getName());
        startElement("fk");

        // columns
        if (cols.length > 1 || consts.length > 0 || constsPK.length > 0)
            for (int i = 0; i < cols.length; i++)
                serializeJoin(cols[i], pks[i]);
        for (int i = 0; i < consts.length; i++)
            serializeJoin(consts[i], fk.getConstant(consts[i]));
        for (int i = 0; i < constsPK.length; i++)
            serializeJoin(fk.getPrimaryKeyConstant(constsPK[i]), constsPK[i]);

        endElement("fk");
    }

    /**
     * Serializes the given column to an 'on' element.
     */
    private void serializeOn(Column col)
        throws SAXException {
        addAttribute("column", col.getName());
        startElement("on");
        endElement("on");
    }

    /**
     * Serializes the given columns to a 'join' element.
     */
    private void serializeJoin(Column col, Column pk)
        throws SAXException {
        addAttribute("column", col.getName());
        addAttribute("to-column", pk.getName());
        startElement("join");
        endElement("join");
    }

    /**
     * Serializes the given values to a 'join' element.
     */
    private void serializeJoin(Object val, Column pk)
        throws SAXException {
        addAttribute("value", stringifyConstant(val));
        addAttribute("to-column", pk.getName());
        startElement("join");
        endElement("join");
    }

    /**
     * Serializes the given values to a 'join' element.
     */
    private void serializeJoin(Column col, Object val)
        throws SAXException {
        addAttribute("column", col.getName());
        addAttribute("value", stringifyConstant(val));
        startElement("join");
        endElement("join");
    }

    /**
     * Stringify the given constant value.
     */
    private static String stringifyConstant(Object val) {
        if (val == null)
            return "null";
        if (val instanceof String)
            return "'" + val + "'";
        return val.toString();
    }
}
