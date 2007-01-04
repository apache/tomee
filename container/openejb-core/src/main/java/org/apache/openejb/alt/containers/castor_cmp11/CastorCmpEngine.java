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
import java.util.StringTokenizer;
import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.EJBException;
import javax.ejb.EJBObject;
import javax.ejb.EntityBean;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.cmp.CmpCallback;
import org.apache.openejb.core.cmp.CmpEngine;
import org.apache.openejb.core.cmp.KeyGenerator;
import org.apache.openejb.util.Logger;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.JDOManager;
import org.exolab.castor.jdo.OQLQuery;
import org.exolab.castor.jdo.PersistenceException;
import org.exolab.castor.jdo.QueryResults;
import org.exolab.castor.mapping.AccessMode;
import org.exolab.castor.persist.spi.CallbackInterceptor;
import org.exolab.castor.persist.spi.InstanceFactory;

public class CastorCmpEngine implements CmpEngine {
    private static final Logger logger = Logger.getInstance("OpenEJB", "org.apache.openejb.core.cmp");
    private static final Object[] NO_ARGS = new Object[0];
    private static final String TRANSACTION_MANAGER_JNDI_NAME = "java:openejb/TransactionManager";

    private final JDOManager globalJdoManager;
    private final CmpCallback cmpCallback;
    private final JndiTxReference txReference;

    private final Map<String,String> abstractSchemaMap = new HashMap<String,String>();

    public CastorCmpEngine(String jarPath, CmpCallback cmpCallback, TransactionManager transactionManager, String engine, String connectorName, ClassLoader classLoader) throws OpenEJBException {
        this.cmpCallback = cmpCallback;

        txReference = new JndiTxReference(transactionManager);

        Map<String, URL> mappings = new HashMap<String, URL>();
        if (jarPath == null) {
            // DMB: This may pull in more than we like
            try {
                Enumeration<URL> resources = classLoader.getResources("META-INF/cmp.mapping.xml");
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    mappings.put(url.toExternalForm(), url);
                }
            } catch (IOException e) {
                throw new OpenEJBException("Error locating mapping file via classloader", e);
            }
        } else {
            URL url = null;
            try {
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
                throw new OpenEJBException("Error locating mapping file " + url, e);
            }
        }

