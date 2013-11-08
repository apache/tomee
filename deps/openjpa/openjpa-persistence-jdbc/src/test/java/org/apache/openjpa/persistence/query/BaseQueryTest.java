/*
 * BaseKernelTest.java
 *
 * Created on October 9, 2006, 12:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

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
package org.apache.openjpa.persistence.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.jpql.JPQLParser;
import org.apache.openjpa.lib.conf.ConfigurationProvider;
import org.apache.openjpa.lib.conf.Configurations;
import org.apache.openjpa.persistence.OpenJPAEntityManager;

public abstract class BaseQueryTest
    extends org.apache.openjpa.persistence.common.utils.AbstractTestCase {

    private static Map _sysprops = new HashMap();

    /**
     * Creates a new instance of BaseQueryTest
     */
    public BaseQueryTest(String name) {
        super(name, "querycactusapp");
    }

    protected OpenJPAEntityManager getEM() {
        return (OpenJPAEntityManager) currentEntityManager();
    }

    protected OpenJPAEntityManager getEM(boolean optimistic,
        boolean retainValues) {
        OpenJPAEntityManager em =
            (OpenJPAEntityManager) currentEntityManager();
        em.setNontransactionalRead(true);
        em.setRetainState(retainValues);
        em.setOptimistic(optimistic);
        return em;
    }

    /**
     * Delete all instances of the given class.
     */
    protected int deleteAll(Broker broker, Class clazz,
        boolean subclasses) {
        final boolean useDeleteByQuery = false;

        if (useDeleteByQuery) {
            org.apache.openjpa.kernel.Query query = broker.newQuery(
                JPQLParser.LANG_JPQL, clazz, "");
            query.setCandidateType(clazz, subclasses);
            return (int) query.deleteAll();
        } else {
            org.apache.openjpa.kernel.Extent extent =
                broker.newExtent(clazz, subclasses);
            List list = extent.list();
            int size = list.size();
            broker.deleteAll(list, null);
            return size;
        }
    }

    protected Object persist(Object ob) {
        Broker broker = getBrokerFactory().newBroker();
        broker.begin();
        broker.persist(ob, null);
        Object id = broker.getObjectId(ob);
        broker.commit();
        broker.close();
        return id;
    }

    protected Properties getProperties() {
        return getProperties(null);
    }

    /**
     * Return the runtime properties, optionally overriding or setting
     * some via the given array, which should be in the form
     * { key, val, key, val, ... }.
     */
    protected synchronized Properties getProperties(String[] props) {
        Properties p = new Properties(System.getProperties());
        String str = p.getProperty("openjpa.properties", "kodo.properties");
        if (str != null && str.length() > 0) {
            // cache system properties to reduce load on file system
            Properties loaded = (Properties) _sysprops.get(str);
            if (loaded == null) {
                loaded = new Properties();
                ConfigurationProvider cp =
                    (ConfigurationProvider) Configurations
                        .getProperty(str, null);
                if (cp != null)
                    loaded.putAll(cp.getProperties());
                _sysprops.put(str, loaded);
            }
            p.putAll(loaded);
        }

        for (int i = 0; props != null && i < props.length; i += 2) {
            if (props[i + 1] != null) {
                // remove any duplicate kodo/openjpa property so we don't clash
                if (props[i].startsWith("openjpa."))
                    p.remove("openjpa." + props[i].substring(5));
                else if (props[i].startsWith("openjpa."))
                    p.remove("openjpa." + props[i].substring(8));

                p.setProperty(props[i], props[i + 1]);
            } else {
                p.remove(props[i]);
            }
        }
        return p;
    }
}
