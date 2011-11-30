package org.apache.openejb.assembler.classic;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.monitoring.DynamicMBeanWrapper;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.persistence.PersistenceUnitInfoImpl;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.management.Description;
import javax.management.MBean;
import javax.management.MBeanServer;
import javax.management.ManagedAttribute;
import javax.management.ManagedOperation;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceUnitTransactionType;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author rmannibucau
 */
public class ReloadableEntityManagerFactory implements EntityManagerFactory {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, ReloadableEntityManagerFactory.class);

    private ClassLoader classLoader;
    private EntityManagerFactory delegate;
    private EntityManagerFactoryCallable entityManagerFactoryCallable;
    private ObjectName objectName = null;

    public ReloadableEntityManagerFactory(final ClassLoader cl, final EntityManagerFactory emf, EntityManagerFactoryCallable callable) {
        classLoader = cl;
        delegate = emf;
        entityManagerFactoryCallable = callable;
    }

    @Override public EntityManager createEntityManager() {
        return delegate.createEntityManager();
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        return delegate.createEntityManager(map);
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return delegate.getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return delegate.getMetamodel();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public Map<String, Object> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public Cache getCache() {
        return delegate.getCache();
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        return delegate.getPersistenceUnitUtil();
    }

    public EntityManagerFactory getDelegate() {
        return delegate;
    }

    public void register() throws OpenEJBException {
        final MBeanServer server = LocalMBeanServer.get();
        try {
            server.registerMBean(mBeanify(), generateObjectName());
        } catch (Exception e) {
            throw new OpenEJBException("can't register the mbean for the entity manager factory " + getPUname(), e);
        }
    }

    private ObjectName generateObjectName() {
        ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
        jmxName.set("ObjectType", "persistence-unit");
        jmxName.set("PersistenceUnit", getPUname());
        objectName = jmxName.build();

        MBeanServer server = LocalMBeanServer.get();
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
            } catch (Exception e) {
                throw new OpenEJBException("can't unregister the mbean for the entity manager factory " + getPUname(), e);
            }
        }
    }

    // only this method is synchronized since we want to avoid locks on other methods.
    // it is just to avoid problems due to the "double click syndrom"
    //
    // Note: it uses the old unitInfo but properties can be modified (not managed classes, provider...)
    public synchronized void reload() {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        try {
            delegate = PersistenceBuilder.createEmf(classLoader, entityManagerFactoryCallable);
        } catch (Exception e) {
            LOGGER.error("can't replace EntityManagerFactory " + delegate, e);
        }
    }

    public synchronized void setSharedCacheMode(SharedCacheMode mode) {
        entityManagerFactoryCallable.getUnitInfo().setSharedCacheMode(mode);
    }

    public synchronized void setValidationMode(ValidationMode mode) {
        entityManagerFactoryCallable.getUnitInfo().setValidationMode(mode);
    }

    public synchronized void setProvider(String providerRaw) {
        final String provider = providerRaw.trim();
        String newProvider;
        if ("hibernate".equals(provider)) {
            newProvider = "org.hibernate.ejb.HibernatePersistence";
        } else if ("openjpa".equals(provider)) {
            newProvider = "org.apache.openjpa.persistence.PersistenceProviderImpl";
        } else if ("eclipse".equals(provider)) {
            newProvider = "oracle.toplink.essentials.PersistenceProvider";
        } else {
            newProvider = provider;
        }

        try {
            classLoader.loadClass(newProvider);
            entityManagerFactoryCallable.getUnitInfo().setPersistenceProviderClassName(newProvider);
        } catch (ClassNotFoundException e) {
            LOGGER.error("can't load new provider " + newProvider, e);
        }
    }

    public synchronized void setTransactionType(PersistenceUnitTransactionType type) {
        entityManagerFactoryCallable.getUnitInfo().setTransactionType(type);
    }

    public synchronized void setProperty(String key, String value) {
        PersistenceUnitInfoImpl unitInfo = entityManagerFactoryCallable.getUnitInfo();
        if (unitInfo.getProperties() == null) {
            unitInfo.setProperties(new Properties());
        }
        unitInfo.getProperties().setProperty(key, value);
    }

    public synchronized void removeProperty(String key) {
        PersistenceUnitInfoImpl unitInfo = entityManagerFactoryCallable.getUnitInfo();
        if (unitInfo.getProperties() != null) {
            unitInfo.getProperties().remove(key);
        }
    }

    public Properties getUnitProperties() {
        PersistenceUnitInfoImpl unitInfo = entityManagerFactoryCallable.getUnitInfo();
        if (unitInfo.getProperties() != null) {
            return unitInfo.getProperties();
        }
        return new Properties();
    }

    public List<String> getMappingFiles() {
        return entityManagerFactoryCallable.getUnitInfo().getMappingFileNames();
    }

    public void addMappingFile(String file) {
        if (new File(file).exists()) {
            entityManagerFactoryCallable.getUnitInfo().addMappingFileName(file);
        } else {
            LOGGER.error("file " + file + " doesn't exists");
        }
    }

    public void removeMappingFile(String file) {
        entityManagerFactoryCallable.getUnitInfo().getMappingFileNames().remove(file);
    }

    public List<URL> getJarFileUrls() {
        return entityManagerFactoryCallable.getUnitInfo().getJarFileUrls();
    }

    public void addJarFileUrls(String file) {
        if (new File(file).exists()) { // should we test real urls?
            try {
                entityManagerFactoryCallable.getUnitInfo().getJarFileUrls().add(new URL(file));
            } catch (MalformedURLException e) {
                LOGGER.error("url " + file + " is malformed");
            }
        } else {
            LOGGER.error("url " + file + " is not correct");
        }
    }

    public void removeJarFileUrls(String file) {
        try {
            entityManagerFactoryCallable.getUnitInfo().getJarFileUrls().remove(new URL(file));
        } catch (MalformedURLException e) {
            LOGGER.error("url " + file + " is malformed");
        }
    }

    public List<String> getManagedClasses() {
        return entityManagerFactoryCallable.getUnitInfo().getManagedClassNames();
    }

    public void addManagedClasses(String clazz) {
        entityManagerFactoryCallable.getUnitInfo().getManagedClassNames().add(clazz);
    }

    public void removeManagedClasses(String clazz) {
        entityManagerFactoryCallable.getUnitInfo().getManagedClassNames().remove(clazz);
    }

    @MBean
    @Description("represents a persistence unit managed by OpenEJB")
    public static class JMXReloadableEntityManagerFactory {
        private ReloadableEntityManagerFactory reloadableEntityManagerFactory;

        public JMXReloadableEntityManagerFactory(ReloadableEntityManagerFactory remf) {
            reloadableEntityManagerFactory = remf;
        }

        @ManagedOperation
        @Description("recreate the entity manager factory using new properties")
        public void reload() {
            reloadableEntityManagerFactory.reload();
        }

        @ManagedOperation
        @Description("change the current JPA provider")
        public void setProvider(String provider) {
            reloadableEntityManagerFactory.setProvider(provider);
        }

        @ManagedOperation
        @Description("change the current transaction type")
        public void setTransactionType(String type) {
            try {
                PersistenceUnitTransactionType tt = PersistenceUnitTransactionType.valueOf(type.toUpperCase());
                reloadableEntityManagerFactory.setTransactionType(tt);
            } catch (Exception iae) {
                // ignored
            }
        }

        @ManagedOperation
        @Description("create or modify a property of the persistence unit")
        public void setProperty(String key, String value) {
            reloadableEntityManagerFactory.setProperty(key, value);
        }

        @ManagedOperation
        @Description("remove a property of the persistence unit if it exists")
        public void removeProperty(String key) {
            reloadableEntityManagerFactory.removeProperty(key);
        }

        @ManagedOperation
        @Description("add a mapping file")
        public void addMappingFile(String file) {
            reloadableEntityManagerFactory.addMappingFile(file);
        }

        @ManagedOperation
        @Description("remove a mapping file")
        public void removeMappingFile(String file) {
            reloadableEntityManagerFactory.removeMappingFile(file);
        }

        @ManagedOperation
        @Description("add a managed class")
        public void addManagedClass(String clazz) {
            reloadableEntityManagerFactory.addManagedClasses(clazz);
        }

        @ManagedOperation
        @Description("remove a managed class")
        public void removeManagedClass(String clazz) {
            reloadableEntityManagerFactory.removeManagedClasses(clazz);
        }

        @ManagedOperation
        @Description("add a jar file")
        public void addJarFile(String file) {
            reloadableEntityManagerFactory.addJarFileUrls(file);
        }

        @ManagedOperation
        @Description("remove a jar file")
        public void removeJarFile(String file) {
            reloadableEntityManagerFactory.removeJarFileUrls(file);
        }

        @ManagedOperation
        @Description("change the shared cache mode if possible (value is ok)")
        public void setSharedCacheMode(String value) {
            try {
                SharedCacheMode mode = SharedCacheMode.valueOf(value.trim().toUpperCase());
                reloadableEntityManagerFactory.setSharedCacheMode(mode);
            } catch (Exception iae) {
                // ignored
            }
        }

        @ManagedOperation
        @Description("change the validation mode if possible (value is ok)")
        public void setValidationMode(String value) {
            try {
                ValidationMode mode = ValidationMode.valueOf(value.trim().toUpperCase());
                reloadableEntityManagerFactory.setValidationMode(mode);
            } catch (Exception iae) {
                // ignored
            }
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
            return tabularData("mappingfile", "mapping file type",
                    "Mapping file of " + reloadableEntityManagerFactory.getPUname(),
                    reloadableEntityManagerFactory.getMappingFiles(), Info.FILE);
        }

        @ManagedAttribute
        @Description("get all jar files")
        public TabularData getJarFiles() {
            return tabularData("jarfile", "jar file type",
                    "Jar file of " + reloadableEntityManagerFactory.getPUname(),
                    reloadableEntityManagerFactory.getJarFileUrls(), Info.URL);
        }

        @ManagedAttribute
        @Description("get all managed classes")
        public TabularData getManagedClasses() {
            return tabularData("managedclass", "managed class type",
                    "Managed class of " + reloadableEntityManagerFactory.getPUname(),
                    reloadableEntityManagerFactory.getManagedClasses(), Info.CLASS);
        }

        private TabularData tabularData(String typeName, String typeDescription, String description, List<?> list, Info info) {
            String[] names = new String[list.size()];
            Object[] values= new Object[names.length];
            int i = 0;
            for (Object o : list) {
                names[i] = o.toString();
                values[i++] = info.info(reloadableEntityManagerFactory.classLoader, o);
            }
            return tabularData(typeName, typeDescription, names, values);
        }

        private static TabularData tabularData(String typeName, String typeDescription, String description, Properties properties) {
            String[] names = properties.keySet().toArray(new String[properties.size()]);
            Object[] values = new Object[names.length];
            for (int i = 0; i < values.length; i++) {
                values[i] = properties.get(names[i]);
            }
            return tabularData(typeName, typeDescription, names, values);
        }

        private static TabularData tabularData(String typeName, String typeDescription, String[] names, Object[] values) {
            if (names.length == 0) {
                return null;
            }

            OpenType<?>[] types = new OpenType<?>[names.length];
            for (int i = 0; i < types.length; i++) {
                types[i] = SimpleType.STRING;
            }

            try {
                CompositeType ct = new CompositeType(typeName, typeDescription, names, names, types);
                TabularType type = new TabularType(typeName, typeDescription, ct, names);
                TabularDataSupport data = new TabularDataSupport(type);

                CompositeData line = new CompositeDataSupport(ct, names, values);
                data.put(line);

                return data;
            } catch (OpenDataException e) {
                return null;
            }
        }

        private enum Info {
            URL, NONE, FILE, CLASS;

            public String info(ClassLoader cl, Object o) {
                switch (this) {
                    case URL:
                        try {
                            if (((URL) o).openConnection().getContentLength() > 0) {
                                return "valid";
                            }
                        } catch (IOException e) {
                            // ignored
                        }
                        return "not valid";

                    case FILE:
                        File file;
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
                        } catch (ClassNotFoundException e) {
                            return "unloadable";
                        }

                    default:
                        return "-";
                }
            }
        }
    }
}
