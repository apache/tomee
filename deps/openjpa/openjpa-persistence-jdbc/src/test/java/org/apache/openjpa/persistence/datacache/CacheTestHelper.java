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
package org.apache.openjpa.persistence.datacache;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.openjpa.datacache.DataCache;
import org.apache.openjpa.datacache.QueryCache;
import org.apache.openjpa.datacache.QueryKey;
import org.apache.openjpa.datacache.DataCacheManager;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.kernel.Query;
import org.apache.openjpa.persistence.common.utils.AbstractTestCase;

class CacheTestHelper {

    static void checkCache(AbstractTestCase tc, DataCache cache, Object[] ids,
        boolean[] stati) {
        for (int i = 0; i < ids.length; i++) {
            if (stati[i])
                tc.assertTrue("id " + i + " (" + ids[i]
                    + ") was not in cache; should have been",
                    cache.contains(ids[i]));
            else
                tc.assertFalse("id " + i + " (" + ids[i]
                    + ") was in cache; should not have been",
                    cache.contains(ids[i]));
        }
    }

    static void assertInCache(AbstractTestCase tc, Query q, Boolean inCache) {
        assertInCache(tc, q, inCache, new Object[0]);
    }

    /**
     * Test whether the given query has valid cached results.
     *
     * @param inCache if {@link Boolean#TRUE}, the query must have valid
     * cached results; if {@link Boolean#FALSE} the query
     * must not have any cached results; if null the
     * query may have cached results, but they cannot be
     * valid (i.e. they can't be returned to the user)
     */
    static void assertInCache(AbstractTestCase tc, Query query, Boolean inCache,
        Object[] args) {
        QueryKey qk = QueryKey.newInstance(query, args);
        Broker broker = query.getBroker();
        BrokerFactory factory = broker.getBrokerFactory();

        QueryCache qc = cacheManager(factory).getSystemQueryCache();
        if (inCache == Boolean.FALSE && qc.get(qk) != null) {
            tc.fail("query should not be in cache; was.");
        } else if (inCache == Boolean.TRUE || (inCache == null
            && qc.get(qk) != null)) {
            Object res = (args == null) ? query.execute()
                : query.execute(args);
            if (inCache == Boolean.TRUE &&
                !isCachedResult(res, inCache, query.getBroker()))
                tc.fail("query should be in cache; was not.");
            else if (inCache == null &&
                isCachedResult(res, inCache, query.getBroker()))
                tc.fail("query should not be returned to user; was.");
            query.closeAll();
        }
    }

    private static boolean isCachedResult(Object res, Boolean expected,
        Broker broker) {
        // we can only check for a CachedQueryResult if the
        // Broker was configured to be a CacheTestBroker
        if (!(broker instanceof CacheTestBroker))
            throw new IllegalArgumentException("Broker was not set to be "
                + "a CacheTestBroker, making it impossible to verify "
                + "if query result is cached");

        if (res instanceof Collection)
            return res instanceof CacheTestBroker.CachedQueryResult;
        // no way to tell if unique results from cache
        return expected.booleanValue();
    }

    static void iterate(Collection c) {
        // iterate through the collection so that the results have an
        // opportunity to register themselves with the PM.
        int count = 0;
        for (Iterator iter = c.iterator(); iter.hasNext();) {
            try {
                count++;
                iter.next();
            } catch (NoSuchElementException e) {
                throw e;
            }
        }
    }

    static DataCacheManager cacheManager(BrokerFactory factory) {
        return factory.getConfiguration().getDataCacheManagerInstance();
    }
}
