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
package org.apache.openjpa.kernel;

import java.util.Map;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.openjpa.datacache.DataCache;
import org.apache.openjpa.kernel.exps.AggregateListener;
import org.apache.openjpa.kernel.exps.FilterListener;
import org.apache.openjpa.kernel.exps.QueryExpressions;
import org.apache.openjpa.kernel.exps.Value;
import org.apache.openjpa.lib.util.OrderedMap;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.UnsupportedException;

/**
 * Abstract {@link StoreQuery} that implements most methods as no-ops.
 *
 * @author Abe White
 * @since 0.4.0
 */
@SuppressWarnings("serial")
public abstract class AbstractStoreQuery
    implements StoreQuery {

    protected QueryContext ctx = null;
   
    public QueryContext getContext() {
        return ctx;
    }

    public void setContext(QueryContext ctx) {
        this.ctx = ctx;
    }

    public boolean setQuery(Object query) {
        return false;
    }

    public FilterListener getFilterListener(String tag) {
        return null;
    }

    public AggregateListener getAggregateListener(String tag) {
        return null;
    }

    public Object newCompilationKey() {
        return null;
    }

    public Object newCompilation() {
        return null;
    }

    public Object getCompilation() {
        return null;
    }

    public void populateFromCompilation(Object comp) {
    }

    public void invalidateCompilation() {
    }

    public boolean supportsDataStoreExecution() {
        return false;
    }

    public boolean supportsInMemoryExecution() {
        return false;
    }

    public Executor newInMemoryExecutor(ClassMetaData meta, boolean subs) {
        throw new InternalException();
    }

    public Executor newDataStoreExecutor(ClassMetaData meta, boolean subs) {
        throw new InternalException();
    }

    public boolean supportsAbstractExecutors() {
        return false;
    }

    public boolean requiresCandidateType() {
        return true;
    }

    public boolean requiresParameterDeclarations() {
        return true;
    }

    public boolean supportsParameterDeclarations() {
        return true;
    }

    public Object evaluate(Object value, Object ob, Object[] params,
        OpenJPAStateManager sm) {
        throw new UnsupportedException();
    }

    /**
     * Abstract {@link Executor} that implements most methods as no-ops.
     */
    public static abstract class AbstractExecutor
        implements Executor {

        public Number executeDelete(StoreQuery q, Object[] params) {
            try {
                return q.getContext().deleteInMemory(q, this, params);
            } finally {
                for (ClassMetaData cmd : getAccessPathMetaDatas(q)) {
                    DataCache cache = cmd.getDataCache();
                    if (cache != null && cache.getEvictOnBulkUpdate()) {
                        cache.removeAll(cmd.getDescribedType(), true);
                    }
                }
            }
        }

        public Number executeUpdate(StoreQuery q, Object[] params) {
            try {
                return q.getContext().updateInMemory(q, this, params);
            } finally {
                for (ClassMetaData cmd : getAccessPathMetaDatas(q)) {
                    DataCache cache = cmd.getDataCache();
                    if (cache != null && cache.getEvictOnBulkUpdate()) {
                        cache.removeAll(cmd.getDescribedType(), true);
                    }
                }
            }
        }

        public String[] getDataStoreActions(StoreQuery q, Object[] params,
            Range range) {
            return EMPTY_STRINGS;
        }

        public void validate(StoreQuery q) {
        }


        public QueryExpressions[] getQueryExpressions() {
            return null;
        }
        
        public ResultShape<?> getResultShape(StoreQuery q) {
            return null;
        }
        
        public void getRange(StoreQuery q, Object[] params, Range range) {
        }

        public Object getOrderingValue(StoreQuery q, Object[] params,
            Object resultObject, int orderIndex) {
            return null;
        }

        public boolean[] getAscending(StoreQuery q) {
            return EMPTY_BOOLEANS;
        }

        public boolean isPacking(StoreQuery q) {
            return false;
        }

        public String getAlias(StoreQuery q) {
            return null;
        }

        public String[] getProjectionAliases(StoreQuery q) {
            return EMPTY_STRINGS;
        }

        public Class<?>[] getProjectionTypes(StoreQuery q) {
            return EMPTY_CLASSES;
        }

        public ClassMetaData[] getAccessPathMetaDatas(StoreQuery q) {
            return EMPTY_METAS;
        }

        public int getOperation(StoreQuery q) {
            return OP_SELECT;
        }

        public boolean isAggregate(StoreQuery q) {
            return false;
        }
        
        public boolean isDistinct(StoreQuery q) {
            return false;
        }

        public boolean hasGrouping(StoreQuery q) {
            return false;
        }

        public OrderedMap<Object,Class<?>> getOrderedParameterTypes(StoreQuery q) {
            return EMPTY_ORDERED_PARAMS;
        }
        
        public LinkedMap getParameterTypes(StoreQuery q) {
            LinkedMap result = new LinkedMap();
            result.putAll(getOrderedParameterTypes(q));
            return result;
        }

        public Class<?> getResultClass(StoreQuery q) {
            return null;
        }

        public Map<FieldMetaData,Value> getUpdates(StoreQuery q) {
            return null;
        }
    }
}
