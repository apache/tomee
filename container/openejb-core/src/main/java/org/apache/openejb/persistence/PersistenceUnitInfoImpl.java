/*
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


import org.apache.openejb.OpenEJB;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.jdbc.managed.xa.DataSourceXADataSource;
import org.apache.openejb.util.URLs;
import org.apache.openejb.util.classloader.URLClassLoaderFirst;

import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import jakarta.transaction.TransactionSynchronizationRegistry;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
     * Does this persistence unit participate in JTA transactions or does it manage
     * resource local transactions using the JDBC APIs.
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
    private final List<String> mappingFileNames = new ArrayList<>();

    /**
     * The jar file locations that make up this persistence unit.
     */
    private final List<URL> jarFileUrls = new ArrayList<>();

    /**
     * Location of the root of the persistent unit.  The directory in which
     * META-INF/persistence.xml is located.
     */
    private URL persistenceUnitRootUrl;

    /**
     * List of the managed entity classes.
     */
    private final List<String> managedClassNames = new ArrayList<>();

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

    // JPA 2.0
    /**
     * Schema version of the persistence.xml file
     */
    private String persistenceXMLSchemaVersion;

    /**
     * Second-level cache mode for the persistence unit
     */
    private SharedCacheMode sharedCacheMode = SharedCacheMode.UNSPECIFIED;

    /**
     * The validation mode to be used for the persistence unit
     */
    private ValidationMode validationMode;

    /**
     * just to be able to dump this PU at runtime
     */
    private String jtaDataSourceName;
    /**
     * just to be able to dump this PU at runtime
     */
    private String nonJtaDataSourceName;

    /**
     * does it need to be created lazily (not in constructor)
     */
    private boolean lazilyInitialized;

    public PersistenceUnitInfoImpl() {
        this.persistenceClassLoaderHandler = null;
    }

    public PersistenceUnitInfoImpl(final PersistenceClassLoaderHandler persistenceClassLoaderHandler) {
        this.persistenceClassLoaderHandler = persistenceClassLoaderHandler;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public void setPersistenceUnitName(final String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    public String getPersistenceProviderClassName() {
        return persistenceProviderClassName;
    }

    public void setPersistenceProviderClassName(final String persistenceProviderClassName) {
        this.persistenceProviderClassName = persistenceProviderClassName;
    }

    public PersistenceUnitTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(final PersistenceUnitTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public DataSource getJtaDataSource() {
        return jtaDataSource;
    }

    public void setJtaDataSource(final CommonDataSource jtaDataSource) {
        if (XADataSource.class.isInstance(jtaDataSource)) {
            this.jtaDataSource = new DataSourceXADataSource(
                    jtaDataSource, OpenEJB.getTransactionManager(), SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class));
        } else {
            this.jtaDataSource = DataSource.class.cast(jtaDataSource);
        }
    }

    public DataSource getNonJtaDataSource() {
        return nonJtaDataSource;
    }

    public void setNonJtaDataSource(final CommonDataSource nonJtaDataSource) {
        if (XADataSource.class.isInstance(nonJtaDataSource)) {
            this.nonJtaDataSource = new DataSourceXADataSource(
                    nonJtaDataSource, OpenEJB.getTransactionManager(), SystemInstance.get().getComponent(TransactionSynchronizationRegistry.class));
        } else {
            this.nonJtaDataSource = DataSource.class.cast(nonJtaDataSource);
        }
    }

    public List<String> getMappingFileNames() {
        return mappingFileNames;
    }

    public void setMappingFileNames(final List<String> mappingFileNames) {
        if (mappingFileNames == null) {
            throw new NullPointerException("mappingFileNames is null");
        }
        this.mappingFileNames.clear();
        this.mappingFileNames.addAll(mappingFileNames);
    }

    public void addMappingFileName(final String mappingFileName) {
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

    public void setRootUrlAndJarUrls(final String persistenceUnitRootUrl, final List<String> jarFiles) throws MalformedURLException {
        File root;
        try {
            final URI rootUri = URLs.uri(persistenceUnitRootUrl);
            root = new File(rootUri);
        } catch (final IllegalArgumentException e) {
            root = new File(persistenceUnitRootUrl);
        }

        this.persistenceUnitRootUrl = toUrl(root);
        try {

            if (!jarFiles.isEmpty()) {
                final File tmpRoot;
                if (root.getName().endsWith(".jar")) {
                    tmpRoot = root.getParentFile(); // lib for a war, / of the ear otherwise, no sense in other cases
                } else {
                    tmpRoot = root;
                }

                for (final String path : jarFiles) {
                    jarFileUrls.add(toUrl(new File(tmpRoot, path).getCanonicalFile()));
                }
            }
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private URL toUrl(final File root) throws MalformedURLException {
        if (!root.isFile() && root.getPath().startsWith("jar:file:")) {
            try {
                final String absolutePath = root.getAbsolutePath();
                final int endIndex = absolutePath.indexOf('!');
                if (endIndex > 0) {
                    final File file = new File(absolutePath.substring(0, endIndex));
                    if (file.isFile() && file.getName().endsWith(".jar")) {
                        return file.toURI().toURL();
                    }
                }
                return new URL(root.getPath());
            } catch (final MalformedURLException me) {
                // no-op keep previous behavior
            }
        }
        return root.toURI().toURL();
    }

    public List<String> getManagedClassNames() {
        return managedClassNames;
    }

    public void setManagedClassNames(final List<String> managedClassNames) {
        if (managedClassNames == null) {
            throw new NullPointerException("managedClassNames is null");
        }
        this.managedClassNames.clear();
        this.managedClassNames.addAll(managedClassNames);
    }

    public void addManagedClassName(final String className) {
        managedClassNames.add(className);
    }

    public boolean excludeUnlistedClasses() {
        return excludeUnlistedClasses;
    }

    public void setExcludeUnlistedClasses(final boolean excludeUnlistedClasses) {
        this.excludeUnlistedClasses = excludeUnlistedClasses;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(final Properties properties) {
        this.properties = properties;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void addTransformer(final ClassTransformer classTransformer) {
        if (persistenceClassLoaderHandler != null) {
            final PersistenceClassFileTransformer classFileTransformer = new PersistenceClassFileTransformer(classTransformer);
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

    // for emf in webapp of ears
    public boolean isLazilyInitialized() {
        return lazilyInitialized;
    }

    public void setLazilyInitialized(final boolean lazilyInitialized) {
        this.lazilyInitialized = lazilyInitialized;
    }

    public static class PersistenceClassFileTransformer implements ClassFileTransformer {
        private final ClassTransformer classTransformer;

        public PersistenceClassFileTransformer(final ClassTransformer classTransformer) {
            this.classTransformer = classTransformer;
        }

        public byte[] transform(final ClassLoader classLoader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {
            // Example code to easily debug transformation of a specific class
            // if ("org/apache/openejb/test/entity/cmp/BasicCmpBean".equals(className) ||
            //        "org/apache/openejb/test/entity/cmp/BasicCmp2Bean_BasicCmp2Bean".equals(className)) {
            //    System.err.println("Loading " + className);
            // }
            if (className == null) {
                return classfileBuffer;
            }
            final String replace = className.replace('/', '.');
            if (isServerClass(replace)) {
                return classfileBuffer;
            }
            return classTransformer.transform(classLoader, replace, classBeingRedefined, protectionDomain, classfileBuffer);
        }
    }

    // not the shouldSkip() method from UrlClassLoaderFirst since we skip more here
    // we just need JPA stuff so all the tricks we have for the server part are useless
    @SuppressWarnings("RedundantIfStatement")
    public static boolean isServerClass(final String input) {

        String name = input;

        if (name == null) {
            return false;
        }

        if (name.startsWith("openejb.shade.")) {
            name = name.substring("openejb.shade.".length());
        }

        for (final String prefix : URLClassLoaderFirst.FORCED_SKIP) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        for (final String prefix : URLClassLoaderFirst.FORCED_LOAD) {
            if (name.startsWith(prefix)) {
                return false;
            }
        }

        if (name.startsWith("java.")) {
            return true;
        }
        if (name.startsWith("javax.")) {
            return true;
        }
        if (name.startsWith("jakarta.")) {
            return true;
        }
        if (name.startsWith("sun.")) {
            return true;
        }
        if (name.startsWith("com.sun.")) {
            return true;
        }

        if (name.startsWith("org.")) {
            final String org = name.substring("org.".length());

            if (org.startsWith("apache.")) {
                final String apache = org.substring("apache.".length());

                if (apache.startsWith("bval.")) {
                    return true;
                }
                if (apache.startsWith("openjpa.")) {
                    return true;
                }
                if (apache.startsWith("derby.")) {
                    return true;
                }
                if (apache.startsWith("xbean.")) {
                    return true;
                }
                if (apache.startsWith("geronimo.")) {
                    return true;
                }
                if (apache.startsWith("coyote")) {
                    return true;
                }
                if (apache.startsWith("webbeans.")) {
                    return true;
                }
                if (apache.startsWith("log4j")) {
                    return true;
                }
                if (apache.startsWith("catalina")) {
                    return true;
                }
                if (apache.startsWith("jasper.")) {
                    return true;
                }
                if (apache.startsWith("tomcat.")) {
                    return true;
                }
                if (apache.startsWith("el.")) {
                    return true;
                }
                if (apache.startsWith("jsp")) {
                    return true;
                }
                if (apache.startsWith("naming")) {
                    return true;
                }
                if (apache.startsWith("taglibs.")) {
                    return true;
                }
                if (apache.startsWith("openejb.")) {
                    return true;
                }
                if (apache.startsWith("openjpa.")) {
                    return true;
                }
                if (apache.startsWith("myfaces.")) {
                    return true;
                }
                if (apache.startsWith("juli.")) {
                    return true;
                }
                if (apache.startsWith("webbeans.")) {
                    return true;
                }
                if (apache.startsWith("cxf.")) {
                    return true;
                }
                if (apache.startsWith("activemq.")) {
                    return true;
                }

                if (apache.startsWith("commons.")) {
                    final String commons = apache.substring("commons.".length());

                    // don't stop on commons package since we don't bring all commons
                    if (commons.startsWith("beanutils")) {
                        return true;
                    }
                    if (commons.startsWith("cli")) {
                        return true;
                    }
                    if (commons.startsWith("codec")) {
                        return true;
                    }
                    if (commons.startsWith("collections")) {
                        return true;
                    }
                    if (commons.startsWith("dbcp")) {
                        return true;
                    }
                    if (commons.startsWith("digester")) {
                        return true;
                    }
                    if (commons.startsWith("jocl")) {
                        return true;
                    }
                    if (commons.startsWith("lang")) {
                        return true;
                    }
                    if (commons.startsWith("logging")) {
                        return false;
                    }
                    if (commons.startsWith("pool")) {
                        return true;
                    }
                    if (commons.startsWith("net")) {
                        return true;
                    }

                    return false;
                }

                return false;
            }

            // other org packages
            if (org.startsWith("codehaus.swizzle")) {
                return true;
            }
            if (org.startsWith("w3c.dom")) {
                return true;
            }
            if (org.startsWith("quartz")) {
                return true;
            }
            if (org.startsWith("eclipse.jdt.")) {
                return true;
            }
            if (org.startsWith("slf4j")) {
                return true;
            }
            if (org.startsWith("openejb")) {
                return true; // old packages
            }
            if (org.startsWith("hsqldb")) {
                return true; // old packages
            }
            if (org.startsWith("hibernate")) {
                return true; // old packages
            }

            return false;
        }

        // other packages
        if (name.startsWith("com.sun.")) {
            return true;
        }
        if (name.startsWith("jdk.")) {
            return true;
        }
        if (name.startsWith("javassist")) {
            return true;
        }
        if (name.startsWith("serp.")) {
            return true;
        }

        return false;
    }

    // JPA 2.0
    /* (non-Javadoc)
     * @see jakarta.persistence.spi.PersistenceUnitInfo#getPersistenceXMLSchemaVersion()
     */
    public String getPersistenceXMLSchemaVersion() {
        return this.persistenceXMLSchemaVersion;
    }

    /**
     * @param persistenceXMLSchemaVersion the persistenceXMLSchemaVersion to set
     */
    public void setPersistenceXMLSchemaVersion(final String persistenceXMLSchemaVersion) {
        this.persistenceXMLSchemaVersion = persistenceXMLSchemaVersion;
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.spi.PersistenceUnitInfo#getSharedCacheMode()
     */
    public SharedCacheMode getSharedCacheMode() {
        return this.sharedCacheMode;
    }

    /**
     * @param sharedCacheMode the sharedCacheMode to set
     */
    public void setSharedCacheMode(final SharedCacheMode sharedCacheMode) {
        this.sharedCacheMode = (null != sharedCacheMode ? sharedCacheMode : SharedCacheMode.UNSPECIFIED);
    }

    /* (non-Javadoc)
     * @see jakarta.persistence.spi.PersistenceUnitInfo#getValidationMode()
     */
    public ValidationMode getValidationMode() {
        return this.validationMode;
    }

    /**
     * @param validationMode the validationMode to set
     */
    public void setValidationMode(final ValidationMode validationMode) {
        this.validationMode = validationMode;
    }

    public String getJtaDataSourceName() {
        return jtaDataSourceName;
    }

    public void setJtaDataSourceName(final String jtaDataSourceName) {
        this.jtaDataSourceName = jtaDataSourceName;
    }

    public String getNonJtaDataSourceName() {
        return nonJtaDataSourceName;
    }

    public void setNonJtaDataSourceName(final String nonJtaDataSourceName) {
        this.nonJtaDataSourceName = nonJtaDataSourceName;
    }
}
