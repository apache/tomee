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

package org.apache.openejb.assembler.classic;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.api.internal.Internal;
import org.apache.openejb.api.jmx.Description;
import org.apache.openejb.api.jmx.MBean;
import org.apache.openejb.api.jmx.ManagedAttribute;
import org.apache.openejb.api.jmx.ManagedOperation;
import org.apache.openejb.jee.JAXBContextFactory;
import org.apache.openejb.jee.Persistence;
import org.apache.openejb.jee.PersistenceUnitCaching;
import org.apache.openejb.jee.PersistenceUnitValidationMode;
import org.apache.openejb.jpa.integration.JPAThreadContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.monitoring.DynamicMBeanWrapper;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.persistence.PersistenceUnitInfoImpl;
import org.apache.openejb.persistence.QueryLogEntityManager;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.TabularData;
import javax.naming.NamingException;
import jakarta.persistence.Cache;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.Query;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.SynchronizationType;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.PersistenceUnitTransactionType;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.apache.openejb.monitoring.LocalMBeanServer.tabularData;

@Internal
public class ReloadableEntityManagerFactory implements EntityManagerFactory, Serializable {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, ReloadableEntityManagerFactory.class);

    public static final String JAVAX_PERSISTENCE_SHARED_CACHE_MODE = "jakarta.persistence.sharedCache.mode";
    public static final String JAVAX_PERSISTENCE_VALIDATION_MODE = "jakarta.persistence.validation.mode";
    public static final String JAVAX_PERSISTENCE_TRANSACTION_TYPE = "jakarta.persistence.transactionType";

    public static final String OPENEJB_JPA_CRITERIA_LOG_JPQL = "openejb.jpa.criteria.log.jpql";
    public static final String OPENEJB_JPA_CRITERIA_LOG_JPQL_LEVEL = "openejb.jpa.criteria.log.jpql.level";

    private final PersistenceUnitInfoImpl unitInfoImpl;
    private ClassLoader classLoader;
    private volatile EntityManagerFactory delegate;
    private final EntityManagerFactoryCallable entityManagerFactoryCallable;
    private ObjectName objectName;

    private final boolean logCriteriaJpql;
    private final String logCriteriaJpqlLevel;

    public ReloadableEntityManagerFactory(final ClassLoader cl, final EntityManagerFactoryCallable callable, final PersistenceUnitInfoImpl unitInfo) {
        classLoader = cl;
        entityManagerFactoryCallable = callable;
        unitInfoImpl = unitInfo;
        final Properties properties = unitInfo.getProperties();
        logCriteriaJpql = logCriteriaQueryJpql(properties);
        logCriteriaJpqlLevel = logCriteriaQueryJpqlLevel(properties);

        if (!callable.getUnitInfo().isLazilyInitialized()) {
            createDelegate();
        }
    }

    public void overrideClassLoader(final ClassLoader loader) {
        classLoader = loader;
        entityManagerFactoryCallable.overrideClassLoader(loader);
        unitInfoImpl.setClassLoader(loader);
    }

    public EntityManagerFactoryCallable getEntityManagerFactoryCallable() {
        return entityManagerFactoryCallable;
    }

    private EntityManagerFactory delegate() {
        if (delegate == null) {
            synchronized (this) {
                if (delegate == null) {
                    createDelegate();
                }
            }
        }
        return delegate;
    }

    public void createDelegate() {
        JPAThreadContext.infos.put("properties", entityManagerFactoryCallable.getUnitInfo().getProperties());
        final long start = System.nanoTime();
        try {
            delegate = entityManagerFactoryCallable.call();
        } catch (final Exception e) {
            throw new OpenEJBRuntimeException(e);
        } finally {
            final long time = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            LOGGER.info("assembler.buildingPersistenceUnit", unitInfoImpl.getPersistenceUnitName(), unitInfoImpl.getPersistenceProviderClassName(), String.valueOf(time));
            if (LOGGER.isDebugEnabled()) {
                for (final Map.Entry<Object, Object> entry : unitInfoImpl.getProperties().entrySet()) {
                    LOGGER.debug(entry.getKey() + "=" + entry.getValue());
                }
            }

            JPAThreadContext.infos.clear();
        }
    }

    private String logCriteriaQueryJpqlLevel(final Properties props) {
        return SystemInstance.get().getOptions().get(OPENEJB_JPA_CRITERIA_LOG_JPQL_LEVEL, props.getProperty(OPENEJB_JPA_CRITERIA_LOG_JPQL_LEVEL, "INFO"));
    }

    private boolean logCriteriaQueryJpql(final Properties prop) {
        return SystemInstance.get().getOptions().get(OPENEJB_JPA_CRITERIA_LOG_JPQL, Boolean.parseBoolean(prop.getProperty(OPENEJB_JPA_CRITERIA_LOG_JPQL, "false")))
            || SystemInstance.get().getOptions().get(OPENEJB_JPA_CRITERIA_LOG_JPQL_LEVEL, prop.getProperty(OPENEJB_JPA_CRITERIA_LOG_JPQL_LEVEL, null)) != null;
    }

    @Override
    public EntityManager createEntityManager() {
        EntityManager em;
        try {
            em = delegate().createEntityManager();
        } catch (final LinkageError le) {
            em = delegate.createEntityManager();
        }

        if (logCriteriaJpql) {
            return new QueryLogEntityManager(em, logCriteriaJpqlLevel);
        }
        return em;
    }

    @Override
    public EntityManager createEntityManager(final Map map) {
        EntityManager em;
        try {
            em = delegate().createEntityManager(map);
        } catch (final LinkageError le) {
            em = delegate.createEntityManager(map);
        }

        if (logCriteriaJpql) {
            return new QueryLogEntityManager(em, logCriteriaJpqlLevel);
        }
        return em;
    }

    @Override
    public EntityManager createEntityManager(final SynchronizationType synchronizationType) {
        EntityManager em;
        try {
            em = delegate().createEntityManager(synchronizationType);
        } catch (final LinkageError le) {
            em = delegate.createEntityManager(synchronizationType);
        }

        if (logCriteriaJpql) {
            return new QueryLogEntityManager(em, logCriteriaJpqlLevel);
        }
        return em;
    }

    @Override
    public EntityManager createEntityManager(final SynchronizationType synchronizationType, final Map map) {
        EntityManager em;
        try {
            em = delegate().createEntityManager(synchronizationType, map);
        } catch (final LinkageError le) {
            em = delegate.createEntityManager(synchronizationType, map);
        }

        if (logCriteriaJpql) {
            return new QueryLogEntityManager(em, logCriteriaJpqlLevel);
        }
        return em;
    }

    @Override
    public <T> T unwrap(final Class<T> cls) {
        if (cls.isAssignableFrom(getClass())) {
            return cls.cast(this);
        }
        return delegate().unwrap(cls);
    }

    @Override
    public void addNamedQuery(final String name, final Query query) {
        delegate().addNamedQuery(name, query);
    }

    @Override
    public <T> void addNamedEntityGraph(final String graphName, final EntityGraph<T> entityGraph) {
        delegate().addNamedEntityGraph(graphName, entityGraph);
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return delegate().getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return delegate().getMetamodel();
    }

    @Override
    public boolean isOpen() {
        return delegate().isOpen();
    }

    @Override
    public synchronized void close() {
        if (delegate != null) {
            delegate.close();
        }
    }

    @Override
    public Map<String, Object> getProperties() {
        return delegate().getProperties();
    }

    @Override
    public Cache getCache() {
        return delegate().getCache();
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return delegate().getPersistenceUnitUtil();
    }

    public EntityManagerFactory getDelegate() {
        return delegate();
    }

    public void register() throws OpenEJBException {
        if (!LocalMBeanServer.isJMXActive()) {
            return;
        }

        final MBeanServer server = LocalMBeanServer.get();
        try {
            generateObjectName();
            if (server.isRegistered(objectName)) {
                server.unregisterMBean(objectName);
            }
            server.registerMBean(mBeanify(), objectName);
        } catch (final Exception e) {
            throw new OpenEJBException("can't register the mbean for the entity manager factory " + getPUname(), e);
        } catch (final NoClassDefFoundError ncdfe) {
            objectName = null;
            LOGGER.error("can't register the mbean for the entity manager factory {0}", getPUname());
        }
    }

    private ObjectName generateObjectName() {
        final ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
        jmxName.set("ObjectType", "persistence-unit");
        jmxName.set("PersistenceUnit", getPUname());
        objectName = jmxName.build();

        final MBeanServer server = LocalMBeanServer.get();
        if (server.isRegistered(objectName)) { // if 2 pu have the same name...a bit uglier but unique
            jmxName.set("PersistenceUnit", getPUname() + "(" + getId() + ")");
            objectName = jmxName.build();
        }

        return objectName;
    }

    private String getPUname() {
        return entityManagerFactoryCallable.getUnitInfo().getPersistenceUnitName();
    }

    private String getId() {
        return entityManagerFactoryCallable.getUnitInfo().getId();
    }

    private Object mBeanify() {
        return new DynamicMBeanWrapper(new JMXReloadableEntityManagerFactory(this));
    }

    public void unregister() throws OpenEJBException {
        if (objectName != null) {
            final MBeanServer server = LocalMBeanServer.get();
            try {
                server.unregisterMBean(objectName);
            } catch (final Exception e) {
                throw new OpenEJBException("can't unregister the mbean for the entity manager factory " + getPUname(), e);
            }
        }
    }

    // only this method is synchronized since we want to avoid locks on other methods.
    // it is just to avoid problems due to the "double click syndrom"
    //
    // Note: it uses the old unitInfo but properties can be modified (not managed classes, provider...)
    public synchronized void reload() {
        try {
            createDelegate();
        } catch (final Exception e) {
            LOGGER.error("can't replace EntityManagerFactory " + delegate, e);
        }
    }

    public synchronized void setSharedCacheMode(final SharedCacheMode mode) {
        final PersistenceUnitInfoImpl info = entityManagerFactoryCallable.getUnitInfo();
        info.setSharedCacheMode(mode);

        final Properties properties = entityManagerFactoryCallable.getUnitInfo().getProperties();
        if (properties.containsKey(JAVAX_PERSISTENCE_SHARED_CACHE_MODE)) {
            properties.setProperty(JAVAX_PERSISTENCE_SHARED_CACHE_MODE, mode.name());
        }
    }

    public synchronized void setValidationMode(final ValidationMode mode) {
        final PersistenceUnitInfoImpl info = entityManagerFactoryCallable.getUnitInfo();
        info.setValidationMode(mode);

        final Properties properties = entityManagerFactoryCallable.getUnitInfo().getProperties();
        if (properties.containsKey(JAVAX_PERSISTENCE_VALIDATION_MODE)) {
            properties.setProperty(JAVAX_PERSISTENCE_VALIDATION_MODE, mode.name());
        }
    }

    public synchronized void setProvider(final String providerRaw) {
        final String provider = providerRaw.trim();
        final String newProvider;
        switch (provider) {
            case "hibernate":
                newProvider = "org.hibernate.ejb.HibernatePersistence";
                break;
            case "openjpa":
                newProvider = "org.apache.openjpa.persistence.PersistenceProviderImpl";
                break;
            case "eclipselink":
                newProvider = "org.eclipse.persistence.jpa.PersistenceProvider";
                break;
            case "toplink":
                newProvider = "oracle.toplink.essentials.PersistenceProvider";
                break;
            default:
                newProvider = provider;
                break;
        }

        try {
            classLoader.loadClass(newProvider);
            entityManagerFactoryCallable.getUnitInfo().setPersistenceProviderClassName(newProvider);
        } catch (final ClassNotFoundException e) {
            LOGGER.error("can't load new provider " + newProvider, e);
        }
    }

    public synchronized void setTransactionType(final PersistenceUnitTransactionType type) {
        final PersistenceUnitInfoImpl info = entityManagerFactoryCallable.getUnitInfo();
        info.setTransactionType(type);

        final Properties properties = entityManagerFactoryCallable.getUnitInfo().getProperties();
        if (properties.containsKey(JAVAX_PERSISTENCE_TRANSACTION_TYPE)) {
            properties.setProperty(JAVAX_PERSISTENCE_TRANSACTION_TYPE, type.name());
        }
    }

    public synchronized void setProperty(final String key, final String value) {
        final PersistenceUnitInfoImpl unitInfo = entityManagerFactoryCallable.getUnitInfo();
        if (unitInfo.getProperties() == null) {
            unitInfo.setProperties(new Properties());
        }
        unitInfo.getProperties().setProperty(key, value);
    }

    public synchronized void removeProperty(final String key) {
        final PersistenceUnitInfoImpl unitInfo = entityManagerFactoryCallable.getUnitInfo();
        if (unitInfo.getProperties() != null) {
            unitInfo.getProperties().remove(key);
        }
    }

    public Properties getUnitProperties() {
        final PersistenceUnitInfoImpl unitInfo = entityManagerFactoryCallable.getUnitInfo();
        if (unitInfo.getProperties() != null) {
            return unitInfo.getProperties();
        }
        return new Properties();
    }

    public List<String> getMappingFiles() {
        return entityManagerFactoryCallable.getUnitInfo().getMappingFileNames();
    }

    public void addMappingFile(final String file) {
        if (new File(file).exists()) {
            entityManagerFactoryCallable.getUnitInfo().addMappingFileName(file);
        } else {
            LOGGER.error("file " + file + " doesn't exists");
        }
    }

    public void removeMappingFile(final String file) {
        entityManagerFactoryCallable.getUnitInfo().getMappingFileNames().remove(file);
    }

    public List<URL> getJarFileUrls() {
        return entityManagerFactoryCallable.getUnitInfo().getJarFileUrls();
    }

    public void addJarFileUrls(final String file) {
        if (new File(file).exists()) { // should we test real urls?
            try {
                entityManagerFactoryCallable.getUnitInfo().getJarFileUrls().add(new URL(file));
            } catch (final MalformedURLException e) {
                LOGGER.error("url " + file + " is malformed");
            }
        } else {
            LOGGER.error("url " + file + " is not correct");
        }
    }

    public void removeJarFileUrls(final String file) {
        try {
            entityManagerFactoryCallable.getUnitInfo().getJarFileUrls().remove(new URL(file));
        } catch (final MalformedURLException e) {
            LOGGER.error("url " + file + " is malformed");
        }
    }

    public List<String> getManagedClasses() {
        return entityManagerFactoryCallable.getUnitInfo().getManagedClassNames();
    }

    public void addManagedClasses(final String clazz) {
        entityManagerFactoryCallable.getUnitInfo().getManagedClassNames().add(clazz);
    }

    public void removeManagedClasses(final String clazz) {
        entityManagerFactoryCallable.getUnitInfo().getManagedClassNames().remove(clazz);
    }

    public PersistenceUnitInfo info() {
        return entityManagerFactoryCallable.getUnitInfo();
    }

    public void setExcludeUnlistedClasses(final boolean excludeUnlistedClasses) {
        entityManagerFactoryCallable.getUnitInfo().setExcludeUnlistedClasses(excludeUnlistedClasses);
    }

    public boolean getExcludeUnlistedClasses() {
        return entityManagerFactoryCallable.getUnitInfo().excludeUnlistedClasses();
    }

    Object writeReplace() throws ObjectStreamException {
        return new SerializableEm("java:" + Assembler.PERSISTENCE_UNIT_NAMING_CONTEXT + unitInfoImpl.getId());
    }

    @MBean
    @Internal
    @Description("represents a persistence unit managed by OpenEJB")
    public static class JMXReloadableEntityManagerFactory {
        private final ReloadableEntityManagerFactory reloadableEntityManagerFactory;

        public JMXReloadableEntityManagerFactory(final ReloadableEntityManagerFactory remf) {
            reloadableEntityManagerFactory = remf;
        }

        @ManagedOperation
        @Description("recreate the entity manager factory using new properties")
        public void reload() {
            reloadableEntityManagerFactory.reload();
        }

        @ManagedOperation
        @Description("change the current JPA provider")
        public void setProvider(final String provider) {
            reloadableEntityManagerFactory.setProvider(provider);
        }

        @ManagedOperation
        @Description("change the current transaction type")
        public void setTransactionType(final String type) {
            try {
                final PersistenceUnitTransactionType tt = PersistenceUnitTransactionType.valueOf(type.toUpperCase());
                reloadableEntityManagerFactory.setTransactionType(tt);
            } catch (final Exception iae) {
                // ignored
            }
        }

        @ManagedOperation
        @Description("create or modify a property of the persistence unit")
        public void setProperty(final String key, final String value) {
            reloadableEntityManagerFactory.setProperty(key, value);
        }

        @ManagedOperation
        @Description("remove a property of the persistence unit if it exists")
        public void removeProperty(final String key) {
            reloadableEntityManagerFactory.removeProperty(key);
        }

        @ManagedOperation
        @Description("add a mapping file")
        public void addMappingFile(final String file) {
            reloadableEntityManagerFactory.addMappingFile(file);
        }

        @ManagedOperation
        @Description("remove a mapping file")
        public void removeMappingFile(final String file) {
            reloadableEntityManagerFactory.removeMappingFile(file);
        }

        @ManagedOperation
        @Description("add a managed class")
        public void addManagedClass(final String clazz) {
            reloadableEntityManagerFactory.addManagedClasses(clazz);
        }

        @ManagedOperation
        @Description("remove a managed class")
        public void removeManagedClass(final String clazz) {
            reloadableEntityManagerFactory.removeManagedClasses(clazz);
        }

        @ManagedOperation
        @Description("add a jar file")
        public void addJarFile(final String file) {
            reloadableEntityManagerFactory.addJarFileUrls(file);
        }

        @ManagedOperation
        @Description("remove a jar file")
        public void removeJarFile(final String file) {
            reloadableEntityManagerFactory.removeJarFileUrls(file);
        }

        @ManagedOperation
        @Description("change the shared cache mode if possible (value is ok)")
        public void setSharedCacheMode(final String value) {
            try {
                final String v = value.trim().toUpperCase();
                final SharedCacheMode mode = v.isEmpty() ? SharedCacheMode.UNSPECIFIED : SharedCacheMode.valueOf(v);
                reloadableEntityManagerFactory.setSharedCacheMode(mode);
            } catch (final Exception iae) {
                // ignored
            }
        }

        @ManagedOperation
        @Description("exclude or not unlisted entities")
        public void setExcludeUnlistedClasses(final boolean value) {
            reloadableEntityManagerFactory.setExcludeUnlistedClasses(value);
        }

        @ManagedOperation
        @Description("change the validation mode if possible (value is ok)")
        public void setValidationMode(final String value) {
            try {
                final ValidationMode mode = ValidationMode.valueOf(value.trim().toUpperCase());
                reloadableEntityManagerFactory.setValidationMode(mode);
            } catch (final Exception iae) {
                LOGGER.warning("Can't set validation mode " + value, iae);
                reloadableEntityManagerFactory.setProperty(JAVAX_PERSISTENCE_VALIDATION_MODE, value);
            }
        }

        @ManagedOperation
        @Description("dump the current configuration for this persistence unit in a file")
        public void dump(final String file) {
            final PersistenceUnitInfoImpl info = reloadableEntityManagerFactory.entityManagerFactoryCallable.getUnitInfo();

            final Persistence.PersistenceUnit pu = new Persistence.PersistenceUnit();
            pu.setJtaDataSource(info.getJtaDataSourceName());
            pu.setNonJtaDataSource(info.getNonJtaDataSourceName());
            pu.getClazz().addAll(info.getManagedClassNames());
            pu.getMappingFile().addAll(info.getMappingFileNames());
            pu.setName(info.getPersistenceUnitName());
            pu.setProvider(info.getPersistenceProviderClassName());
            pu.setTransactionType(info.getTransactionType().name());
            pu.setExcludeUnlistedClasses(info.excludeUnlistedClasses());
            pu.setSharedCacheMode(PersistenceUnitCaching.fromValue(info.getSharedCacheMode().name()));
            pu.setValidationMode(PersistenceUnitValidationMode.fromValue(info.getValidationMode().name()));
            for (final URL url : info.getJarFileUrls()) {
                pu.getJarFile().add(url.toString());
            }
            for (final String key : info.getProperties().stringPropertyNames()) {
                final Persistence.PersistenceUnit.Properties.Property prop = new Persistence.PersistenceUnit.Properties.Property();
                prop.setName(key);
                prop.setValue(info.getProperties().getProperty(key));
                if (pu.getProperties() == null) {
                    pu.setProperties(new Persistence.PersistenceUnit.Properties());
                }
                pu.getProperties().getProperty().add(prop);
            }

            final Persistence persistence = new Persistence();
            persistence.setVersion(info.getPersistenceXMLSchemaVersion());
            persistence.getPersistenceUnit().add(pu);

            try (final FileWriter writer = new FileWriter(file)) {
                final JAXBContext jc = JAXBContextFactory.newInstance(Persistence.class);
                final Marshaller marshaller = jc.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                marshaller.marshal(persistence, writer);
            } catch (final Exception e) {
                LOGGER.error("can't dump pu " + reloadableEntityManagerFactory.getPUname() + " in file " + file, e);
            }
        }

        @ManagedAttribute
        @Description("get exclude unlisted classes")
        public boolean getExcludeUnlistedClasses() {
            return reloadableEntityManagerFactory.getExcludeUnlistedClasses();
        }

        @ManagedAttribute
        @Description("get all properties")
        public TabularData getProperties() {
            return tabularData("properties", "properties type",
                "Property of " + reloadableEntityManagerFactory.getPUname(),
                reloadableEntityManagerFactory.getUnitProperties());
        }

        @ManagedAttribute
        @Description("get all mapping files")
        public TabularData getMappingFiles() {
            return buildTabularData("mappingfile", "mapping file type",
                reloadableEntityManagerFactory.getMappingFiles(), Info.FILE);
        }

        @ManagedAttribute
        @Description("get all jar files")
        public TabularData getJarFiles() {
            return buildTabularData("jarfile", "jar file type",
                reloadableEntityManagerFactory.getJarFileUrls(), Info.URL);
        }

        @ManagedAttribute
        @Description("get all managed classes")
        public TabularData getManagedClasses() {
            return buildTabularData("managedclass", "managed class type",
                reloadableEntityManagerFactory.getManagedClasses(), Info.CLASS);
        }

        private TabularData buildTabularData(final String typeName, final String typeDescription, final List<?> list, final Info info) {
            final String[] names = new String[list.size()];
            final Object[] values = new Object[names.length];
            int i = 0;
            for (final Object o : list) {
                names[i] = o.toString();
                values[i++] = info.info(reloadableEntityManagerFactory.classLoader, o);
            }
            return tabularData(typeName, typeDescription, names, values);
        }

        private enum Info {
            URL, NONE, FILE, CLASS;

            public String info(final ClassLoader cl, final Object o) {
                switch (this) {
                    case URL:
                        try {
                            if (((URL) o).openConnection().getContentLength() > 0) {
                                return "valid";
                            }
                        } catch (final IOException e) {
                            // ignored
                        }
                        return "not valid";

                    case FILE:
                        final File file;
                        if (o instanceof String) {
                            file = new File((String) o);
                        } else if (o instanceof File) {
                            file = (File) o;
                        } else {
                            return "unknown";
                        }
                        return "exist? " + file.exists();

                    case CLASS:
                        try {
                            cl.loadClass((String) o);
                            return "loaded";
                        } catch (final ClassNotFoundException e) {
                            return "unloadable";
                        }

                    default:
                        return "-";
                }
            }
        }
    }

    private static final class SerializableEm implements Serializable {
        private final String jndiName;

        private SerializableEm(final String jndiName) {
            this.jndiName = jndiName;
        }

        Object readResolve() throws ObjectStreamException {
            try {
                return SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext().lookup(jndiName);
            } catch (final NamingException e) {
                throw new InvalidObjectException(e.getMessage());
            }
        }
    }
}
