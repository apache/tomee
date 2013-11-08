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

import java.io.ObjectOutput;
import java.util.BitSet;

import org.apache.openjpa.enhance.FieldManager;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.StateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.UnsupportedException;
import org.apache.openjpa.util.ImplHelper;

/**
 * Implementation of {@link OpenJPAStateManager} designed to retrieve
 * values from a detached instance, including when managed by a
 * {@link DetachedStateManager}.
 */
public class DetachedValueStateManager
    extends TransferFieldManager
    implements OpenJPAStateManager {

    private static final Localizer _loc = Localizer.forPackage
        (DetachedValueStateManager.class);

    private PersistenceCapable _pc;
    private StoreContext _ctx;
    private ClassMetaData _meta;

    public DetachedValueStateManager(Object pc, StoreContext ctx) {
        this(ImplHelper.toPersistenceCapable(pc, ctx.getConfiguration()),
            ctx.getConfiguration().getMetaDataRepositoryInstance()
                .getMetaData(ImplHelper.getManagedInstance(pc).getClass(),
            ctx.getClassLoader(), true), ctx);
    }

    public DetachedValueStateManager(PersistenceCapable pc, ClassMetaData meta,
        StoreContext ctx) {
        _pc = ImplHelper.toPersistenceCapable(pc, ctx.getConfiguration());
        _meta = meta;
        _ctx = ctx;
    }

    public void initialize(Class forType, PCState state) {
        throw new UnsupportedOperationException();
    }

    public void load(FetchConfiguration fetch) {
        throw new UnsupportedOperationException();
    }

    public Object getManagedInstance() {
        return _pc;
    }

    public PersistenceCapable getPersistenceCapable() {
        return _pc;
    }

    public ClassMetaData getMetaData() {
        return _meta;
    }

    public OpenJPAStateManager getOwner() {
        return null;
    }

    public int getOwnerIndex() {
        throw new UnsupportedOperationException();
    }

    public boolean isEmbedded() {
        return false;
    }

    public boolean isFlushed() {
        return false;
    }

    public boolean isFlushedDirty() {
        return false;
    }

    public boolean isProvisional() {
        return false;
    }

    public BitSet getLoaded() {
        throw new UnsupportedOperationException();
    }

    public BitSet getDirty() {
        throw new UnsupportedOperationException();
    }

    public BitSet getFlushed() {
        throw new UnsupportedOperationException();
    }

    public BitSet getUnloaded(FetchConfiguration fetch) {
        throw new UnsupportedOperationException();
    }

    public Object newProxy(int field) {
        throw new UnsupportedOperationException();
    }

    public Object newFieldProxy(int field) {
        throw new UnsupportedOperationException();
    }

    public boolean isDefaultValue(int field) {
        throw new UnsupportedOperationException();
    }

    public StoreContext getContext() {
        return _ctx;
    }

    public PCState getPCState() {
        throw new UnsupportedOperationException();
    }

    public Object getId() {
        return getObjectId();
    }

    public Object getObjectId() {
        throw new UnsupportedOperationException();
    }

    public void setObjectId(Object oid) {
        throw new UnsupportedOperationException();
    }

    public boolean assignObjectId(boolean flush) {
        throw new UnsupportedOperationException();
    }

    public Object getLock() {
        throw new UnsupportedOperationException();
    }

    public void setLock(Object lock) {
        throw new UnsupportedOperationException();
    }

    public Object getVersion() {
        throw new UnsupportedOperationException();
    }

    public void setVersion(Object version) {
        throw new UnsupportedOperationException();
    }

    public void setNextVersion(Object version) {
        throw new UnsupportedOperationException();
    }

    public boolean isVersionUpdateRequired() {
        throw new UnsupportedOperationException();
    }

    public boolean isVersionCheckRequired() {
        throw new UnsupportedOperationException();
    }

    public Object getImplData() {
        throw new UnsupportedOperationException();
    }

    public Object setImplData(Object data, boolean cacheable) {
        throw new UnsupportedOperationException();
    }

    public boolean isImplDataCacheable() {
        throw new UnsupportedOperationException();
    }

    public Object getImplData(int field) {
        throw new UnsupportedOperationException();
    }

    public Object setImplData(int field, Object data) {
        throw new UnsupportedOperationException();
    }

    public boolean isImplDataCacheable(int field) {
        throw new UnsupportedOperationException();
    }

    public Object getIntermediate(int field) {
        throw new UnsupportedOperationException();
    }

    public void setIntermediate(int field, Object value) {
        throw new UnsupportedOperationException();
    }

    void provideField(int field) {
        if (_pc.pcGetStateManager() != null)
            throw new InternalException(_loc.get("detach-val-mismatch", _pc));
        _pc.pcReplaceStateManager(this);
        _pc.pcProvideField(field);
        _pc.pcReplaceStateManager(null);
    }

    public boolean fetchBoolean(int field) {
        throw new UnsupportedOperationException();
    }

    public byte fetchByte(int field) {
        throw new UnsupportedOperationException();
    }

    public char fetchChar(int field) {
        throw new UnsupportedOperationException();
    }

    public double fetchDouble(int field) {
        throw new UnsupportedOperationException();
    }

    public float fetchFloat(int field) {
        throw new UnsupportedOperationException();
    }

    public int fetchInt(int field) {
        throw new UnsupportedOperationException();
    }

    public long fetchLong(int field) {
        throw new UnsupportedOperationException();
    }

    public Object fetchObject(int field) {
        throw new UnsupportedOperationException();
    }

    public short fetchShort(int field) {
        throw new UnsupportedOperationException();
    }

    public String fetchString(int field) {
        throw new UnsupportedOperationException();
    }

    public Object fetchFromDetachedSM(DetachedStateManager sm, int field) {
        sm.lock();
        sm.provideField(field);
        Object val = fetchField(sm, field);
        sm.clear();
        sm.unlock();
        return val;
    }

    public Object fetch(int field) {
        StateManager sm = _pc.pcGetStateManager();
        if (sm != null) {
            if (sm instanceof DetachedStateManager)
                return fetchFromDetachedSM((DetachedStateManager) sm, field);
            if (_ctx.getAllowReferenceToSiblingContext() && sm instanceof StateManagerImpl) {
                return ((StateManagerImpl) sm).fetch(field);
            }
            throw new UnsupportedException(_loc.get("detach-val-badsm", _pc));
        }
        provideField(field);
        Object val = fetchField(field, false);
        clear();
        return _meta.getField(field).getExternalValue(val, _ctx.getBroker());
    }

    public Object fetchField(int field, boolean transitions) {
        if (transitions)
            throw new IllegalArgumentException();
        return fetchField(this, field);
    }

    private Object fetchField(FieldManager fm, int field) {
        FieldMetaData fmd = _meta.getField(field);
        if (fmd == null)
            throw new InternalException();

        switch (fmd.getDeclaredTypeCode()) {
            case JavaTypes.STRING:
                return fm.fetchStringField(field);
            case JavaTypes.OBJECT:
                return fm.fetchObjectField(field);
            case JavaTypes.BOOLEAN:
                return (fm.fetchBooleanField(field)) ? Boolean.TRUE
                    : Boolean.FALSE;
            case JavaTypes.BYTE:
                return Byte.valueOf(fm.fetchByteField(field));
            case JavaTypes.CHAR:
                return Character.valueOf(fm.fetchCharField(field));
            case JavaTypes.DOUBLE:
                return Double.valueOf(fm.fetchDoubleField(field));
            case JavaTypes.FLOAT:
                return Float.valueOf(fm.fetchFloatField(field));
            case JavaTypes.INT:
                return fm.fetchIntField(field);
            case JavaTypes.LONG:
                return fm.fetchLongField(field);
            case JavaTypes.SHORT:
                return Short.valueOf(fm.fetchShortField(field));
            default:
                return fm.fetchObjectField(field);
        }
    }

    public Object fetchInitialField(int field) {
        throw new UnsupportedOperationException();
    }

    public void storeBoolean(int field, boolean externalVal) {
        throw new UnsupportedOperationException();
    }

    public void storeByte(int field, byte externalVal) {
        throw new UnsupportedOperationException();
    }

    public void storeChar(int field, char externalVal) {
        throw new UnsupportedOperationException();
    }

    public void storeDouble(int field, double externalVal) {
        throw new UnsupportedOperationException();
    }

    public void storeFloat(int field, float externalVal) {
        throw new UnsupportedOperationException();
    }

    public void storeInt(int field, int externalVal) {
        throw new UnsupportedOperationException();
    }

    public void storeLong(int field, long externalVal) {
        throw new UnsupportedOperationException();
    }

    public void storeObject(int field, Object externalVal) {
        throw new UnsupportedOperationException();
    }

    public void storeShort(int field, short externalVal) {
        throw new UnsupportedOperationException();
    }

    public void storeString(int field, String externalVal) {
        throw new UnsupportedOperationException();
    }

    public void store(int field, Object value) {
        throw new UnsupportedOperationException();
    }

    public void storeField(int field, Object value) {
        throw new UnsupportedOperationException();
    }

    public void dirty(int field) {
        throw new UnsupportedOperationException();
    }

    public void removed(int field, Object removed, boolean key) {
        throw new UnsupportedOperationException();
    }

    public boolean beforeRefresh(boolean refreshAll) {
        throw new UnsupportedOperationException();
    }

    public void setRemote(int field, Object value) {
        throw new UnsupportedOperationException();
    }

    ///////////////////////////////
    // StateManager implementation
    ///////////////////////////////

    public Object getGenericContext() {
        return _ctx;
    }

    public Object getPCPrimaryKey(Object oid, int field) {
        throw new UnsupportedOperationException();
    }

    public StateManager replaceStateManager(StateManager sm) {
        return sm;
    }

    public boolean isDirty() {
        return true;
    }

    public boolean isTransactional() {
        return false;
    }

    public boolean isPersistent() {
        return true;
    }

    public boolean isNew() {
        return false;
    }

    public boolean isDeleted() {
        return false;
    }

    public boolean isDetached() {
        return true;
    }

    public void dirty(String field) {
        throw new UnsupportedOperationException();
    }

    public Object fetchObjectId() {
        return getObjectId();
    }

    public boolean serializing() {
        throw new UnsupportedOperationException();
    }

    public boolean writeDetached(ObjectOutput out) {
        throw new UnsupportedOperationException();
    }

    public void proxyDetachedDeserialized(int idx) {
        throw new UnsupportedOperationException();
    }

    public void accessingField(int idx) {
        throw new UnsupportedOperationException();
    }

    public void providedBooleanField(PersistenceCapable pc, int idx,
        boolean cur) {
        longval = cur ? 1 : 0;
    }

    public void providedCharField(PersistenceCapable pc, int idx, char cur) {
        longval = cur;
    }

    public void providedByteField(PersistenceCapable pc, int idx, byte cur) {
        longval = cur;
    }

    public void providedShortField(PersistenceCapable pc, int idx, short cur) {
        longval = cur;
    }

    public void providedIntField(PersistenceCapable pc, int idx, int cur) {
        longval = cur;
    }

    public void providedLongField(PersistenceCapable pc, int idx, long cur) {
        longval = cur;
    }

    public void providedFloatField(PersistenceCapable pc, int idx, float cur) {
        dblval = cur;
    }

    public void providedDoubleField(PersistenceCapable pc, int idx,
        double cur) {
        dblval = cur;
    }

    public void providedStringField(PersistenceCapable pc, int idx,
        String cur) {
        objval = cur;
    }

    public void providedObjectField(PersistenceCapable pc, int idx,
        Object cur) {
        objval = cur;
    }

    public void settingBooleanField(PersistenceCapable pc, int idx,
        boolean cur, boolean next, int set) {
        throw new UnsupportedOperationException();
    }

    public void settingCharField(PersistenceCapable pc, int idx, char cur,
        char next, int set) {
        throw new UnsupportedOperationException();
    }

    public void settingByteField(PersistenceCapable pc, int idx, byte cur,
        byte next, int set) {
        throw new UnsupportedOperationException();
    }

    public void settingShortField(PersistenceCapable pc, int idx, short cur,
        short next, int set) {
        throw new UnsupportedOperationException();
    }

    public void settingIntField(PersistenceCapable pc, int idx, int cur,
        int next, int set) {
        throw new UnsupportedOperationException();
    }

    public void settingLongField(PersistenceCapable pc, int idx, long cur,
        long next, int set) {
        throw new UnsupportedOperationException();
    }

    public void settingFloatField(PersistenceCapable pc, int idx, float cur,
        float next, int set) {
        throw new UnsupportedOperationException();
    }

    public void settingDoubleField(PersistenceCapable pc, int idx, double cur,
        double next, int set) {
        throw new UnsupportedOperationException();
    }

    public void settingStringField(PersistenceCapable pc, int idx, String cur,
        String next, int set) {
        throw new UnsupportedOperationException();
    }

    public void settingObjectField(PersistenceCapable pc, int idx, Object cur,
        Object next, int set) {
        throw new UnsupportedOperationException();
    }

    public boolean replaceBooleanField(PersistenceCapable pc, int idx) {
        throw new UnsupportedOperationException();
    }

    public char replaceCharField(PersistenceCapable pc, int idx) {
        throw new UnsupportedOperationException();
    }

    public byte replaceByteField(PersistenceCapable pc, int idx) {
        throw new UnsupportedOperationException();
    }

    public short replaceShortField(PersistenceCapable pc, int idx) {
        throw new UnsupportedOperationException();
    }

    public int replaceIntField(PersistenceCapable pc, int idx) {
        throw new UnsupportedOperationException();
    }

    public long replaceLongField(PersistenceCapable pc, int idx) {
        throw new UnsupportedOperationException();
    }

    public float replaceFloatField(PersistenceCapable pc, int idx) {
        throw new UnsupportedOperationException();
    }

    public double replaceDoubleField(PersistenceCapable pc, int idx) {
        throw new UnsupportedOperationException();
    }

    public String replaceStringField(PersistenceCapable pc, int idx) {
        throw new UnsupportedOperationException();
    }

    public Object replaceObjectField(PersistenceCapable pc, int idx) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isDelayed(int field) {
        return false;
    }
    
    @Override
    public void setDelayed(int field, boolean delay) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void loadDelayedField(int field) {
        throw new UnsupportedOperationException();
    }
}

