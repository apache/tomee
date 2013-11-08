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

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import org.apache.openjpa.persistence.criteria.OpenJPACriteriaBuilder;
import org.apache.openjpa.persistence.query.QueryBuilder;

/**
 * Interface implemented by OpenJPA entity manager factories.
 *
 * @author Abe White
 * @since 0.4.0
 * @published
 */
public interface OpenJPAEntityManagerFactory
    extends EntityManagerFactory, Serializable {

    /**
     * Return properties describing this runtime.
     */
    public Map<String, Object> getProperties();

    /**
     * Put the specified key-value pair into the map of user objects.
     */
    public Object putUserObject(Object key, Object val);

    /**
     * Get the value for the specified key from the map of user objects.
     */
    public Object getUserObject(Object key);

    /**
     * Access the level 2 store cache. This cache acts as a proxy to all
     * named caches.
     */
    public StoreCache getStoreCache();

    /**
     * Access a named level 2 store cache.
     */
    public StoreCache getStoreCache(String name);

    /**
     * Access query result cache.
     */
    public QueryResultCache getQueryResultCache();

    public OpenJPAEntityManager createEntityManager();

    /**
     * Return an entity manager with the provided additional configuration
     * settings. OpenJPA recognizes the following configuration settings in this
     * method:
     * <ul>
     * <li>openjpa.ConnectionUsername</li>
     * <li>openjpa.ConnectionPassword</li>
     * <li>openjpa.ConnectionRetainMode</li>
     * <li>openjpa.TransactionMode</li>
     * </ul>
     */
    public OpenJPAEntityManager createEntityManager(Map props);

    /**
     * @deprecated use {@link ConnectionRetainMode} enums instead.
     */
    public static final int CONN_RETAIN_DEMAND = 0;

    /**
     * @deprecated use {@link ConnectionRetainMode} enums instead.
     */
    public static final int CONN_RETAIN_TRANS = 1;

    /**
     * @deprecated use {@link ConnectionRetainMode} enums instead.
     */
    public static final int CONN_RETAIN_ALWAYS = 2;

    /**
     * @deprecated cast to {@link OpenJPAEntityManagerFactorySPI} instead. This
     * method pierces the published-API boundary, as does the SPI cast.
     */
    public org.apache.openjpa.conf.OpenJPAConfiguration getConfiguration();

    /**
     * @deprecated cast to {@link OpenJPAEntityManagerFactorySPI} instead. This
     * method pierces the published-API boundary, as does the SPI cast.
     */
    public void addLifecycleListener(Object listener, Class... classes);

    /**
     * @deprecated cast to {@link OpenJPAEntityManagerFactorySPI} instead. This
     * method pierces the published-API boundary, as does the SPI cast.
     */
    public void removeLifecycleListener(Object listener);

    /**
     * @deprecated cast to {@link OpenJPAEntityManagerFactorySPI} instead. This
     * method pierces the published-API boundary, as does the SPI cast.
     */
    public void addTransactionListener(Object listener);

    /**
     * @deprecated cast to {@link OpenJPAEntityManagerFactorySPI} instead. This
     * method pierces the published-API boundary, as does the SPI cast.
     */
    public void removeTransactionListener(Object listener);
    
    /**
     * Gets a builder for dynamic queries.
     */
    public QueryBuilder getDynamicQueryBuilder();
    
    /**
     * Gets the QueryBuilder with OpenJPA-extended capabilities. 
     * 
     * @since 2.0.0
     */
    public OpenJPACriteriaBuilder getCriteriaBuilder();
    
    /**
     * Get the properties supported by this runtime.
     * 
     * @since 2.0.0
    */
    public Set<String> getSupportedProperties();
}
