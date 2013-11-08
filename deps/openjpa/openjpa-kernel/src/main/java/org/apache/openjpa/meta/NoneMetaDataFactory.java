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
import java.lang.reflect.Member;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.lib.meta.ClassArgParser;

/**
 * No-op metadata I/O to prevent attempts to load other classes.
 *
 * @author Abe White
 * @nojavadoc
 */
public class NoneMetaDataFactory
    implements MetaDataFactory, MetaDataDefaults {

    private static final NoneMetaDataFactory _instance =
        new NoneMetaDataFactory();

    public static NoneMetaDataFactory getInstance() {
        return _instance;
    }

    public void setRepository(MetaDataRepository repos) {
    }

    public void setStoreDirectory(File dir) {
    }

    public void setStoreMode(int store) {
    }

    public void setStrict(boolean strict) {
    }

    public void load(Class cls, int mode, ClassLoader envLoader) {
    }

    public boolean store(ClassMetaData[] metas, QueryMetaData[] queries,
        SequenceMetaData[] seqs, int mode, Map output) {
        return false;
    }

    public boolean drop(Class[] cls, int mode, ClassLoader envLoader) {
        return false;
    }

    public MetaDataDefaults getDefaults() {
        return this;
    }

    public Set getPersistentTypeNames(boolean classpath,
        ClassLoader envLoader) {
        return null;
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

    public int getDefaultAccessType() {
        return AccessCode.UNKNOWN;
    }

    public int getDefaultIdentityType() {
        return ClassMetaData.ID_UNKNOWN;
    }

    public int getCallbackMode() {
        return CALLBACK_IGNORE;
    }

    public boolean getCallbacksBeforeListeners(int type) {
        return false;
    }

    public void setIgnoreNonPersistent(boolean ignore) {
    }

    public boolean isDeclaredInterfacePersistent() {
        return false;
    }

    public boolean isDataStoreObjectIdFieldUnwrapped() {
        return false;
    }

    public void populate(ClassMetaData meta, int access) {
    }

    public void populate(ClassMetaData meta, int access, boolean ignoreTransient) {
    }

    public Member getBackingMember(FieldMetaData fmd) {
        return null;
    }

    public Member getMemberByProperty(ClassMetaData meta, String property,
    		int access, boolean scan) {
    	return null;
    }
    
    public Class<? extends Exception> getUnimplementedExceptionType() {
        return null;
    }
    
    public void loadXMLMetaData(Class<?> cls) {
    }

    public String getMetaModelClassName(String managedClassName) {
        return null;
    }
    public String getManagedClassName(String metamodelClassName) {
        return null;
    }
    
    public boolean isMetaClass(Class<?> c) {
        return false;
    }

    public Class<?> getManagedClass(Class<?> c) {
        return null;
    }

    public boolean isAbstractMappingUniDirectional(OpenJPAConfiguration conf) {
        return false;
    }
    
    public boolean isNonDefaultMappingAllowed(OpenJPAConfiguration conf) {
        return false;
    }
}
