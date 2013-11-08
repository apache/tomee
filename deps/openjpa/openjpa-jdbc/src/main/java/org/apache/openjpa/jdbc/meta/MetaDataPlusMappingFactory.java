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
package org.apache.openjpa.jdbc.meta;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.DelegatingMetaDataFactory;
import org.apache.openjpa.meta.MetaDataFactory;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.meta.QueryMetaData;
import org.apache.openjpa.meta.SequenceMetaData;

/**
 * Combines two internal {@link MetaDataFactory} instances -- one for
 * metadata, one for mappings -- into a single {@link MetaDataFactory} facade.
 *
 * @author Abe White
 * @nojavadoc
 */
public class MetaDataPlusMappingFactory
    extends DelegatingMetaDataFactory {

    private final MetaDataFactory _map;

    
    /**
     * Constructor; supply delegates.
     */
    public MetaDataPlusMappingFactory(MetaDataFactory meta, MetaDataFactory map) {
        this(meta, map, null);

    }
    
    /**
     * Constructor, supply delegates and Configuration. 
     * 
     * @param meta MetaFactory delegate, should not be null.
     * @param map  MappingFactory delegate, should not be null.
     * @param conf Configuration in use. Used to determine whether delegates should use strict mode. 
     */
    public MetaDataPlusMappingFactory(MetaDataFactory meta, MetaDataFactory map, OpenJPAConfiguration conf) {
        super(meta);
        _map = map;

        if(conf.getCompatibilityInstance().getMetaFactoriesAreStrict()) {
            meta.setStrict(true);
            map.setStrict(true);
        }
    }

    /**
     * Mapping factory delegate.
     */
    public MetaDataFactory getMappingDelegate() {
        return _map;
    }

    /**
     * Innermost mapping delegate.
     */
    public MetaDataFactory getInnermostMappingDelegate() {
        if (_map instanceof DelegatingMetaDataFactory)
            return ((DelegatingMetaDataFactory) _map).getInnermostDelegate();
        return _map;
    }

    public void setRepository(MetaDataRepository repos) {
        super.setRepository(repos);
        _map.setRepository(repos);
    }

    public void setStoreDirectory(File dir) {
        super.setStoreDirectory(dir);
        _map.setStoreDirectory(dir);
    }

    public void setStoreMode(int store) {
        super.setStoreMode(store);
        _map.setStoreMode(store);
    }

    public void setStrict(boolean strict) {
        // always in strict mode
    }

    public void load(Class cls, int mode, ClassLoader envLoader) {
        if ((mode & ~MODE_MAPPING) != MODE_NONE)
            super.load(cls, mode & ~MODE_MAPPING, envLoader);
        if (cls != null && (mode & MODE_MAPPING) != 0)
            _map.load(cls, mode & ~MODE_META, envLoader);
    }

    public boolean store(ClassMetaData[] metas, QueryMetaData[] queries,
        SequenceMetaData[] seqs, int mode, Map output) {
        boolean store = true;
        if ((mode & ~MODE_MAPPING) != MODE_NONE)
            store &= super.store(metas, queries, seqs, mode & ~MODE_MAPPING,
                output);
        if ((mode & MODE_MAPPING) != 0)
            store &= _map.store(metas, queries, seqs, mode & ~MODE_META,
                output);
        return store;
    }

    public boolean drop(Class[] cls, int mode, ClassLoader envLoader) {
        boolean drop = true;
        if ((mode & ~MODE_MAPPING) != MODE_NONE)
            drop &= super.drop(cls, mode & ~MODE_MAPPING, envLoader);
        if ((mode & MODE_MAPPING) != 0)
            drop &= _map.drop(cls, mode & ~MODE_META, envLoader);
        return drop;
    }

    public Set getPersistentTypeNames(boolean classpath,
        ClassLoader envLoader) {
        Set names = super.getPersistentTypeNames(classpath, envLoader);
        if (names != null && !names.isEmpty())
            return names;
        return _map.getPersistentTypeNames(classpath, envLoader);
    }

    public void clear() {
        super.clear();
        _map.clear();
    }

    public void addClassExtensionKeys(Collection exts) {
        super.addClassExtensionKeys(exts);
        _map.addClassExtensionKeys(exts);
    }

    public void addFieldExtensionKeys(Collection exts) {
        super.addFieldExtensionKeys(exts);
        _map.addFieldExtensionKeys(exts);
    }
}
