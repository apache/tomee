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
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.enhance.DynamicPersistenceCapable;
import org.apache.openjpa.enhance.FieldManager;
import org.apache.openjpa.enhance.ManagedInstanceProvider;
import org.apache.openjpa.enhance.PCRegistry;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.RedefinitionHelper;
import org.apache.openjpa.enhance.StateManager;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.event.LifecycleEventManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.AccessCode;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FetchGroup;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.UpdateStrategies;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.meta.ValueStrategies;
import org.apache.openjpa.util.ApplicationIds;
import org.apache.openjpa.util.Exceptions;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.InvalidStateException;
import org.apache.openjpa.util.ObjectNotFoundException;
import org.apache.openjpa.util.OpenJPAId;
import org.apache.openjpa.util.ProxyManager;
import org.apache.openjpa.util.RuntimeExceptionTranslator;
import org.apache.openjpa.util.UserException;

/**
 * Implementation of the {@link OpenJPAStateManager} interface for use
 * with this runtime. Each state manager manages the state of a single
 * persistence capable instance. The state manager is also responsible for
 * all communications about the instance to the {@link StoreManager}.
 *  The state manager uses the State pattern in both its interaction with
 * the governed instance and its interaction with the broker.
 * In its interactions with the persistence capable instance, it uses the
 * {@link FieldManager} interface. Similarly, when interacting with the
 * broker, it uses the {@link PCState} singleton that represents
 * the current lifecycle state of the instance.
 *
 * @author Abe White
 */
public class StateManagerImpl implements OpenJPAStateManager, Serializable {
    public static final int LOAD_FGS = 0;
    public static final int LOAD_ALL = 1;
    public static final int LOAD_SERIALIZE = 2;

    private static final int FLAG_SAVE = 2 << 0;
    private static final int FLAG_DEREF = 2 << 1;
    private static final int FLAG_LOADED = 2 << 2;
    private static final int FLAG_READ_LOCKED = 2 << 3;
    private static final int FLAG_WRITE_LOCKED = 2 << 4;
    private static final int FLAG_OID_ASSIGNED = 2 << 5;
    private static final int FLAG_LOADING = 2 << 6;
    private static final int FLAG_PRE_DELETING = 2 << 7;
    private static final int FLAG_FLUSHED = 2 << 8;
    private static final int FLAG_PRE_FLUSHED = 2 << 9;
    private static final int FLAG_FLUSHED_DIRTY = 2 << 10;
    private static final int FLAG_IMPL_CACHE = 2 << 11;
    private static final int FLAG_INVERSES = 2 << 12;
    private static final int FLAG_NO_UNPROXY = 2 << 13;
    private static final int FLAG_VERSION_CHECK = 2 << 14;
    private static final int FLAG_VERSION_UPDATE = 2 << 15;
    private static final int FLAG_DETACHING = 2 << 16;
    private static final int FLAG_EMBED_DEREF = 2 << 17;

    private static final Localizer _loc = Localizer.forPackage
        (StateManagerImpl.class);

    // information about the instance
    private transient PersistenceCapable _pc = null;
    protected transient ClassMetaData _meta = null;
    protected BitSet _loaded = null;
    
    // Care needs to be taken when accessing these fields as they will can be null if no fields are
    // dirty, or have been flushed.
    private BitSet _dirty = null;
    private BitSet _flush = null;
    
    private BitSet _delayed = null;
    private int _flags = 0;

    // id is the state manager identity; oid is the persistent identity.  oid
    // may be null for embedded and transient-transactional objects or new
    // instances that haven't been assigned an oid.  id is reassigned to oid
    // on successful oid assignment (or flush completion if assignment is
    // during flush)
    private Object _id = null;
    private Object _oid = null;

    // the managing persistence manager and lifecycle state
    private transient BrokerImpl _broker; // this is serialized specially
    protected PCState _state = PCState.TRANSIENT;

    // the current and last loaded version indicators, and the lock object
    protected Object _version = null;
    protected Object _loadVersion = null;
    private Object _lock = null;
    private int _readLockLevel = -1;
    private int _writeLockLevel = -1;

    // delegates when providing/replacing instance data
    private SingleFieldManager _single = null;
    private SaveFieldManager _saved = null;
    private FieldManager _fm = null;

    // impldata; field impldata and intermediate data share the same array
    private Object _impl = null;
    protected Object[] _fieldImpl = null;

    // information about the owner of this instance, if it is embedded
    private StateManagerImpl _owner = null;
    // for embeddable object from query result
    private Object _ownerId = null;
    private int _ownerIndex = -1;
    private List<FieldMetaData> _mappedByIdFields = null;
    
    private transient ReentrantLock _instanceLock = null;

    /**
     * <p>set to <code>false</code> to prevent the postLoad method from
     * sending lifecycle callback events.</p>
     * <p>Callbacks are enabled by default</>
     */
    private boolean postLoadCallback = true;

    /**
     * Constructor; supply id, type metadata, and owning persistence manager.
     */
    protected StateManagerImpl(Object id, ClassMetaData meta, BrokerImpl broker) {
        _id = id;
        _meta = meta;
        _broker = broker;
        _single = new SingleFieldManager(this, broker);
        if (broker.getMultithreaded())
        	_instanceLock = new ReentrantLock();

        if (_meta.getIdentityType() == ClassMetaData.ID_UNKNOWN && !_meta.isEmbeddable())
            throw new UserException(_loc.get("meta-unknownid", _meta));
    }

    /**
     * Create a new StateManager instance based on the StateManager provided. A
     * new PersistenceCapable instance will be created and associated with the
     * new StateManager. All fields will be copied into the ne PC instance as
     * well as the dirty, loaded, and flushed bitsets.
     * 
     * @param sm A statemanager instance which will effectively be cloned.
     */
    public StateManagerImpl(StateManagerImpl sm) {
        this(sm, sm.getPCState());
    }

    /**
     * Create a new StateManager instance, optionally overriding the state
     * (FLUSHED, DELETED, etc) of the underlying PersistenceCapable instance).
     * 
     * @param sm
     *            A statemanager instance which will effectively be cloned.
     * @param newState
     *            The new state of the underlying persistence capable object.
     */
    public StateManagerImpl(StateManagerImpl sm, PCState newState) { 
        this(sm.getId(), sm.getMetaData(), sm.getBroker());

        PersistenceCapable origPC = sm.getPersistenceCapable();
        _pc = origPC.pcNewInstance(sm, false);
        
        int[] fields = new int[sm.getMetaData().getFields().length];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = i;
        }
        _pc.pcCopyFields(origPC, fields);
        _pc.pcReplaceStateManager(this);
        _state = newState;

        // clone the field bitsets. 
        _dirty=(BitSet)sm.getDirty().clone();
        _loaded = (BitSet)sm.getLoaded().clone();
        _flush = (BitSet) sm.getFlushed().clone();
        _version = sm.getVersion();
        
        _oid = sm.getObjectId(); 
        _id = sm.getId();
        
