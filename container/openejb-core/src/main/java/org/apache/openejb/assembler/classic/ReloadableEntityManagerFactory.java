package org.apache.openejb.assembler.classic;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.monitoring.DynamicMBeanWrapper;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.persistence.PersistenceUnitInfoImpl;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.management.*;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import java.lang.management.ManagementFactory;
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
        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            server.registerMBean(mBeanify(), generateObjectName());
        } catch (Exception e) {
            throw new OpenEJBException("can't register the mbean for the entity manager factory " + getPUname(), e);
        }
    }

    private ObjectName generateObjectName() {
        ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management");
        jmxName.set("J2EEServer", "openejb");
        jmxName.set("ObjectType", "persistence-unit");
        jmxName.set("PersistenceUnit", getPUname());
        objectName = jmxName.build();
        return objectName;
    }

    public String getPUname() {
        return entityManagerFactoryCallable.getUnitInfo().getPersistenceUnitName();
    }

    private Object mBeanify() {
        return new DynamicMBeanWrapper(new JMXReloadableEntityManagerFactory(this));
    }

    public void unregister() throws OpenEJBException {
        if (objectName != null) {
            final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
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
            LOGGER.error("can't replace EntityManagerFactory " + delegate);
        }
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
        @Description("create or modify a property of the persistence unit")
        public void setProperty(String key, String value) {
            reloadableEntityManagerFactory.setProperty(key, value);
        }

        @ManagedOperation
        @Description("remove a property of the persistence unit if it exists")
        public void removeProperty(String key) {
            reloadableEntityManagerFactory.removeProperty(key);
        }

        @ManagedAttribute
        @Description("get all properties colon separated")
        public String getProperties() {
            return reloadableEntityManagerFactory.getUnitProperties().toString();
        }
    }
}
