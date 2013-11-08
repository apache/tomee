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

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;

import org.apache.openjpa.conf.Compatibility;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.StateManager;
import org.apache.openjpa.lib.util.Localizer;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.util.Exceptions;
import org.apache.openjpa.util.Proxy;
import org.apache.openjpa.util.UnsupportedException;
import org.apache.openjpa.util.ImplHelper;

/**
 * Internal state manager for detached instances. Does not fully
 * implement {@link OpenJPAStateManager} contract to allow for serialization.
 *
 * @author Steve Kim
 * @nojavadoc
 */
public class DetachedStateManager
    extends AttachStrategy
    implements OpenJPAStateManager, Serializable {

    private static final long serialVersionUID = 4112223665584731100L;

    private static final Localizer _loc = Localizer.forPackage
        (DetachedStateManager.class);

    private final PersistenceCapable _pc;
    private final boolean _embedded;
    private final boolean _access;
    private final BitSet _loaded;
    private final BitSet _dirty;
    private final Object _oid;
    private final Object _version;
    private final ReentrantLock _lock;
    private final boolean _useDSFForUnproxy;   // old releases will default to FALSE, which is the old behavior

    /**
     * Constructor.
     *
     * @param pc the managed instance
     * @param sm the instance's state manager
     * @param load the set of detached field indexes
     * @param access whether to allow access to unloaded fields
     * @param multithreaded whether the instance will be used concurrently
     * by multiple threads
     */
    public DetachedStateManager(PersistenceCapable pc, OpenJPAStateManager sm,
        BitSet load, boolean access, boolean multithreaded) {
        _pc = pc;
        _embedded = sm.isEmbedded();
        _loaded = load;
        _access = access;
        if (!sm.isFlushed())
            _dirty = (BitSet) sm.getDirty().clone();
        else
            _dirty = new BitSet(_loaded.length());
        _oid = sm.fetchObjectId();
        _version = sm.getVersion();
        if (multithreaded)
            _lock = new ReentrantLock();
        else
            _lock = null;
        if (sm.getContext() != null && sm.getContext().getConfiguration() != null) {
            Compatibility compat = sm.getContext().getConfiguration().getCompatibilityInstance();
            if (compat != null && !compat.getIgnoreDetachedStateFieldForProxySerialization())
                _useDSFForUnproxy = true;      // new 2.0 behavior
            else
                _useDSFForUnproxy = false;
        } else
            _useDSFForUnproxy = false;
    }

    /////////////////////////////////
    // AttachStrategy implementation
    /////////////////////////////////

    public Object attach(AttachManager manager, Object toAttach,
        ClassMetaData meta, PersistenceCapable into, OpenJPAStateManager owner,
        ValueMetaData ownerMeta, boolean explicit) {
        BrokerImpl broker = manager.getBroker();
        StateManagerImpl sm;
        if (_embedded) {
            if (_dirty.length () > 0)
                owner.dirty(ownerMeta.getFieldMetaData().getIndex());
            sm = (StateManagerImpl) broker.embed(_pc, _oid, owner, ownerMeta);
            ImplHelper.toPersistenceCapable(toAttach, broker.getConfiguration())
                .pcReplaceStateManager(this);
        } else {
            PCState state = (_dirty.length() > 0) ? PCState.PDIRTY
                : PCState.PCLEAN;
            sm = (StateManagerImpl) broker.copy(this, state);
        }
        PersistenceCapable pc = sm.getPersistenceCapable();
        manager.setAttachedCopy(toAttach, pc);

        manager.fireBeforeAttach(toAttach, meta);

        // pre-load for efficiency: current field values for restore, dependent
        // for delete
        FieldMetaData[] fields = sm.getMetaData().getFields();
        int restore = broker.getRestoreState();

        boolean postLoadOnMerge = broker.getPostLoadOnMerge();
        if (_dirty.length() > 0 || postLoadOnMerge) {
            BitSet load = new BitSet(fields.length);
            if (postLoadOnMerge && broker.getLifecycleEventManager().hasLoadListeners(pc, meta)) {
                // load all fields
                // this will automatically lead to invoking the PostLoad lifecycle event
                // when the last field got set
                // @see StateManagerImpl#postLoad(String, FetchConfiguration)
                load.set(0, fields.length);
            }
            else {
                for (int i = 0; i < fields.length; i++) {
                    if (!_dirty.get(i))
                        continue;

                    switch (fields[i].getDeclaredTypeCode()) {
                        case JavaTypes.ARRAY:
                        case JavaTypes.COLLECTION:
                            if (restore == RestoreState.RESTORE_ALL
                                || fields[i].getElement().getCascadeDelete()
                                == ValueMetaData.CASCADE_AUTO)
                                load.set(i);
                            break;
                        case JavaTypes.MAP:
                            if (restore == RestoreState.RESTORE_ALL
                                || fields[i].getElement().getCascadeDelete()
                                == ValueMetaData.CASCADE_AUTO
                                || fields[i].getKey().getCascadeDelete()
                                == ValueMetaData.CASCADE_AUTO)
                                load.set(i);
                            break;
                        default:
                            if (restore != RestoreState.RESTORE_NONE
                                || fields[i].getCascadeDelete()
                                == ValueMetaData.CASCADE_AUTO)
                                load.set(i);
                    }
                }
            }

            if (!postLoadOnMerge) {
                // prevent PostLoad callbacks even for the load operation
                sm.setPostLoadCallback(false);
            }
            FetchConfiguration fc = broker.getFetchConfiguration();
            sm.loadFields(load, fc, fc.getWriteLockLevel(), null);
        }
        Object origVersion = sm.getVersion();
        sm.setVersion(_version);

        BitSet loaded = sm.getLoaded();
        int set = StateManager.SET_ATTACH;
        sm.setPostLoadCallback(false);
        for (int i = 0; i < fields.length; i++) {
            if (!_loaded.get(i))
                continue;
            // don't reload already loaded non-mutable objects
            if (!_dirty.get(i) && loaded.get(i) && ignoreLoaded(fields[i]))
                continue;

            provideField(i);
            switch (fields[i].getDeclaredTypeCode()) {
                case JavaTypes.BOOLEAN:
                    if (_dirty.get(i))
                        sm.settingBooleanField(pc, i,
                            (loaded.get(i)) && sm.fetchBooleanField(i),
                            longval == 1, set);
                    else
                        sm.storeBooleanField(i, longval == 1);
                    break;
                case JavaTypes.BYTE:
                    if (_dirty.get(i))
                        sm.settingByteField(pc, i, (!loaded.get(i)) ? (byte) 0
                            : sm.fetchByteField(i), (byte) longval, set);
                    else
                        sm.storeByteField(i, (byte) longval);
                    break;
                case JavaTypes.CHAR:
                    if (_dirty.get(i))
                        sm.settingCharField(pc, i, (!loaded.get(i)) ? (char) 0
                            : sm.fetchCharField(i), (char) longval, set);
                    else
                        sm.storeCharField(i, (char) longval);
                    break;
                case JavaTypes.INT:
                    if (_dirty.get(i))
                        sm.settingIntField(pc, i, (!loaded.get(i)) ? 0
                            : sm.fetchIntField(i), (int) longval, set);
                    else
                        sm.storeIntField(i, (int) longval);
                    break;
                case JavaTypes.LONG:
                    if (_dirty.get(i))
                        sm.settingLongField(pc, i, (!loaded.get(i)) ? 0L
                            : sm.fetchLongField(i), longval, set);
                    else
                        sm.storeLongField(i, longval);
                    break;
                case JavaTypes.SHORT:
                    if (_dirty.get(i))
                        sm.settingShortField(pc, i, 
                            (!loaded.get(i)) ? (short) 0 : sm.fetchShortField(i), (short) longval, set);
                    else
                        sm.storeShortField(i, (short) longval);
                    break;
                case JavaTypes.FLOAT:
                    if (_dirty.get(i))
                        sm.settingFloatField(pc, i, (!loaded.get(i)) ? 0F
                            : sm.fetchFloatField(i), (float) dblval, set);
                    else
                        sm.storeFloatField(i, (float) dblval);
                    break;
                case JavaTypes.DOUBLE:
                    if (_dirty.get(i))
                        sm.settingDoubleField(pc, i, (!loaded.get(i)) ? 0D
                            : sm.fetchDoubleField(i), dblval, set);
                    else
                        sm.storeDoubleField(i, dblval);
                    break;
                case JavaTypes.STRING:
                    if (_dirty.get(i))
                        sm.settingStringField(pc, i, (!loaded.get(i)) ? null
                            : sm.fetchStringField(i), (String) objval, set);
                    else
                        sm.storeStringField(i, (String) objval);
                    objval = null;
                    break;
                case JavaTypes.PC:
                case JavaTypes.PC_UNTYPED:
                    if (fields[i].getCascadeAttach() == ValueMetaData
                        .CASCADE_NONE) {
                        // Use the attached copy of the object, if available
                        PersistenceCapable cpy = manager.getAttachedCopy(objval);
                        if (cpy != null) {
                            objval = cpy;
                        } else {
                        	objval = getReference(manager, objval, sm, fields[i]);
                        }
                    } 
                    else {
                        PersistenceCapable toPC = null;
                        if (objval != null && fields[i].isEmbeddedPC())
                            toPC = ImplHelper.toPersistenceCapable(objval,
                                broker.getConfiguration());
                        objval = manager.attach(objval, toPC, sm, fields[i],
                            false);
                    }
                    if (_dirty.get(i))
                        sm.settingObjectField(pc, i, (!loaded.get(i)) ? null
                            : sm.fetchObjectField(i), objval, set);
                    else
                        sm.storeObjectField(i, objval);
                    objval = null;
                    break;
                case JavaTypes.COLLECTION:
                    Collection coll = (Collection) objval;
                    objval = null;
                    if (coll != null)
                        coll = attachCollection(manager, coll, sm, fields[i]);
                    if (_dirty.get(i))
                        sm.settingObjectField(pc, i, (!loaded.get(i)) ? null
                            : sm.fetchObjectField(i), coll, set);
                    else
                        sm.storeObjectField(i, coll);
                    break;
                case JavaTypes.MAP:
                    Map map = (Map) objval;
                    objval = null;
                    if (map != null)
                        map = attachMap(manager, map, sm, fields[i]);
                    if (_dirty.get(i))
                        sm.settingObjectField(pc, i, (!loaded.get(i)) ? null
                            : sm.fetchObjectField(i), map, set);
                    else
                        sm.storeObjectField(i, map);
                    break;
                default:
                    if (_dirty.get(i))
                        sm.settingObjectField(pc, i, (!loaded.get(i)) ? null
                            : sm.fetchObjectField(i), objval, set);
                    else
                        sm.storeObjectField(i, objval);
                    objval = null;
            }
        }
        sm.setPostLoadCallback(true);
        pc.pcReplaceStateManager(sm);

        // if we were clean at least make sure a version check is done to
        // prevent using old state
        if (!sm.isVersionCheckRequired() && broker.isActive()
            && _version != origVersion && (origVersion == null 
            || broker.getStoreManager().compareVersion(sm, _version, 
            origVersion) != StoreManager.VERSION_SAME)) {
            broker.transactional(sm.getManagedInstance(), false, 
                manager.getBehavior());
        }

        return sm.getManagedInstance();
    }

    protected Object getDetachedObjectId(AttachManager manager,
        Object toAttach) {
        return _oid;
    }

    void provideField(int field) {
        _pc.pcProvideField(field);
    }

    protected void provideField(Object toAttach, StateManagerImpl sm,
        int field) {
        provideField(field);
    }

    /**
     * Ignore if the field is not dirty but loaded
     */
    protected static boolean ignoreLoaded(FieldMetaData fmd) {
        switch (fmd.getTypeCode()) {
            case JavaTypes.BOOLEAN:
            case JavaTypes.BOOLEAN_OBJ:
            case JavaTypes.BYTE:
            case JavaTypes.BYTE_OBJ:
            case JavaTypes.INT:
            case JavaTypes.INT_OBJ:
            case JavaTypes.LONG:
            case JavaTypes.LONG_OBJ:
            case JavaTypes.SHORT:
            case JavaTypes.SHORT_OBJ:
            case JavaTypes.DOUBLE:
            case JavaTypes.DOUBLE_OBJ:
            case JavaTypes.FLOAT:
            case JavaTypes.FLOAT_OBJ:
            case JavaTypes.CHAR:
            case JavaTypes.CHAR_OBJ:
            case JavaTypes.STRING:
                return true;
        }
        return false;
    }

    ///////////////////////////////
    // StateManager implementation
    ///////////////////////////////

    public Object getGenericContext() {
        return null;
    }

    public Object getPCPrimaryKey(Object oid, int field) {
        throw new UnsupportedOperationException();
    }

    public StateManager replaceStateManager(StateManager sm) {
        return sm;
    }

    public Object getVersion() {
        return _version;
    }

    public void setVersion(Object version) {
        throw new UnsupportedException();
    }

    public boolean isDirty() {
        return _dirty.length() != 0;
    }

    public boolean isTransactional() {
        return false;
    }

    public boolean isPersistent() {
        return false;
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

    public boolean isVersionUpdateRequired() {
        return false;
    }

    public boolean isVersionCheckRequired() {
        return false;
    }

    public void dirty(String field) {
        // should we store ClassMetaData?
        throw new UnsupportedException();
    }

    public Object fetchObjectId() {
        return _oid;
    }

    public void accessingField(int idx) {
        if (!_access && !_loaded.get(idx))
            // do not access the pc fields by implictly invoking _pc.toString()
            // may cause infinite loop if again tries to access unloaded field 
            throw new IllegalStateException(_loc.get("unloaded-detached",
               Exceptions.toString(_pc)).getMessage());
    }

    public boolean serializing() {
        return false;
    }

    public boolean writeDetached(ObjectOutput out)
        throws IOException {
        out.writeObject(_pc.pcGetDetachedState());
        out.writeObject(this);
        return false;
    }

    public void proxyDetachedDeserialized(int idx) {
        lock();
        try {
            _pc.pcProvideField(idx);
            if (objval instanceof Proxy)
                ((Proxy) objval).setOwner(this, idx);
            objval = null;
        } finally {
            unlock();
        }
    }

    public void settingBooleanField(PersistenceCapable pc, int idx,
        boolean cur, boolean next, int set) {
        accessingField(idx);
        if (cur == next || !_loaded.get(idx))
            return;
        lock();
        try {
            _dirty.set(idx);
            longval = next ? 1 : 0;
            pc.pcReplaceField(idx);
        } finally {
            unlock();
        }
    }

    public void settingCharField(PersistenceCapable pc, int idx, char cur,
        char next, int set) {
        accessingField(idx);
        if (cur == next || !_loaded.get(idx))
            return;
        lock();
        try {
            _dirty.set(idx);
            longval = next;
            pc.pcReplaceField(idx);
        } finally {
            unlock();
        }
    }

    public void settingByteField(PersistenceCapable pc, int idx, byte cur,
        byte next, int set) {
        accessingField(idx);
        if (cur == next || !_loaded.get(idx))
            return;
        lock();
        try {
            _dirty.set(idx);
            longval = next;
            pc.pcReplaceField(idx);
        } finally {
            unlock();
        }
    }

    public void settingShortField(PersistenceCapable pc, int idx, short cur,
        short next, int set) {
        accessingField(idx);
        if (cur == next || !_loaded.get(idx))
            return;
        lock();
        try {
            _dirty.set(idx);
            longval = next;
            pc.pcReplaceField(idx);
        } finally {
            unlock();
        }
    }

    public void settingIntField(PersistenceCapable pc, int idx, int cur,
        int next, int set) {
        accessingField(idx);
        if (cur == next || !_loaded.get(idx))
            return;
        lock();
        try {
            _dirty.set(idx);
            longval = next;
            pc.pcReplaceField(idx);
        } finally {
            unlock();
        }
    }

    public void settingLongField(PersistenceCapable pc, int idx, long cur,
        long next, int set) {
        accessingField(idx);
        if (cur == next || !_loaded.get(idx))
            return;
        lock();
        try {
            _dirty.set(idx);
            longval = next;
            pc.pcReplaceField(idx);
        } finally {
            unlock();
        }
    }

    public void settingFloatField(PersistenceCapable pc, int idx, float cur,
        float next, int set) {
        accessingField(idx);
        if (cur == next || !_loaded.get(idx))
            return;
        lock();
        try {
            _dirty.set(idx);
            dblval = next;
            pc.pcReplaceField(idx);
        } finally {
            unlock();
        }
    }

    public void settingDoubleField(PersistenceCapable pc, int idx, double cur,
        double next, int set) {
        accessingField(idx);
        if (cur == next || !_loaded.get(idx))
            return;
        lock();
        try {
            _dirty.set(idx);
            dblval = next;
            pc.pcReplaceField(idx);
        } finally {
            unlock();
        }
    }

    public void settingStringField(PersistenceCapable pc, int idx, String cur,
        String next, int set) {
        accessingField(idx);
        if (cur == next || (cur != null && cur.equals(next))
                || !_loaded.get(idx))
            return;
        lock();
        try {
            _dirty.set(idx);
            objval = next;
            pc.pcReplaceField(idx);
        } finally {
            unlock();
            objval = null;
        }
    }

    public void settingObjectField(PersistenceCapable pc, int idx, Object cur,
        Object next, int set) {
        accessingField(idx);
        if (cur == next || !_loaded.get(idx))
            return;
        lock();
        try {
            _dirty.set(idx);
            objval = next;
            pc.pcReplaceField(idx);
        } finally {
            unlock();
            objval = null;
        }
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

    public boolean replaceBooleanField(PersistenceCapable pc, int idx) {
        return longval == 1;
    }

    public char replaceCharField(PersistenceCapable pc, int idx) {
        return (char) longval;
    }

    public byte replaceByteField(PersistenceCapable pc, int idx) {
        return (byte) longval;
    }

    public short replaceShortField(PersistenceCapable pc, int idx) {
        return (short) longval;
    }

    public int replaceIntField(PersistenceCapable pc, int idx) {
        return (int) longval;
    }

    public long replaceLongField(PersistenceCapable pc, int idx) {
        return longval;
    }

    public float replaceFloatField(PersistenceCapable pc, int idx) {
        return (float) dblval;
    }

    public double replaceDoubleField(PersistenceCapable pc, int idx) {
        return dblval;
    }

    public String replaceStringField(PersistenceCapable pc, int idx) {
        String str = (String) objval;
        objval = null;
        return str;
    }

    public Object replaceObjectField(PersistenceCapable pc, int idx) {
        Object ret = objval;
        objval = null;
        return ret;
    }

    //////////////////////////////////////
    // OpenJPAStateManager implementation
    //////////////////////////////////////

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
        throw new UnsupportedOperationException();
    }

    public OpenJPAStateManager getOwner() {
        throw new UnsupportedOperationException();
    }

    public int getOwnerIndex() {
        throw new UnsupportedOperationException();
    }

    public boolean isEmbedded() {
        return _embedded;
    }

    public boolean isFlushed() {
        throw new UnsupportedOperationException();
    }

    public boolean isFlushedDirty() {
        throw new UnsupportedOperationException();
    }

    public boolean isProvisional() {
        throw new UnsupportedOperationException();
    }

    public BitSet getLoaded() {
        return _loaded;
    }

    public BitSet getDirty() {
        return _dirty;
    }

    /**
     * Should DetachedStateField be used by Proxies to determine when to remove
     * $proxy wrappers during serialization.
     * @since 2.0.0
     */
    public boolean getUseDSFForUnproxy() {
        return _useDSFForUnproxy;
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
        return null;
    }

    public PCState getPCState() {
        throw new UnsupportedOperationException();
    }

    public Object getObjectId() {
        return _oid;
    }

    public void setObjectId(Object oid) {
        throw new UnsupportedOperationException();
    }

    public boolean assignObjectId(boolean flush) {
        return true;
    }

    public Object getId() {
        return getObjectId();
    }

    public Object getLock() {
        throw new UnsupportedOperationException();
    }

    public void setLock(Object lock) {
        throw new UnsupportedOperationException();
    }

    public void setNextVersion(Object version) {
        throw new UnsupportedOperationException();
    }

    public Object getImplData() {
        throw new UnsupportedOperationException();
    }

    public Object setImplData(Object data, boolean cacheable) {
        throw new UnsupportedOperationException();
    }

    public boolean isImplDataCacheable() {
        return false;
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

    public void setIntermediate(int field, Object data) {
        throw new UnsupportedOperationException();
    }

    public void removed(int field, Object removed, boolean key) {
        dirty(field);
    }

    public boolean beforeRefresh(boolean all) {
        throw new UnsupportedOperationException();
    }

    public void dirty(int field) {
        lock();
        try {
            _dirty.set(field);
        } finally {
            unlock();
        }
    }

    public void storeBoolean(int field, boolean extVal) {
        throw new UnsupportedOperationException();
    }

    public void storeByte(int field, byte extVal) {
        throw new UnsupportedOperationException();
    }

    public void storeChar(int field, char extVal) {
        throw new UnsupportedOperationException();
    }

    public void storeInt(int field, int extVal) {
        throw new UnsupportedOperationException();
    }

    public void storeShort(int field, short extVal) {
        throw new UnsupportedOperationException();
    }

    public void storeLong(int field, long extVal) {
        throw new UnsupportedOperationException();
    }

    public void storeFloat(int field, float extVal) {
        throw new UnsupportedOperationException();
    }

    public void storeDouble(int field, double extVal) {
        throw new UnsupportedOperationException();
    }

    public void storeString(int field, String extVal) {
        throw new UnsupportedOperationException();
    }

    public void storeObject(int field, Object extVal) {
        throw new UnsupportedOperationException();
    }

    public void store(int field, Object extVal) {
        throw new UnsupportedOperationException();
    }

    public void storeField(int field, Object value) {
        throw new UnsupportedOperationException();
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

    public short fetchShort(int field) {
        throw new UnsupportedOperationException();
    }

    public int fetchInt(int field) {
        throw new UnsupportedOperationException();
    }

    public long fetchLong(int field) {
        throw new UnsupportedOperationException();
    }

    public float fetchFloat(int field) {
        throw new UnsupportedOperationException();
    }

    public double fetchDouble(int field) {
        throw new UnsupportedOperationException();
    }

    public String fetchString(int field) {
        throw new UnsupportedOperationException();
    }

    public Object fetchObject(int field) {
        throw new UnsupportedOperationException();
    }

    public Object fetch(int field) {
        throw new UnsupportedOperationException();
    }

    public Object fetchField(int field, boolean transitions) {
        throw new UnsupportedOperationException();
    }

    public Object fetchInitialField(int field) {
        throw new UnsupportedOperationException();
    }

    public void setRemote(int field, Object value) {
        throw new UnsupportedOperationException();
    }

    public void lock() {
        if (_lock != null)
            _lock.lock();
    }

    public void unlock() {
        if (_lock != null)
            _lock.unlock();
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