        try {
            JDOManagerBuilder jdoManagerBuilder = new JDOManagerBuilder(engine, TRANSACTION_MANAGER_JNDI_NAME, classLoader);

            Collection<URL> urls = mappings.values();
            for (Iterator<URL> iterator = urls.iterator(); iterator.hasNext();) {
                URL url = iterator.next();
                logger.debug("Mapping file: " + url.toExternalForm());
                jdoManagerBuilder.addMapping(url);
            }

            String jdbcName = "java:openejb/connector/" + connectorName;
            DataSource connectionFactory = (DataSource) new InitialContext().lookup(jdbcName);
            if (connectionFactory == null) {
                throw new OpenEJBException(jdbcName + " does not exist");
            }

            globalJdoManager = jdoManagerBuilder.buildGlobalJDOManager(jdbcName);
            globalJdoManager.setDatabasePooling(true);
            globalJdoManager.setCallbackInterceptor(new CastorCallbackInterceptor());
            globalJdoManager.setInstanceFactory(new CastorInstanceFactory());
        } catch (Exception e) {
            e.printStackTrace();
            throw new OpenEJBException("Unable to construct the Castor JDOManager objects: " + e.getClass().getName() + ": " + e.getMessage(), e);
        }
    }

    public void deploy(CoreDeploymentInfo deploymentInfo) throws SystemException {
        bindTransactionManagerReference(deploymentInfo);
        configureKeyGenerator(deploymentInfo);
        String beanClassName = deploymentInfo.getBeanClass().getName();
        String abstractSchemaName = beanClassName.substring(beanClassName.lastIndexOf('.') + 1);
        abstractSchemaMap.put(abstractSchemaName, beanClassName);
    }

    public Object createBean(EntityBean bean, ThreadContext callContext) throws CreateException {
        try {
            Database db = getDatabase();

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
            Database db = getDatabase();

            Object castorPrimaryKey = getCastorPrimaryKey(callContext, primaryKey);
            Object bean = db.load(callContext.getDeploymentInfo().getBeanClass(), castorPrimaryKey);
            return bean;
        } catch (org.exolab.castor.jdo.PersistenceException e) {
            throw new EJBException("Unable to load ejb (DeploymentID=\"" + callContext.getDeploymentInfo().getDeploymentID() + "\")", e);
        }
    }

    public void removeBean(ThreadContext callContext) {
        try {
            Database db = getDatabase();

            Object primaryKey = getCastorPrimaryKey(callContext, callContext.getPrimaryKey());
            Object bean = db.load(callContext.getDeploymentInfo().getBeanClass(), primaryKey);
            db.remove(bean);
        } catch (org.exolab.castor.jdo.PersistenceException e) {
            throw new EJBException("Unable to remove ejb (DeploymentID=\"" + callContext.getDeploymentInfo().getDeploymentID() + "\")", e);
        }
    }

    public List<Object> queryBeans(ThreadContext callContext, String queryString, Object[] args) throws FinderException {
        // EJB-QL and OQL are close enough we try to support both for basic queries
        queryString = preprocessQuery(queryString);
        try {
            Database db = getDatabase();

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

    private String preprocessQuery(String queryString) {
        // OQL uses FQN for abstract schema name so replace abstract
        // schema name with FQN
        String lowerCase = queryString.toLowerCase();
        int fromStart = lowerCase.indexOf("from") + 4;
        int fromEnd = lowerCase.indexOf("where", fromStart);
        if (fromEnd < 0) {
            fromEnd = lowerCase.indexOf("group", fromStart);
        }
        if (fromEnd < 0) {
            fromEnd = lowerCase.indexOf("order", fromStart);
        }
        if (fromEnd < 0) {
            fromEnd = lowerCase.indexOf("limit", fromStart);
        }
        String selectClause = queryString.substring(0, fromStart);
        String fromClause = queryString.substring(fromStart, fromEnd);
        String remaining = queryString.substring(fromEnd);

        StringBuilder newFromClause = new StringBuilder(fromClause.length() * 2);
        for (StringTokenizer iteratorDefTokenizer = new StringTokenizer(fromClause, ","); iteratorDefTokenizer.hasMoreTokens();) {
            String iteratorDef = iteratorDefTokenizer.nextToken();
            boolean isFirst = true;
            for (StringTokenizer tokenizer = new StringTokenizer(iteratorDef); tokenizer.hasMoreTokens();) {
                String identifier = tokenizer.nextToken();
                if (isFirst) {
                    String newIdentifier = abstractSchemaMap.get(identifier);
                    if (newIdentifier != null) {
                        identifier = newIdentifier;
                    }
                    isFirst = false;
                }
                newFromClause.append(identifier);
                if (tokenizer.hasMoreTokens()) {
                    newFromClause.append(" ");
                }
            }
            if (iteratorDefTokenizer.hasMoreTokens()) {
                newFromClause.append(",");
            }
        }

        String newQueryString = selectClause + " " + newFromClause + " " + remaining;

        // OQL uses $N for parameters and EJBQL uses ?N, so lets replace any
        // occurance of ? with $ which works since ? is an illegal character
        // in an OQL query
        newQueryString = newQueryString.replace('?', '$');

        return newQueryString;
    }

    private Object getCastorPrimaryKey(ThreadContext callContext, Object primaryKey) {
        CastorKeyGenerator kg = (CastorKeyGenerator) callContext.getDeploymentInfo().getKeyGenerator();
        if (kg.isKeyComplex()) {
            return kg.getJdoComplex(primaryKey);
        } else {
            return primaryKey;
        }
    }

    private Database getDatabase() throws PersistenceException {
        return globalJdoManager.getDatabase();
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
    private void bindTransactionManagerReference(CoreDeploymentInfo deploymentInfo) throws SystemException {
        try {
            deploymentInfo.getJndiEnc().bind(TRANSACTION_MANAGER_JNDI_NAME, txReference);
        } catch (Exception e) {
            logger.error("Unable to bind TransactionManager to deployment id = " + deploymentInfo.getDeploymentID() + " using JNDI name = \"" + TRANSACTION_MANAGER_JNDI_NAME + "\"", e);
            throw new SystemException("Unable to bind TransactionManager to deployment id = " + deploymentInfo.getDeploymentID() + " using JNDI name = \"" + TRANSACTION_MANAGER_JNDI_NAME + "\"", e);
        }
    }

    private void configureKeyGenerator(CoreDeploymentInfo di) throws SystemException {
        try {
            String primaryKeyField = di.getPrimaryKeyField();
            Class beanClass = di.getBeanClass();
            if (primaryKeyField != null) {
                di.setKeyGenerator(new CastorSimpleKeyGenerator(beanClass, primaryKeyField));
            } else {
                di.setKeyGenerator(new CastorComplexKeyGenerator(beanClass, di.getPrimaryKeyClass()));
            }
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
}
