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
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.lib.meta.ClassArgParser;

/**
 * Base class for factory instances that use a delegate.
 *
 * @author Abe White
 * @nojavadoc
 */
public class DelegatingMetaDataFactory
    implements MetaDataFactory {

    private final MetaDataFactory _delegate;

    /**
     * Constructor; supply delegate.
     */
    public DelegatingMetaDataFactory(MetaDataFactory delegate) {
        _delegate = delegate;
    }

    /**
     * Factory delegate.
     */
    public MetaDataFactory getDelegate() {
        return _delegate;
    }

    /**
     * Innermost delegate.
     */
    public MetaDataFactory getInnermostDelegate() {
        if (_delegate instanceof DelegatingMetaDataFactory)
            return ((DelegatingMetaDataFactory) _delegate).
                getInnermostDelegate();
        return _delegate;
    }

    public void setRepository(MetaDataRepository repos) {
        _delegate.setRepository(repos);
    }

    public void setStoreDirectory(File dir) {
        _delegate.setStoreDirectory(dir);
    }

    public void setStoreMode(int store) {
        _delegate.setStoreMode(store);
    }

    public void setStrict(boolean strict) {
        _delegate.setStrict(true);
    }

    public void load(Class cls, int mode, ClassLoader envLoader) {
        _delegate.load(cls, mode, envLoader);
    }

    public boolean store(ClassMetaData[] metas, QueryMetaData[] queries,
        SequenceMetaData[] seqs, int mode, Map output) {
        return _delegate.store(metas, queries, seqs, mode, output);
    }

    public boolean drop(Class[] cls, int mode, ClassLoader envLoader) {
        return _delegate.drop(cls, mode, envLoader);
    }

    public MetaDataDefaults getDefaults() {
        return _delegate.getDefaults();
    }

    public ClassArgParser newClassArgParser() {
        return _delegate.newClassArgParser();
    }

    public Set getPersistentTypeNames(boolean classpath,
        ClassLoader envLoader) {
        return _delegate.getPersistentTypeNames(classpath, envLoader);
    }

    public Class getQueryScope(String queryName, ClassLoader loader) {
        return _delegate.getQueryScope(queryName, loader);
    }

    public Class getResultSetMappingScope(String resultSetMappingName,
        ClassLoader loader) {
        return _delegate.getResultSetMappingScope(resultSetMappingName, loader);
    }

    public void clear() {
        _delegate.clear();
    }

    public void addClassExtensionKeys(Collection exts) {
        _delegate.addClassExtensionKeys(exts);
    }

    public void addFieldExtensionKeys(Collection exts) {
        _delegate.addFieldExtensionKeys(exts);
    }

    public void loadXMLMetaData(Class<?> cls) {
        _delegate.loadXMLMetaData(cls);
    }
    
    public String getMetaModelClassName(String managedClassName) {
        return _delegate.getMetaModelClassName(managedClassName);
    }
    public String getManagedClassName(String metamodelClassName) {
        return _delegate.getManagedClassName(metamodelClassName);
    }
    
    public boolean isMetaClass(Class<?> c) {
        return _delegate.isMetaClass(c);
    }

    public Class<?> getManagedClass(Class<?> c) {
        return _delegate.getManagedClass(c);
    }
}
