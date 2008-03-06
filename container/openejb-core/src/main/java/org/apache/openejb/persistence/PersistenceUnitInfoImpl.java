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

import org.apache.openejb.util.Join;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.File;
import java.io.IOException;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

public class PersistenceUnitInfoImpl implements PersistenceUnitInfo {
    /**
     * External handler which handles adding a runtime ClassTransformer to the classloader.
     */
    private final PersistenceClassLoaderHandler persistenceClassLoaderHandler;

    /**
     * The unique id of this persistence unit.
     */
    private String id;

    /**
     * Name of this persistence unit.  The JPA specification has restrictions on the
     * uniqueness of this name.
     */
    private String persistenceUnitName;

    /**
     * Name of the persistence provider implementation class.
     */
    private String persistenceProviderClassName;

    /**
     * Does this persistence unti participate in JTA transactions or does it manage
     * resource local tranactions using the JDBC APIs.
     */
    private PersistenceUnitTransactionType transactionType = PersistenceUnitTransactionType.JTA;

    /**
     * Data source used by jta persistence units for accessing transactional data.
     */
    private DataSource jtaDataSource;

    /**
     * Data source used by non-jta persistence units and by jta persistence units for
     * non-transactional operations such as accessing a primary key table or sequence.
     */
    private DataSource nonJtaDataSource;

    /**
     * Names if the entity-mapping.xml files relative to to the persistenceUnitRootUrl.
     */
    private final List<String> mappingFileNames = new ArrayList<String>();

    /**
     * The jar file locations that make up this persistence unit.
     */
    private final List<URL> jarFileUrls = new ArrayList<URL>();

    /**
     * Location of the root of the persistent unit.  The directory in which
     * META-INF/persistence.xml is located.
     */
    private URL persistenceUnitRootUrl;

    /**
     * List of the managed entity classes.
     */
    private final List<String> managedClassNames = new ArrayList<String>();

    /**
     * Should class not listed in the persistence unit be managed by the EntityManager?
     */
    private boolean excludeUnlistedClasses;

    /**
     * JPA provider properties for this persistence unit.
     */
    private Properties properties;

    /**
     * Class loader used by JPA to load Entity classes.
     */
    private ClassLoader classLoader;

    public PersistenceUnitInfoImpl() {
        this.persistenceClassLoaderHandler = null;
    }

    public PersistenceUnitInfoImpl(PersistenceClassLoaderHandler persistenceClassLoaderHandler) {
        this.persistenceClassLoaderHandler = persistenceClassLoaderHandler;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public void setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    public String getPersistenceProviderClassName() {
        return persistenceProviderClassName;
    }

    public void setPersistenceProviderClassName(String persistenceProviderClassName) {
        this.persistenceProviderClassName = persistenceProviderClassName;
    }

    public PersistenceUnitTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(PersistenceUnitTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public DataSource getJtaDataSource() {
        return jtaDataSource;
    }

    public void setJtaDataSource(DataSource jtaDataSource) {
        this.jtaDataSource = jtaDataSource;
    }

    public DataSource getNonJtaDataSource() {
        return nonJtaDataSource;
    }

    public void setNonJtaDataSource(DataSource nonJtaDataSource) {
        this.nonJtaDataSource = nonJtaDataSource;
    }

    public List<String> getMappingFileNames() {
        return mappingFileNames;
    }

    public void setMappingFileNames(List<String> mappingFileNames) {
        if (mappingFileNames == null) {
            throw new NullPointerException("mappingFileNames is null");
        }
        this.mappingFileNames.clear();
        this.mappingFileNames.addAll(mappingFileNames);
    }

    public void addMappingFileName(String mappingFileName) {
        if (mappingFileName == null) {
            throw new NullPointerException("mappingFileName is null");
        }
        mappingFileNames.add(mappingFileName);
    }

    public List<URL> getJarFileUrls() {
        return jarFileUrls;
    }

    public URL getPersistenceUnitRootUrl() {
        return persistenceUnitRootUrl;
    }

    public void setRootUrlAndJarUrls(String persistenceUnitRootUrl, List<String> jarFiles) throws MalformedURLException {
        File root = new File(persistenceUnitRootUrl);

        this.persistenceUnitRootUrl = toUrl(root);
        try {

            for (String path : jarFiles) {
                File file = new File(root, path);
                file = file.getCanonicalFile();
                jarFileUrls.add(toUrl(file));
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private URL toUrl(File root) throws MalformedURLException {
        URL url = root.toURL();

        try {
            url.toURI();
        } catch (URISyntaxException e) {
            // Likely has spaces in it.
            try {
                String s = url.toExternalForm();
                URL fixed = new URL(s.replaceAll(" ", "%20"));
                fixed.toURI();
                url = fixed;
            } catch (MalformedURLException e1) {
            } catch (URISyntaxException e1) {
                // oh well, we tried.
            }
        }

        return url;
    }

    public List<String> getManagedClassNames() {
        return managedClassNames;
    }

    public void setManagedClassNames(List<String> managedClassNames) {
        if (managedClassNames == null) {
            throw new NullPointerException("managedClassNames is null");
        }
        this.managedClassNames.clear();
        this.managedClassNames.addAll(managedClassNames);
    }

    public void addManagedClassName(String className) {
        managedClassNames.add(className);
    }

    public boolean excludeUnlistedClasses() {
        return excludeUnlistedClasses;
    }

    public void setExcludeUnlistedClasses(boolean excludeUnlistedClasses) {
        this.excludeUnlistedClasses = excludeUnlistedClasses;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void addTransformer(ClassTransformer classTransformer) {
        if (persistenceClassLoaderHandler != null) {
            PersistenceClassFileTransformer classFileTransformer = new PersistenceClassFileTransformer(classTransformer);
            persistenceClassLoaderHandler.addTransformer(id, classLoader, classFileTransformer);
        }
    }

    public ClassLoader getNewTempClassLoader() {
        if (persistenceClassLoaderHandler != null) {
            return persistenceClassLoaderHandler.getNewTempClassLoader(classLoader);
        } else {
            return null;
        }
    }

    public static class PersistenceClassFileTransformer implements ClassFileTransformer {
        private final ClassTransformer classTransformer;

        public PersistenceClassFileTransformer(ClassTransformer classTransformer) {
            this.classTransformer = classTransformer;
        }

        public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            // Example code to easily debug transformation of a specific class
            // if ("org/apache/openejb/test/entity/cmp/BasicCmpBean".equals(className) ||
            //        "org/apache/openejb/test/entity/cmp/BasicCmp2Bean_BasicCmp2Bean".equals(className)) {
            //    System.err.println("Loading " + className);
            // }
            byte[] bytes = classTransformer.transform(classLoader, className.replace('/', '.'), classBeingRedefined, protectionDomain, classfileBuffer);
            return bytes;
        }
    }
}
