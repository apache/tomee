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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.apache.openjpa.jdbc.conf.JDBCConfiguration;
import org.apache.openjpa.jdbc.sql.DBDictionary;
import org.apache.openjpa.lib.meta.SourceTracker;
import org.apache.openjpa.lib.meta.XMLMetaDataParser;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.Localizer.Message;
import org.apache.openjpa.util.UserException;


/**
 * Custom SAX parser used to parse {@link Schema} objects. The parser
 * will place all parsed schemas into the current {@link SchemaGroup}, set
 * via the {@link #setSchemaGroup} method. This allows parsing of
 * multiple files into a single schema group.
 *  The parser deserializes from the following XML format:<br />
 * <code> &lt;!ELEMENT schemas (schema)+&gt;<br />
 * &lt;!ELEMENT schema (table|sequence)+&gt;<br />
 * &lt;!ATTLIST schema name CDATA #IMPLIED&gt;<br />
 * &lt;!ELEMENT table (column|index|pk|fk|unique)+&gt;<br />
 * &lt;!ATTLIST table name CDATA #REQUIRED&gt;<br />
 * &lt;!ELEMENT column EMPTY&gt;<br />
 * &lt;!ATTLIST column name CDATA #REQUIRED&gt;<br />
 * &lt;!ATTLIST column type (array|bigint|binary|bit|blob|char|clob
 * |date|decimal|distinct|double|float|integer|java_object
 * |longvarbinary|longvarchar|null|numeric|other|real|ref|smallint|struct
 * |time|timstamp|tinyint|varbinary|varchar) #REQUIRED&gt;<br />
 * &lt;!ATTLIST column type-name CDATA #IMPLIED&gt;<br />
 * &lt;!ATTLIST column size CDATA #IMPLIED&gt;<br />
 * &lt;!ATTLIST column decimal-digits CDATA #IMPLIED&gt;<br />
 * &lt;!ATTLIST column not-null (true|false) "false"&gt;<br />
 * &lt;!ATTLIST column default CDATA #IMPLIED&gt;<br />
 * &lt;!ATTLIST column auto-assign (true|false) "false"&gt;<br />
 * &lt;!ELEMENT index (on)*&gt;<br />
 * &lt;!ATTLIST index name CDATA #REQUIRED&gt;<br />
 * &lt;!ATTLIST index column CDATA #IMPLIED&gt;<br />
 * &lt;!ATTLIST index unique (true|false) "false"&gt;<br />
 * &lt;!ELEMENT on EMPTY&gt;<br />
 * &lt;!ATTLIST on column CDATA #REQUIRED&gt;<br />
 * &lt;!ELEMENT pk (on)*&gt;<br /> &lt;!ATTLIST pk name CDATA #IMPLIED&gt;<br />
 * &lt;!ATTLIST pk column CDATA #IMPLIED&gt;<br />
 * &lt;!ELEMENT fk (join)*&gt;<br />
 * &lt;!ATTLIST fk name CDATA #IMPLIED&gt;<br />
 * &lt;!ATTLIST fk deferred (true|false) "false"&gt;<br />
 * &lt;!ATTLIST fk column CDATA #IMPLIED&gt;<br />
 * &lt;!ATTLIST fk to-table CDATA #REQUIRED&gt;<br />
 * &lt;!ATTLIST fk delete-action (cascade|default|restrict|none|null)
 * "none"&gt;<br />
 * &lt;!ATTLIST fk update-action (cascade|default|restrict|none|null)
 * "none"&gt;<br /> &lt;!ELEMENT unique (on)*&gt;<br />
 * &lt;!ATTLIST unique name CDATA #IMPLIED&gt;<br />
 * &lt;!ATTLIST unique column CDATA #IMPLIED&gt;<br />
 * &lt;!ATTLIST unique deferred (true|false) "false"&gt;<br />
 * &lt;!ELEMENT join EMPTY&gt;<br />
 * &lt;!ATTLIST join column CDATA #IMPLIED&gt;<br />
 * &lt;!ATTLIST join value CDATA #IMPLIED&gt;<br />
 * &lt;!ATTLIST join to-column CDATA #REQUIRED&gt;<br />
 * &lt;!ELEMENT sequence EMPTY&gt;<br />
 * &lt;!ATTLIST sequence name CDATA #REQUIRED&gt;<br />
 * &lt;!ATTLIST sequence initial-value CDATA #IMPLIED&gt;<br />
 * &lt;!ATTLIST sequence increment CDATA #IMPLIED&gt;<br />
 * &lt;!ATTLIST sequence allocate CDATA #IMPLIED&gt;<br />
 * </code>
 *  Schema parsers are not threadsafe.
 *
 * @author Abe White
 * @nojavadoc
 */