        // more data may need to be copied. 
    }

    /**
     * Set the owning state and field if this is an embedded instance.
     */
    void setOwner(StateManagerImpl owner, ValueMetaData ownerMeta) {
        _owner = owner;
        _ownerIndex = ownerMeta.getFieldMetaData().getIndex();
    }

    /**
     * Whether this state manager is in the middle of a load.
     */
    boolean isLoading() {
        return (_flags & FLAG_LOADING) > 0;
    }

    /**
     * Whether this state manager is in the middle of a load initiated
     * by outside code; for any internal methods that cause loading, the
     * loading flag is set automatically.
     */
    void setLoading(boolean loading) {
        if (loading)
            _flags |= FLAG_LOADING;
        else
            _flags &= ~FLAG_LOADING;
    }

    /**
     * Set or reset the lifecycle state of the managed instance. If the
     * transactional state of the instance changes, it will be enlisted/
     * delisted from the current transaction as necessary. The given
     * state will be initialized after being set. If the given state
     * is the same as the current state, this method will have no effect.
     */
    private void setPCState(PCState state) {
        if (_state == state)
            return;

        PCState prev = _state;
        lock();
        try {
            // notify the store manager that we're changing states; can veto
            _broker.getStoreManager().beforeStateChange(this, _state, state);

            // replace state
            boolean wasDeleted = _state.isDeleted();
            boolean wasDirty = _state.isDirty();
            boolean wasPending = _state.isPendingTransactional();
            _state = state;

            // enlist/delist from transaction
            if (_state.isTransactional()) {
                _broker.addToTransaction(this);
                if (_state.isDeleted() != wasDeleted)
                    _broker.setDirty(this, !wasDirty || isFlushed());
                else if (_state.isDirty() && !wasDirty)
                    _broker.setDirty(this, true);
            } else if (!wasPending && _state.isPendingTransactional())
                _broker.addToPendingTransaction(this);
            else if (wasPending && !_state.isPendingTransactional())
                _broker.removeFromPendingTransaction(this);
            else
                _broker.removeFromTransaction(this);

            _state.initialize(this, prev);
            if (_state.isDeleted() && !wasDeleted)
                fireLifecycleEvent(LifecycleEvent.AFTER_DELETE);
        } finally {
            unlock();
        }
    }

    //////////////////////////////////////
    // OpenJPAStateManager implementation
    //////////////////////////////////////

    public void initialize(Class cls, PCState state) {
        // check to see if our current object id instance is the
        // correct id type for the specified class; this is for cases
        // when we have an application id hierarchy and we had set the
        // metadata to a superclass id -- the subclass' id may be a
        // different class, so we need to reset it
        if (_meta.getDescribedType() != cls) {
            ClassMetaData sub = _meta.getRepository().getMetaData
                (cls, _broker.getClassLoader(), true);
            if (_oid != null) {
                if (_meta.getIdentityType() == ClassMetaData.ID_DATASTORE)
                    _oid = _broker.getStoreManager().copyDataStoreId(_oid,
                        sub);
                else if (_meta.isOpenJPAIdentity())
                    _oid = ApplicationIds.copy(_oid, sub);
                else if (sub.getObjectIdType() != _meta.getObjectIdType()) {
                    Object[] pkFields = ApplicationIds.toPKValues(_oid, _meta);
                    _oid = ApplicationIds.fromPKValues(pkFields, sub);
                }
            }
            _meta = sub;
        }

        PersistenceCapable inst = PCRegistry.newInstance(cls, this, _oid, true);
        if (inst == null) {
            // the instance was null: check to see if the instance is
            // abstract (as can sometimes be the case when the
            // class discriminator strategy is not configured correctly)
            if (Modifier.isAbstract(cls.getModifiers()))
                throw new UserException(_loc.get("instantiate-abstract",
                    cls.getName(), _oid));
            throw new InternalException();
        }

        initialize(inst, state);
    }

    /**
     * Initialize with the given instance and state.
     */
    protected void initialize(PersistenceCapable pc, PCState state) {
        if (pc == null)
            throw new UserException(_loc.get("init-null-pc", _meta));
        if (pc.pcGetStateManager() != null && pc.pcGetStateManager() != this)
            throw new UserException(_loc.get("init-sm-pc",
                Exceptions.toString(pc))).setFailedObject(pc);
        pc.pcReplaceStateManager(this);

        FieldMetaData[] fmds = _meta.getFields();
        _loaded = new BitSet(fmds.length);
        
        // mark primary key and non-persistent fields as loaded
        for(int i : _meta.getPkAndNonPersistentManagedFmdIndexes()){
            _loaded.set(i);
        }
            
        _mappedByIdFields = _meta.getMappyedByIdFields();
        
        // record whether there are any managed inverse fields
        if (_broker.getInverseManager() != null && _meta.hasInverseManagedFields())
            _flags |= FLAG_INVERSES;

        pc.pcSetDetachedState(null);
        _pc = pc;

        if (_oid instanceof OpenJPAId)
            ((OpenJPAId) _oid).setManagedInstanceType(_meta.getDescribedType());

        // initialize our state and add ourselves to the broker's cache
        setPCState(state);
        if ( _oid == null ||  
            _broker.getStateManagerImplById(_oid, false) == null) {
        	_broker.setStateManager(_id, this, BrokerImpl.STATUS_INIT);
        }
        if (state == PCState.PNEW)
            fireLifecycleEvent(LifecycleEvent.AFTER_PERSIST);

        // if this is a non-tracking PC, add a hard ref to the appropriate data
        // sets and give it an opportunity to make a state snapshot.
        if (!isIntercepting()) {
            saveFields(true);
            if (!isNew())
                RedefinitionHelper.assignLazyLoadProxies(this);
        }
    }

    /**
     * Whether or not data access in this instance is intercepted. This differs
     * from {@link ClassMetaData#isIntercepting()} in that it checks for
     * property access + subclassing in addition to the redefinition /
     * enhancement checks.
     *
     * @since 1.0.0
     */
    public boolean isIntercepting() {
        if (getMetaData().isIntercepting())
            return true;
        // TODO:JRB Intercepting 
        if (AccessCode.isProperty(getMetaData().getAccessType())
            && _pc instanceof DynamicPersistenceCapable)
            return true;
        return false;
    }

    /**
     * Fire the given lifecycle event to all listeners.
     */
    private boolean fireLifecycleEvent(int type) {
        if (type == LifecycleEvent.AFTER_PERSIST
                && _broker.getConfiguration().getCallbackOptionsInstance().getPostPersistCallbackImmediate()) {
            fetchObjectId();
        }
        return _broker.fireLifecycleEvent(getManagedInstance(), null, _meta, type);
    }

    public void load(FetchConfiguration fetch) {
        load(fetch, LOAD_FGS, null, null, false);
    }

    /**
     * Load the state of this instance based on the given fetch configuration
     * and load mode. Return true if any data was loaded, false otherwise.
     */
    protected boolean load(FetchConfiguration fetch, int loadMode,
        BitSet exclude, Object sdata, boolean forWrite) {
        if (!forWrite
            && (!isPersistent() || (isNew() && !isFlushed()) || isDeleted()))
            return false;

        // if any fields being loaded, do state transitions for read
        BitSet fields = getUnloadedInternal(fetch, loadMode, exclude);
        boolean active = _broker.isActive();
        if (!forWrite && fields != null)
            beforeRead(-1);

        // call load even if no fields are being loaded, because it takes
        // care of checking if the DFG is loaded, making sure version info
        // is loaded, etc
        int lockLevel = calculateLockLevel(active, forWrite, fetch);
        boolean ret = loadFields(fields, fetch, lockLevel, sdata);
        obtainLocks(active, forWrite, lockLevel, fetch, sdata);
        return ret;
    }

    public Object getManagedInstance() {
        if (_pc instanceof ManagedInstanceProvider)
            return ((ManagedInstanceProvider) _pc).getManagedInstance();
        else
            return _pc;
    }

    public PersistenceCapable getPersistenceCapable() {
        return _pc;
    }

    public ClassMetaData getMetaData() {
        return _meta;
    }

    public OpenJPAStateManager getOwner() {
        return _owner;
    }

    public int getOwnerIndex() {
        return _ownerIndex;
    }

    public void setOwner(Object oid) {
        _ownerId = oid;
    }

    public boolean isEmbedded() {
        // _owner may not be set if embed object is from query result
        return _owner != null || _state instanceof ENonTransState;
    }

    public boolean isFlushed() {
        return (_flags & FLAG_FLUSHED) > 0;
    }

    public boolean isFlushedDirty() {
        return (_flags & FLAG_FLUSHED_DIRTY) > 0;
    }

    public BitSet getLoaded() {
        return _loaded;
    }

    public BitSet getUnloaded(FetchConfiguration fetch) {
        // collect fields to load from data store based on fetch configuration
        BitSet fields = getUnloadedInternal(fetch, LOAD_FGS, null);
        return (fields == null) ? new BitSet(0) : fields;
    }

    /**
     * Internal version of {@link OpenJPAStateManager#getUnloaded} that avoids
     * creating an empty bit set by returning null when there are no unloaded
     * fields.
     */
    private BitSet getUnloadedInternal(FetchConfiguration fetch, int mode,
        BitSet exclude) {
        if (exclude == StoreContext.EXCLUDE_ALL)
            return null;

        BitSet fields = null;
        FieldMetaData[] fmds = _meta.getFields();
        boolean load;
        for (int i = 0; i < fmds.length; i++) {
            if (_loaded.get(i) || (exclude != null && exclude.get(i)))
                continue;

            switch (mode) {
                case LOAD_SERIALIZE:
                    load = !fmds[i].isTransient();
                    break;
                case LOAD_FGS:
                    load = fetch == null || fetch.requiresFetch(fmds[i]) 
                        != FetchConfiguration.FETCH_NONE;
                    break;
                default: // LOAD_ALL
                    load = true;
            }

            if (load) {
                if (fields == null)
                    fields = new BitSet(fmds.length);
                fields.set(i);
            }
        }
        return fields;
    }

    public StoreContext getContext() {
        return _broker;
    }

    /**
     * Managing broker.
     */
    BrokerImpl getBroker() {
        return _broker;
    }

    public Object getId() {
        return _id;
    }

    public Object getObjectId() {
        StateManagerImpl sm = this;
        while (sm.getOwner() != null)
            sm = (StateManagerImpl) sm.getOwner();
        if (sm.isEmbedded() && sm.getOwner() == null)
            return sm._ownerId;
        return sm._oid;
    }

    public void setObjectId(Object oid) {
        _oid = oid;
        if (_pc != null && oid instanceof OpenJPAId)
            ((OpenJPAId) oid).setManagedInstanceType(_meta.getDescribedType());
    }

    public StateManagerImpl getObjectIdOwner() {
        StateManagerImpl sm = this;
        while (sm.getOwner() != null)
            sm = (StateManagerImpl) sm.getOwner();
        return sm;
    }
    public boolean assignObjectId(boolean flush) {
        lock();
        try {
            return assignObjectId(flush, false);
        } finally {
            unlock();
        }
    }

    /**
     * Ask store manager to assign our oid, optionally flushing and
     * optionally recaching on the new oid.
     */
    boolean assignObjectId(boolean flush, boolean preFlushing) {
        if (_oid != null || isEmbedded() || !isPersistent())
            return true;

        if (_broker.getStoreManager().assignObjectId(this, preFlushing)) {
            if (!preFlushing)
                assertObjectIdAssigned(true);
        } else if (flush)
            _broker.flush();
        else
            return false;
        return true;
    }

    /**
     * Make sure we were assigned an oid, and perform actions to make it
     * permanent.
     *
     * @param recache whether to recache ourself on the new oid
     */
    private void assertObjectIdAssigned(boolean recache) {
        if (!isNew() || isDeleted() || isProvisional() 
            || (_flags & FLAG_OID_ASSIGNED) != 0)
            return;
        if (_oid == null) {
            if (_meta.getIdentityType() == ClassMetaData.ID_DATASTORE)
                throw new InternalException(Exceptions.toString
                    (getManagedInstance()));
            _oid = ApplicationIds.create(_pc, _meta);
        }

        Object orig = _id;
        _id = _oid;
        if (recache) {
            try {
                _broker.setStateManager(orig, this,
                    BrokerImpl.STATUS_OID_ASSIGN);
            } catch (RuntimeException re) {
                _id = orig;
                _oid = null;
                throw re;
            }
        }
        _flags |= FLAG_OID_ASSIGNED;
    }

    /**
     * Assign the proper generated value to the given field based on its
     * value-strategy.
     */
    private boolean assignField(int field, boolean preFlushing) {
        OpenJPAStateManager sm = this;
        while (sm != null && sm.isEmbedded())
            sm = sm.getOwner();
        if (sm == null)
            return false;
        if (!sm.isNew() || sm.isFlushed() || sm.isDeleted())
            return false;

        // special-case oid fields, which require us to look inside the oid
        // object
        FieldMetaData fmd = _meta.getField(field);
        if (fmd.getDeclaredTypeCode() == JavaTypes.OID) {
            // try to shortcut if possible
            if (_oid != null || isEmbedded() || !isPersistent())
                return true;

            // check embedded fields of oid for value strategy + default value
            FieldMetaData[] pks = fmd.getEmbeddedMetaData().getFields();
            OpenJPAStateManager oidsm = null;
            boolean assign = false;
            for (int i = 0; !assign && i < pks.length; i++) {
                if (pks[i].getValueStrategy() == ValueStrategies.NONE)
                    continue;
                if (oidsm == null)
                    oidsm = new ObjectIdStateManager(fetchObjectField(field),
                        this, fmd);
                assign = oidsm.isDefaultValue(i);
            }
            return assign && assignObjectId(!preFlushing, preFlushing);
        }

        // Just return if there's no value generation strategy
        if (fmd.getValueStrategy() == ValueStrategies.NONE)
            return false;
        
        // Throw exception if field already has a value assigned.
        // @GeneratedValue overrides POJO initial values and setter methods
        if (!fmd.isValueGenerated() && !isDefaultValue(field))
            throw new InvalidStateException(_loc.get(
                "existing-value-override-excep", fmd.getFullName(false)));

        // for primary key fields, assign the object id and recache so that
        // to the user, so it looks like the oid always matches the pk fields
        if (fmd.isPrimaryKey() && !isEmbedded())
            return assignObjectId(!preFlushing, preFlushing);

        // for other fields just assign the field or flush if needed
        if (_broker.getStoreManager().assignField(this, field, preFlushing)) {
            fmd.setValueGenerated(true);
            return true;
        }
        if (!preFlushing)
            _broker.flush();
        return !preFlushing;
    }

    public Object getLock() {
        return _lock;
    }

    public void setLock(Object lock) {
        _lock = lock;
    }

    public Object getVersion() {
        return _version;
    }

    public void setVersion(Object version) {
        _loadVersion = version;
        assignVersionField(version);
    }

    Object getLoadVersion() {
        return _loadVersion;
    }

    public void setNextVersion(Object version) {
        assignVersionField(version);
    }

    private void assignVersionField(Object version) {
        _version = version;
        FieldMetaData vfield = _meta.getVersionField();
        if (vfield != null)
            store(vfield.getIndex(), JavaTypes.convert(version,
                vfield.getTypeCode()));
    }

    public PCState getPCState() {
        return _state;
    }

    public synchronized Object getImplData() {
        return _impl;
    }

    public synchronized Object setImplData(Object data, boolean cacheable) {
        Object old = _impl;
        _impl = data;
        if (cacheable && data != null)
            _flags |= FLAG_IMPL_CACHE;
        else
            _flags &= ~FLAG_IMPL_CACHE;
        return old;
    }

    public boolean isImplDataCacheable() {
        return (_flags & FLAG_IMPL_CACHE) != 0;
    }

    public Object getImplData(int field) {
        return getExtraFieldData(field, true);
    }

    public Object setImplData(int field, Object data) {
        return setExtraFieldData(field, data, true);
    }

    public synchronized boolean isImplDataCacheable(int field) {
        if (_fieldImpl == null || !_loaded.get(field))
            return false;
        if (_meta.getField(field).usesImplData() != null)
            return false;
        int idx = _meta.getExtraFieldDataIndex(field);
        return idx != -1 && _fieldImpl[idx] != null;
    }

    public Object getIntermediate(int field) {
        return getExtraFieldData(field, false);
    }

    public void setIntermediate(int field, Object data) {
        setExtraFieldData(field, data, false);
    }

    /**
     * Return the data from the proper index of the extra field data array.
     */
    protected synchronized Object getExtraFieldData(int field, boolean isLoaded) {
        // only return the field data if the field is in the right loaded
        // state; otherwise we might return intermediate for impl data or
        // vice versa
        if (_fieldImpl == null || _loaded.get(field) != isLoaded)
            return null;
        int idx = _meta.getExtraFieldDataIndex(field);
        return (idx == -1) ? null : _fieldImpl[idx];
    }

    /**
     * Set the data from to proper index of the extra field data array.
     */
    private synchronized Object setExtraFieldData(int field, Object data,
        boolean loaded) {
        int idx = _meta.getExtraFieldDataIndex(field);
        if (idx == -1)
            throw new InternalException(String.valueOf(_meta.getField(field)));

        Object old = (_fieldImpl == null) ? null : _fieldImpl[idx];
        if (data != null) {
            // cannot set if field in wrong loaded state
            if (_loaded.get(field) != loaded)
                throw new InternalException(String.valueOf(_meta.getField
                    (field)));

            // set data
            if (_fieldImpl == null)
                _fieldImpl = new Object[_meta.getExtraFieldDataLength()];
            _fieldImpl[idx] = data;
        } else if (_fieldImpl != null && _loaded.get(field) == loaded)
            _fieldImpl[idx] = null;
        return old;
    }

    public Object fetch(int field) {
        Object val = fetchField(field, false);
        return _meta.getField(field).getExternalValue(val, _broker);
    }

    public Object fetchField(int field, boolean transitions) {
        FieldMetaData fmd = _meta.getField(field);
        if (fmd == null)
            throw new UserException(_loc.get("no-field",
                String.valueOf(field), getManagedInstance().getClass())).
                setFailedObject(getManagedInstance());

        // do normal state transitions
        if (!fmd.isPrimaryKey() && transitions)
            accessingField(field);

        switch (fmd.getDeclaredTypeCode()) {
            case JavaTypes.STRING:
                return fetchStringField(field);
            case JavaTypes.OBJECT:
                return fetchObjectField(field);
            case JavaTypes.BOOLEAN:
                return (fetchBooleanField(field)) ? Boolean.TRUE
                    : Boolean.FALSE;
            case JavaTypes.BYTE:
                return Byte.valueOf(fetchByteField(field));
            case JavaTypes.CHAR:
                return Character.valueOf(fetchCharField(field));
            case JavaTypes.DOUBLE:
                return Double.valueOf(fetchDoubleField(field));
            case JavaTypes.FLOAT:
                return Float.valueOf(fetchFloatField(field));
            case JavaTypes.INT:
                return fetchIntField(field);
            case JavaTypes.LONG:
                return fetchLongField(field);
            case JavaTypes.SHORT:
                return Short.valueOf(fetchShortField(field));
            default:
                return fetchObjectField(field);
        }
    }

    public void store(int field, Object val) {
        val = _meta.getField(field).getFieldValue(val, _broker);
        storeField(field, val);
    }

    public void storeField(int field, Object val) {
        storeField(field, val, this);
    }

    /**
     * <p>Checks whether or not <code>_pc</code> is dirty. In the cases where
     * field tracking is not happening (see below), this method will do a
     * state comparison to find whether <code>_pc</code> is dirty, and will
     * update this instance with this information. In the cases where field
     * tracking is happening, this method is a no-op.</p>
     *
     * <p>Fields are tracked for all classes that are run through the OpenJPA
     * enhancer prior to or during deployment, and all classes (enhanced or
     * unenhanced) in a Java 6 environment or newer.</p>
     *
     * <p>In a Java 5 VM or older:
     * <br>- instances of unenhanced classes that use
     * property access and obey the property access limitations are tracked
     * when the instances are loaded from the database by OpenJPA, and are
     * not tracked when the instances are created by application code.
     * <br>- instances of unenhanced classes that use field access are
     * never tracked.</p>
     *
     * @since 1.0.0
     */
    public void dirtyCheck() {
        if (!needsDirtyCheck())
            return;

        SaveFieldManager saved = getSaveFieldManager();
        if (saved == null)
            throw new InternalException(_loc.get("no-saved-fields",
                getMetaData().getDescribedType().getName()));

        FieldMetaData[] fmds = getMetaData().getFields();
        for (int i = 0; i < fmds.length; i++) {
            // pk and version fields cannot be mutated; don't mark them
            // as such. ##### validate?
            if (!fmds[i].isPrimaryKey() && !fmds[i].isVersion()
                && _loaded.get(i)) {
                if (!saved.isFieldEqual(i, fetch(i))) {
                    dirty(i);
                }
            }
        }
    }

    private boolean needsDirtyCheck() {
        if (isIntercepting())
            return false;
        if (isDeleted())
            return false;
        if (isNew() && !isFlushed())
            return false;
        return true;
    }

    public Object fetchInitialField(int field) {
        FieldMetaData fmd = _meta.getField(field);
        if (_broker.getRestoreState() == RestoreState.RESTORE_NONE
            && ((_flags & FLAG_INVERSES) == 0
            || fmd.getInverseMetaDatas().length == 0))
            throw new InvalidStateException(_loc.get("restore-unset"));

        switch (fmd.getDeclaredTypeCode()) {
            case JavaTypes.DATE:
            case JavaTypes.CALENDAR:
            case JavaTypes.ARRAY:
            case JavaTypes.COLLECTION:
            case JavaTypes.MAP:
            case JavaTypes.OBJECT:
                // if we're not saving mutable types, throw an exception
                if (_broker.getRestoreState() != RestoreState.RESTORE_ALL
                    && ((_flags & FLAG_INVERSES) == 0
                    || fmd.getInverseMetaDatas().length == 0))
                    throw new InvalidStateException(_loc.get
                        ("mutable-restore-unset"));
        }

        lock();
        try {
            if (_saved == null || !_loaded.get(field) || !isFieldDirty(field))
                return fetchField(field, false);

            // if the field is dirty but we never loaded it, we can't restore it
            if (_saved.getUnloaded().get(field))
                throw new InvalidStateException(_loc.get("initial-unloaded",
                    fmd));

            provideField(_saved.getState(), _single, field);
            return fetchField(_single, fmd);
        } finally {
            unlock();
        }
    }

    /**
     * Fetch the specified field from the specified field manager, wrapping it
     * in an object if it's a primitive. A field should be provided to the
     * field manager before this call is made.
     */
    private static Object fetchField(FieldManager fm, FieldMetaData fmd) {
        int field = fmd.getIndex();
        switch (fmd.getDeclaredTypeCode()) {
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
            case JavaTypes.STRING:
                return fm.fetchStringField(field);
            default:
                return fm.fetchObjectField(field);
        }
    }

    public void setRemote(int field, Object value) {
        lock();
        try {
            Boolean stat = dirty(field, Boolean.FALSE, false);
            storeField(field, value, _single);
            replaceField(_pc, _single, field);
            postDirty(stat);
        } finally {
            unlock();
        }
    }

    ////////////////////////
    // Lifecycle operations
    ////////////////////////

    /**
     * Notification that the object is about to be accessed.
     *
     * @param field the field number being read, or -1 if not a single
     * field read
     */
    void beforeRead(int field) {
        // allow unmediated reads of primary key fields
        if (field != -1 && _meta.getField(field).isPrimaryKey())
            return;

        if (_broker.isActive() && !_broker.isTransactionEnding()) {
            if (_broker.getOptimistic())
                setPCState(_state.beforeOptimisticRead(this, field));
            else
                setPCState(_state.beforeRead(this, field));
        } else if (_broker.getNontransactionalRead())
            setPCState(_state.beforeNontransactionalRead(this, field));
        else
            throw new InvalidStateException(_loc.get("non-trans-read")).
                setFailedObject(getManagedInstance());
    }

    /**
     * Delegates to the current state.
     *
     * @see PCState#beforeFlush
     */
    void beforeFlush(int reason, OpCallbacks call) {
        _state.beforeFlush(this, reason == BrokerImpl.FLUSH_LOGICAL, call);
    }

    /**
     * Delegates to the current state.
     *
     * @see PCState#flush
     */
    void afterFlush(int reason) {
        // nothing happens when we flush non-persistent states
        if (!isPersistent())
            return;

        if (reason != BrokerImpl.FLUSH_ROLLBACK
            && reason != BrokerImpl.FLUSH_LOGICAL) {
            // analyze previous state for later
            boolean wasNew = isNew();
            boolean wasFlushed = isFlushed();
            boolean wasDeleted = isDeleted();
            boolean needPostUpdate = !(wasNew && !wasFlushed)
                    && (ImplHelper.getUpdateFields(this) != null);

            // all dirty fields were flushed, we are referencing the _dirty BitSet directly here
            // because we don't want to instantiate it if we don't have to.
            if (_dirty != null) {
                getFlushed().or(_dirty);
            }

            // important to set flushed bit after calling _state.flush so
            // that the state can tell whether this is the first flush
            setPCState(_state.flush(this));
            _flags |= FLAG_FLUSHED;
            _flags &= ~FLAG_FLUSHED_DIRTY;

            _flags &= ~FLAG_VERSION_CHECK;
            _flags &= ~FLAG_VERSION_UPDATE;

            // if this was an inc flush during which we had our identity
            // assigned, tell the broker to cache us under our final oid
            if (reason == BrokerImpl.FLUSH_INC)
                assertObjectIdAssigned(true);

            // if this object was stored with preFlush, do post-store callback
            if ((_flags & FLAG_PRE_FLUSHED) > 0)
                fireLifecycleEvent(LifecycleEvent.AFTER_STORE);

            // do post-update as needed
            if (wasNew && !wasFlushed)
                fireLifecycleEvent(LifecycleEvent.AFTER_PERSIST_PERFORMED);
            else if (wasDeleted)
                fireLifecycleEvent(LifecycleEvent.AFTER_DELETE_PERFORMED);
            else if (needPostUpdate)
                // updates and new-flushed with changes
                fireLifecycleEvent(LifecycleEvent.AFTER_UPDATE_PERFORMED);
        } else if (reason == BrokerImpl.FLUSH_ROLLBACK) {
            // revert to last loaded version and original oid
            assignVersionField(_loadVersion);
            if (isNew() && (_flags & FLAG_OID_ASSIGNED) == 0)
                _oid = null;
        }
        _flags &= ~FLAG_PRE_FLUSHED;
    }

    /**
     * Delegates to the current state after checking the value
     * of the RetainState flag.
     *
     * @see PCState#commit
     * @see PCState#commitRetain
     */
    void commit() {
        // release locks before oid updated
        releaseLocks();
 
        // update version and oid information
        setVersion(_version);
        _flags &= ~FLAG_FLUSHED;
        _flags &= ~FLAG_FLUSHED_DIRTY;

        Object orig = _id;
        assertObjectIdAssigned(false);

        boolean wasNew = isNew() && !isDeleted() && !isProvisional();
        if (_broker.getRetainState())
            setPCState(_state.commitRetain(this));
        else
            setPCState(_state.commit(this));

        // ask the broker to re-cache us if we were new previously
        if (wasNew)
            _broker.setStateManager(orig, this, BrokerImpl.STATUS_COMMIT_NEW);
    }

    /**
     * Delegates to the current state after checking the value
     * of the RetainState flag.
     *
     * @see PCState#rollback
     * @see PCState#rollbackRestore
     */
    void rollback() {
        // release locks
        releaseLocks();
        _flags &= ~FLAG_FLUSHED;
        _flags &= ~FLAG_FLUSHED_DIRTY;
        afterFlush(BrokerImpl.FLUSH_ROLLBACK);

        if (_broker.getRestoreState() != RestoreState.RESTORE_NONE)
            setPCState(_state.rollbackRestore(this));
        else
            setPCState(_state.rollback(this));
    }

    /**
     * Rollback state of the managed instance to the given savepoint.
     */
    void rollbackToSavepoint(SavepointFieldManager savepoint) {
        _state = savepoint.getPCState();
        BitSet loaded = savepoint.getLoaded();
        for (int i = 0, len = loaded.length(); i < len; i++) {
            if (loaded.get(i) && savepoint.restoreField(i)) {
                provideField(savepoint.getCopy(), savepoint, i);
                replaceField(_pc, savepoint, i);
            }
        }
        _loaded = loaded;
        _dirty = savepoint.getDirty();
        _flush = savepoint.getFlushed();
        _version = savepoint.getVersion();
        _loadVersion = savepoint.getLoadVersion();
    }

    /**
     * Delegates to the current state.
     *
     * @see PCState#persist
     * @see Broker#persist
     */
    void persist() {
        setPCState(_state.persist(this));
    }

    /**
     * Delegates to the current state.
     *
     * @see PCState#delete
     * @see Broker#delete
     */
    void delete() {
        setPCState(_state.delete(this));
    }

    /**
     * Delegates to the current state.
     *
     * @see PCState#nontransactional
     * @see Broker#nontransactional
     */
    void nontransactional() {
        setPCState(_state.nontransactional(this));
    }

    /**
     * Delegates to the current state.
     *
     * @see PCState#transactional
     * @see Broker#transactional
     */
    void transactional() {
        setPCState(_state.transactional(this));
    }

    /**
     * Delegates to the current state.
     *
     * @see PCState#nonprovisional
     */
    void nonprovisional(boolean logical, OpCallbacks call) {
        setPCState(_state.nonprovisional(this, logical, call));
    }

    /**
     * Delegates to the current state.
     *
     * @see PCState#release
     * @see Broker#release
     */
    void release(boolean unproxy) {
        release(unproxy, false);
    }

    void release(boolean unproxy, boolean force) {
        // optimization for detach-in-place special case when fields are
        // already (un)proxied correctly
        if (!unproxy)
            _flags |= FLAG_NO_UNPROXY;
        try {
            if (force)
                setPCState(PCState.TRANSIENT);
            else
                setPCState(_state.release(this));
        } finally {
            _flags &= ~FLAG_NO_UNPROXY;
        }
    }

    /**
     * Delegates to the current state.
     *
     * @see PCState#evict
     * @see Broker#evict
     */
    void evict() {
        setPCState(_state.evict(this));
    }

    /**
     * Gather relations reachable from values using
     * {@link ValueMetaData#CASCADE_IMMEDIATE}.
     */
    void gatherCascadeRefresh(OpCallbacks call) {
        FieldMetaData[] fmds = _meta.getFields();
        for (int i = 0; i < fmds.length; i++) {
            if (!_loaded.get(i))
                continue;

            if (fmds[i].getCascadeRefresh() == ValueMetaData.CASCADE_IMMEDIATE
                || fmds[i].getKey().getCascadeRefresh()
                == ValueMetaData.CASCADE_IMMEDIATE
                || fmds[i].getElement().getCascadeRefresh()
                == ValueMetaData.CASCADE_IMMEDIATE) {
                _single.storeObjectField(i, fetchField(i, false));
                _single.gatherCascadeRefresh(call);
                _single.clear();
            }
        }
    }

    public boolean beforeRefresh(boolean refreshAll) {
        // note: all logic placed here rather than in the states for
        // optimization; this method public b/c used by remote package

        // nothing to do for non persistent or new unflushed instances
        if (!isPersistent() || (isNew() && !isFlushed()))
            return false;

        lock();
        try {
            // if dirty need to clear fields
            if (isDirty()) {
                clearFields();
                return true;
            }

            // if some fields have been loaded but the instance is out of
            // date or this is part of a refreshAll() and we don't want to
            // take the extra hit to see if the instance is out of date, clear
            if (_loaded.length() > 0 && (refreshAll || isEmbedded()
                || !syncVersion(null))) {
                Object version = _version;
                clearFields();

                // if syncVersion just replaced the version, reset it
                if (!refreshAll && !isEmbedded())
                    setVersion(version);
                return true;
            }
            return false;
        } finally {
            unlock();
        }
    }

    /**
     * Perform state transitions after refresh. This method is only
     * called if {@link #beforeRefresh} returns true.
     */
    void afterRefresh() {
        lock();
        try {
            // transition to clean or nontransactional depending on trans status
            if (!_broker.isActive())
                setPCState(_state.afterNontransactionalRefresh());
            else if (_broker.getOptimistic())
                setPCState(_state.afterOptimisticRefresh());
            else
                setPCState(_state.afterRefresh());
        } finally {
            unlock();
        }
    }

    /**
     * Mark this object as a dereferenced dependent object.
     */
    void setDereferencedDependent(boolean deref, boolean notify) {
        if (!deref && (_flags & FLAG_DEREF) > 0) {
            if (notify)
                _broker.removeDereferencedDependent(this);
            _flags &= ~FLAG_DEREF;
        } else if (deref && (_flags & FLAG_DEREF) == 0) {
            _flags |= FLAG_DEREF;
            if (notify)
                _broker.addDereferencedDependent(this);
        }
    }
    
    void setDereferencedEmbedDependent(boolean deref) {
        if (!deref && (_flags & FLAG_EMBED_DEREF) > 0) {
            _flags &= ~FLAG_EMBED_DEREF;
        } else if (deref && (_flags & FLAG_EMBED_DEREF) == 0) {
            _flags |= FLAG_EMBED_DEREF;
        }
    }
    
    public boolean getDereferencedEmbedDependent() {
        return ((_flags & FLAG_EMBED_DEREF) == 0 ? false : true);
    }

    ///////////
    // Locking
    ///////////

    /**
     * Notification that we've been read-locked. Pass in the level at which
     * we were locked and the level at which we should write lock ourselves
     * on dirty.
     */
    void readLocked(int readLockLevel, int writeLockLevel) {
        // make sure object is added to transaction so lock will get
        // cleared on commit/rollback
        if (readLockLevel != LockLevels.LOCK_NONE)
            transactional();

        _readLockLevel = readLockLevel;
        _writeLockLevel = writeLockLevel;
        _flags |= FLAG_READ_LOCKED;
        _flags &= ~FLAG_WRITE_LOCKED;
    }

    /**
     * Return the lock level to use when loading state.
     */
    private int calculateLockLevel(boolean active, boolean forWrite,
        FetchConfiguration fetch) {
        if (!active)
            return LockLevels.LOCK_NONE;
        if (fetch == null)
            fetch = _broker.getFetchConfiguration();

        if (_readLockLevel == -1 || _readLockLevel < fetch.getReadLockLevel())
            _readLockLevel = fetch.getReadLockLevel();
        if (_writeLockLevel == -1 || _writeLockLevel < fetch.getWriteLockLevel())
            _writeLockLevel = fetch.getWriteLockLevel();
        return (forWrite) ? _writeLockLevel : _readLockLevel;
    }

    /**
     * Make sure we're locked at the given level.
     */
    private void obtainLocks(boolean active, boolean forWrite, int lockLevel,
        FetchConfiguration fetch, Object sdata) {
        if (!active)
            return;

        // if we haven't been locked yet, lock now at the given level
        int flag = (forWrite) ? FLAG_WRITE_LOCKED : FLAG_READ_LOCKED;
        if ((_flags & flag) == 0) {
            // make sure object is added to transaction so lock will get
            // cleared on commit/rollback
            if (lockLevel != LockLevels.LOCK_NONE)
                transactional();

            if (fetch == null)
                fetch = _broker.getFetchConfiguration();
            _broker.getLockManager().lock(this, lockLevel,
                fetch.getLockTimeout(), sdata);
            _flags |= FLAG_READ_LOCKED;
            _flags |= flag;
        }
    }

    /**
     * Release locks.
     */
    private void releaseLocks() {
        if (_lock != null)
            _broker.getLockManager().release(this);
        _readLockLevel = -1;
        _writeLockLevel = -1;
        _flags &= ~FLAG_READ_LOCKED;
        _flags &= ~FLAG_WRITE_LOCKED;
    }

    ////////////////////////////////////////////
    // Implementation of StateManager interface
    ////////////////////////////////////////////

    /**
     * @return whether or not unloaded fields should be closed.
     */
    public boolean serializing() {
        // if the broker is in the midst of a serialization, then no special
        // handling should be performed on the instance, and no subsequent
        // load should happen
        if (_broker.isSerializing())
            return false;

        try {
            if (_meta.isDetachable())
                return DetachManager.preSerialize(this);

            load(_broker.getFetchConfiguration(), LOAD_SERIALIZE, null, null, 
                false);
            return false;
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean writeDetached(ObjectOutput out)
        throws IOException {
        BitSet idxs = new BitSet(_meta.getFields().length);
        lock();
        try {
            boolean detsm = DetachManager.writeDetachedState(this, out, idxs);
            if (detsm)
                _flags |= FLAG_DETACHING;

            FieldMetaData[] fmds = _meta.getFields();
            for (int i = 0; i < fmds.length; i++) {
                if (fmds[i].isTransient())
                    continue;
                provideField(_pc, _single, i);
                _single.serialize(out, !idxs.get(i));
                _single.clear();
            }
            return true;
        } catch (RuntimeException re) {
            throw translate(re);
        } finally {
            _flags &= ~FLAG_DETACHING;
            unlock();
        }
    }

    public void proxyDetachedDeserialized(int idx) {
        // we don't serialize state manager impls
        throw new InternalException();
    }

    public boolean isTransactional() {
        // special case for TCLEAN, which we want to appear non-trans to
        // internal code, but which publicly should be transactional
        return _state == PCState.TCLEAN || _state.isTransactional();
    }

    public boolean isPendingTransactional() {
        return _state.isPendingTransactional();
    }

    public boolean isProvisional() {
        return _state.isProvisional();
    }

    public boolean isPersistent() {
        return _state.isPersistent();
    }

    public boolean isNew() {
        return _state.isNew();
    }

    public boolean isDeleted() {
        return _state.isDeleted();
    }

    public boolean isDirty() {
        return _state.isDirty();
    }

    public boolean isDetached() {
        return (_flags & FLAG_DETACHING) != 0;
    }

    public Object getGenericContext() {
        return _broker;
    }

    public Object fetchObjectId() {
        try {
            if (hasGeneratedKey() && _state instanceof PNewState && 
                _oid == null) 
                return _oid;
            assignObjectId(true);
            if (_oid == null || !_broker.getConfiguration().
                getCompatibilityInstance().getCopyObjectIds())
                return _oid;

            if (_meta.getIdentityType() == ClassMetaData.ID_DATASTORE)
                return _broker.getStoreManager().copyDataStoreId(_oid, _meta);
            return ApplicationIds.copy(_oid, _meta);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }
    
    private boolean hasGeneratedKey() {
        FieldMetaData[] pkFields = _meta.getPrimaryKeyFields();
        for (int i = 0; i < pkFields.length; i++) {
            if (pkFields[i].getValueStrategy() == ValueStrategies.AUTOASSIGN)
                return true;
        }
        return false;
    }

    public Object getPCPrimaryKey(Object oid, int field) {
        FieldMetaData fmd = _meta.getField(field);
        Object pk = ApplicationIds.get(oid, fmd);
        if (pk == null)
            return null;

        ClassMetaData relmeta = fmd.getDeclaredTypeMetaData();
        pk = ApplicationIds.wrap(relmeta, pk);
        if (relmeta.getIdentityType() == ClassMetaData.ID_DATASTORE
            && fmd.getObjectIdFieldTypeCode() == JavaTypes.LONG)
            pk = _broker.getStoreManager().newDataStoreId(pk, relmeta);
        else if (relmeta.getIdentityType() == ClassMetaData.ID_APPLICATION 
            && fmd.getObjectIdFieldType() != relmeta.getObjectIdType())
            pk = ApplicationIds.fromPKValues(new Object[] { pk }, relmeta);
        return _broker.find(pk, false, null);
    }

    public byte replaceFlags() {
        // we always use load required so that we can detect when objects
        // are touched for locking or making transactional
        return PersistenceCapable.LOAD_REQUIRED;
    }

    public StateManager replaceStateManager(StateManager sm) {
        return sm;
    }

    public void accessingField(int field) {
        // possibly change state
        try {
            // If this field is loaded, and not a PK field allow pass through
            // TODO -- what about version fields? Could probably UT this
            if(_loaded.get(field) && !_meta.getField(field).isPrimaryKey())
                return;
                
            beforeRead(field);
            beforeAccessField(field);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public boolean isDelayed(int field) {
        if (_delayed == null) {
            return false;
        }
        return _delayed.get(field);
    }

    public void setDelayed(int field, boolean delay) {
        if (_delayed == null) {
            _delayed = new BitSet();
        }
        if (delay) {
            _delayed.set(field);
        } else {
            _delayed.clear(field);
        }
    }

    /**
     * Loads a delayed access field.
     * @param field
     */
    public void loadDelayedField(int field) {
        if (!isDelayed(field)) {
            return;
        }

        try {
            beforeRead(field);
        } catch (RuntimeException re) {
            throw translate(re);
        }
        lock();
        try {
            boolean active = _broker.isActive();
            int lockLevel = calculateLockLevel(active, false, null);
            BitSet fields = new BitSet();
            fields.set(field);
            if (!_broker.getStoreManager().load(this, fields, _broker.getFetchConfiguration(), lockLevel, null)) {
                throw new ObjectNotFoundException(_loc.get("del-instance", _meta.getDescribedType(), _oid)).
                    setFailedObject(getManagedInstance());
            }
            // Cleared the delayed bit
            _delayed.clear(field);
            obtainLocks(active, false, lockLevel, null, null);
        } catch (RuntimeException re) {
            throw translate(re);
        } finally {
            unlock();
        }
    }

    /**
     * Load the given field before access.
     */
    protected void beforeAccessField(int field) {
        lock();
        try {
            boolean active = _broker.isActive();
            int lockLevel = calculateLockLevel(active, false, null);
            if (!_loaded.get(field))
                loadField(field, lockLevel, false, true);
            else
                assignField(field, false);
            obtainLocks(active, false, lockLevel, null, null);
        } catch (RuntimeException re) {
            throw translate(re);
        } finally {
            unlock();
        }
    }

    public void dirty(String field) {
        FieldMetaData fmd = _meta.getField(field);
        if (fmd == null)
            throw translate(new UserException(_loc.get("no-field", field,
                ImplHelper.getManagedInstance(_pc).getClass()))
                .setFailedObject(getManagedInstance()));

        dirty(fmd.getIndex(), null, true);
    }

    public void dirty(int field) {
        dirty(field, null, true);
    }

    private boolean isEmbeddedNotUpdatable() {
        // embeddable object returned from query result is not uptable
        return (_owner == null && _ownerId != null);
    }

    /**
     * Make the given field dirty.
     *
     * @param mutate if null, may be an SCO mutation; if true, is certainly
     * a mutation (or at least treat as one)
     * @return {@link Boolean#FALSE} if this instance was already dirty,
     * <code>null</code> if it was dirty but not since flush, and
     * {@link Boolean#TRUE} if it was not dirty
     */
    private Boolean dirty(int field, Boolean mutate, boolean loadFetchGroup) {
        boolean locked = false;
        boolean newFlush = false;
        boolean clean = false;
        try {
            FieldMetaData fmd = _meta.getField(field);
            if (!isNew() || isFlushed()) {
                if (fmd.getUpdateStrategy() == UpdateStrategies.RESTRICT)
                    throw new InvalidStateException(_loc.get
                        ("update-restrict", fmd));
                if (fmd.getUpdateStrategy() == UpdateStrategies.IGNORE)
                    return Boolean.FALSE;
            }

            if (isEmbedded()) {
                if (isEmbeddedNotUpdatable())
                    throw new UserException(_loc.get
                        ("cant-update-embed-in-query-result")).setFailedObject
                        (getManagedInstance());
                else
                    // notify owner of change
                    _owner.dirty(_ownerIndex, Boolean.TRUE, loadFetchGroup);
            }

            // is this a direct mutation of an sco field?
            if (mutate == null) {
                switch (fmd.getDeclaredTypeCode()) {
                    case JavaTypes.COLLECTION:
                    case JavaTypes.MAP:
                    case JavaTypes.ARRAY:
                    case JavaTypes.DATE:
                    case JavaTypes.CALENDAR:
                    case JavaTypes.OBJECT:
                        mutate = Boolean.TRUE;
                        break;
                    case JavaTypes.PC:
                        mutate =
                            (fmd.isEmbedded()) ? Boolean.TRUE : Boolean.FALSE;
                        break;
                    default:
                        mutate = Boolean.FALSE; // not sco
                }
            }

            // possibly change state
            boolean active = _broker.isActive();
            clean = !_state.isDirty(); // intentional direct access

            // fire event fast before state change.
            if (clean)
                fireLifecycleEvent(LifecycleEvent.BEFORE_DIRTY);
            if (active) {
                if (_broker.getOptimistic())
                    setPCState(_state.beforeOptimisticWrite(this, field,
                        mutate.booleanValue()));
                else
                    setPCState(_state.beforeWrite(this, field,
                        mutate.booleanValue()));
            } else if (fmd.getManagement() == FieldMetaData.MANAGE_PERSISTENT) {
                if (isPersistent() && !_broker.getNontransactionalWrite())
                    throw new InvalidStateException(_loc.get
                        ("non-trans-write")).setFailedObject
                        (getManagedInstance());

                setPCState(_state.beforeNontransactionalWrite(this, field,
                    mutate.booleanValue()));
            }

            if ((_flags & FLAG_FLUSHED) != 0) {
                newFlush = (_flags & FLAG_FLUSHED_DIRTY) == 0;
                _flags |= FLAG_FLUSHED_DIRTY;
            }

            lock();
            locked = true;

            // note that the field is in need of flushing again, and tell the
            // broker too
            clearFlushField(field);
            _broker.setDirty(this, newFlush && !clean);

            // save the field for rollback if needed
            saveField(field);

            // dirty the field and mark loaded; load fetch group if needed
            int lockLevel = calculateLockLevel(active, true, null);
            if (!isFieldDirty(field)) {
                setLoaded(field, true);
                setFieldDirty(field);

                // make sure the field's fetch group is loaded
                if (loadFetchGroup && isPersistent()
                    && fmd.getManagement() == fmd.MANAGE_PERSISTENT)
                    loadField(field, lockLevel, true, true);
            }
            obtainLocks(active, true, lockLevel, null, null);
        } catch (RuntimeException re) {
            throw translate(re);
        } finally {
            if (locked)
                unlock();
        }

        if (clean)
            return Boolean.TRUE;
        if (newFlush) {
            // this event can be fired later cause we're already dirty.
            fireLifecycleEvent(LifecycleEvent.BEFORE_DIRTY_FLUSHED);
            return null;
        }
        return Boolean.FALSE;
    }

    /**
     * Fire post-dirty events after field value changes.
     *
     * @param status return value from {@link #dirty(int, Boolean, boolean)}
     */
    private void postDirty(Boolean status) {
        if (Boolean.TRUE.equals(status))
            fireLifecycleEvent(LifecycleEvent.AFTER_DIRTY);
        else if (status == null)
            fireLifecycleEvent(LifecycleEvent.AFTER_DIRTY_FLUSHED);
    }

    public void removed(int field, Object removed, boolean key) {
        if (removed == null)
            return;

        try {
            // dereference dependent fields, delete embedded
            FieldMetaData fmd = _meta.getField(field);
            ValueMetaData vmd = (key) ? fmd.getKey() : fmd.getElement();
            if (vmd.isEmbeddedPC())
                _single.delete(vmd, removed, null);
            else if (vmd.getCascadeDelete() == ValueMetaData.CASCADE_AUTO)
                _single.dereferenceDependent(removed);
        } catch (RuntimeException re) {
            throw translate(re);
        }
    }

    public Object newProxy(int field) {
        FieldMetaData fmd = _meta.getField(field);
        if (!fmd.isExternalized())
            return newFieldProxy(field);

        switch (fmd.getTypeCode()) {
            case JavaTypes.DATE:
                if (fmd.getDeclaredType() == java.sql.Date.class)
                    return new java.sql.Date(System.currentTimeMillis());
                if (fmd.getDeclaredType() == java.sql.Timestamp.class)
                    return new java.sql.Timestamp(System.currentTimeMillis());
                if (fmd.getDeclaredType() == java.sql.Time.class)
                    return new java.sql.Time(System.currentTimeMillis());
                return new Date();
            case JavaTypes.CALENDAR:
                return Calendar.getInstance();
            case JavaTypes.COLLECTION:
                return new ArrayList();
            case JavaTypes.MAP:
                return new HashMap();
        }
        return null;
    }

    public Object newFieldProxy(int field) {
        FieldMetaData fmd = _meta.getField(field);
        ProxyManager mgr = _broker.getConfiguration().
            getProxyManagerInstance();
        Object init = fmd.getInitializer();

        switch (fmd.getDeclaredTypeCode()) {
            case JavaTypes.DATE:
                return mgr.newDateProxy(fmd.getDeclaredType());
            case JavaTypes.CALENDAR:
                return mgr.newCalendarProxy(fmd.getDeclaredType(),
                    init instanceof TimeZone ? (TimeZone) init : null);
            case JavaTypes.COLLECTION:
                return mgr.newCollectionProxy(fmd.getProxyType(),
                    fmd.getElement().getDeclaredType(),
                    init instanceof Comparator ? (Comparator) init : null,
                        _broker.getConfiguration().getCompatibilityInstance().getAutoOff());
            case JavaTypes.MAP:
                return mgr.newMapProxy(fmd.getProxyType(),
                    fmd.getKey().getDeclaredType(),
                    fmd.getElement().getDeclaredType(),
                    init instanceof Comparator ? (Comparator) init : null,
                        _broker.getConfiguration().getCompatibilityInstance().getAutoOff());
        }
        return null;
    }

    public boolean isDefaultValue(int field) {
        lock();
        try {
            _single.clear();
            provideField(_pc, _single, field);
            boolean ret = _single.isDefaultValue();
            _single.clear();
            return ret;
        } finally {
            unlock();
        }
    }

    /////////////////////////////////////////////////////////
    // Record that the field is dirty (which might load DFG)
    /////////////////////////////////////////////////////////

    public void settingBooleanField(PersistenceCapable pc, int field,
        boolean curVal, boolean newVal, int set) {
        if (set != SET_REMOTE) {
            if (newVal == curVal && _loaded.get(field))
                return;
            assertNoPrimaryKeyChange(field);
        }

        lock();
        try {
            Boolean stat = dirty(field, Boolean.FALSE, set == SET_USER);
            _single.storeBooleanField(field, newVal);
            replaceField(pc, _single, field);
            postDirty(stat);
        } finally {
            unlock();
        }
    }

    public void settingByteField(PersistenceCapable pc, int field,
        byte curVal, byte newVal, int set) {
        if (set != SET_REMOTE) {
            if (newVal == curVal && _loaded.get(field))
                return;
            assertNoPrimaryKeyChange(field);
        }

        lock();
        try {
            Boolean stat = dirty(field, Boolean.FALSE, set == SET_USER);
            _single.storeByteField(field, newVal);
            replaceField(pc, _single, field);
            postDirty(stat);
        } finally {
            unlock();
        }
    }

    public void settingCharField(PersistenceCapable pc, int field,
        char curVal, char newVal, int set) {
        if (set != SET_REMOTE) {
            if (newVal == curVal && _loaded.get(field))
                return;
            assertNoPrimaryKeyChange(field);
        }

        lock();
        try {
            Boolean stat = dirty(field, Boolean.FALSE, set == SET_USER);
            _single.storeCharField(field, newVal);
            replaceField(pc, _single, field);
            postDirty(stat);
        } finally {
            unlock();
        }
    }

    public void settingDoubleField(PersistenceCapable pc, int field,
        double curVal, double newVal, int set) {
        if (set != SET_REMOTE) {
            if (newVal == curVal && _loaded.get(field))
                return;
            assertNoPrimaryKeyChange(field);
        }

        lock();
        try {
            Boolean stat = dirty(field, Boolean.FALSE, set == SET_USER);
            _single.storeDoubleField(field, newVal);
            replaceField(pc, _single, field);
            postDirty(stat);
        } finally {
            unlock();
        }
    }

    public void settingFloatField(PersistenceCapable pc, int field,
        float curVal, float newVal, int set) {
        if (set != SET_REMOTE) {
            if (newVal == curVal && _loaded.get(field))
                return;
            assertNoPrimaryKeyChange(field);
        }

        lock();
        try {
            Boolean stat = dirty(field, Boolean.FALSE, set == SET_USER);
            _single.storeFloatField(field, newVal);
            replaceField(pc, _single, field);
            postDirty(stat);
        } finally {
            unlock();
        }
    }

    public void settingIntField(PersistenceCapable pc, int field,
        int curVal, int newVal, int set) {
        if (set != SET_REMOTE) {
            if (newVal == curVal && _loaded.get(field))
                return;
            assertNoPrimaryKeyChange(field);
        }

        lock();
        try {
            Boolean stat = dirty(field, Boolean.FALSE, set == SET_USER);
            _single.storeIntField(field, newVal);
            replaceField(pc, _single, field);
            postDirty(stat);
        } finally {
            unlock();
        }
    }

    public void settingLongField(PersistenceCapable pc, int field,
        long curVal, long newVal, int set) {
        if (set != SET_REMOTE) {
            if (newVal == curVal && _loaded.get(field))
                return;
            assertNoPrimaryKeyChange(field);
        }

        lock();
        try {
            Boolean stat = dirty(field, Boolean.FALSE, set == SET_USER);
            _single.storeLongField(field, newVal);
            replaceField(pc, _single, field);
            postDirty(stat);
        } finally {
            unlock();
        }
    }

    public void settingObjectField(PersistenceCapable pc, int field,
        Object curVal, Object newVal, int set) {
        if (set != SET_REMOTE) {
            FieldMetaData fmd = _meta.getField(field);
            if (_loaded.get(field)) {
                if (newVal == curVal)
                    return;

                // only compare new to old values if the comparison is going to
                // be cheap -- don't compare collections, maps, UDTs
                switch (fmd.getDeclaredTypeCode()) {
                    case JavaTypes.ARRAY:
                    case JavaTypes.COLLECTION:
                    case JavaTypes.MAP:
                    case JavaTypes.PC_UNTYPED:
                        break;
                    case JavaTypes.PC:
                        if (_meta.getField(field).isPrimaryKey()) {
                            // this field is a derived identity
                            //if (newVal != null && newVal.equals(curVal))
                            //    return;
                            //else {
                                if (curVal != null && newVal != null && 
                                    curVal instanceof PersistenceCapable && newVal instanceof PersistenceCapable) {
                                    PersistenceCapable curPc = (PersistenceCapable) curVal;
                                    PersistenceCapable newPc = (PersistenceCapable) newVal;
                                    if (curPc.pcFetchObjectId().equals(newPc.pcFetchObjectId()))
                                        return;
                                    
                                }
                            //}
                        } else     
                            break;
                    default:
                        if (newVal != null && newVal.equals(curVal))
                            return;
                }
            } else {
                // if this is a dependent unloaded field, make sure to load
                // it now
                if (fmd.getCascadeDelete() == ValueMetaData.CASCADE_AUTO
                    || fmd.getKey().getCascadeDelete()
                    == ValueMetaData.CASCADE_AUTO
                    || fmd.getElement().getCascadeDelete()
                    == ValueMetaData.CASCADE_AUTO)
                    curVal = fetchObjectField(field);
            }

            assertNoPrimaryKeyChange(field);
            if (fmd.getDeclaredTypeCode() == JavaTypes.OID)
                assertNotManagedObjectId(newVal);
        }

        lock();
        try {
            Boolean stat = dirty(field, Boolean.FALSE, set == SET_USER);
            if (set != SET_REMOTE) {
                _single.storeObjectField(field, curVal);
                _single.unproxy();
                _single.dereferenceDependent();
                _single.clear();
            }
            _single.storeObjectField(field, newVal);
            replaceField(pc, _single, field);
            postDirty(stat);
        } finally {
            unlock();
        }
    }

    public void settingShortField(PersistenceCapable pc, int field,
        short curVal, short newVal, int set) {
        if (set != SET_REMOTE) {
            if (newVal == curVal && _loaded.get(field))
                return;
            assertNoPrimaryKeyChange(field);
        }

        lock();
        try {
            Boolean stat = dirty(field, Boolean.FALSE, set == SET_USER);
            _single.storeShortField(field, newVal);
            replaceField(pc, _single, field);
            postDirty(stat);
        } finally {
            unlock();
        }
    }

    public void settingStringField(PersistenceCapable pc, int field,
        String curVal, String newVal, int set) {
        if (set != SET_REMOTE) {
            if (StringUtils.equals(newVal, curVal) && _loaded.get(field))
                return;
            assertNoPrimaryKeyChange(field);
        }

        lock();
        try {
            Boolean stat = dirty(field, Boolean.FALSE, set == SET_USER);
            _single.storeStringField(field, newVal);
            replaceField(pc, _single, field);
            postDirty(stat);
        } finally {
            unlock();
        }
    }

    /**
     * Disallows changing primary key fields for instances.
     */
    private void assertNoPrimaryKeyChange(int field) {
        if (_oid != null && _meta.getField(field).isPrimaryKey())
            throw translate(new InvalidStateException(_loc.get
                ("change-identity")).setFailedObject(getManagedInstance()));
    }

    /**
     * Disallows setting an object id field to a managed instance.
     */
    void assertNotManagedObjectId(Object val) {
        if (val != null
            && (ImplHelper.toPersistenceCapable(val,
                 getContext().getConfiguration())).pcGetGenericContext()!= null)
            throw translate(new InvalidStateException(_loc.get
                ("managed-oid", Exceptions.toString(val),
                    Exceptions.toString(getManagedInstance()))).
                setFailedObject(getManagedInstance()));
    }

    ////////////////////////////
    // Delegate to FieldManager
    ////////////////////////////

    public void providedBooleanField(PersistenceCapable pc, int field,
        boolean curVal) {
        _fm.storeBooleanField(field, curVal);
    }

    public void providedByteField(PersistenceCapable pc, int field,
        byte curVal) {
        _fm.storeByteField(field, curVal);
    }

    public void providedCharField(PersistenceCapable pc, int field,
        char curVal) {
        _fm.storeCharField(field, curVal);
    }

    public void providedDoubleField(PersistenceCapable pc, int field,
        double curVal) {
        _fm.storeDoubleField(field, curVal);
    }

    public void providedFloatField(PersistenceCapable pc, int field,
        float curVal) {
        _fm.storeFloatField(field, curVal);
    }

    public void providedIntField(PersistenceCapable pc, int field,
        int curVal) {
        _fm.storeIntField(field, curVal);
    }

    public void providedLongField(PersistenceCapable pc, int field,
        long curVal) {
        _fm.storeLongField(field, curVal);
    }

    public void providedObjectField(PersistenceCapable pc, int field,
        Object curVal) {
        _fm.storeObjectField(field, curVal);
    }

    public void providedShortField(PersistenceCapable pc, int field,
        short curVal) {
        _fm.storeShortField(field, curVal);
    }

    public void providedStringField(PersistenceCapable pc, int field,
        String curVal) {
        _fm.storeStringField(field, curVal);
    }

    public boolean replaceBooleanField(PersistenceCapable pc, int field) {
        return _fm.fetchBooleanField(field);
    }

    public byte replaceByteField(PersistenceCapable pc, int field) {
        return _fm.fetchByteField(field);
    }

    public char replaceCharField(PersistenceCapable pc, int field) {
        return _fm.fetchCharField(field);
    }

    public double replaceDoubleField(PersistenceCapable pc, int field) {
        return _fm.fetchDoubleField(field);
    }

    public float replaceFloatField(PersistenceCapable pc, int field) {
        return _fm.fetchFloatField(field);
    }

    public int replaceIntField(PersistenceCapable pc, int field) {
        return _fm.fetchIntField(field);
    }

    public long replaceLongField(PersistenceCapable pc, int field) {
        return _fm.fetchLongField(field);
    }

    public Object replaceObjectField(PersistenceCapable pc, int field) {
        return _fm.fetchObjectField(field);
    }

    public short replaceShortField(PersistenceCapable pc, int field) {
        return _fm.fetchShortField(field);
    }

    public String replaceStringField(PersistenceCapable pc, int field) {
        return _fm.fetchStringField(field);
    }

    //////////////////////////////////
    // Implementation of FieldManager
    //////////////////////////////////

    public boolean fetchBoolean(int field) {
        FieldMetaData fmd = _meta.getField(field);
        if (!fmd.isExternalized())
            return fetchBooleanField(field);

        Object val = fetchField(field, false);
        return ((Boolean) fmd.getExternalValue(val, _broker)).booleanValue();
    }

    public boolean fetchBooleanField(int field) {
        lock();
        try {
            if (!_loaded.get(field))
                loadField(field, LockLevels.LOCK_NONE, false, false);

            provideField(_pc, _single, field);
            return _single.fetchBooleanField(field);
        } finally {
            unlock();
        }
    }

    public byte fetchByte(int field) {
        FieldMetaData fmd = _meta.getField(field);
        if (!fmd.isExternalized())
            return fetchByteField(field);

        Object val = fetchField(field, false);
        return ((Number) fmd.getExternalValue(val, _broker)).byteValue();
    }

    public byte fetchByteField(int field) {
        lock();
        try {
            if (!_loaded.get(field))
                loadField(field, LockLevels.LOCK_NONE, false, false);

            provideField(_pc, _single, field);
            return _single.fetchByteField(field);
        } finally {
            unlock();
        }
    }

    public char fetchChar(int field) {
        FieldMetaData fmd = _meta.getField(field);
        if (!fmd.isExternalized())
            return fetchCharField(field);

        Object val = fetchField(field, false);
        return ((Character) fmd.getExternalValue(val, _broker)).charValue();
    }

    public char fetchCharField(int field) {
        lock();
        try {
            if (!_loaded.get(field))
                loadField(field, LockLevels.LOCK_NONE, false, false);

            provideField(_pc, _single, field);
            return _single.fetchCharField(field);
        } finally {
            unlock();
        }
    }

    public double fetchDouble(int field) {
        FieldMetaData fmd = _meta.getField(field);
        if (!fmd.isExternalized())
            return fetchDoubleField(field);

        Object val = fetchField(field, false);
        return ((Number) fmd.getExternalValue(val, _broker)).doubleValue();
    }

    public double fetchDoubleField(int field) {
        lock();
        try {
            if (!_loaded.get(field))
                loadField(field, LockLevels.LOCK_NONE, false, false);

            provideField(_pc, _single, field);
            return _single.fetchDoubleField(field);
        } finally {
            unlock();
        }
    }

    public float fetchFloat(int field) {
        FieldMetaData fmd = _meta.getField(field);
        if (!fmd.isExternalized())
            return fetchFloatField(field);

        Object val = fetchField(field, false);
        return ((Number) fmd.getExternalValue(val, _broker)).floatValue();
    }

    public float fetchFloatField(int field) {
        lock();
        try {
            if (!_loaded.get(field))
                loadField(field, LockLevels.LOCK_NONE, false, false);

            provideField(_pc, _single, field);
            return _single.fetchFloatField(field);
        } finally {
            unlock();
        }
    }

    public int fetchInt(int field) {
        FieldMetaData fmd = _meta.getField(field);
        if (!fmd.isExternalized())
            return fetchIntField(field);

        Object val = fetchField(field, false);
        return ((Number) fmd.getExternalValue(val, _broker)).intValue();
    }

    public int fetchIntField(int field) {
        lock();
        try {
            if (!_loaded.get(field))
                loadField(field, LockLevels.LOCK_NONE, false, false);

            provideField(_pc, _single, field);
            return _single.fetchIntField(field);
        } finally {
            unlock();
        }
    }

    public long fetchLong(int field) {
        FieldMetaData fmd = _meta.getField(field);
        if (!fmd.isExternalized())
            return fetchLongField(field);

        Object val = fetchField(field, false);
        return ((Number) fmd.getExternalValue(val, _broker)).longValue();
    }

    public long fetchLongField(int field) {
        lock();
        try {
            if (!_loaded.get(field))
                loadField(field, LockLevels.LOCK_NONE, false, false);

            provideField(_pc, _single, field);
            return _single.fetchLongField(field);
        } finally {
            unlock();
        }
    }

    public Object fetchObject(int field) {
        FieldMetaData fmd = _meta.getField(field);
        if (!fmd.isExternalized())
            return fetchObjectField(field);

        Object val = fetchField(field, false);
        return fmd.getExternalValue(val, _broker);
    }

    public Object fetchObjectField(int field) {
        lock();
        try {
            if (!_loaded.get(field))
                loadField(field, LockLevels.LOCK_NONE, false, false);

            provideField(_pc, _single, field);
            return _single.fetchObjectField(field);
        } finally {
            unlock();
        }
    }

    public short fetchShort(int field) {
        FieldMetaData fmd = _meta.getField(field);
        if (!fmd.isExternalized())
            return fetchShortField(field);

        Object val = fetchField(field, false);
        return ((Number) fmd.getExternalValue(val, _broker)).shortValue();
    }

    public short fetchShortField(int field) {
        lock();
        try {
            if (!_loaded.get(field))
                loadField(field, LockLevels.LOCK_NONE, false, false);

            provideField(_pc, _single, field);
            return _single.fetchShortField(field);
        } finally {
            unlock();
        }
    }

    public String fetchString(int field) {
        FieldMetaData fmd = _meta.getField(field);
        if (!fmd.isExternalized())
            return fetchStringField(field);

        Object val = fetchField(field, false);
        return (String) fmd.getExternalValue(val, _broker);
    }

    public String fetchStringField(int field) {
        lock();
        try {
            if (!_loaded.get(field))
                loadField(field, LockLevels.LOCK_NONE, false, false);

            provideField(_pc, _single, field);
            return _single.fetchStringField(field);
        } finally {
            unlock();
        }
    }

    public void storeBoolean(int field, boolean externalVal) {
        FieldMetaData fmd = _meta.getField(field);
        if (!fmd.isExternalized())
            storeBooleanField(field, externalVal);
        else {
            Object val = (externalVal) ? Boolean.TRUE : Boolean.FALSE;
            storeField(field, fmd.getFieldValue(val, _broker));
        }
    }

    public void storeBooleanField(int field, boolean curVal) {
        lock();
        try {
            _single.storeBooleanField(field, curVal);
            replaceField(_pc, _single, field);
            setLoaded(field, true);
            postLoad(field, null);
        } finally {
            unlock();
        }
    }

    public void storeByte(int field, byte externalVal) {
        FieldMetaData fmd = _meta.getField(field);
        if (!fmd.isExternalized())
            storeByteField(field, externalVal);
        else
            storeField(field, fmd.getFieldValue(Byte.valueOf(externalVal),
                _broker));
    }

    public void storeByteField(int field, byte curVal) {
        lock();
        try {
            _single.storeByteField(field, curVal);
            replaceField(_pc, _single, field);
            setLoaded(field, true);
            postLoad(field, null);
        } finally {
            unlock();
        }
    }

    public void storeChar(int field, char externalVal) {
        FieldMetaData fmd = _meta.getField(field);
        if (!fmd.isExternalized())
            storeCharField(field, externalVal);
        else
            storeField(field, fmd.getFieldValue(Character.valueOf(externalVal),
                _broker));
    }

    public void storeCharField(int field, char curVal) {
        lock();
        try {
            _single.storeCharField(field, curVal);
            replaceField(_pc, _single, field);
            setLoaded(field, true);
            postLoad(field, null);
        } finally {
            unlock();
        }
    }

    public void storeDouble(int field, double externalVal) {
        FieldMetaData fmd = _meta.getField(field);
        if (!fmd.isExternalized())
            storeDoubleField(field, externalVal);
        else
            storeField(field, fmd.getFieldValue(Double.valueOf(externalVal), _broker));
    }

    public void storeDoubleField(int field, double curVal) {
        lock();
        try {
            _single.storeDoubleField(field, curVal);
            replaceField(_pc, _single, field);
            setLoaded(field, true);
            postLoad(field, null);
        } finally {
            unlock();
        }
    }

    public void storeFloat(int field, float externalVal) {
        FieldMetaData fmd = _meta.getField(field);
        if (!fmd.isExternalized())
            storeFloatField(field, externalVal);
        else
            storeField(field, fmd.getFieldValue(Float.valueOf(externalVal), _broker));
    }

    public void storeFloatField(int field, float curVal) {
        lock();
        try {
            _single.storeFloatField(field, curVal);
            replaceField(_pc, _single, field);
            setLoaded(field, true);
            postLoad(field, null);
        } finally {
            unlock();
        }
    }

    public void storeInt(int field, int externalVal) {
        FieldMetaData fmd = _meta.getField(field);
        if (!fmd.isExternalized())
            storeIntField(field, externalVal);
        else
            storeField(field, fmd.getFieldValue(externalVal,
                _broker));
    }

    public void storeIntField(int field, int curVal) {
        lock();
        try {
            _single.storeIntField(field, curVal);
            replaceField(_pc, _single, field);
            setLoaded(field, true);
            postLoad(field, null);
        } finally {
            unlock();
        }
    }

    public void storeLong(int field, long externalVal) {
        FieldMetaData fmd = _meta.getField(field);
        if (!fmd.isExternalized())
            storeLongField(field, externalVal);
        else
            storeField(field, fmd.getFieldValue(externalVal,
                _broker));
    }

    public void storeLongField(int field, long curVal) {
        lock();
        try {
            _single.storeLongField(field, curVal);
            replaceField(_pc, _single, field);
            setLoaded(field, true);
            postLoad(field, null);
        } finally {
            unlock();
        }
    }

    public void storeObject(int field, Object externalVal) {
        FieldMetaData fmd = _meta.getField(field);
        externalVal = fmd.order(externalVal);
        if (!fmd.isExternalized())
            storeObjectField(field, externalVal);
        else
            storeField(field, fmd.getFieldValue(externalVal, _broker));
    }

    public void storeObjectField(int field, Object curVal) {
        lock();
        try {
            _single.storeObjectField(field, curVal);
            _single.proxy(true, false);
            replaceField(_pc, _single, field);
            setLoaded(field, true);
            postLoad(field, null);
        } finally {
            unlock();
        }
    }

    public void storeShort(int field, short externalVal) {
        FieldMetaData fmd = _meta.getField(field);
        if (!fmd.isExternalized())
            storeShortField(field, externalVal);
        else
            storeField(field, fmd.getFieldValue(Short.valueOf(externalVal),
                _broker));
    }

    public void storeShortField(int field, short curVal) {
        lock();
        try {
            _single.storeShortField(field, curVal);
            replaceField(_pc, _single, field);
            setLoaded(field, true);
            postLoad(field, null);
        } finally {
            unlock();
        }
    }

    public void storeString(int field, String externalVal) {
        FieldMetaData fmd = _meta.getField(field);
        if (!fmd.isExternalized())
            storeStringField(field, externalVal);
        else
            storeField(field, fmd.getFieldValue(externalVal, _broker));
    }

    public void storeStringField(int field, String curVal) {
        lock();
        try {
            _single.storeStringField(field, curVal);
            replaceField(_pc, _single, field);
            setLoaded(field, true);
            postLoad(field, null);
        } finally {
            unlock();
        }
    }

    /**
     * Store the given field value into the given field manager.
     */
    private void storeField(int field, Object val, FieldManager fm) {
        FieldMetaData fmd = _meta.getField(field);
        if (fmd == null)
            throw new UserException(_loc.get("no-field-index",
                String.valueOf(field), _meta.getDescribedType())).
                setFailedObject(getManagedInstance());

        switch (fmd.getDeclaredTypeCode()) {
            case JavaTypes.BOOLEAN:
                boolean bool = val != null && ((Boolean) val).booleanValue();
                fm.storeBooleanField(field, bool);
                break;
            case JavaTypes.BYTE:
                byte b = (val == null) ? 0 : ((Number) val).byteValue();
                fm.storeByteField(field, b);
                break;
            case JavaTypes.CHAR:
                char c = (val == null) ? 0 : ((Character) val).charValue();
                fm.storeCharField(field, c);
                break;
            case JavaTypes.DOUBLE:
                double d = (val == null) ? 0 : ((Number) val).doubleValue();
                fm.storeDoubleField(field, d);
                break;
            case JavaTypes.FLOAT:
                float f = (val == null) ? 0 : ((Number) val).floatValue();
                fm.storeFloatField(field, f);
                break;
            case JavaTypes.INT:
                int i = (val == null) ? 0 : ((Number) val).intValue();
                fm.storeIntField(field, i);
                break;
            case JavaTypes.LONG:
                long l = (val == null) ? 0 : ((Number) val).longValue();
                fm.storeLongField(field, l);
                break;
            case JavaTypes.SHORT:
                short s = (val == null) ? 0 : ((Number) val).shortValue();
                fm.storeShortField(field, s);
                break;
            case JavaTypes.STRING:
                fm.storeStringField(field, (String) val);
                break;
            default:
                fm.storeObjectField(field, val);
        }
    }

    /////////////
    // Utilities
    /////////////

    /**
     * Erase the fact that this instance has been flushed.
     */
    void eraseFlush() {
        _flags &= ~FLAG_FLUSHED;
        _flags &= ~FLAG_FLUSHED_DIRTY;

        _flush = null;
    }

    /**
     * Records that all instance fields are/are not loaded.
     * Primary key and non-persistent fields are not affected.
     */
    void setLoaded(boolean val) {
        FieldMetaData[] fmds = _meta.getFields();
        for (int i = 0; i < fmds.length; i++) {
            if (!fmds[i].isPrimaryKey()
                && fmds[i].getManagement() == fmds[i].MANAGE_PERSISTENT)
                setLoaded(i, val);
        }
        if (!val) {
            _flags &= ~FLAG_LOADED;
            setDirty(false);
        } else
            _flags |= FLAG_LOADED;
    }

    /**
     * Records that all instance fields are/are not dirty,
     * and changes the flags of the instance accordingly.
     */
    void setDirty(boolean val) {
        FieldMetaData[] fmds = _meta.getFields();
        boolean update = !isNew() || isFlushed();
        for (int i = 0; i < fmds.length; i++) {
            if (val && (!update || fmds[i].getUpdateStrategy() != UpdateStrategies.IGNORE))
                setFieldDirty(i);
            else if (!val) {
                // we never consider clean fields flushed; this also takes
                // care of clearing the flushed fields on commit/rollback
                clearFlushField(i);
                clearDirty(i);
            }
        }

        if (val)
            _flags |= FLAG_LOADED;
    }

    /**
     * Executes pre-clear callbacks, clears all managed fields, and calls the
     * {@link #setLoaded} method with a value of false. Primary key fields
     * are not cleared.
     */
    void clearFields() {
        if (!isIntercepting())
            return;

        fireLifecycleEvent(LifecycleEvent.BEFORE_CLEAR);

        // unproxy all fields
        unproxyFields();

        lock();
        try {
            // clear non-pk fields
            FieldMetaData[] fmds = _meta.getFields();
            for (int i = 0; i < fmds.length; i++) {
                if (!fmds[i].isPrimaryKey() && fmds[i].getManagement()
                    == FieldMetaData.MANAGE_PERSISTENT)
                    replaceField(_pc, ClearFieldManager.getInstance(), i);
            }

            // forget version info and impl data so we re-read next time
            setLoaded(false);
            _version = null;
            _loadVersion = null;
            if (_fieldImpl != null)
                Arrays.fill(_fieldImpl, null);
        } finally {
            unlock();
        }

        fireLifecycleEvent(LifecycleEvent.AFTER_CLEAR);
    }

    /**
     * Record that we should save any fields that change from this point
     * forward.
     */
    void saveFields(boolean immediate) {
        if (_broker.getRestoreState() == RestoreState.RESTORE_NONE
            && (_flags & FLAG_INVERSES) == 0)
            return;

        _flags |= FLAG_SAVE;
        if (immediate) {
            for (int i = 0, len = _loaded.length(); i < len; i++)
                saveField(i);
            _flags &= ~FLAG_SAVE;
            // OPENJPA-659
            // record a saved field manager even if no field is currently loaded
            // as existence of a SaveFieldManager is critical for a dirty check
            if (_saved == null)
                _saved = new SaveFieldManager(this, getPersistenceCapable(), getDirty());
        }
    }

    /**
     * If the field isn't already saved, saves the currently loaded field
     * state of the instance. The saved values can all be restored via
     * {@link #restoreFields}.
     */
    private void saveField(int field) {
        if ((_flags & FLAG_SAVE) == 0)
            return;

        // if this is a managed inverse field, load it so we're sure to have
        // the original value
        if (!_loaded.get(field) && ((_flags & FLAG_INVERSES) != 0
            && _meta.getField(field).getInverseMetaDatas().length > 0))
            loadField(field, LockLevels.LOCK_NONE, false, false);

        // don't bother creating the save field manager if we're not going to
        // save the old field value anyway
        if (_saved == null) {
            if (_loaded.get(field))
                _saved = new SaveFieldManager(this, null, getDirty());
            else
                return;
        }

        // copy the field to save field manager; if the field is not directly
        // copyable, immediately provide and replace it via the save field
        // manager, which will copy the mutable value to prevent by-ref mods
        if (_saved.saveField(field)) {
            provideField(_pc, _saved, field);
            replaceField(_saved.getState(), _saved, field);
        }
    }

    /**
     * Notification that the state will not need to be rolled back
     * to that of the last call to {@link #saveFields}.
     */
    void clearSavedFields() {
        if (isIntercepting()) {
            _flags &= ~FLAG_SAVE;
            _saved = null;
        }
    }

    public SaveFieldManager getSaveFieldManager() {
        return _saved;
    }

    /**
     * Rollback the state of the instance to the saved state from the
     * last call to {@link #saveFields}, or to default values if never saved.
     */
    void restoreFields() {
        lock();
        try {
            if (_saved == null) {
                if ((_flags & FLAG_SAVE) == 0)
                    clearFields();
                else // only unloaded fields were dirtied
                    _loaded.andNot(_loaded);
            }
            // we direct state transitions based on our own getRestoreState
            // method, but to decide whether to actually rollback field
            // values, we consult the broker for the user's setting
            else if (_broker.getRestoreState() != RestoreState.RESTORE_NONE) {
                // rollback all currently-loaded fields
                for (int i = 0, len = _loaded.length(); i < len; i++)
                    if (_loaded.get(i) && _saved.restoreField(i))
                        replaceField(_pc, _saved, i);

                // rollback loaded set
                _loaded.andNot(_saved.getUnloaded());
            }
        }
        finally {
            unlock();
        }
    }

    /**
     * Replaces all second class object fields with fresh proxied instances
     * containing the same information as the originals.
     * <br>
     * <B>Note:</B> Proxying is bypassed if {@link AutoDetach#DETACH_NONE} option is set.
     */
    void proxyFields(boolean reset, boolean replaceNull) {
    	if (getBroker().getAutoDetach() == AutoDetach.DETACH_NONE)
           return;
        // we only replace nulls if the runtime can't differentiate between
        // null and empty containers.  we replace nulls in this case to
        // maintain consistency whether values are being retained or not
        if (replaceNull)
            replaceNull = !_broker.getConfiguration().supportedOptions().
                contains(OpenJPAConfiguration.OPTION_NULL_CONTAINER);

        lock();
        try {
            for (FieldMetaData fmd : _meta.getProxyFields()) {
                int index = fmd.getIndex();
                // only reload if dirty
                if (_loaded.get(index) && isFieldDirty(index)) {
                    provideField(_pc, _single, index);
                    if (_single.proxy(reset, replaceNull)) {
                        replaceField(_pc, _single, index);
                    } else {
                        _single.clear();
                    }
                }
            }
        } finally {
            unlock();
        }
    }

    /**
     * Unproxy all fields.
     */
    void unproxyFields() {
        if ((_flags & FLAG_NO_UNPROXY) != 0)
            return;

        lock();
        try {
            for (int i = 0, len = _loaded.length(); i < len; i++) {
                provideField(_pc, _single, i);
                _single.unproxy();
                _single.releaseEmbedded();
                _single.clear();
            }
        }
        finally {
            unlock();
        }
    }

    /**
     * Get ready for a flush. Persists all persistence-capable object fields,
     * and checks for illegal null values. Also assigns oids and field values
     * for all strategies that don't require flushing.
     */
    void preFlush(boolean logical, OpCallbacks call) {
        if ((_flags & FLAG_PRE_FLUSHED) != 0)
            return;

        if (isPersistent()) {
            fireLifecycleEvent(LifecycleEvent.BEFORE_STORE);
            // BEFORE_PERSIST is handled during Broker.persist and Broker.attach
            if (isDeleted())
                fireLifecycleEvent(LifecycleEvent.BEFORE_DELETE);
            else if (!(isNew() && !isFlushed())
				&& (ImplHelper.getUpdateFields(this) != null))
                fireLifecycleEvent(LifecycleEvent.BEFORE_UPDATE);
            _flags |= FLAG_PRE_FLUSHED;
        }

        lock();
        try {
            if (!logical)
                assignObjectId(false, true);
            for (int i = 0, len = _meta.getFields().length; i < len; i++) {
                if ((logical || !assignField(i, true)) && !isFieldFlushed(i) && isFieldDirty(i)) {
                    provideField(_pc, _single, i);
                    if (_single.preFlush(logical, call))
                        replaceField(_pc, _single, i);
                    else
                        _single.clear();
                }
            }

            dirtyCheck();
        } finally {
            unlock();
        }
    }

    /**
     * Make callbacks for deletion.
     */
    void preDelete() {
        // set a flag while call pre delete callback so that user can't
        // get into infinite recursion by calling delete(this)
        // within his callback method
        if ((_flags & FLAG_PRE_DELETING) == 0) {
            _flags |= FLAG_PRE_DELETING;
            try {
                fireLifecycleEvent(LifecycleEvent.BEFORE_DELETE);
            } finally {
                _flags &= ~FLAG_PRE_DELETING;
            }
        }
    }

    /**
     * Cascade deletes and dereference dependent fields.
     */
    void cascadeDelete(OpCallbacks call) {
        FieldMetaData[] fmds = _meta.getFields();
        for (int i = 0; i < fmds.length; i++) {
            if (fmds[i].getCascadeDelete() != ValueMetaData.CASCADE_NONE
                || fmds[i].getKey().getCascadeDelete()
                != ValueMetaData.CASCADE_NONE
                || fmds[i].getElement().getCascadeDelete()
                != ValueMetaData.CASCADE_NONE) {
                _single.storeObjectField(i, fetchField(i, false));
                _single.delete(call);
                _single.clear();
            }
        }
    }

    /**
     * Called after an instance is persisted by a user through the broker.
     * Cascades the persist operation to fields marked
     * {@link ValueMetaData#CASCADE_IMMEDIATE}.
     */
    void cascadePersist(OpCallbacks call) {
        FieldMetaData[] fmds = _meta.getFields();
        for (int i = 0; i < fmds.length; i++) {
            if (!_loaded.get(i))
                continue;

            if (fmds[i].getCascadePersist() == ValueMetaData.CASCADE_IMMEDIATE
             || fmds[i].getKey().getCascadePersist() == ValueMetaData.CASCADE_IMMEDIATE
             || fmds[i].getElement().getCascadePersist() == ValueMetaData.CASCADE_IMMEDIATE) {
                _single.storeObjectField(i, fetchField(i, false));
                _single.persist(call);
                _single.clear();
            }
        }
    }

    /**
     * Load the given field set from the data store into the instance.
     * Return true if any data is loaded, false otherwise.
     */
    boolean loadFields(BitSet fields, FetchConfiguration fetch, int lockLevel,
        Object sdata) {
        // can't load version field from store
        if (fields != null) {
            FieldMetaData vfield = _meta.getVersionField();
            if (vfield != null)
                fields.clear(vfield.getIndex());
        }

        boolean ret = false;
        setLoading(true);
        try {
            // if any fields given, load them
            int len = (fields == null) ? 0 : fields.length();
            if (len > 0) {
                if (fetch == null)
                    fetch = _broker.getFetchConfiguration();
                if (!_broker.getStoreManager().load(this, fields, fetch, lockLevel, sdata)) {
                    throw new ObjectNotFoundException(_loc.get("del-instance", _meta.getDescribedType(), _oid)).
                        setFailedObject(getManagedInstance());
                }
                ret = true;
            }

            // make sure version information has been set; version info must
            // always be set after the first state load or set (which is why
            // we do this even if no fields were loaded -- could be that this
            // method is being called after a field is set)
            // If the _loadVersion field is null AND the version field has been loaded, skip calling sync version.
            // This indicates that the DB has a null value for the version column. 
            FieldMetaData versionMeta = _meta != null ? _meta.getVersionField() : null;
            if (_loadVersion == null && (versionMeta != null && !_loaded.get(versionMeta.getIndex()))) {
                syncVersion(sdata);
                ret = ret || _loadVersion != null;
            }
        }
        finally {
            setLoading(false);
        }

        // see if the dfg is now loaded; do this regardless of whether we
        // loaded any fields, cause may already have been loaded by
        // StoreManager during initialization
        postLoad(-1, fetch);
        return ret;
    }

    /**
     * Load the given field's fetch group; the field itself may already be
     * loaded if it is being set by the user.
     */
    protected void loadField(int field, int lockLevel, boolean forWrite,
        boolean fgs) {
        FetchConfiguration fetch = _broker.getFetchConfiguration();
        FieldMetaData fmd = _meta.getField(field);
        BitSet fields = null;
        boolean unloadedDFGFieldMarked = false;

        // if this is a dfg field or we need to load our dfg, do so
        if (fgs && (_flags & FLAG_LOADED) == 0){
            fields = getUnloadedInternal(fetch, LOAD_FGS, null);
            unloadedDFGFieldMarked = true;
        }
        // check for load fetch group
        String lfg = fmd.getLoadFetchGroup();
        boolean lfgAdded = false;
        if (lfg != null) {  
            FieldMetaData[] fmds = _meta.getFields();
            for (int i = 0; i < fmds.length; i++) {
                if (!_loaded.get(i) && (i == field
                    || fmds[i].isInFetchGroup(lfg))) {
                    if (fields == null)
                        fields = new BitSet(fmds.length);
                    fields.set(i);
                }
            }

            // relation field is loaded with the load-fetch-group
            // but this addition must be reverted once the load is over
            if (!fetch.hasFetchGroup(lfg)) {
                fetch.addFetchGroup(lfg);
                lfgAdded = true;
            }
        } else if (fmd.isInDefaultFetchGroup() && fields == null) {
            // no load group but dfg: add dfg fields if we haven't already
            if (!unloadedDFGFieldMarked)
                fields = getUnloadedInternal(fetch, LOAD_FGS, null);
        } else if (!_loaded.get(fmd.getIndex())) {
            // no load group or dfg: load individual field
            if (fields == null)
                fields = new BitSet();
            fields.set(fmd.getIndex());
        }

        // call this method even if there are no unloaded fields; loadFields
        // takes care of things like loading version info and setting PC flags
        try {
            loadFields(fields, fetch, lockLevel, null);
        } finally {
            if (lfgAdded)
                fetch.removeFetchGroup(lfg);
        }
    }

    /**
     * Helper method to provide the given field number to the given
     * field manager.
     */
    void provideField(PersistenceCapable pc, FieldManager store, int field) {
        if (pc != null) {
            FieldManager beforeFM = _fm;
            _fm = store;
            pc.pcProvideField(field);
            // Retaining original FM because of the possibility of reentrant calls
            if (beforeFM != null) _fm = beforeFM;
        }
    }

    /**
     * Helper method to replace the given field number to the given
     * field manager.
     */
    void replaceField(PersistenceCapable pc, FieldManager load, int field) {
        FieldManager beforeFM = _fm;
        _fm = load;
        pc.pcReplaceField(field);
        // Retaining original FM because of the possibility of reentrant calls
        if (beforeFM != null) _fm = beforeFM;
    }

    /**
     * Mark the field as loaded or unloaded.
     */
    private void setLoaded(int field, boolean isLoaded) {
        // don't continue if loaded state is already correct; otherwise we
        // can end up clearing _fieldImpl when we shouldn't
        if (_loaded.get(field) == isLoaded)
            return;

        // if loading, clear intermediate data; if unloading, clear impl data
        if (_fieldImpl != null) {
            int idx = _meta.getExtraFieldDataIndex(field);
            if (idx != -1)
                _fieldImpl[idx] = null;
        }

        if (isLoaded)
            _loaded.set(field);
        else
            _loaded.clear(field);
    }

    /**
     * Set to <code>false</code> to prevent the postLoad method from
     * sending lifecycle callback events.
     */
    public void setPostLoadCallback(boolean enabled) {
        this.postLoadCallback = enabled;
    }

    /**
     * Perform post-load steps, including the post load callback.
     * We have to check the dfg after all field loads because it might be
     * loaded in multiple steps when paging is involved; the initial load
     * might exclude some fields which are then immediately loaded in a
     * separate step before being returned to the user.
     *
     * @param field the field index that was loaded, or -1 to indicate
     * that a group of possibly unknown fields was loaded
     */
    private void postLoad(int field, FetchConfiguration fetch) {
        // no need for postLoad callback?
        if ((_flags & FLAG_LOADED) != 0)
            return;

        // in the middle of a group load, after which this method will be
        // called again?
        if (field != -1 && isLoading())
            return;

        // no listeners?
        LifecycleEventManager mgr = _broker.getLifecycleEventManager();
        if (mgr == null || !mgr.isActive(_meta) || !mgr.hasLoadListeners(getManagedInstance(), _meta))
            return;

        if (fetch == null)
            fetch = _broker.getFetchConfiguration();
        // is this field a post-load field?
        if (field != -1) {
            FieldMetaData fmd = _meta.getField(field);
            if (fmd.isInDefaultFetchGroup() 
                && fetch.hasFetchGroup(FetchGroup.NAME_DEFAULT)
                && postLoad(FetchGroup.NAME_DEFAULT, fetch))
                return;
            String[] fgs = fmd.getCustomFetchGroups();
            for (int i = 0; i < fgs.length; i++)
                if (fetch.hasFetchGroup(fgs[i]) && postLoad(fgs[i], fetch))
                    return;
        } else {
            for (Iterator itr = fetch.getFetchGroups().iterator(); 
                itr.hasNext();) {
                if (postLoad((String) itr.next(), fetch))
                    return;
            }
        }
    }

    /**
     * Perform post-load actions if the given fetch group is a post-load group
     * and is fully loaded.
     */
    private boolean postLoad(String fgName, FetchConfiguration fetch) {
        FetchGroup fg = _meta.getFetchGroup(fgName);
        if (fg == null || !fg.isPostLoad())
            return false;

        FieldMetaData[] fmds = _meta.getFields();
        for (int i = 0; i < fmds.length; i++)
            if (!_loaded.get(i) && fmds[i].isInFetchGroup(fgName))
                return false;

        _flags |= FLAG_LOADED;
        if (postLoadCallback)
            _broker.fireLifecycleEvent(getManagedInstance(), fetch, _meta, LifecycleEvent.AFTER_LOAD);
        return true;
    }

    /**
     * Synchronize our version object with the datastore.
     */
    private boolean syncVersion(Object sdata) {
        return _broker.getStoreManager().syncVersion(this, sdata);
    }

    /**
     * Returns whether this instance needs a version check.
     */
    public boolean isVersionCheckRequired() {
        // explicit flag for version check
        if ((_flags & FLAG_VERSION_CHECK) != 0)
            return true;

        if (!_broker.getOptimistic() && !_broker.getConfiguration().
            getCompatibilityInstance().getNonOptimisticVersionCheck())
            return false;
        return _state.isVersionCheckRequired(this);
    }

    /**
     * Set whether this instance requires a version check on the next flush.
     */
    void setCheckVersion(boolean versionCheck) {
        if (versionCheck)
            _flags |= FLAG_VERSION_CHECK;
        else
            _flags &= ~FLAG_VERSION_CHECK;
    }

    /**
     * Returns whether this instance needs a version update.
     */
    public boolean isVersionUpdateRequired() {
        return (_flags & FLAG_VERSION_UPDATE) > 0;
    }

    /**
     * Set whether this instance requires a version update on the next flush.
     */
    void setUpdateVersion(boolean versionUpdate) {
        if (versionUpdate)
            _flags |= FLAG_VERSION_UPDATE;
        else
            _flags &= ~FLAG_VERSION_UPDATE;
    }

    /**
     * Translate the given exception based on the broker's implicit behavior.
     * Translation only occurs if the exception is initiated by a user action
     * on an instance, and therefore will not be caught and translated by the
     * broker.
     */
    protected RuntimeException translate(RuntimeException re) {
        RuntimeExceptionTranslator trans = _broker.getInstanceExceptionTranslator();
        return (trans == null) ? re : trans.translate(re);
    }

    /**
     * Lock the state manager if the multithreaded option is set.
     */
    protected void lock() {
        if (_instanceLock != null)
        	_instanceLock.lock();
    }

    /**
     * Unlock the state manager.
     */
	protected void unlock () {
        if (_instanceLock != null)
        	_instanceLock.unlock();
	}

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeObject(_broker);
        oos.defaultWriteObject();
        oos.writeObject(_meta.getDescribedType());
        writePC(oos, _pc);
    }

    /**
     * Write <code>pc</code> to <code>oos</code>, handling internal-form
     * serialization. <code>pc</code> must be of the same type that this
     * state manager manages.
     *
     * @since 1.1.0
     */
    void writePC(ObjectOutputStream oos, PersistenceCapable pc)
        throws IOException {
        if (!Serializable.class.isAssignableFrom(_meta.getDescribedType()))
            throw new NotSerializableException(_meta.getDescribedType().getName());

        oos.writeObject(pc);
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        _broker = (BrokerImpl) in.readObject();
        in.defaultReadObject();

        // we need to store the class before the pc instance so that we can
        // create _meta before calling readPC(), which relies on _meta being
        // non-null when reconstituting ReflectingPC instances. Sadly, this
        // penalizes the serialization footprint of non-ReflectingPC SMs also.
        Class managedType = (Class) in.readObject();
        _meta = _broker.getConfiguration().getMetaDataRepositoryInstance()
            .getMetaData(managedType, null, true);

        _pc = readPC(in);
    }

    /**
     * Converts the deserialized <code>o</code> to a {@link PersistenceCapable}
     * instance appropriate for storing in <code>_pc</code>.
     *
     * @since 1.1.0
     */
    PersistenceCapable readPC(ObjectInputStream in)
        throws ClassNotFoundException, IOException {
        Object o = in.readObject();

        if (o == null)
            return null;

        PersistenceCapable pc;
        if (!(o instanceof PersistenceCapable))
            pc = ImplHelper.toPersistenceCapable(o, this);
        else
            pc = (PersistenceCapable) o;

        pc.pcReplaceStateManager(this);
        return pc;
    }
    
    public List<FieldMetaData> getMappedByIdFields() {
        return _mappedByIdFields;
    }
    
    public boolean requiresFetch(FieldMetaData fmd) {
        return (_broker.getFetchConfiguration().requiresFetch(fmd) != FetchConfiguration.FETCH_NONE);
    }
    
    public void setPc(PersistenceCapable pc) {
        _pc = pc;
    }

    public void setBroker(BrokerImpl ctx) {
        _broker = ctx;
    }

    public BitSet getFlushed() {
        if (_flush == null) {
            _flush = new BitSet(_meta.getFields().length);
        }
        return _flush;
    }

    private boolean isFieldFlushed(int index) {
        if (_flush == null) {
            return false;
        }
        return _flush.get(index);
    }

    /**
     * Will clear the bit at the specified if the _flush BetSet has been created.
     */
    private void clearFlushField(int index) {
        if (_flush != null) {
            getFlushed().clear(index);
        }
    }

    public BitSet getDirty() {
        if (_dirty == null) {
            _dirty = new BitSet(_meta.getFields().length);
        }
        return _dirty;
    }

    private boolean isFieldDirty(int index) {
        if (_dirty == null) {
            return false;
        }
        return _dirty.get(index);
    }

    private void setFieldDirty(int index) {
        getDirty().set(index);
    }

    /**
     * Will clear the bit at the specified index if the _dirty BetSet has been created.
     */
    private void clearDirty(int index) {
        if (_dirty != null) {
            getDirty().clear(index);
        }
    }

    public String toString() {
    	return "SM[" + _meta.getDescribedType().getSimpleName() + "]:" + getObjectId(); 
    }

}
