/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.persistence;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

public class PersistenceUnitInfoImpl implements PersistenceUnitInfo {
    private String persistenceUnitName = null;

    private String persistenceProviderClassName = null;

    private PersistenceUnitTransactionType transactionType = PersistenceUnitTransactionType.JTA;

    private DataSource jtaDataSource = null;

    private DataSource nonJtaDataSource = null;

    private List<String> mappingFileNames = null;

    private List<URL> jarFileUrls = null;

    private URL persistenceUnitRootUrl = null;

    private List<String> managedClassNames = null;

    private boolean excludeUnlistedClasses = false;

    private Properties vendorProperties = null;

    private ClassLoader classLoader = null;
    
    private ClassLoader tempClassLoader = null;

    public void setExcludeUnlistedClasses(boolean excludeUnlistedClasses) {
        this.excludeUnlistedClasses = excludeUnlistedClasses;
    }

    public void setProperties(Properties vendorProperties) {
        this.vendorProperties = vendorProperties;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setJarFileUrls(List<String> jarFiles) throws MalformedURLException {
        if (jarFileUrls == null) {
            jarFileUrls = new ArrayList<URL>();
        }

        for (String jarFile : jarFiles) {
            jarFileUrls.add((new File(jarFile)).toURL());
        }
    }

    public void setJtaDataSource(DataSource jtaDataSource) {
        this.jtaDataSource = jtaDataSource;
    }

    public void setManagedClassNames(List<String> managedClassNames) {
        this.managedClassNames = managedClassNames;
    }

    public void addManagedClassName(String className) {
        if (managedClassNames == null) {
            managedClassNames = new ArrayList<String>();
        }
        managedClassNames.add(className);
    }

    public void setMappingFileNames(List<String> mappingFileNames) {
        this.mappingFileNames = mappingFileNames;
    }

    public void addMappingFileName(String mappingFileName) {
        if (mappingFileNames == null) {
            mappingFileNames = new ArrayList<String>();
        }
        mappingFileNames.add(mappingFileName);
    }

    public void setNonJtaDataSource(DataSource nonJtaDataSource) {
        this.nonJtaDataSource = nonJtaDataSource;
    }

    public void setPersistenceProviderClassName(
            String persistenceProviderClassName) {
        this.persistenceProviderClassName = persistenceProviderClassName;
    }

    public void setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    public void setPersistenceUnitRootUrl(URL persistenceUnitRootUrl) {
        this.persistenceUnitRootUrl = persistenceUnitRootUrl;
    }

    public void setTransactionType(
            PersistenceUnitTransactionType transactionType) {
        this.transactionType = transactionType;
    }
    
    void setNewTempClassLoader(ClassLoader tempClassLoader) {
        this.tempClassLoader = tempClassLoader;
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public String getPersistenceProviderClassName() {
        return persistenceProviderClassName;
    }

    public PersistenceUnitTransactionType getTransactionType() {
        return transactionType;
    }

    public DataSource getJtaDataSource() {
        return jtaDataSource;
    }

    public DataSource getNonJtaDataSource() {
        return nonJtaDataSource;
    }

    public List<String> getMappingFileNames() {
        return mappingFileNames;
    }

    public List<URL> getJarFileUrls() {
        return jarFileUrls;
    }

    public URL getPersistenceUnitRootUrl() {
        return persistenceUnitRootUrl;
    }

    public List<String> getManagedClassNames() {
        return managedClassNames;
    }

    public boolean excludeUnlistedClasses() {
        return excludeUnlistedClasses;
    }

    public Properties getProperties() {
        return vendorProperties;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void addTransformer(ClassTransformer arg0) {
        // TODO - Need something to do here
    }

    public ClassLoader getNewTempClassLoader() {
        // TODO - This probably isn't correct and may need changing
        return tempClassLoader;
    }
    
}
