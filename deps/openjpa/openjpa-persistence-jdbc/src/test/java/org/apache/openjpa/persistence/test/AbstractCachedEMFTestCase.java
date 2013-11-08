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
package org.apache.openjpa.persistence.test;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.openjpa.kernel.AbstractBrokerFactory;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactorySPI;

/**
 * Testcase which caches EntityManagerFactories based on the persistence unit name and the properties provided at
 * runtime.  Extends AbstractPersistenceTestCase and provides cleanup of EMFs created by createNamedEMF() through
 * LinkHashMap.removeEldestEntry().
 * 
 */
public abstract class AbstractCachedEMFTestCase extends AbstractPersistenceTestCase {
    private static FixedMap<EMFKey, OpenJPAEntityManagerFactorySPI> _emfs =
        new FixedMap<EMFKey, OpenJPAEntityManagerFactorySPI>();

    /**
     * Create an entity manager factory for persistence unit <code>pu</code>. Put {@link #CLEAR_TABLES} in this list to
     * tell the test framework to delete all table contents before running the tests.
     * 
     * @param props
     *            list of persistent types used in testing and/or configuration values in the form
     *            key,value,key,value...
     */
    @Override
    protected OpenJPAEntityManagerFactorySPI createNamedEMF(String pu, Object... props) {
        Map<String, Object> map = getPropertiesMap(props);
        EMFKey key = new EMFKey(pu, map);
        OpenJPAEntityManagerFactorySPI oemf = _emfs.get(key);
        if (_fresh || oemf == null || !oemf.isOpen()) {
            oemf = super.createNamedEMF(pu, props);
            if (!_fresh) {
                _emfs.put(key, oemf);
            }
        }
        _fresh = false;
        return oemf;
    }

    @Override
    protected OpenJPAEntityManagerFactorySPI createNamedOpenJPAEMF(String pu,
            String res, Map<String,Object> props) {
        EMFKey key = new EMFKey(pu, props);
        OpenJPAEntityManagerFactorySPI oemf = _emfs.get(key);
        if (_fresh || oemf == null || !oemf.isOpen()) {
            oemf = super.createNamedOpenJPAEMF(pu, res, props);
            if (!_fresh) {
                _emfs.put(key, oemf);
            }
        }
        _fresh = false;
        return oemf;
    }

    private static class FixedMap<K, V> extends LinkedHashMap<K, V> {
        private static final long serialVersionUID = -3153852097468390779L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<K,V> entry) {
            OpenJPAEntityManagerFactorySPI oemf = (OpenJPAEntityManagerFactorySPI)entry.getValue();
            if (this.size() > 2) {
                // if the eldest should be removed, then try to close it first
                if (oemf != null && oemf.isOpen()) {
                    try {
                        // same code as AbstractPersistenceTestCase.closeAllOpenEMs()
                        for (Broker b:((AbstractBrokerFactory)JPAFacadeHelper.toBrokerFactory(oemf)).getOpenBrokers()) {
                            if (b != null && !b.isClosed()) {
                                EntityManager em = JPAFacadeHelper.toEntityManager(b);
                                if (em == null || !em.isOpen()) {
                                    continue;
                                }
                                if (em.getTransaction().isActive()) {
                                    em.getTransaction().rollback();
                                }
                                em.close();
                            }
                        }
                        oemf.close();
                    } catch (Exception e) {
                        // no-op - eat it
                    }
                }
                return true;
            }
            return false;
        }
    }

    private static class EMFKey {
        final String unit;
        final Map<String, Object> config;

        EMFKey(String unit, Map<String, Object> config) {
            this.unit = unit;
            this.config = config;
        }

        public int hashCode() {
            return (unit != null ? unit.hashCode() : 0) + config.hashCode();
        }

        public boolean equals(Object other) {
            EMFKey that = (EMFKey) other;
            return (unit != null ? unit.equals(that.unit) : that.unit == null) && config.equals(that.config);
        }
    }

}
