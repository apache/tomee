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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.meta.ClassArgParser;
import serp.util.Strings;

/**
 * Abstract {@link MetaDataFactory} that provides default implementations
 * of many methods.
 *
 * @author Abe White
 */
public abstract class AbstractMetaDataFactory
    implements MetaDataFactory {

    protected MetaDataRepository repos = null;
    protected transient Log log = null;
    protected File dir = null;
    protected int store = STORE_DEFAULT;
    protected boolean strict = false;
    protected Set types = null;


    /**
     * Set of persistent type names supplied by user.
     */
    public void setTypes(Set types) {
        this.types = types;
    }

    /**
     * Set of semicolon-separated persistent type names supplied by user via
     * auto-configuration.
     */
    public void setTypes(String types) {
        this.types = (StringUtils.isEmpty(types)) ? null
            : new HashSet(Arrays.asList(Strings.split(types, ";", 0)));
    }

    public void setRepository(MetaDataRepository repos) {
        this.repos = repos;
        if (repos != null)
            log = repos.getConfiguration().getLog
                (OpenJPAConfiguration.LOG_METADATA);
    }

    public void setStoreDirectory(File dir) {
        this.dir = dir;
    }

    public void setStoreMode(int store) {
        this.store = store;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    public boolean store(ClassMetaData[] metas, QueryMetaData[] queries,
        SequenceMetaData[] seqs, int mode, Map<File, String> output) {
        return false;
    }

    public boolean drop(Class[] cls, int mode, ClassLoader envLoader) {
        return false;
    }

    public Set getPersistentTypeNames(boolean devpath, ClassLoader envLoader) {
        return types;
    }

    public Class getQueryScope(String queryName, ClassLoader loader) {
        return null;
    }

    public Class getResultSetMappingScope(String resultSetMappingName,
        ClassLoader loader) {
        return null;
    }

    public ClassArgParser newClassArgParser() {
        return new ClassArgParser();
    }

    public void clear() {
    }

    public void addClassExtensionKeys(Collection exts) {
    }

    public void addFieldExtensionKeys(Collection exts) {
    }
}
