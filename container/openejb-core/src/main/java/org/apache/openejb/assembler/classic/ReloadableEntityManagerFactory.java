package org.apache.openejb.assembler.classic;

import org.apache.openejb.persistence.PersistenceUnitInfoImpl;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
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

    public ReloadableEntityManagerFactory(final ClassLoader cl, final EntityManagerFactory emf, EntityManagerFactoryCallable callable) {
        classLoader = cl;
        delegate = emf;
        this.entityManagerFactoryCallable = callable;
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

    public synchronized void setProperty(String key , String value) {
        PersistenceUnitInfoImpl unitInfo = entityManagerFactoryCallable.getUnitInfo();
        if (unitInfo.getProperties() == null) {
            unitInfo.setProperties(new Properties());
        }
        unitInfo.getProperties().setProperty(key, value);
    }
}
