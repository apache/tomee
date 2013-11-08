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

import java.io.File;

import org.apache.openjpa.jdbc.identifier.DBIdentifier;
import org.apache.openjpa.jdbc.identifier.QualifiedDBIdentifier;
import org.apache.openjpa.lib.meta.SourceTracker;

/**
 * Represents a database sequence.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
public class Sequence
    extends ReferenceCounter
    implements Comparable<Sequence>, SourceTracker {

    private DBIdentifier _name = DBIdentifier.NULL;
    private Schema _schema = null;
    private DBIdentifier _schemaName = DBIdentifier.NULL;
    private int _initial = 1;
    private int _increment = 1;
    private int _cache = 0;
    private int _lineNum = 0;  
    private int _colNum = 0;  
    private QualifiedDBIdentifier _fullPath = null;
    
    // keep track of source
    private File _source = null;
    private int _srcType = SRC_OTHER;

    /**
     * Default constructor.
     */
    public Sequence() {
    }

    /**
     * Constructor.
     *
     * @param name the sequence name
     * @param schema the sequence schema
     * @deprecated
     */
    public Sequence(String name, Schema schema) {
        this(DBIdentifier.newSequence(name), schema);
    }

    public Sequence(DBIdentifier name, Schema schema) {
        setIdentifier(name);
        if (schema != null)
            setSchemaIdentifier(schema.getIdentifier());
        _schema = schema;
    }

    /**
     * Called when the sequence is removed from its schema.
     */
    void remove() {
        _schema = null;
        _fullPath = null;
    }

    /**
     * Return the schema for the sequence.
     */
    public Schema getSchema() {
        return _schema;
    }

    /**
     * The sequence's schema name.
     */
    public String getSchemaName() {
        return getSchemaIdentifier().getName();
    }
    
    public DBIdentifier getSchemaIdentifier() {
        return _schemaName == null ? DBIdentifier.NULL : _schemaName;
    }

    /**
     * The sequence's schema name. You can only call this method on sequences
     * whose schema object is not set.
     * @deprecated
     */
    public void setSchemaName(String name) {
        setSchemaIdentifier(DBIdentifier.newSchema(name));
    }

    public void setSchemaIdentifier(DBIdentifier name) {
        if (getSchema() != null)
            throw new IllegalStateException();
        _schemaName = name;
        _fullPath = null;
    }

    /**
     * Return the name of the sequence.
     * @deprecated
     */
    public String getName() {
        return getIdentifier().getName();
    }

    public DBIdentifier getIdentifier() {
        return _name == null ? DBIdentifier.NULL : _name;
    }

    /**
     * Set the name of the sequence. This method can only be called on
     * sequences that are not part of a schema.
     * @deprecated
     */
    public void setName(String name) {
        setIdentifier(DBIdentifier.newSequence(name));
    }

    public void setIdentifier(DBIdentifier name) {
        if (getSchema() != null)
            throw new IllegalStateException();
        _name = name;
        _fullPath = null;
    }

    /**
     * Return the sequence name, including schema, using '.' as the
     * catalog separator.
     * @deprecated
     */
    public String getFullName() {
        return getFullIdentifier().getName();
    }

    public DBIdentifier getFullIdentifier() {
        return getQualifiedPath().getIdentifier();
    }

    public QualifiedDBIdentifier getQualifiedPath() {
        if (_fullPath  == null) {
            _fullPath = QualifiedDBIdentifier.newPath(_schemaName, _name );
        }
        return _fullPath;
    }

    /**
     * The sequence's initial value.
     */
    public int getInitialValue() {
        return _initial;
    }

    /**
     * The sequence's initial value.
     */
    public void setInitialValue(int initial) {
        _initial = initial;
    }

    /**
     * The sequence's increment.
     */
    public int getIncrement() {
        return _increment;
    }

    /**
     * The sequence's increment.
     */
    public void setIncrement(int increment) {
        _increment = increment;
    }

    /**
     * The sequence's cache size.
     */
    public int getAllocate() {
        return _cache;
    }

    /**
     * The sequence's cache size.
     */
    public void setAllocate(int cache) {
        _cache = cache;
    }

    public File getSourceFile() {
        return _source;
    }

    public Object getSourceScope() {
        return null;
    }

    public int getSourceType() {
        return _srcType;
    }

    public void setSource(File source, int srcType) {
        _source = source;
        _srcType = srcType;
    }

    public String getResourceName() {
        return getFullIdentifier().getName();
    }

    public int compareTo(Sequence other) {
        DBIdentifier name = getIdentifier();
        DBIdentifier otherName = other.getIdentifier();
        if (DBIdentifier.isNull(name) && DBIdentifier.isNull(otherName)) {
            return 0;
        }
        if (DBIdentifier.isNull(name))
            return 1;
        if (DBIdentifier.isNull(otherName))
            return -1;
        return name.compareTo(otherName);
    }

    public String toString() {
        return getFullIdentifier().getName();
    }
    
    public int getLineNumber() {
        return _lineNum;
    }

    public void setLineNumber(int lineNum) {
        _lineNum = lineNum;
    }

    public int getColNumber() {
        return _colNum;
    }

    public void setColNumber(int colNum) {
        _colNum = colNum;
    }

}
