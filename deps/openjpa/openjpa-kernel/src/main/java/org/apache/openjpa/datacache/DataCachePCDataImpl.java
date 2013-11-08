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
package org.apache.openjpa.datacache;

import java.util.BitSet;

import org.apache.openjpa.kernel.AbstractPCData;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.PCData;
import org.apache.openjpa.kernel.PCDataImpl;
import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.ValueMetaData;

/**
 * Specialized {@link PCData} implementation for data caching. This
 * implementation is properly synchronized.
 *
 * @author Patrick Linskey
 */
@SuppressWarnings("serial")
public class DataCachePCDataImpl
    extends PCDataImpl
    implements DataCachePCData {

    private final long _exp;

    public DataCachePCDataImpl(Object oid, ClassMetaData meta) {
        this(oid, meta, DataCache.NAME_DEFAULT);
    }
    
    /**
     * Constructor.
     */
    public DataCachePCDataImpl(Object oid, ClassMetaData meta, String name) {
        super(oid, meta, name);

        int timeout = meta.getDataCacheTimeout();
        if (timeout > 0)
            _exp = System.currentTimeMillis() + timeout;
        else
            _exp = -1;
    }

    public boolean isTimedOut() {
        return _exp != -1 && _exp < System.currentTimeMillis();
    }
    
    public long getTimeOut() {
        return _exp;
    }

    public synchronized Object getData(int index) {
        return super.getData(index);
    }

    public synchronized void setData(int index, Object val) {
        super.setData(index, val);
    }

    public synchronized void clearData(int index) {
        super.clearData(index);
    }

    public synchronized Object getImplData() {
        return super.getImplData();
    }

    public synchronized void setImplData(Object val) {
        super.setImplData(val);
    }

    public synchronized Object getImplData(int index) {
        return super.getImplData(index);
    }

    public synchronized void setImplData(int index, Object val) {
        super.setImplData(index, val);
    }

    public synchronized Object getIntermediate(int index) {
        return super.getIntermediate(index);
    }

    public synchronized void setIntermediate(int index, Object val) {
        super.setIntermediate(index, val);
    }

    public synchronized boolean isLoaded(int index) {
        return super.isLoaded(index);
    }

    public synchronized void setLoaded(int index, boolean loaded) {
        super.setLoaded(index, loaded);
    }

    public synchronized Object getVersion() {
        return super.getVersion();
    }

    public synchronized void setVersion(Object version) {
        super.setVersion(version);
    }

    public synchronized void store(OpenJPAStateManager sm) {
        super.store(sm);
    }

    public synchronized void store(OpenJPAStateManager sm, BitSet fields) {
        super.store(sm, fields);
    }

    /**
     * Store field-level information from the given state manager.
     * Special process of checking if the cached collection data is out of
     * order.
     */
    protected void storeField(OpenJPAStateManager sm, FieldMetaData fmd) {
        if (fmd.getManagement() != FieldMetaData.MANAGE_PERSISTENT)
            return;
        int index = fmd.getIndex();

        // if the field is a collection and has "order by" set, don't cache
        // it if this store is coming from a create or update (i.e., only
        // enlist in cache if this is coming from a database read).
        if (fmd.getOrders().length > 0) {
            if (sm.getPCState() == PCState.PNEW)
                return;
            if (sm.getPCState() == PCState.PDIRTY) {
                clearData(index);
                return;
            }
        }

        super.storeField(sm, fmd);

        // If this field is used in "order by", we need to invalidate cache
        // for the collection that refer to this field.
        if ((sm.getPCState() == PCState.PDIRTY) && fmd.isUsedInOrderBy()) {
            clearInverseRelationCache(sm, fmd);
        }
    }

    /**
     * Check if this field is in use of "order by" by other field collections
     * in inverse relation. If it is, clear the other field cache because it
     * could be out of order.
     */
    protected void clearInverseRelationCache(OpenJPAStateManager sm, FieldMetaData fmd) {
        DataCache cache = sm.getMetaData().getDataCache();
        if (cache == null)
            return;
        ClassMetaData cmd = sm.getMetaData();
        FieldMetaData[] fields = cmd.getFields();
        for (int i = 0; i < fields.length; i++) {
            FieldMetaData[] inverses = fields[i].getInverseMetaDatas();
            if (inverses.length == 0)
                continue;
            for (FieldMetaData inverse : inverses) {
                if (inverse.getOrderDeclaration().indexOf(fmd.getName()) != -1) {
                    Object oid = sm.getContext().getObjectId(sm.fetch(i));
                    DataCachePCData data = cache.get(oid);
                    if (data instanceof DataCachePCDataImpl) {
                        ((DataCachePCDataImpl) data).clearData(inverse.getIndex());
                    }
                }
            }
        }
    }

    protected Object toData(FieldMetaData fmd, Object val, StoreContext ctx) {
        // avoid caching large result set fields
        if (fmd.isLRS() || fmd.isStream())
            return NULL;
        return super.toData(fmd, val, ctx);
    }

    protected Object toNestedData(ValueMetaData vmd, Object val,
        StoreContext ctx) {
        if (val == null)
            return null;

        // don't try to cache nested containers
        switch (vmd.getDeclaredTypeCode()) {
            case JavaTypes.COLLECTION:
            case JavaTypes.MAP:
            case JavaTypes.ARRAY:
                return NULL;
            default:
                return super.toNestedData(vmd, val, ctx);
        }
    }

    public AbstractPCData newEmbeddedPCData(OpenJPAStateManager sm) {
        return new DataCachePCDataImpl(sm.getId(), sm.getMetaData(), getCache());
    }
}
