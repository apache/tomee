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
package org.apache.openjpa.meta;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.kernel.Query;
import org.apache.openjpa.kernel.jpql.JPQLParser;
import org.apache.openjpa.lib.meta.SourceTracker;
import org.apache.openjpa.lib.util.OrderedMap;
import org.apache.openjpa.lib.xml.Commentable;

/**
 * Holds metadata about named queries.
 *  Information stored in this instance gets transfered to
 * new {@link Query} instances.
 *
 * @author Steve Kim
 */
public class QueryMetaData
    implements MetaDataModes, SourceTracker, Commentable, Serializable {

    private static final String[] EMPTY_KEYS = new String[0];
    private static final Object[] EMPTY_VALS = new Object[0];

    private final String _name;
    private Boolean _readOnly;
    private File _file;
    private Object _scope;
    private int _srcType;
    private int _mode = MODE_QUERY;
    private String _language;
    private Class _class;
    private Class _candidate;
    private Class _res;
    private String _query;
    private String[] _comments;
    private List _hintKeys;
    private List _hintVals;
    private String _resultSetMappingName;
    private int _lineNum;  
    private int _colNum;
    private String _srcName; 
    private boolean _convertPositionalParametersToNamed;
    private OrderedMap<Object,Class<?>> _paramTypes;
    
    /**
     * Construct with the given name.
     */
    protected QueryMetaData(String name, boolean convertPositionalParametersToNamed) {
        _name = name;
        _convertPositionalParametersToNamed = convertPositionalParametersToNamed;
    }

    /**
     * Return the name for this query.
     */
    public String getName() {
        return _name;
    }

    /**
     * The class that defines this query, or null if none.
     */
    public Class getDefiningType() {
        return _class;
    }

    /**
     * The class that defines this query, or null if none.
     */
    public void setDefiningType(Class cls) {
        _class = cls;
    }

    /**
     * Whether the query has been marked read-only.
     */
    public boolean isReadOnly() {
        return _readOnly != null && _readOnly.booleanValue();
    }

    /**
     * Whether the query has been marked read-only.
     */
    public void setReadOnly(boolean readOnly) {
        _readOnly = (readOnly) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * The query candidate class, or null if none.
     */
    public Class getCandidateType() {
        return _candidate;
    }

    /**
     * The query result class, or null if none.
     */
    public void setCandidateType(Class cls) {
        _candidate = cls;
    }

    /**
     * The query result class, or null if none.
     */
    public Class getResultType() {
        return _res;
    }

    /**
     * The query result class, or null if none.
     */
    public void setResultType(Class cls) {
        _res = cls;
    }

    /**
     * Return the query language.
     */
    public String getLanguage() {
        return _language;
    }

    /**
     * Set the language for this query.
     */
    public void setLanguage(String language) {
        _language = language;
    }

    /**
     * The full query string, or null if none.
     */
    public String getQueryString() {
        return _query;
    }

    /**
     * The full query string, or null if none.
     */
    public void setQueryString(String query) {
        if (query != null && _convertPositionalParametersToNamed && JPQLParser.LANG_JPQL.equals(_language)) {
            query = query.replaceAll("[\\?]", "\\:_");
        }
        _query = query;
    }

    /**
     * Query hints.
     */
    public String[] getHintKeys() {
        return (_hintKeys == null) ? EMPTY_KEYS
            : (String[]) _hintKeys.toArray(new String[_hintKeys.size()]);
    }

    /**
     * Query hints.
     */
    public Object[] getHintValues() {
        return (_hintVals == null) ? EMPTY_VALS : _hintVals.toArray();
    }

    /**
     * Add a query hint.
     */
    public void addHint(String key, Object value) {
        if (_hintKeys == null) {
            _hintKeys = new LinkedList();
            _hintVals = new LinkedList();
        }
        _hintKeys.add(key);
        _hintVals.add(value);
    }

    public String getResultSetMappingName() {
        return _resultSetMappingName;
    }

    public void setResultSetMappingName(String setMappingName) {
        _resultSetMappingName = setMappingName;
    }
    
    /**
     * Set query template information into the given concrete
     * query instance. However, the language, query string, and
     * candidate class are assumed to be declared in the query
     * instantiation, and hints are not transferred.
     */
    public void setInto(Query query) {
        if (_candidate != null)
            query.setCandidateType(_candidate, true);
        if (!StringUtils.isEmpty(_query))
            query.setQuery(_query);
        if (_res != null)
            query.setResultType(_res);
        if (_readOnly != null)
            query.setReadOnly(_readOnly.booleanValue());
        if (_resultSetMappingName != null)
            query.setResultMapping(null, _resultSetMappingName);
    }

    /**
     * Initialize this instance from the values held in the
     * specified {@link Query}.
     */
    public void setFrom(Query query) {
        _language = query.getLanguage();
        _candidate = query.getCandidateType();
        _res = query.getResultType();
        _query = query.getQueryString();
    }

    /**
     * The source mode of this query.
     */
    public int getSourceMode() {
        return _mode;
    }

    /**
     * The source mode of this query.
     */
    public void setSourceMode(int mode) {
        _mode = mode;
    }

    public String toString() {
        return _name;
    }

    ///////////////
    // Commentable
    ///////////////

    public String[] getComments() {
        return (_comments == null) ? EMPTY_COMMENTS : _comments;
    }

    public void setComments(String[] comments) {
        _comments = comments;
    }

    ////////////////////////////////
    // SourceTracker implementation
    ////////////////////////////////

    public File getSourceFile() {
        return _file;
    }

    public Object getSourceScope() {
        return _scope;
    }

    public int getSourceType() {
        return _srcType;
    }

    public void setSource(File file, Object scope, int srcType, String srcName) {
        _file = file;
        _scope = scope;
        _srcType = srcType;
        _srcName = srcName;
    }

    public String getResourceName() {
        return (_class == null) ? _name : _class.getName () + ":" + _name;
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
    
    public String getSourceName() {
        return _srcName;
    }

    public void setParamTypes(OrderedMap<Object, Class<?>> paramTypes) {
        _paramTypes = paramTypes;
    }

    /**
     * @return a map of parameter name to type for this named query or <b>null if this data hasn't been set.</b>
     */
    public OrderedMap<Object, Class<?>> getParamTypes() {
        return _paramTypes;
    }
}
