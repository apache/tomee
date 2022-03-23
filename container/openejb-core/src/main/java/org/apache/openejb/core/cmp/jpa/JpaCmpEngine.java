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

package org.apache.openejb.core.cmp.jpa;

import org.apache.openejb.BeanContext;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.cmp.CmpCallback;
import org.apache.openejb.core.cmp.CmpEngine;
import org.apache.openejb.core.cmp.ComplexKeyGenerator;
import org.apache.openejb.core.cmp.KeyGenerator;
import org.apache.openejb.core.cmp.SimpleKeyGenerator;
import org.apache.openejb.core.cmp.cmp2.Cmp2KeyGenerator;
import org.apache.openejb.core.cmp.cmp2.Cmp2Util;
import org.apache.openejb.core.transaction.TransactionPolicy;
import org.apache.openejb.core.transaction.TransactionType;
import org.apache.openjpa.event.AbstractLifecycleListener;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;
import org.apache.openjpa.persistence.OpenJPAEntityManagerSPI;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EJBObject;
import jakarta.ejb.EntityBean;
import jakarta.ejb.FinderException;
import jakarta.ejb.RemoveException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.openejb.core.transaction.EjbTransactionUtil.afterInvoke;
import static org.apache.openejb.core.transaction.EjbTransactionUtil.createTransactionPolicy;

public class JpaCmpEngine implements CmpEngine {
    private static final Object[] NO_ARGS = new Object[0];
    public static final String CMP_PERSISTENCE_CONTEXT_REF_NAME = "comp/env/openejb/cmp";

    /**
     * Used to notify call CMP callback methods.
     */
    private final CmpCallback cmpCallback;

    /**
     * Thread local to track the beans we are creating to avoid an extra ejbStore callback
     */
    private final ThreadLocal<Set<EntityBean>> creating = new ThreadLocal<Set<EntityBean>>() {
        protected Set<EntityBean> initialValue() {
            return new HashSet<>();
        }
    };

    /**
     * Listener added to entity managers.
     */
    protected Object entityManagerListener;

    public JpaCmpEngine(final CmpCallback cmpCallback) {
        this.cmpCallback = cmpCallback;
    }

    public synchronized void deploy(final BeanContext beanContext) throws OpenEJBException {
        configureKeyGenerator(beanContext);
    }

    public synchronized void undeploy(final BeanContext beanContext) throws OpenEJBException {
        beanContext.setKeyGenerator(null);
    }

    private EntityManager getEntityManager(final BeanContext beanContext) {
        EntityManager entityManager = null;
        try {
            entityManager = (EntityManager) beanContext.getJndiEnc().lookup(CMP_PERSISTENCE_CONTEXT_REF_NAME);
        } catch (final NamingException ignored) {
            //TODO see OPENEJB-1259 temporary hack until geronimo jndi integration works better
            try {
                entityManager = (EntityManager) new InitialContext().lookup("java:" + CMP_PERSISTENCE_CONTEXT_REF_NAME);
            } catch (final NamingException ignored2) {
                //ignore
            }
        }

        if (entityManager == null) {
            throw new EJBException("Entity manager not found at \"openejb/cmp\" in jndi ejb " + beanContext.getDeploymentID());
        }

        registerListener(entityManager);

        return entityManager;
    }

    private synchronized void registerListener(final EntityManager entityManager) {
        if (entityManager instanceof OpenJPAEntityManagerSPI) {
            final OpenJPAEntityManagerSPI openjpaEM = (OpenJPAEntityManagerSPI) entityManager;
            final OpenJPAEntityManagerFactorySPI openjpaEMF = (OpenJPAEntityManagerFactorySPI) openjpaEM.getEntityManagerFactory();

            if (entityManagerListener == null) {
                entityManagerListener = new OpenJPALifecycleListener();
            }
            openjpaEMF.addLifecycleListener(entityManagerListener, (Class[]) null);
            return;
        }

        final Object delegate = entityManager.getDelegate();
        if (delegate != entityManager && delegate instanceof EntityManager) {
            registerListener((EntityManager) delegate);
        }
    }

