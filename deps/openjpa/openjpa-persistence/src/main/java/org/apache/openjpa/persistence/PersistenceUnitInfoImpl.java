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
package org.apache.openjpa.persistence;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.lib.conf.ProductDerivations;
import org.apache.openjpa.lib.meta.SourceTracker;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.MultiClassLoader;
import org.apache.openjpa.util.ClassResolver;

/**
 * Implementation of the {@link PersistenceUnitInfo} interface used by OpenJPA 
 * when parsing persistence configuration information.
 *
 * @nojavadoc
 */
public class PersistenceUnitInfoImpl
    implements PersistenceUnitInfo, SourceTracker {

    public static final String PERSISTENCE_VERSION = "PersistenceVersion";
    

    private static final Localizer s_loc = Localizer.forPackage
        (PersistenceUnitInfoImpl.class);

    private String _name;
    private final HashMap<Object,Object> _props = new HashMap<Object, Object>();
    private PersistenceUnitTransactionType _transType =
        PersistenceUnitTransactionType.RESOURCE_LOCAL;

    private String _providerClassName;
    private List<String> _mappingFileNames;
    private List<String> _entityClassNames;
    private List<URL> _jarFiles;
    private List<String> _jarFileNames;
    private String _jtaDataSourceName;
    private DataSource _jtaDataSource;
    private String _nonJtaDataSourceName;
    private DataSource _nonJtaDataSource;
    private boolean _excludeUnlisted;
    private URL _persistenceXmlFile;
    private String _schemaVersion = "1.0";
    private ValidationMode _validationMode;
    private SharedCacheMode _sharedCacheMode;

    // A persistence unit is defined by a persistence.xml file. The jar
    // file or directory whose META-INF directory contains the
    // persistence.xml file is termed the root of the persistence unit.
    //
    // In Java EE, the root of a persistence unit may be one of the following:
    // - an EJB-JAR file
    // - the WEB-INF/classes directory of a WAR file[38]
    // - a jar file in the WEB-INF/lib directory of a WAR file
    // - a jar file in the root of the EAR
    // - a jar file in the EAR library directory
    // - an application client jar file
    private URL _root;

    public ClassLoader getClassLoader() {
        return null;
    }

    public ClassLoader getNewTempClassLoader() {
        return AccessController.doPrivileged(J2DoPrivHelper
            .newTemporaryClassLoaderAction(AccessController
                .doPrivileged(J2DoPrivHelper.getContextClassLoaderAction())));
    }

    public String getPersistenceUnitName() {
        return _name;
    }

    public void setPersistenceUnitName(String emName) {
        _name = emName;
    }

    public String getPersistenceProviderClassName() {
        return _providerClassName;
    }

    public void setPersistenceProviderClassName(String providerClassName) {
        _providerClassName = providerClassName;
    }

    public PersistenceUnitTransactionType getTransactionType() {
        return _transType;
    }

    public void setTransactionType(PersistenceUnitTransactionType transType) {
        _transType = transType;
    }

    public String getJtaDataSourceName() {
        return _jtaDataSourceName;
    }

    public void setJtaDataSourceName(String jta) {
        _jtaDataSourceName = jta;
        if (jta != null)
            _jtaDataSource = null;
    }

    public DataSource getJtaDataSource() {
        return _jtaDataSource;
    }

    public void setJtaDataSource(DataSource ds) {
        _jtaDataSource = ds;
        if (ds != null)
            _jtaDataSourceName = null;
    }

    public String getNonJtaDataSourceName() {
        return _nonJtaDataSourceName;
    }

    public void setNonJtaDataSourceName(String nonJta) {
        _nonJtaDataSourceName = nonJta;
        if (nonJta != null)
            _nonJtaDataSource = null;
    }

    public DataSource getNonJtaDataSource() {
        return _nonJtaDataSource;
    }

    public void setNonJtaDataSource(DataSource ds) {
        _nonJtaDataSource = ds;
        if (ds != null)
            _nonJtaDataSourceName = null;
    }

    public URL getPersistenceUnitRootUrl() {
        return _root;
    }

    public void setPersistenceUnitRootUrl(URL root) {
        _root = root;
    }

    public boolean excludeUnlistedClasses() {
        return _excludeUnlisted;
    }

    public void setExcludeUnlistedClasses(boolean excludeUnlisted) {
        _excludeUnlisted = excludeUnlisted;
    }

    public List<String> getMappingFileNames() {
        if (_mappingFileNames == null)
            return Collections.emptyList();
        return _mappingFileNames;
    }

    public void addMappingFileName(String name) {
        if (_mappingFileNames == null)
            _mappingFileNames = new ArrayList<String>();
        _mappingFileNames.add(name);
    }

    public List<URL> getJarFileUrls() {
        if (_jarFiles == null) 
            return Collections.emptyList();
        return _jarFiles;
    }

    public void addJarFile(URL jar) {
        if (_jarFiles == null)
            _jarFiles = new ArrayList<URL>();
        _jarFiles.add(jar);
    }

    public void addJarFileName(String name) {
        // Defer searching the classpath for jar files referenced by the jar-file element until after
        // the XML has been parsed and it has been confirmed that OpenJPA is the desired JPA provider.

        if (_jarFileNames == null) {
            _jarFileNames = new ArrayList<String>();
        }
        _jarFileNames.add(name);
    }

    /**
     * Process jar-file elements. An IllegalArgumentException may be thrown if the jar file does not exist in the
     * classpath.
     */
    public void processJarFileNames() {
        if (_jarFileNames != null) {
            for (String name : _jarFileNames) {
                validateJarFileName(name);
            }

            _jarFileNames.clear();
        }
    }
    
    public void validateJarFileName(String name) {
        ClassLoader contextClassLoader = AccessController.doPrivileged(J2DoPrivHelper.getContextClassLoaderAction());
        MultiClassLoader loader = AccessController
            .doPrivileged(J2DoPrivHelper.newMultiClassLoaderAction());
        loader.addClassLoader(contextClassLoader);
        loader.addClassLoader(getClass().getClassLoader());
        loader.addClassLoader(MultiClassLoader.THREAD_LOADER);
        URL url = AccessController.doPrivileged(
            J2DoPrivHelper.getResourceAction(loader, name));
        if (url != null) {
            addJarFile(url);
            return;
        }

        // jar file is not a resource; check classpath
        String classPath = null;        

        //first check if the classpath is set from ant class loader
        if (contextClassLoader instanceof MultiClassLoader) {
            for (ClassLoader classLoader : ((MultiClassLoader) contextClassLoader).getClassLoaders()){
                try {
                    Method getClassPathMethod = classLoader.getClass().getMethod("getClasspath", new Class[]{});
                    classPath = (String) getClassPathMethod.invoke(classLoader, new Object[]{});
                    if (classPath != null) 
                        break;
                } catch (Exception e) {
                    //do nothing
                } 
            }                
        }                
        
        if (classPath == null) {
            classPath = AccessController.doPrivileged(
                    J2DoPrivHelper.getPropertyAction("java.class.path"));
        }
        String[] cp = classPath.split(J2DoPrivHelper.getPathSeparator());

        for (int i = 0; i < cp.length; i++) {
            if (cp[i].equals(name)
                || cp[i].endsWith(File.separatorChar + name)) {
                try {
                    addJarFile(AccessController
                        .doPrivileged(J2DoPrivHelper
                            .toURLAction(new File(cp[i]))));
                    return;
                } catch (PrivilegedActionException pae) {
                    break;
                } catch (MalformedURLException mue) {
                    break;
                }
            }
        }
        throw new IllegalArgumentException(s_loc.get("bad-jar-name", name).
            getMessage());
    }

    public List<String> getManagedClassNames() {
        if (_entityClassNames == null)
            return Collections.emptyList();
        return _entityClassNames;
    }

    public void addManagedClassName(String name) {
        if (_entityClassNames == null)
            _entityClassNames = new ArrayList<String>();
        _entityClassNames.add(name);
    }

    public Properties getProperties() {
        Properties copy = new Properties();
        copy.putAll(_props);
        return copy;
    }

    public void setProperty(String key, String value) {
        _props.put(key, value);
    }

    public void addTransformer(ClassTransformer transformer) {
        throw new UnsupportedOperationException();
    }

    /**
     * The location of the persistence.xml resource. May be null.
     */
    public URL getPersistenceXmlFileUrl() {
        return _persistenceXmlFile;
    }

    /**
     * The location of the persistence.xml resource. May be null.
     */
    public void setPersistenceXmlFileUrl(URL url) {
        _persistenceXmlFile = url;
    }

    /**
     * Load the given user-supplied map of properties into this persistence
     * unit.
     */
    public void fromUserProperties(Map map) {
        if (map == null)
            return;

        Object key;
        Object val;
        for (Object o : map.entrySet()) {
            key = ((Map.Entry) o).getKey();
            val = ((Map.Entry) o).getValue();
            if (JPAProperties.PROVIDER.equals(key))
                setPersistenceProviderClassName((String) val);
            else if (JPAProperties.TRANSACTION_TYPE.equals(key)) {
                setTransactionType(JPAProperties.getEnumValue(PersistenceUnitTransactionType.class, val));
            } else if (JPAProperties.DATASOURCE_JTA.equals(key)) {
                if (val instanceof String) {
                    setJtaDataSourceName((String) val);
                } else {
                    setJtaDataSource((DataSource) val);
                }
            } else if (JPAProperties.DATASOURCE_NONJTA.equals(key)) {
                if (val instanceof String) {
                    setNonJtaDataSourceName((String) val);
                } else {
                    setNonJtaDataSource((DataSource) val);
                }
            } else if (JPAProperties.VALIDATE_MODE.equals(key)) {
                setValidationMode(JPAProperties.getEnumValue(ValidationMode.class, val));
            } else if (JPAProperties.CACHE_MODE.equals(key)) { 
                setSharedCacheMode(JPAProperties.getEnumValue(SharedCacheMode.class, val));
            } else {
                _props.put(key, val);
            }
        }
    }

    /**
     * Return a {@link Map} containing the properties necessary to create
     * a {@link Configuration} that reflects the information in this
     * persistence unit info.
     */
    public Map toOpenJPAProperties() {
        return toOpenJPAProperties(this);
    }

    /**
     * Return a {@link Map} containing the properties necessary to create
     * a {@link Configuration} that reflects the information in the given
     * persistence unit info.
     */
    public static Map toOpenJPAProperties(PersistenceUnitInfo info) {
        Map map = new HashMap<String,Object>();
        Set<String> added = new HashSet<String>();
        if (info.getTransactionType() == PersistenceUnitTransactionType.JTA)
            replaceAsOpenJPAProperty(map, added, "TransactionMode", "managed");

        boolean hasJta = false;
        DataSource ds = info.getJtaDataSource();
        if (ds != null) {
            replaceAsOpenJPAProperty(map, added, "ConnectionFactory", ds);
            replaceAsOpenJPAProperty(map, added, "ConnectionFactoryMode", "managed");
            hasJta = true;
        } else if (info instanceof PersistenceUnitInfoImpl
            && ((PersistenceUnitInfoImpl) info).getJtaDataSourceName() != null){
            replaceAsOpenJPAProperty(map, added, "ConnectionFactoryName", 
                    ((PersistenceUnitInfoImpl)info).getJtaDataSourceName());
            replaceAsOpenJPAProperty(map, added, "ConnectionFactoryMode", "managed");
            hasJta = true;
        }

        ds = info.getNonJtaDataSource();
        if (ds != null) {
             replaceAsOpenJPAProperty(map, added, hasJta ? "ConnectionFactory2" : "ConnectionFactory", ds);
        } else if (info instanceof PersistenceUnitInfoImpl
            && ((PersistenceUnitInfoImpl) info).getNonJtaDataSourceName() != null) {
            String nonJtaName = ((PersistenceUnitInfoImpl) info).getNonJtaDataSourceName();
            replaceAsOpenJPAProperty(map, added, hasJta ? "ConnectionFactory2Name" : "ConnectionFactoryName", 
                    nonJtaName);
        }

        if (info.getClassLoader() != null)
            replaceAsOpenJPAProperty(map, added, "ClassResolver", new ClassResolverImpl(info.getClassLoader()));

        Properties props = info.getProperties();
        if (props != null) {
            // remove any of the things that were set above
            for (String key : added) {
                if (Configurations.containsProperty(key, props))
                    Configurations.removeProperty(key, props);
            }

            // add all the non-conflicting props in the <properties> section
            map.putAll(props);

            // this isn't a real config property; remove it
            map.remove(PersistenceProviderImpl.CLASS_TRANSFORMER_OPTIONS);
        }

        if (!Configurations.containsProperty("Id", map))
            map.put("openjpa.Id", info.getPersistenceUnitName());
        
        Properties metaFactoryProps = new Properties();
        if (info.getManagedClassNames() != null 
            && !info.getManagedClassNames().isEmpty()) {
            StringBuilder types = new StringBuilder();
            for (String type : info.getManagedClassNames()) {
                if (types.length() > 0)
                    types.append(';');
                types.append(type);
            }
            metaFactoryProps.put("Types", types.toString());
        }
        if (info.getJarFileUrls() != null && !info.getJarFileUrls().isEmpty()
            || (!info.excludeUnlistedClasses()
            && info.getPersistenceUnitRootUrl() != null)) {
            StringBuilder jars = new StringBuilder();
            String file = null;
            if (!info.excludeUnlistedClasses()
                && info.getPersistenceUnitRootUrl() != null) {
                URL url = info.getPersistenceUnitRootUrl();
                if ("file".equals(url.getProtocol())) // exploded jar?
                    file = URLDecoder.decode(url.getPath());
                else
                    jars.append(url);
            }
            for (URL jar : info.getJarFileUrls()) {
                if (jars.length() > 0)
                    jars.append(';');
                jars.append(jar);
            }
            if (file != null)
                metaFactoryProps.put("Files", file);
            if (jars.length() != 0)
                metaFactoryProps.put("URLs", jars.toString());
        }
        if (info.getMappingFileNames() != null
            && !info.getMappingFileNames().isEmpty()) {
            StringBuilder rsrcs = new StringBuilder();
            for (String rsrc : info.getMappingFileNames()) {
                if (rsrcs.length() > 0)
                    rsrcs.append(';');
                rsrcs.append(rsrc);
            }
            metaFactoryProps.put("Resources", rsrcs.toString());
        }

        // set persistent class locations as properties of metadata factory,
        // combining them with any existing metadata factory props
        if (!metaFactoryProps.isEmpty()) {
            String key = ProductDerivations.getConfigurationKey
                ("MetaDataFactory", map);
            map.put(key, Configurations.combinePlugins((String) map.get(key),
                Configurations.serializeProperties(metaFactoryProps)));
        }
        
        // always record provider name for product derivations to access
        if (info.getPersistenceProviderClassName() != null)
            map.put(JPAProperties.PROVIDER, info.getPersistenceProviderClassName());
        
        // convert validation-mode enum to a StringValue
        if (info.getValidationMode() != null)
            map.put(JPAProperties.VALIDATE_MODE, info.getValidationMode());

        if (info.getPersistenceXMLSchemaVersion() != null) {
            map.put(PERSISTENCE_VERSION, info.getPersistenceXMLSchemaVersion());
        }
        
        if (info.getSharedCacheMode() != null) { 
            map.put(JPAProperties.CACHE_MODE, info.getSharedCacheMode());
        }
        
        return map;
    }

    /**
     * Adds the given key-val to the given map after adding "openjpa." prefix to the key.
     * Tracks this addition in the given set of added keys.
     */
    private static void replaceAsOpenJPAProperty(Map map, Set<String> added, String key, Object val) {
        map.put("openjpa." + key, val);
        added.add(key);
    }

    // --------------------

    public File getSourceFile() {
        if (_persistenceXmlFile == null)
            return null;

        try {
            return new File(_persistenceXmlFile.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public Object getSourceScope() {
        return null;
    }

    public int getSourceType() {
        return SRC_XML;
    }
    
    public int getLineNumber() {
        return 0;
    }
        
    public int getColNumber() {
        return 0;
    }

    public String getResourceName() {
        return "PersistenceUnitInfo:" + _name;
    }

    /**
     * Simple class resolver built around the persistence unit loader.
     */
    public static class ClassResolverImpl
        implements ClassResolver {

        private final ClassLoader _loader;

        public ClassResolverImpl(ClassLoader loader) {
            _loader = loader;
        }

        public ClassLoader getClassLoader(Class ctx, ClassLoader env) {
            return _loader;
        }
	}

    public String getPersistenceXMLSchemaVersion() {
        return _schemaVersion;
    }

    public void setPersistenceXMLSchemaVersion(String version) {
        _schemaVersion = version;
    }

    public ValidationMode getValidationMode() {
        return _validationMode;
    }
    
    public void setValidationMode(ValidationMode mode) {
        _validationMode = mode;
    }

    public SharedCacheMode getSharedCacheMode() {
        return _sharedCacheMode;
    }
    
    public void setSharedCacheMode(SharedCacheMode mode) { 
        _sharedCacheMode = mode;
    }
}
