//This class access the private class QueryCacheStoreQuery.
//So this has to be in kodo.datacache package
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

import java.util.Collections;
import java.util.List;

import org.apache.openjpa.datacache.QueryCacheStoreQuery.CachedList;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.BrokerImpl;
import org.apache.openjpa.kernel.QueryImpl;
import org.apache.openjpa.kernel.StoreQuery;
import org.apache.openjpa.lib.rop.ListResultList;
import org.apache.openjpa.lib.rop.ListResultObjectProvider;
import org.apache.openjpa.lib.rop.ResultObjectProvider;

/**
 * <p>
 * Used to mark cached query results for testing.
 * </p>
 */
public class CacheTestBroker extends BrokerImpl {

    protected QueryImpl newQueryImpl(String language, StoreQuery sq) {
        return new CacheTestQuery(this, language, sq);
    }

    public static class CachedQueryResult extends ListResultList {

        public CachedQueryResult(List list) {
            super(list);
        }
    }

    private static class CacheTestQuery extends QueryImpl {

        public CacheTestQuery(Broker broker, String language,
            StoreQuery query) {
            super(broker, language, query);
        }

        protected Object toResult(StoreQuery q, StoreQuery.Executor ex,
            ResultObjectProvider rop, StoreQuery.Range range)
            throws Exception {
            boolean cached = rop instanceof ListResultObjectProvider
                && (((ListResultObjectProvider) rop)
                .getDelegate() instanceof CachedList ||
                ((ListResultObjectProvider) rop)
                    .getDelegate() == Collections.EMPTY_LIST);
            Object res = super.toResult(q, ex, rop, range);
            if (cached && res instanceof List)
                return new CachedQueryResult((List) res);
            return res;
        }
    }
}