    public Object createBean(EntityBean bean, final ThreadContext callContext) throws CreateException {
        // TODO verify that extract primary key requires a flush followed by a merge
        final TransactionPolicy txPolicy = startTransaction("persist", callContext);
        creating.get().add(bean);
        try {
            final BeanContext beanContext = callContext.getBeanContext();
            final EntityManager entityManager = getEntityManager(beanContext);

            entityManager.persist(bean);
            entityManager.flush();
            bean = entityManager.merge(bean);

            // extract the primary key from the bean
            final KeyGenerator kg = beanContext.getKeyGenerator();
            final Object primaryKey = kg.getPrimaryKey(bean);

            return primaryKey;
        } finally {
            creating.get().remove(bean);
            commitTransaction("persist", callContext, txPolicy);
        }
    }

    public Object loadBean(final ThreadContext callContext, final Object primaryKey) {
        final TransactionPolicy txPolicy = startTransaction("load", callContext);
        try {
            final BeanContext beanContext = callContext.getBeanContext();
            final Class<?> beanClass = beanContext.getCmpImplClass();

            // Try to load it from the entity manager
            final EntityManager entityManager = getEntityManager(beanContext);
            return entityManager.find(beanClass, primaryKey);
        } finally {
            commitTransaction("load", callContext, txPolicy);
        }
    }

    public void storeBeanIfNoTx(final ThreadContext callContext, final Object bean) {
        final TransactionPolicy callerTxPolicy = callContext.getTransactionPolicy();
        if (callerTxPolicy != null && callerTxPolicy.isTransactionActive()) {
            return;
        }

        final TransactionPolicy txPolicy = startTransaction("store", callContext);
        try {
            // only store if we started a new transaction
            if (txPolicy.isNewTransaction()) {
                final EntityManager entityManager = getEntityManager(callContext.getBeanContext());
                entityManager.merge(bean);
            }
        } finally {
            commitTransaction("store", callContext, txPolicy);
        }
    }

    public void removeBean(final ThreadContext callContext) {
        final TransactionPolicy txPolicy = startTransaction("remove", callContext);
        try {
            final BeanContext deploymentInfo = callContext.getBeanContext();
            final Class<?> beanClass = deploymentInfo.getCmpImplClass();

            final EntityManager entityManager = getEntityManager(deploymentInfo);
            final Object primaryKey = callContext.getPrimaryKey();

            // Try to load it from the entity manager
            final Object bean = entityManager.find(beanClass, primaryKey);
            // remove the bean
            entityManager.remove(bean);
        } finally {
            commitTransaction("remove", callContext, txPolicy);
        }
    }

    public List<Object> queryBeans(final ThreadContext callContext, final Method queryMethod, final Object[] args) throws FinderException {
        final BeanContext deploymentInfo = callContext.getBeanContext();
        final EntityManager entityManager = getEntityManager(deploymentInfo);

        final StringBuilder queryName = new StringBuilder();
        queryName.append(deploymentInfo.getAbstractSchemaName()).append(".").append(queryMethod.getName());
        final String shortName = queryName.toString();
        if (queryMethod.getParameterTypes().length > 0) {
            queryName.append('(');
            boolean first = true;
            for (final Class<?> parameterType : queryMethod.getParameterTypes()) {
                if (!first) {
                    queryName.append(',');
                }
                queryName.append(parameterType.getCanonicalName());
                first = false;
            }
            queryName.append(')');

        }

        final String fullName = queryName.toString();
        Query query = createNamedQuery(entityManager, fullName);
        if (query == null) {
            query = createNamedQuery(entityManager, shortName);
            if (query == null) {
                throw new FinderException("No query defined for method " + fullName);
            }
        }
        return executeSelectQuery(query, args);
    }

    public List<Object> queryBeans(final BeanContext beanContext, final String signature, final Object[] args) throws FinderException {
        final EntityManager entityManager = getEntityManager(beanContext);

        Query query = createNamedQuery(entityManager, signature);
        if (query == null) {
            final int parenIndex = signature.indexOf('(');
            if (parenIndex > 0) {
                final String shortName = signature.substring(0, parenIndex);
                query = createNamedQuery(entityManager, shortName);
            }
            if (query == null) {
                throw new FinderException("No query defined for method " + signature);
            }
        }
        return executeSelectQuery(query, args);
    }

