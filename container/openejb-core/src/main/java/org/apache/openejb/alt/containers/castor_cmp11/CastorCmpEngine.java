/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.alt.containers.castor_cmp11;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.cmp.CmpCallback;
import org.apache.openejb.core.cmp.CmpEngine;
import org.apache.openejb.resource.jdbc.JdbcConnectionFactory;
import org.apache.openejb.util.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.JDOManager;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.QueryResults;
import org.exolab.castor.jdo.TransactionNotInProgressException;
import org.exolab.castor.mapping.AccessMode;
import org.exolab.castor.persist.spi.CallbackInterceptor;
import org.exolab.castor.persist.spi.InstanceFactory;

import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.EntityBean;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.naming.InitialContext;
import javax.persistence.EntityTransaction;
import javax.transaction.TransactionManager;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CastorCmpEngine implements CmpEngine {
    private static final Logger logger = Logger.getInstance("OpenEJB", "org.apache.openejb.core.cmp");
    private static final Object[] NO_ARGS = new Object[0];

    private final JDOManager localJdoManager;
    private final JDOManager globalJdoManager;
    private final CmpCallback cmpCallback;

    public CastorCmpEngine(CmpCallback cmpCallback, TransactionManager transactionManager, DeploymentInfo[] deploys, String engine, String connectorName) throws OpenEJBException {
        this.cmpCallback = cmpCallback;
        String transactionManagerJndiName = "java:openejb/TransactionManager";

        Map<String, URL> mappings = new HashMap<String, URL>();
        JndiTxReference txReference = new JndiTxReference(transactionManager);
        for (DeploymentInfo deploymentInfo : deploys) {
            CoreDeploymentInfo di = (CoreDeploymentInfo) deploymentInfo;

            if (di.getJarPath() == null) {
                // DMB: This may pull in more than we like
                try {
                    ClassLoader classLoader = di.getBeanClass().getClassLoader();
                    Enumeration<URL> resources = classLoader.getResources("META-INF/cmp.mapping.xml");
                    while (resources.hasMoreElements()) {
                        URL url = resources.nextElement();
                        mappings.put(url.toExternalForm(), url);
                    }
                } catch (IOException e) {
                    throw new OpenEJBException("Error locating mapping file via classloader for deployment " + di.getDeploymentID(), e);
                }
            } else {
                URL url = null;
                try {
                    String jarPath = di.getJarPath();
                    File file = new File(jarPath);

                    if (file.isDirectory()) {
                        file = new File(file, "META-INF");
                        file = new File(file, "cmp.mapping.xml");
                        url = file.toURL();
                    } else {
                        url = file.toURL();
                        url = new URL("jar:" + url.toExternalForm() + "!/META-INF/cmp.mapping.xml");
                    }
                    mappings.put(url.toExternalForm(), url);
                } catch (MalformedURLException e) {
                    throw new OpenEJBException("Error locating mapping file " + url + " for deployment " + di.getDeploymentID(), e);
                }
            }

            bindTransactionManagerReference(di, transactionManagerJndiName, txReference);

            configureKeyGenerator(di);
        }


        try {
            JDOManagerBuilder jdoManagerBuilder = new JDOManagerBuilder(engine, transactionManagerJndiName, new JoinedClassLoader(deploys));

            Collection<URL> urls = mappings.values();
            for (Iterator<URL> iterator = urls.iterator(); iterator.hasNext();) {
                URL url = iterator.next();
                logger.debug("Mapping file: " + url.toExternalForm());
                jdoManagerBuilder.addMapping(url);
            }

            String jdbcName = "java:openejb/connector/" + connectorName;
            JdbcConnectionFactory connectionFactory = (JdbcConnectionFactory) new InitialContext().lookup(jdbcName);
            if (connectionFactory == null) {
                throw new OpenEJBException(jdbcName + " does not exist");
            }

            globalJdoManager = jdoManagerBuilder.buildGlobalJDOManager(jdbcName);
            globalJdoManager.setDatabasePooling(true);
            globalJdoManager.setCallbackInterceptor(new CastorCallbackInterceptor());
            globalJdoManager.setInstanceFactory(new CastorInstanceFactory());

            localJdoManager = jdoManagerBuilder.buildLocalJDOManager(connectionFactory.getJdbcDriver(), connectionFactory.getJdbcUrl(), connectionFactory.getDefaultUserName(), connectionFactory.getDefaultPassword());
            localJdoManager.setCallbackInterceptor(new CastorCallbackInterceptor());
            localJdoManager.setInstanceFactory(new CastorInstanceFactory());
        } catch (Exception e) {
            e.printStackTrace();
            throw new OpenEJBException("Unable to construct the Castor JDOManager objects: " + e.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

    public EntityTransaction getTransaction() {
        try {
            Database database = localJdoManager.getDatabase();
            return new CastorEntityTransaction(database);
        } catch (PersistenceException e) {
            throw new javax.persistence.PersistenceException(e);
        }
    }

    public Object createBean(EntityBean bean, ThreadContext callContext) throws CreateException {
        try {
            Database db = getDatabase(callContext);

            // Create a Castor Transaction if there isn't one in progress
            if (!db.isActive()) db.begin();

            // Use Castor JDO to insert the entity bean into the database
            db.create(bean);

            // extract the primary key from the bean
            CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
            KeyGenerator kg = deploymentInfo.getKeyGenerator();
            Object primaryKey = kg.getPrimaryKey(bean);

            return primaryKey;
        } catch (org.exolab.castor.jdo.DuplicateIdentityException e) {
            throw new DuplicateKeyException("Attempt to create an entity bean (DeploymentID=\"" + callContext.getDeploymentInfo().getDeploymentID() + "\") with an primary key that already exsists. Castor nested exception message = " + e.getMessage());
        } catch (PersistenceException e) {
            throw (CreateException) new CreateException("Unable to create ejb (DeploymentID=\"" + callContext.getDeploymentInfo().getDeploymentID() + "\")").initCause(e);
        }
    }

    public Object loadBean(ThreadContext callContext, Object primaryKey) {
        try {
            Database db = getDatabase(callContext);

            Object castorPrimaryKey = getCastorPrimaryKey(callContext, primaryKey);
            Object bean = db.load(callContext.getDeploymentInfo().getBeanClass(), castorPrimaryKey);
            return bean;
        } catch (org.exolab.castor.jdo.PersistenceException e) {
            throw new EJBException("Unable to load ejb (DeploymentID=\"" + callContext.getDeploymentInfo().getDeploymentID() + "\")", e);
        }
    }

    public void removeBean(ThreadContext callContext) {
        try {
            Database db = getDatabase(callContext);

            if (!db.isActive()) db.begin();

            Object primaryKey = getCastorPrimaryKey(callContext, callContext.getPrimaryKey());
            Object bean = db.load(callContext.getDeploymentInfo().getBeanClass(), primaryKey);
            db.remove(bean);
        } catch (org.exolab.castor.jdo.PersistenceException e) {
            throw new EJBException("Unable to remove ejb (DeploymentID=\"" + callContext.getDeploymentInfo().getDeploymentID() + "\")", e);
        }
    }

    public List<Object> queryBeans(ThreadContext callContext, String queryString, Object[] args) throws FinderException {
        try {
            Database db = getDatabase(callContext);

            // Create a Castor Transaction if there isn't one in progress
            if (!db.isActive()) db.begin();

            /*
              Obtain a OQLQuery object based on the String query
            */
            OQLQuery query = db.getOQLQuery(queryString);

            // process args
            Object[] queryArgs;
            if (args == null) {
                queryArgs = NO_ARGS;
            } else {
                queryArgs = args.clone();
            }

            for (int i = 0; i < queryArgs.length; i++) {
                if (queryArgs[i] instanceof EJBObject) {
                    /*
                    Its possible that the finder method's arguments are actually EJBObject reference in
                    which case the EJBObject reference is replaced with the EJB object's primary key.
                    The limitation of this facility is that the EJB object must use a single field primary key
                    and not a complex primary key. Complex primary keys of EJBObject argumetns are not supported.
                    For Example:

                    EJB Home Interface Find method:
                    public Collection findThings(Customer customer);

                    OQL in deployment descriptor
                    "SELECT t FROM Thing t WHERE t.customer_id = $1"

                    */
                    try {
                        queryArgs[i] = ((EJBObject) queryArgs[i]).getPrimaryKey();
                    } catch (RemoteException re) {

                        throw new FinderException("Could not extract primary key from EJBObject reference; argument number " + i);
                    }
                }

                /*
                Bind the arguments of the home interface find method to the query.
                The big assumption here is that the arguments of the find operation
                are in the same order as the arguments in the query.  The bean developer
                must declare the OQL arguments with the proper number so that match the order
                of the find arguments.
                For Example:

                EJB Home Interface Find method:
                public Collection findThings(String name, double weight, String Type);

                OQL in deployment descriptor
                "SELECT t FROM Thing t WHERE t.weight = $2 AND t.type = $3 AND t.name = $1"
                */

                query.bind(queryArgs[i]);
            }

            /*  execute the query */
            List<Object> results;
            QueryResults queryResults = null;
            try {
                queryResults = query.execute();

                results = new ArrayList<Object>();
                while (queryResults.hasMore()) {
                    Object item = queryResults.next();
                    results.add(item);
                }
            } finally {
                if (queryResults != null) queryResults.close();
            }
            return results;
        } catch (PersistenceException e) {
            throw (FinderException) new FinderException("Error executing query").initCause(e);
        }
    }

    private Object getCastorPrimaryKey(ThreadContext callContext, Object primaryKey) {
        KeyGenerator kg = callContext.getDeploymentInfo().getKeyGenerator();
        if (kg.isKeyComplex()) {
            return kg.getJdoComplex(primaryKey);
        } else {
            return primaryKey;
        }
    }

    private Database getDatabase(ThreadContext callContext) throws PersistenceException {
        CastorEntityTransaction castorEntityTransaction = (CastorEntityTransaction) callContext.getUnspecified();
        if (castorEntityTransaction != null) {
            return castorEntityTransaction.database;
        } else {
            /*
             BIG PROBLEM: Transacitons should use the same Database object.
             If Thomas won't put this into JDO then I'll have to put into the
             container.

             1. Check thread to see if current transacion is mapped to any
                existing Database object.

             2. If it is, return that Database object.

             3. If not obtain new Database object

             4. Register the Tranaction and Database object in a hashmap keyed
                by tx.

             5. When transaction completes, remove tx-to-database mapping from
                hashmap.

             */
            return globalJdoManager.getDatabase();
        }
    }


    private static class JoinedClassLoader extends ClassLoader {
        private final DeploymentInfo[] deploymentInfos;

        public JoinedClassLoader(DeploymentInfo[] deploymentInfos) {
            this.deploymentInfos = deploymentInfos;
        }

        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            for (DeploymentInfo info : deploymentInfos) {
                try {
                    ClassLoader classLoader = info.getBeanClass().getClassLoader();
                    return classLoader.loadClass(name);
                } catch (ClassNotFoundException keepTrying) {
                }
            }
            throw new ClassNotFoundException(name);
        }
    }

    /**
     * Castor JDO obtains a reference to the TransactionManager throught the InitialContext.
     * The new InitialContext will use the deployment's JNDI Context, which is normal inside
     * the container system, so we need to bind the TransactionManager to the deployment's name space
     * The biggest problem with this is that the bean itself may access the TransactionManager if it
     * knows the JNDI name, so we bind the TransactionManager into dynamically created transient name
     * space based every time the container starts. It nearly impossible for the bean to anticipate
     * and use the binding directly.  It may be possible, however, to locate it using a Context listing method.
     */
    private void bindTransactionManagerReference(CoreDeploymentInfo di, String transactionManagerJndiName, JndiTxReference txReference) throws SystemException {
        try {
            di.getJndiEnc().bind(transactionManagerJndiName, txReference);
        } catch (Exception e) {
            logger.error("Unable to bind TransactionManager to deployment id = " + di.getDeploymentID() + " using JNDI name = \"" + transactionManagerJndiName + "\"", e);
            throw new SystemException("Unable to bind TransactionManager to deployment id = " + di.getDeploymentID() + " using JNDI name = \"" + transactionManagerJndiName + "\"", e);
        }
    }

    private void configureKeyGenerator(CoreDeploymentInfo di) throws SystemException {
        try {
            KeyGenerator kg = KeyGeneratorFactory.createKeyGenerator(di);
            di.setKeyGenerator(kg);
        } catch (Exception e) {
            logger.error("Unable to create KeyGenerator for deployment id = " + di.getDeploymentID(), e);
            throw new SystemException("Unable to create KeyGenerator for deployment id = " + di.getDeploymentID(), e);
        }
    }

    private class CastorInstanceFactory implements InstanceFactory {
        public Object newInstance(String className, ClassLoader loader) {
            EntityBean bean;
            try {
                ThreadContext callContext = ThreadContext.getThreadContext();
                CoreDeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
                bean = (EntityBean) deploymentInfo.getBeanClass().newInstance();
            } catch (Exception e) {
                throw new EJBException("Unable to create new entity bean instance");
            }

            cmpCallback.setEntityContext(bean);
            return bean;
        }
    }

    private class CastorCallbackInterceptor implements CallbackInterceptor {
        public Class loaded(Object bean, AccessMode mode) throws Exception {
            cmpCallback.ejbActivate((EntityBean) bean);
            cmpCallback.ejbLoad((EntityBean) bean);
            return null;
        }

        public void storing(Object bean, boolean modified) {
            cmpCallback.ejbStore((EntityBean) bean);
        }

        public void removing(Object bean) throws RemoveException {
            cmpCallback.ejbRemove((EntityBean) bean);
        }

        public void releasing(Object bean, boolean committed) {
            cmpCallback.ejbPassivate((EntityBean) bean);
            cmpCallback.unsetEntityContext((EntityBean) bean);
        }

        public void creating(Object bean, Database db) {
        }

        public void created(Object bean) {
        }

        public void removed(Object bean) {
        }

        public void using(Object bean, Database db) {
        }

        public void updated(Object bean) {
        }
    }

    public static class CastorEntityTransaction implements EntityTransaction {
        private final Database database;
        private boolean rollbackOnly;

        public CastorEntityTransaction(Database database) {
            this.database = database;
        }

        public Database getDatabase() {
            return database;
        }

        public void begin() {
            if (database.isActive()) throw new IllegalStateException("Transaction already in progress");

            try {
                database.begin();
            } catch (PersistenceException e) {
                throw new javax.persistence.PersistenceException(e);
            }
        }

        public void commit() {
            try {
                if (rollbackOnly) {
                    database.rollback();
                    throw new javax.persistence.RollbackException();
                } else {
                    database.commit();
                }
            } catch (TransactionNotInProgressException e) {
                throw new IllegalStateException("Transaction not in progress");
            } catch (PersistenceException e) {
                throw new javax.persistence.PersistenceException(e);
            }
        }

        public void rollback() {
            try {
                database.rollback();
            } catch (TransactionNotInProgressException e) {
                throw new IllegalStateException("Transaction not in progress");
            }
        }

        public boolean isActive() {
            return database.isActive();
        }

        public void setRollbackOnly() {
            rollbackOnly = true;
        }

        public boolean getRollbackOnly() {
            return rollbackOnly;
        }
    }
}