public class XMLSchemaParser
    extends XMLMetaDataParser
    implements SchemaParser {

    private static final Localizer _loc = Localizer.forPackage
        (XMLSchemaParser.class);

    private final DBDictionary _dict;

    // state for current parse
    private SchemaGroup _group = null;
    private Schema _schema = null;
    private Table _table = null;
    private PrimaryKeyInfo _pk = null;
    private IndexInfo _index = null;
    private UniqueInfo _unq = null;
    private ForeignKeyInfo _fk = null;
    private boolean _delay = false;

    // used to collect info on schema elements before they're resolved
    private final Collection<PrimaryKeyInfo> _pkInfos = new LinkedList<PrimaryKeyInfo>();
    private final Collection<IndexInfo> _indexInfos = new LinkedList<IndexInfo>();
    private final Collection<UniqueInfo> _unqInfos = new LinkedList<UniqueInfo>();
    private final Collection<ForeignKeyInfo> _fkInfos = new LinkedList<ForeignKeyInfo>();

    /**
     * Constructor. Supply configuration.
     */
    public XMLSchemaParser(JDBCConfiguration conf) {
        _dict = conf.getDBDictionaryInstance();
        setLog(conf.getLog(JDBCConfiguration.LOG_SCHEMA));
        setParseText(false);
        setSuffix(".schema");
    }

    public boolean getDelayConstraintResolve() {
        return _delay;
    }

    public void setDelayConstraintResolve(boolean delay) {
        _delay = delay;
    }

    public void resolveConstraints() {
        resolvePrimaryKeys();
        resolveIndexes();
        resolveForeignKeys();
        resolveUniques();
        clearConstraintInfo();
    }

    /**
     * Clear constraint infos.
     */
    private void clearConstraintInfo() {
        _pkInfos.clear();
        _indexInfos.clear();
        _fkInfos.clear();
        _unqInfos.clear();
    }

    public SchemaGroup getSchemaGroup() {
        if (_group == null)
            _group = new SchemaGroup();
        return _group;
    }

    public void setSchemaGroup(SchemaGroup group) {
        _group = group;
    }

    /**
     * Parse the schema relating to the given class. The schemas will
     * be added to the current schema group.
     */
    protected void finish() {
        // now resolve pk, idx, fk info
        super.finish();
        if (!_delay)
            resolveConstraints();
    }

    /**
     * Transforms the collected primary key information into actual
     * primary keys on the schema tables.
     */
    private void resolvePrimaryKeys() {
        PrimaryKeyInfo pkInfo;
        String colName;
        Column col;
        for (Iterator<PrimaryKeyInfo> itr = _pkInfos.iterator(); itr.hasNext();) {
            pkInfo = itr.next();
            for (Iterator<String> cols = pkInfo.cols.iterator(); cols.hasNext();) {
                colName = cols.next();
                col = pkInfo.pk.getTable().getColumn(colName);
                if (col == null)
                    throwUserException(_loc.get("pk-resolve", new Object[]
                        { colName, pkInfo.pk.getTable() }));
                pkInfo.pk.addColumn(col);
            }
        }
    }

    /**
     * Transforms the collected index information into actual
     * indexes on the schema tables.
     */
    private void resolveIndexes() {
        IndexInfo indexInfo;
        String colName;
        Column col;
        for (Iterator<IndexInfo> itr = _indexInfos.iterator(); itr.hasNext();) {
            indexInfo = itr.next();
            for (Iterator<String> cols = indexInfo.cols.iterator(); cols.hasNext();) {
                colName = cols.next();
                col = indexInfo.index.getTable().getColumn(colName);
                if (col == null)
                    throwUserException(_loc.get("index-resolve", new Object[]
                        { indexInfo.index, colName,
                            indexInfo.index.getTable() }));
                indexInfo.index.addColumn(col);
            }
        }
    }

    /**
     * Transforms the collected foreign key information into actual
     * foreign keys on the schema tables.
     */
    private void resolveForeignKeys() {
        ForeignKeyInfo fkInfo;
        Table toTable;
        Column col;
        String colName;
        Column pkCol;
        String pkColName;
        PrimaryKey pk;
        Iterator<String> pks;
        Iterator<String> cols;
        for (Iterator<ForeignKeyInfo> itr = _fkInfos.iterator(); itr.hasNext();) {
            fkInfo = itr.next();
            toTable = _group.findTable(fkInfo.toTable);
            if (toTable == null || toTable.getPrimaryKey() == null)
                throwUserException(_loc.get("fk-totable", new Object[]
                    { fkInfo.fk, fkInfo.toTable, fkInfo.fk.getTable() }));

            // check if only one fk column listed using shortcut
            pk = toTable.getPrimaryKey();
            if (fkInfo.cols.size() == 1 && fkInfo.pks.size() == 0)
                fkInfo.pks.add(pk.getColumns()[0].getName());

            // make joins
            pks = fkInfo.pks.iterator();
            for (cols = fkInfo.cols.iterator(); cols.hasNext();) {
                colName = (String) cols.next();
                col = fkInfo.fk.getTable().getColumn(colName);
                if (col == null)
                    throwUserException(_loc.get("fk-nocol",
                        fkInfo.fk, colName, fkInfo.fk.getTable()));

                pkColName = (String) pks.next();
                pkCol = toTable.getColumn(pkColName);
                if (pkCol == null)
                    throwUserException(_loc.get("fk-nopkcol", new Object[]
                        { fkInfo.fk, pkColName, toTable,
                            fkInfo.fk.getTable() }));

                fkInfo.fk.join(col, pkCol);
            }

            // make constant joins
            cols = fkInfo.constCols.iterator();
            for (Iterator<Object> vals = fkInfo.consts.iterator(); vals.hasNext();) {
                colName = cols.next();
                col = fkInfo.fk.getTable().getColumn(colName);
                if (col == null)
                    throwUserException(_loc.get("fk-nocol",
                        fkInfo.fk, colName, fkInfo.fk.getTable()));

                fkInfo.fk.joinConstant(col, vals.next());
            }

            pks = fkInfo.constColsPK.iterator();
            for (Iterator<Object> vals = fkInfo.constsPK.iterator(); vals.hasNext();) {
                pkColName = pks.next();
                pkCol = toTable.getColumn(pkColName);
                if (pkCol == null)
                    throwUserException(_loc.get("fk-nopkcol", new Object[]
                        { fkInfo.fk, pkColName, toTable,
                            fkInfo.fk.getTable() }));

                fkInfo.fk.joinConstant(vals.next(), pkCol);
            }
        }
    }

    /**
     * Transforms the collected unique constraint information into actual
     * constraints on the schema tables.
     */
    private void resolveUniques() {
        UniqueInfo unqInfo;
        String colName;
        Column col;
        for (Iterator<UniqueInfo> itr = _unqInfos.iterator(); itr.hasNext();) {
            unqInfo = itr.next();
            for (Iterator<String> cols = unqInfo.cols.iterator(); cols.hasNext();) {
                colName = (String) cols.next();
                col = unqInfo.unq.getTable().getColumn(colName);
                if (col == null)
                    throwUserException(_loc.get("unq-resolve", new Object[]
                        { unqInfo.unq, colName, unqInfo.unq.getTable() }));
                unqInfo.unq.addColumn(col);
            }
        }
    }

    protected void reset() {
        _schema = null;
        _table = null;
        _pk = null;
        _index = null;
        _fk = null;
        _unq = null;
        if (!_delay)
            clearConstraintInfo();
    }

    protected Reader getDocType()
        throws IOException {
        return new InputStreamReader(XMLSchemaParser.class
            .getResourceAsStream("schemas-doctype.rsrc"));
    }

    protected boolean startElement(String name, Attributes attrs)
        throws SAXException {
        switch (name.charAt(0)) {
            case's':
                if ("schema".equals(name))
                    startSchema(attrs);
                else if ("sequence".equals(name))
                    startSequence(attrs);
                return true;
            case't':
                startTable(attrs);
                return true;
            case'c':
                startColumn(attrs);
                return true;
            case'p':
                startPrimaryKey(attrs);
                return true;
            case'i':
                startIndex(attrs);
                return true;
            case'u':
                startUnique(attrs);
                return true;
            case'f':
                startForeignKey(attrs);
                return true;
            case'o':
                startOn(attrs);
                return true;
            case'j':
                startJoin(attrs);
                return true;
            default:
                return false;
        }
    }

    protected void endElement(String name) {
        switch (name.charAt(0)) {
            case's':
                if ("schema".equals(name))
                    endSchema();
                break;
            case't':
                endTable();
                break;
            case'p':
                endPrimaryKey();
                break;
            case'i':
                endIndex();
                break;
            case'u':
                endUnique();
                break;
            case'f':
                endForeignKey();
                break;
        }
    }

    private void startSchema(Attributes attrs) {
        // creates group if not set
        SchemaGroup group = getSchemaGroup();

        String name = attrs.getValue("name");
        _schema = group.getSchema(name);
        if (_schema == null)
            _schema = group.addSchema(name);
    }

    private void endSchema() {
        _schema = null;
    }

    private void startSequence(Attributes attrs) {
        Sequence seq = _schema.addSequence(attrs.getValue("name"));
        Locator locator = getLocation().getLocator();
        if (locator != null) {
            seq.setLineNumber(locator.getLineNumber());
            seq.setColNumber(locator.getColumnNumber());
        }
        seq.setSource(getSourceFile(), SourceTracker.SRC_XML);
        try {
            String val = attrs.getValue("initial-value");
            if (val != null)
                seq.setInitialValue(Integer.parseInt(val));
            val = attrs.getValue("increment");
            if (val != null)
                seq.setIncrement(Integer.parseInt(val));
            val = attrs.getValue("allocate");
            if (val != null)
                seq.setAllocate(Integer.parseInt(val));
        } catch (NumberFormatException nfe) {
            throwUserException(_loc.get("bad-seq-num", seq.getFullName()));
        }
    }

    private void startTable(Attributes attrs) {
        _table = _schema.addTable(attrs.getValue("name"));
        _table.setSource(getSourceFile(), SourceTracker.SRC_XML);
        Locator locator = getLocation().getLocator();
        if (locator != null) {
            _table.setLineNumber(locator.getLineNumber());
            _table.setColNumber(locator.getColumnNumber());
        }
    }

    private void endTable() {
        _table = null;
    }

    private void startColumn(Attributes attrs) {
        Column col = _table.addColumn(attrs.getValue("name"));
        col.setType(_dict.getPreferredType(Schemas.getJDBCType
            (attrs.getValue("type"))));
        col.setTypeName(attrs.getValue("type-name"));
        String val = attrs.getValue("size");
        if (val != null)
            col.setSize(Integer.parseInt(val));
        val = attrs.getValue("decimal-digits");
        if (val != null)
            col.setDecimalDigits(Integer.parseInt(val));
        col.setNotNull("true".equals(attrs.getValue("not-null")));
        col.setAutoAssigned("true".equals(attrs.getValue("auto-assign"))
            || "true".equals(attrs.getValue("auto-increment"))); // old attr
        col.setDefaultString(attrs.getValue("default"));
    }

    private void startPrimaryKey(Attributes attrs) {
        _pk = new PrimaryKeyInfo();
        _pk.pk = _table.addPrimaryKey(attrs.getValue("name"));
        _pk.pk.setLogical("true".equals(attrs.getValue("logical")));

        String val = attrs.getValue("column");
        if (val != null)
            _pk.cols.add(val);
    }

    private void endPrimaryKey() {
        _pkInfos.add(_pk);
        _pk = null;
    }

    private void startIndex(Attributes attrs) {
        _index = new IndexInfo();
        _index.index = _table.addIndex(attrs.getValue("name"));
        _index.index.setUnique("true".equals(attrs.getValue("unique")));

        String val = attrs.getValue("column");
        if (val != null)
            _index.cols.add(val);
    }

    private void endIndex() {
        _indexInfos.add(_index);
        _index = null;
    }

    private void startUnique(Attributes attrs) {
        _unq = new UniqueInfo();
        _unq.unq = _table.addUnique(attrs.getValue("name"));
        _unq.unq.setDeferred("true".equals(attrs.getValue("deferred")));

        String val = attrs.getValue("column");
        if (val != null)
            _unq.cols.add(val);
    }

    private void endUnique() {
        _unqInfos.add(_unq);
        _unq = null;
    }

    private void startForeignKey(Attributes attrs) {
        _fk = new ForeignKeyInfo();
        _fk.fk = _table.addForeignKey(attrs.getValue("name"));

        if ("true".equals(attrs.getValue("deferred")))
            _fk.fk.setDeferred(true);

        // set update action before delete action in case user incorrectly
        // sets update-action to "none" when there is a delete-action; otherwise
        // setting the update-action to "none" will also automatically set the
        // delete-action to "none", since FKs cannot have one actio be none and
        // the other be non-none
        String action = attrs.getValue("update-action");
        if (action != null)
            _fk.fk.setUpdateAction(ForeignKey.getAction(action));
        action = attrs.getValue("delete-action");
        if (action != null)
            _fk.fk.setDeleteAction(ForeignKey.getAction(action));

        _fk.toTable = attrs.getValue("to-table");
        String val = attrs.getValue("column");
        if (val != null)
            _fk.cols.add(val);
    }

    private void endForeignKey() {
        _fkInfos.add(_fk);
        _fk = null;
    }

    private void startOn(Attributes attrs) {
        String col = attrs.getValue("column");
        if (_pk != null)
            _pk.cols.add(col);
        else if (_index != null)
            _index.cols.add(col);
        else
            _unq.cols.add(col);
    }

    private void startJoin(Attributes attrs) {
        String col = attrs.getValue("column");
        String toCol = attrs.getValue("to-column");
        String val = attrs.getValue("value");
        if (val == null) {
            _fk.cols.add(col);
            _fk.pks.add(toCol);
        } else if (col == null) {
            _fk.constsPK.add(convertConstant(val));
            _fk.constColsPK.add(toCol);
        } else {
            _fk.consts.add(convertConstant(val));
            _fk.constCols.add(col);
        }
    }

    private static Object convertConstant(String val) {
        if ("null".equals(val))
            return null;
        if (val.startsWith("'"))
            return val.substring(1, val.length() - 1);
        if (val.indexOf('.') == -1)
            return new Long(val);
        return new Double(val);
    }

    private void throwUserException(Message msg) {
        throw new UserException(getSourceName() + ": " + msg.getMessage());
    }

    /**
     * Used to hold primary key info before it is resolved.
     */
    private static class PrimaryKeyInfo {

        public PrimaryKey pk = null;
        public Collection<String> cols = new LinkedList<String>();
    }

    /**
     * Used to hold index info before it is resolved.
     */
    private static class IndexInfo {

        public Index index = null;
        public Collection<String> cols = new LinkedList<String>();
    }

    /**
     * Used to hold unique constraint info before it is resolved.
     */
    public static class UniqueInfo {

        public Unique unq = null;
        public Collection<String> cols = new LinkedList<String>();
    }

    /**
     * Used to hold foreign key info before it is resolved.
     */
    private static class ForeignKeyInfo {

        public ForeignKey fk = null;
        public String toTable = null;
        public Collection<String> cols = new LinkedList<String>();
        public Collection<String> pks = new LinkedList<String>();
        public Collection<Object> consts = new LinkedList<Object>();
        public Collection<String> constCols = new LinkedList<String>();
        public Collection<Object> constsPK = new LinkedList<Object>();
        public Collection<String> constColsPK = new LinkedList<String>();
    }
}