    private List<Object> executeSelectQuery(final Query query, Object[] args) {
        // process args
        if (args == null) {
            args = NO_ARGS;
        }
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            // ejb proxies need to be swapped out for real instance classes
            if (arg instanceof EJBObject) {
                arg = Cmp2Util.getEntityBean((EJBObject) arg);
            }
            if (arg instanceof EJBLocalObject) {
                arg = Cmp2Util.getEntityBean((EJBLocalObject) arg);
            }
            try {
                query.getParameter(i + 1);
            } catch (final IllegalArgumentException e) {
                // IllegalArgumentException means that the parameter with the
                // specified position does not exist
                continue;
            }
            query.setParameter(i + 1, arg);
        }

        // todo results should not be iterated over, but should instead
        // perform all work in a wrapper list on demand by the application code
        final List results = query.getResultList();
        for (final Object value : results) {
            if (value instanceof EntityBean) {
                // todo don't activate beans already activated
                final EntityBean entity = (EntityBean) value;
                cmpCallback.setEntityContext(entity);
                cmpCallback.ejbActivate(entity);
            }
        }
        //noinspection unchecked
        return results;
    }

    public int executeUpdateQuery(final BeanContext beanContext, final String signature, Object[] args) throws FinderException {
        final EntityManager entityManager = getEntityManager(beanContext);

        Query query = createNamedQuery(entityManager, signature);
        if (query == null) {
            final int parenIndex = signature.indexOf('(');
            if (parenIndex > 0) {
                final String shortName = signature.substring(0, parenIndex);
                query = createNamedQuery(entityManager, shortName);
            }
            if (query == null) {
                throw new FinderException("No query defined for method " + signature);
            }
        }

        // process args
        if (args == null) {
            args = NO_ARGS;
        }
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            // ejb proxies need to be swapped out for real instance classes
            if (arg instanceof EJBObject) {
                arg = Cmp2Util.getEntityBean((EJBObject) arg);
            }
            if (arg instanceof EJBLocalObject) {
                arg = Cmp2Util.getEntityBean((EJBLocalObject) arg);
            }
            query.setParameter(i + 1, arg);
        }

        final int result = query.executeUpdate();
        return result;
    }

    private Query createNamedQuery(final EntityManager entityManager, final String name) {
        try {
            return entityManager.createNamedQuery(name);
        } catch (final IllegalArgumentException ignored) {
            // soooo lame that jpa throws an exception instead of returning null....
            ignored.printStackTrace();
            return null;
        }
    }

    private TransactionPolicy startTransaction(final String operation, final ThreadContext callContext) {
        try {
            final TransactionPolicy txPolicy = createTransactionPolicy(TransactionType.Required, callContext);
            return txPolicy;
        } catch (final Exception e) {
            throw new EJBException("Unable to start transaction for " + operation + " operation", e);
        }
    }

    private void commitTransaction(final String operation, final ThreadContext callContext, final TransactionPolicy txPolicy) {
        try {
            afterInvoke(txPolicy, callContext);
        } catch (final Exception e) {
            throw new EJBException("Unable to complete transaction for " + operation + " operation", e);
        }
    }

    private void configureKeyGenerator(final BeanContext di) throws OpenEJBException {
        if (di.isCmp2()) {
            di.setKeyGenerator(new Cmp2KeyGenerator());
        } else {
            final String primaryKeyField = di.getPrimaryKeyField();
            final Class cmpBeanImpl = di.getCmpImplClass();
            if (primaryKeyField != null) {
                di.setKeyGenerator(new SimpleKeyGenerator(cmpBeanImpl, primaryKeyField));
            } else if (Object.class.equals(di.getPrimaryKeyClass())) {
                di.setKeyGenerator(new SimpleKeyGenerator(cmpBeanImpl, "OpenEJB_pk"));
            } else {
                di.setKeyGenerator(new ComplexKeyGenerator(cmpBeanImpl, di.getPrimaryKeyClass()));
            }
        }
    }

    private class OpenJPALifecycleListener extends AbstractLifecycleListener {
//        protected void eventOccurred(LifecycleEvent event) {
//            int type = event.getType();
//            switch (type) {
//                case LifecycleEvent.BEFORE_PERSIST:
//                    System.out.println("BEFORE_PERSIST");
//                    break;
//                case LifecycleEvent.AFTER_PERSIST:
//                    System.out.println("AFTER_PERSIST");
//                    break;
//                case LifecycleEvent.AFTER_LOAD:
//                    System.out.println("AFTER_LOAD");
//                    break;
//                case LifecycleEvent.BEFORE_STORE:
//                    System.out.println("BEFORE_STORE");
//                    break;
//                case LifecycleEvent.AFTER_STORE:
//                    System.out.println("AFTER_STORE");
//                    break;
//                case LifecycleEvent.BEFORE_CLEAR:
//                    System.out.println("BEFORE_CLEAR");
//                    break;
//                case LifecycleEvent.AFTER_CLEAR:
//                    System.out.println("AFTER_CLEAR");
//                    break;
//                case LifecycleEvent.BEFORE_DELETE:
//                    System.out.println("BEFORE_DELETE");
//                    break;
//                case LifecycleEvent.AFTER_DELETE:
//                    System.out.println("AFTER_DELETE");
//                    break;
//                case LifecycleEvent.BEFORE_DIRTY:
//                    System.out.println("BEFORE_DIRTY");
//                    break;
//                case LifecycleEvent.AFTER_DIRTY:
//                    System.out.println("AFTER_DIRTY");
//                    break;
//                case LifecycleEvent.BEFORE_DIRTY_FLUSHED:
//                    System.out.println("BEFORE_DIRTY_FLUSHED");
//                    break;
//                case LifecycleEvent.AFTER_DIRTY_FLUSHED:
//                    System.out.println("AFTER_DIRTY_FLUSHED");
//                    break;
//                case LifecycleEvent.BEFORE_DETACH:
//                    System.out.println("BEFORE_DETACH");
//                    break;
//                case LifecycleEvent.AFTER_DETACH:
//                    System.out.println("AFTER_DETACH");
//                    break;
//                case LifecycleEvent.BEFORE_ATTACH:
//                    System.out.println("BEFORE_ATTACH");
//                    break;
//                case LifecycleEvent.AFTER_ATTACH:
//                    System.out.println("AFTER_ATTACH");
//                    break;
//                case LifecycleEvent.AFTER_REFRESH:
//                    System.out.println("AFTER_REFRESH");
//                    break;
//                default:
//                    System.out.println("default");
//                    break;
//            }
//            super.eventOccurred(event);
//        }

        public void afterLoad(final LifecycleEvent lifecycleEvent) {
            eventOccurred(lifecycleEvent);
            final Object bean = lifecycleEvent.getSource();
            // This may seem a bit strange to call ejbActivate immedately followed by ejbLoad,
            // but it is completely legal.  Since the ejbActivate method is not allowed to access
            // persistent state of the bean (EJB 3.0fr 8.5.2) there should be no concern that the
            // call back method clears the bean state before ejbLoad is called.
            cmpCallback.setEntityContext((EntityBean) bean);
            cmpCallback.ejbActivate((EntityBean) bean);
            cmpCallback.ejbLoad((EntityBean) bean);
        }

        public void beforeStore(final LifecycleEvent lifecycleEvent) {
            eventOccurred(lifecycleEvent);
            final EntityBean bean = (EntityBean) lifecycleEvent.getSource();
            if (!creating.get().contains(bean)) {
                cmpCallback.ejbStore(bean);
            }
        }

        public void afterAttach(final LifecycleEvent lifecycleEvent) {
            eventOccurred(lifecycleEvent);
            final Object bean = lifecycleEvent.getSource();
            cmpCallback.setEntityContext((EntityBean) bean);
        }

        public void beforeDelete(final LifecycleEvent lifecycleEvent) {
            eventOccurred(lifecycleEvent);
            try {
                final Object bean = lifecycleEvent.getSource();
                cmpCallback.ejbRemove((EntityBean) bean);
            } catch (final RemoveException e) {
                throw new PersistenceException(e);
            }
        }

        public void afterDetach(final LifecycleEvent lifecycleEvent) {
            eventOccurred(lifecycleEvent);
            // todo detach is called after ejbRemove which does not need ejbPassivate
            final Object bean = lifecycleEvent.getSource();
            cmpCallback.ejbPassivate((EntityBean) bean);
            cmpCallback.unsetEntityContext((EntityBean) bean);
        }

        public void beforePersist(final LifecycleEvent lifecycleEvent) {
            eventOccurred(lifecycleEvent);
        }

        public void afterRefresh(final LifecycleEvent lifecycleEvent) {
            eventOccurred(lifecycleEvent);
        }

        public void beforeDetach(final LifecycleEvent lifecycleEvent) {
            eventOccurred(lifecycleEvent);
        }

        public void beforeAttach(final LifecycleEvent lifecycleEvent) {
            eventOccurred(lifecycleEvent);
        }
    }
}
