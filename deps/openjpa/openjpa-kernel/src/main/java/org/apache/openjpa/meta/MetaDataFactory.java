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
 * The {@link MetaDataRepository} uses implementations of this interface
 * to load and store metadata. Implementations need not be threadsafe.
 *
 * @author Patrick Linskey
 * @author Abe White
 * @author Pinaki Poddar
 */
public interface MetaDataFactory
    extends MetaDataModes {

    public static final int STORE_DEFAULT = 0;
    public static final int STORE_PER_CLASS = 1;
    public static final int STORE_VERBOSE = 2;

    /**
     * Set the repository to load metadata into.
     * This method will be called before use.
     */
    public void setRepository(MetaDataRepository repos);

    /**
     * Base directory for storing metadata. May not be called.
     */
    public void setStoreDirectory(File dir);

    /**
     * Storage mode. May not be called.
     */
    public void setStoreMode(int store);

    /**
     * If true, I/O's must exactly obey the mode directives given, and may
     * not load additional information.
     */
    public void setStrict(boolean strict);

    /**
     * Load metadata for the given class in the given mode(s). If loading
     * in {@link MetaDataModes#MODE_QUERY}, the class may be null. Loaded
     * metadata should be added directly to the repository. It should have
     * its source mode set appropriately via
     * {@link ClassMetaData#setSourceMode}.
     *
     * @param mode the mode to load metadata in: if mapping information is
     * stored together with metadata, then you can load mapping
     * data even if this mode only includes
     * {@link MetaDataModes#MODE_META MODE_META}, so long as
     * the <code>strict</code> property hasn't been set
     */
    public void load(Class<?> cls, int mode, ClassLoader envLoader);

    /**
     * Store the given metadata.
     *
     * @param mode hint about what aspects of the metadata have changed
     * @param output if non-null, rather than storing metadata directly,
     * add entries mapping each output destination such
     * as a <code>File</code> to the planned output for that
     * destination in string form
     * @return false if this factory is unable to store metadata
     */
    public boolean store(ClassMetaData[] metas, QueryMetaData[] queries,
        SequenceMetaData[] seqs, int mode, Map<File, String> output);

    /**
     * Drop the metadata for the given classes in the given mode(s).
     *
     * @return false if any metadata could not be dropped
     */
    public boolean drop(Class<?>[] cls, int mode, ClassLoader envLoader);

    /**
     * Return the metadata defaults for this factory.
     */
    public MetaDataDefaults getDefaults();

    /**
     * Return all persistent class names, using the metadata locations supplied
     * in configuration, optionally scanning the classpath.
     * Return null if no types are supplied and this factory is unable to scan
     * the classpath. This method should not be used directly by outside
     * code; use {@link MetaDataRepository#getPersistentTypeNames} instead.
     *
     * @see MetaDataRepository#getPersistentTypeNames
     * @see MetaDataRepository#loadPersistentTypes
     */
    public Set<String> getPersistentTypeNames(boolean devpath, 
    		ClassLoader envLoader);

    /**
     * Return the type defining the given query name, if any.
     */
    public Class<?> getQueryScope(String queryName, ClassLoader loader);

    /**
     * Return the type defining the given result set mapping name, if any.
     */
    public Class<?> getResultSetMappingScope(String resultSetMappingName,
        ClassLoader loader);
    
    /**
     * Return a properly-configured class arg parser for our expected
     * metadata format.
     */
    public ClassArgParser newClassArgParser();

    /**
     * Clear any internal caches.
     */
    public void clear();

    /**
     * Add any extension keys used by this instance to the given set.
     */
    public void addClassExtensionKeys(Collection<?> exts);

    /**
     * Add any extension keys used by this instance to the given set.
     */
    public void addFieldExtensionKeys (Collection<?> exts);

    /**
     * Load XMLClassMetadata for the given class. Loaded
     * metadata should be added directly to the repository.
     */
    public void loadXMLMetaData(Class<?> cls);
    
    /**
     * Gets the name of the meta-model class for the given fully-qualified
     * managed class name.
     * 
     * @since 2.0.0
     */
    public String getMetaModelClassName(String managedClassName);
    
    /**
     * Gets the name of the managed class for the given fully-qualified
     * meta-model class name.
     * 
     * @since 2.0.0
     */
    public String getManagedClassName(String metamodelClassName);
    
    /**
     * Affirms if the given class is a meta-class.
     * 
     * @since 2.0.0
     */
    public boolean isMetaClass(Class<?> c);
    
    /**
     * Gets the managed class corresponding to the given meta-class.
     * 
     * @return null if the given input is not a meta-class.
     * 
     * @since 2.0.0
     */
    public Class<?> getManagedClass(Class<?> c);

}
