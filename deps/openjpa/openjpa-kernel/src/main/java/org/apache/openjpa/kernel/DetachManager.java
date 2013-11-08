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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.IdentityMap;
import org.apache.openjpa.conf.Compatibility;
import org.apache.openjpa.conf.DetachOptions;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.event.CallbackModes;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.util.CallbackException;
import org.apache.openjpa.util.ObjectNotFoundException;
import org.apache.openjpa.util.Proxy;
import org.apache.openjpa.util.ProxyManager;
import org.apache.openjpa.util.UserException;

/**
 * Handles detaching instances.
 *
 * @author Marc Prud'hommeaux
 * @nojavadoc
 */
public class DetachManager
    implements DetachState {

    private static Localizer _loc = Localizer.forPackage(DetachManager.class);

    private final BrokerImpl _broker;
    private final boolean _copy;
    private final boolean _full;
    private final ProxyManager _proxy;
    private final DetachOptions _opts;
    private final OpCallbacks _call;
    private final boolean _failFast;
    private boolean _flushed = false;
    private boolean _flushBeforeDetach;
    private boolean _cascadeWithDetach; 
    private boolean _reloadOnDetach;

    // if we're not detaching full, we need to track all detached objects;
    // if we are, then we use a special field manager for more efficient
    // detachment than the standard one
    private final IdentityMap _detached;
    private final DetachFieldManager _fullFM;

    /**
     * Used to prepare a detachable instance that does not externalize
     * detached state.
     */
    static boolean preSerialize(StateManagerImpl sm) {
        if (!sm.isPersistent())
            return false;

        if (sm.getBroker().getConfiguration().getCompatibilityInstance()
                .getFlushBeforeDetach()) {
            flushDirty(sm);
        }

        ClassMetaData meta = sm.getMetaData();
        boolean setState = meta.getDetachedState() != null
            && !ClassMetaData.SYNTHETIC.equals(meta.getDetachedState());
        BitSet idxs = (setState) ? new BitSet(meta.getFields().length) : null;
        preDetach(sm.getBroker(), sm, idxs, false, true);

        if (setState) {
            sm.getPersistenceCapable().pcSetDetachedState(getDetachedState
                (sm, idxs));
            return false; // don't null state
        }
        return true;
    }

    /**
     * Used by classes that externalize detached state.
     *
     * @return whether to use a detached state manager
     */
    static boolean writeDetachedState(StateManagerImpl sm, ObjectOutput out,
        BitSet idxs)
        throws IOException {
        if (!sm.isPersistent()) {
            out.writeObject(null); // state
            out.writeObject(null); // sm
            return false;
        }

        // dirty state causes flush
        flushDirty(sm);

        Broker broker = sm.getBroker();
        preDetach(broker, sm, idxs, false, true);

        // write detached state object and state manager
        DetachOptions opts = broker.getConfiguration().
            getDetachStateInstance();
        if (!opts.getDetachedStateManager()
            || !useDetachedStateManager(sm, opts)) {
            out.writeObject(getDetachedState(sm, idxs));
            out.writeObject(null);
            return false;
        }
        out.writeObject(null);
        out.writeObject(new DetachedStateManager(sm.getPersistenceCapable(),
            sm, idxs, opts.getAccessUnloaded(), broker.getMultithreaded()));
        return true;
    }

    /**
     * Ready the object for detachment, including loading the fields to be
     * detached and updating version information.
     *
     * @param idxs the indexes of fields to detach will be set as a side
     * effect of this method
     */
    private static void preDetach(Broker broker, StateManagerImpl sm,
        BitSet idxs, boolean full,
        boolean reloadOnDetach) {
        // make sure the existing object has the right fields fetched; call
        // even if using currently-loaded fields for detach to make sure
        // version is set
        int detachMode = broker.getDetachState();
        int loadMode = StateManagerImpl.LOAD_FGS;
        BitSet exclude = null;
        if (detachMode == DETACH_LOADED)
            exclude = StoreContext.EXCLUDE_ALL;
        else if (detachMode == DETACH_ALL)
            loadMode = StateManagerImpl.LOAD_ALL;
        try {
            if (detachMode != DETACH_LOADED || 
                    reloadOnDetach ||
                    (!reloadOnDetach && !full)) {
                sm.load(broker.getFetchConfiguration(), loadMode, exclude,
                    null, false);
            }
        } catch (ObjectNotFoundException onfe) {
            // consume the exception
        }

        // create bitset of fields to detach; if mode is all we can use
        // currently loaded bitset clone, since we know all fields are loaded
        if (idxs != null) {
            if (detachMode == DETACH_FETCH_GROUPS)
                setFetchGroupFields(broker, sm, idxs);
            else
                idxs.or(sm.getLoaded());

            // clear lrs fields
            FieldMetaData[] fmds = sm.getMetaData().getFields();
            for (int i = 0; i < fmds.length; i++) 
                if (fmds[i].isLRS())
                    idxs.clear(i);
        }
    }

    /**
     * Generate the detached state for the given instance.
     */
    private static Object getDetachedState(StateManagerImpl sm, BitSet fields) {
        // if datastore, store id in first element
        int offset = (sm.getMetaData().getIdentityType() ==
            ClassMetaData.ID_DATASTORE) ? 1 : 0;

        // make version state array one larger for new instances; marks new
        // instances without affecting serialization size much
        Object[] state;
        if (sm.isNew())
            state = new Object[3 + offset];
        else
            state = new Object[2 + offset];

        if (offset > 0) {
            Object id;
            if (sm.isEmbedded() || sm.getObjectId() == null)
                id = sm.getId();
            else
                id = sm.getObjectId();
            state[0] = id.toString();
        }
        state[offset] = sm.getVersion();
        state[offset + 1] = fields;
        return state;
    }

    /**
     * Flush or invoke pre-store callbacks on the given broker if
     * needed. Return true if flushed/stored, false otherwise.
     */
    private static boolean flushDirty(StateManagerImpl sm) {
        if (!sm.isDirty() || !sm.getBroker().isActive())
            return false;

        // only flush if there are actually any dirty non-flushed fields
        BitSet dirtyFields = sm.getDirty();
        BitSet flushedFields = sm.getFlushed();
        for (int i = 0; i < dirtyFields.size(); i++) {
            if (dirtyFields.get(i) && !flushedFields.get(i)) {
                if (sm.getBroker().getRollbackOnly())
                    sm.getBroker().preFlush();
                else
                    sm.getBroker().flush();
                return true;
            }
        }
        return false;
    }

    /**
     * Create a bit set for the fields in the current fetch groups.
     */
    private static void setFetchGroupFields(Broker broker,
        StateManagerImpl sm, BitSet idxs) {
        FetchConfiguration fetch = broker.getFetchConfiguration();
        FieldMetaData[] fmds = sm.getMetaData().getFields();
        for (int i = 0; i < fmds.length; i++) {
            if (fmds[i].isPrimaryKey() || fetch.requiresFetch(fmds[i])
                != FetchConfiguration.FETCH_NONE)
                idxs.set(i);
        }
    }
    

    /**
     * Constructor.
     *
     * @param broker owning broker
     * @param full whether the entire broker cache is being detached; if
     * this is the case, we assume the broker has already
     * flushed if needed, and that we're detaching in-place
     */
    public DetachManager(BrokerImpl broker, boolean full, OpCallbacks call) {
        _broker = broker;
        _proxy = broker.getConfiguration().getProxyManagerInstance();
        _opts = broker.getConfiguration().getDetachStateInstance();
        _flushed = full;
        _call = call;
        _failFast = (broker.getConfiguration().getMetaDataRepositoryInstance().
            getMetaDataFactory().getDefaults().getCallbackMode()
            & CallbackModes.CALLBACK_FAIL_FAST) != 0;

        // we can only rely on our "full" shortcuts if we know we won't be
        // loading any more data
        _full = full && broker.getDetachState() == DetachState.DETACH_LOADED;
        if (_full) {
            _detached = null;
            _fullFM = new DetachFieldManager();
        } else {
            _detached = new IdentityMap();
            _fullFM = null;
        }
        Compatibility compatibility = 
            broker.getConfiguration().getCompatibilityInstance();
        _flushBeforeDetach = compatibility.getFlushBeforeDetach();
        _reloadOnDetach = compatibility.getReloadOnDetach();
        _cascadeWithDetach = compatibility.getCascadeWithDetach();
        if (full) {
            _copy = false;
        }
        else {
            _copy = compatibility.getCopyOnDetach();;
        }
    }

    /**
     * Return a detached version of the given instance.
     */
    public Object detach(Object toDetach) {
        List exceps = null;
        try {
            return detachInternal(toDetach);
        } catch (CallbackException ce) {
            exceps = new ArrayList(1);
            exceps.add(ce);
            return null; // won't be reached as exception will be rethrown
        } finally {
            if (exceps == null || !_failFast)
                exceps = invokeAfterDetach(Collections.singleton(toDetach),
                    exceps);
            if (_detached != null)
                _detached.clear();
            throwExceptions(exceps);
        }
    }

    /**
     * Return detached versions of all the given instances. If not copying,
     * null will be returned.
     */
    public Object[] detachAll(Collection instances) {
        List exceps = null;
        List detached = null;
        if (_copy)
            detached = new ArrayList(instances.size());

        boolean failFast = false;
        try {
            Object detach;
            for (Iterator itr = instances.iterator(); itr.hasNext();) {
                detach = detachInternal(itr.next());
                if (_copy)
                    detached.add(detach);
            }
        }
        catch (RuntimeException re) {
            if (re instanceof CallbackException && _failFast)
                failFast = true;
            exceps = add(exceps, re);
        } finally {
            if (!failFast)
                exceps = invokeAfterDetach(instances, exceps);
            if (_detached != null)
                _detached.clear();
        }
        throwExceptions(exceps);

        if (_copy)
            return detached.toArray();
        return null;
    }

    /**
     * Invoke postDetach() on any detached instances that implement
     * PostDetachCallback. This will be done after the entire graph has
     * been detached. This method has the side-effect of also clearing
     * out the map of all detached instances.
     */
    private List invokeAfterDetach(Collection objs, List exceps) {
        Iterator itr = (_full) ? objs.iterator()
            : _detached.entrySet().iterator();

        Object orig, detached;
        Map.Entry entry;
        while (itr.hasNext()) {
            if (_full) {
                orig = itr.next();
                detached = orig;
            } else {
                entry = (Map.Entry) itr.next();
                orig = entry.getKey();
                detached = entry.getValue();
            }

            StateManagerImpl sm = _broker.getStateManagerImpl(orig, true);
            try {
                if (sm != null)
                    _broker.fireLifecycleEvent(detached, orig,
                        sm.getMetaData(), LifecycleEvent.AFTER_DETACH);
            } catch (CallbackException ce) {
                exceps = add(exceps, ce);
                if (_failFast)
                    break; // don't continue processing
            }
        }
        return exceps;
    }

    /**
     * Add an exception to the list.
     */
    private List add(List exceps, RuntimeException re) {
        if (exceps == null)
            exceps = new LinkedList();
        exceps.add(re);
        return exceps;
    }

    /**
     * Throw all gathered exceptions.
     */
    private void throwExceptions(List exceps) {
        if (exceps == null)
            return;

        if (exceps.size() == 1)
            throw (RuntimeException) exceps.get(0);
        throw new UserException(_loc.get("nested-exceps")).
            setNestedThrowables((Throwable[]) exceps.toArray
                (new Throwable[exceps.size()]));
    }

    /**
     * Detach.
     */
    private Object detachInternal(Object toDetach) {
        if (toDetach == null)
            return null;

        // already detached?
        if (_detached != null) {
            Object detached = _detached.get(toDetach);
            if (detached != null)
                return detached;
        }

        StateManagerImpl sm = _broker.getStateManagerImpl(toDetach, true);
        if (_call != null && (_call.processArgument(OpCallbacks.OP_DETACH,
            toDetach, sm) & OpCallbacks.ACT_RUN) == 0)
            return toDetach;
        if (sm == null)
            return toDetach;

        // Call PreDetach first as we can't tell if the new system
        // fired an event or just did not fail.
        _broker.fireLifecycleEvent(toDetach, null, sm.getMetaData(),
            LifecycleEvent.BEFORE_DETACH);

        if(! _flushed)  {
            if(_flushBeforeDetach) {
                // any dirty instances cause a flush to occur
                flushDirty(sm);
            }
            _flushed = true;
        }
        
        BitSet fields = new BitSet();
        preDetach(_broker, sm, fields, _full,
            _reloadOnDetach);

        // create and store new object before copy to avoid endless recursion
        PersistenceCapable pc = sm.getPersistenceCapable();
        PersistenceCapable detachedPC;
        if (_copy)
            detachedPC = pc.pcNewInstance(null, true);
        else
            detachedPC = pc;
        if (_detached != null)
            _detached.put(toDetach, detachedPC);

        // detach fields and set detached variables
        DetachedStateManager detSM = null;
        if (_opts.getDetachedStateManager()
            && useDetachedStateManager(sm, _opts)
            && !(sm.isNew() && !sm.isDeleted() && !sm.isFlushed()))
            detSM = new DetachedStateManager(detachedPC, sm, fields,
                _opts.getAccessUnloaded(), _broker.getMultithreaded());
        if (_full) {
            _fullFM.setStateManager(sm);
            if (_copy || _reloadOnDetach) {
                _fullFM.detachVersion();
            }
            _fullFM.reproxy(detSM);
            _fullFM.setStateManager(null);
        } else {
            InstanceDetachFieldManager fm = new InstanceDetachFieldManager(detachedPC, detSM);
            fm.setStateManager(sm);
            fm.detachFields(fields);
        }

        if (!Boolean.FALSE.equals(sm.getMetaData().usesDetachedState()))
            detachedPC.pcSetDetachedState(getDetachedState(sm, fields));
        if (!_copy)
            sm.release(false, true);
        if (detSM != null)
            detachedPC.pcReplaceStateManager(detSM);
        return detachedPC;
    }

    private static boolean useDetachedStateManager(StateManagerImpl sm,
        DetachOptions opts) {
        ClassMetaData meta = sm.getMetaData();
        return !Boolean.FALSE.equals(meta.usesDetachedState()) &&
            ClassMetaData.SYNTHETIC.equals(meta.getDetachedState()) &&
            opts.getDetachedStateManager();
    }

    /**
     * Base detach field manager.
     */
    private static class DetachFieldManager
        extends TransferFieldManager {

        protected StateManagerImpl sm;

        /**
         * Set the source state manager.
         */
        public void setStateManager(StateManagerImpl sm) {
            this.sm = sm;
        }

        /**
         * Transfer the current version object from the state manager to the
         * detached instance.
         */
        public void detachVersion() {
            FieldMetaData fmd = sm.getMetaData().getVersionField();
            if (fmd == null)
                return;

            Object val = JavaTypes.convert(sm.getVersion(),
                fmd.getTypeCode());
            val = fmd.getFieldValue(val, sm.getBroker());
            switch (fmd.getDeclaredTypeCode()) {
            case JavaTypes.LONG:
            case JavaTypes.SHORT:
            case JavaTypes.INT:
            case JavaTypes.BYTE:
                longval = (val == null) ? 0L : ((Number) val).longValue();
                break;
            case JavaTypes.DOUBLE:
            case JavaTypes.FLOAT:
                dblval = (val == null) ? 0D : ((Number) val).doubleValue();
                break;
            default:
                objval = val;
            }
            sm.replaceField(getDetachedPersistenceCapable(), this,
                fmd.getIndex());
        }

        /**
         * Unproxies second class object fields.
         */
        public void reproxy(DetachedStateManager dsm) {
            for (FieldMetaData fmd : sm.getMetaData().getProxyFields()) {
                switch (fmd.getDeclaredTypeCode()) {
                case JavaTypes.COLLECTION:
                case JavaTypes.MAP:
                    // lrs proxies not detached
                    if (fmd.isLRS()) {
                        objval = null;
                        sm.replaceField(getDetachedPersistenceCapable(), this, fmd.getIndex());
                        break;
                    }
                    // no break
                case JavaTypes.CALENDAR:
                case JavaTypes.DATE:
                case JavaTypes.OBJECT:
                    sm.provideField(getDetachedPersistenceCapable(), this, fmd.getIndex());
                    if (objval instanceof Proxy) {
                        Proxy proxy = (Proxy) objval;
                        if (proxy.getChangeTracker() != null)
                            proxy.getChangeTracker().stopTracking();
                        proxy.setOwner(dsm, (dsm == null) ? -1 : fmd.getIndex());
                    }
                }
            }
            clear();
        }

        /**
         * Return the instance being detached.
         */
        protected PersistenceCapable getDetachedPersistenceCapable() {
            return sm.getPersistenceCapable();
        }
    }

    /**
     * FieldManager that can copy all the fields from one
     * PersistenceCapable instance to another. One of the
     * instances must be managed by a StateManager, and the
     * other must be unmanaged.
     *
     * @author Marc Prud'hommeaux
     */
    private class InstanceDetachFieldManager
        extends DetachFieldManager {

        private final PersistenceCapable _to;
        private final DetachedStateManager _detSM;

        /**
         * Constructor. Supply instance to to copy to.
         */
        public InstanceDetachFieldManager(PersistenceCapable to,
            DetachedStateManager detSM) {
            _to = to;
            _detSM = detSM;
        }

        protected PersistenceCapable getDetachedPersistenceCapable() {
            return _to;
        }

        /**
         * Detach the fields of the state manager given on construction to
         * the persistence capable given on construction.
         * Only the fields in the given bit set will be copied.
         */
        public void detachFields(BitSet fgfields) {
            PersistenceCapable from = sm.getPersistenceCapable();
            FieldMetaData[] pks = sm.getMetaData().getPrimaryKeyFields();
            FieldMetaData[] fmds = sm.getMetaData().getFields();

            if (_copy)
                _to.pcReplaceStateManager(sm);
            try {
                // we start with pk fields: objects might rely on pk fields for
                // equals and hashCode methods, and this ensures that pk fields
                // are set properly if we return any partially-detached objects
                // due to reentrant calls when traversing relations
                for (int i = 0; i < pks.length; i++)
                    detachField(from, pks[i].getIndex(), true);
                detachVersion();
                for (int i = 0; i < fmds.length; i++)
                    if (!fmds[i].isPrimaryKey() && !fmds[i].isVersion()) 
                        detachField(from, i, fgfields.get(i));
            } finally {
                // clear the StateManager from the target object
                if (_copy)
                    _to.pcReplaceStateManager(null);
            }
        }

        /**
         * Detach (or clear) the given field index.
         */
        private void detachField(PersistenceCapable from, int i, boolean fg) {
            // tell the state manager to provide the fields from the source to
            // this field manager, which will then replace the field with a
            // detached version
            if (fg)
                sm.provideField(from, this, i);
            else if (!_copy) {
                // if not copying and field should not be detached, clear it
                clear();
                sm.replaceField(_to, this, i);
            }
        }

        public void storeBooleanField(int field, boolean curVal) {
            super.storeBooleanField(field, curVal);
            sm.replaceField(_to, this, field);
        }

        public void storeByteField(int field, byte curVal) {
            super.storeByteField(field, curVal);
            sm.replaceField(_to, this, field);
        }

        public void storeCharField(int field, char curVal) {
            super.storeCharField(field, curVal);
            sm.replaceField(_to, this, field);
        }

        public void storeDoubleField(int field, double curVal) {
            super.storeDoubleField(field, curVal);
            sm.replaceField(_to, this, field);
        }

        public void storeFloatField(int field, float curVal) {
            super.storeFloatField(field, curVal);
            sm.replaceField(_to, this, field);
        }

        public void storeIntField(int field, int curVal) {
            super.storeIntField(field, curVal);
            sm.replaceField(_to, this, field);
        }

        public void storeLongField(int field, long curVal) {
            super.storeLongField(field, curVal);
            sm.replaceField(_to, this, field);
        }

        public void storeShortField(int field, short curVal) {
            super.storeShortField(field, curVal);
            sm.replaceField(_to, this, field);
        }

        public void storeStringField(int field, String curVal) {
            super.storeStringField(field, curVal);
            sm.replaceField(_to, this, field);
        }

        public void storeObjectField(int field, Object curVal) {
            super.storeObjectField(field, detachField(curVal, field));
            sm.replaceField(_to, this, field);
        }

        /**
         * Set the owner of the field's proxy to the detached state manager.
         */
        private Object reproxy(Object obj, int field) {
            if (obj != null && _detSM != null && obj instanceof Proxy)
                ((Proxy) obj).setOwner(_detSM, field);
            return obj;
        }

        /**
         * Detach the given value if needed.
         */
        private Object detachField(Object curVal, int field) {
            if (curVal == null)
                return null;

            FieldMetaData fmd = sm.getMetaData().getField(field);
            
            boolean cascade = false;
            if(_cascadeWithDetach
                || fmd.getCascadeDetach() == 
                    ValueMetaData.CASCADE_IMMEDIATE
                || fmd.getKey().getCascadeDetach() == 
                    ValueMetaData.CASCADE_IMMEDIATE 
                || fmd.getElement().getCascadeDetach() == 
                    ValueMetaData.CASCADE_IMMEDIATE) {
                cascade = true;
            }
            
            Object newVal = null;
            switch (fmd.getDeclaredTypeCode()) {
            case JavaTypes.ARRAY:
                if (_copy)
                    newVal = _proxy.copyArray(curVal);
                else
                    newVal = curVal;
                if (cascade) {
                    detachArray(newVal, fmd);
                }
                return newVal;
            case JavaTypes.COLLECTION:
                if (_copy) {
                    if (_detSM != null) {
                        newVal = _proxy.newCollectionProxy(fmd.getProxyType(),
                            fmd.getElement().getDeclaredType(),
                            fmd.getInitializer() instanceof Comparator ?
                            (Comparator) fmd.getInitializer() : null,
                            sm.getBroker().getConfiguration().
                            getCompatibilityInstance().getAutoOff());
                        ((Collection) newVal).addAll((Collection) curVal);
                    } else
                        newVal = _proxy.copyCollection((Collection) curVal);
                } else
                    newVal = curVal;
                if (cascade) {
                    detachCollection((Collection) newVal, (Collection) curVal,
                        fmd);
                }
                return reproxy(newVal, field);
            case JavaTypes.MAP:
                if (_copy) {
                    if (_detSM != null) {
                        newVal = _proxy.newMapProxy(fmd.getProxyType(),
                            fmd.getKey().getDeclaredType(),
                            fmd.getElement().getDeclaredType(),
                            fmd.getInitializer() instanceof Comparator ?
                                (Comparator) fmd.getInitializer() : null,
                                sm.getBroker().getConfiguration().
                                getCompatibilityInstance().getAutoOff());
                        ((Map) newVal).putAll((Map) curVal);
                    } else
                        newVal = _proxy.copyMap((Map) curVal);
                } else
                    newVal = curVal;
                if (cascade) {
                    detachMap((Map) newVal, (Map) curVal, fmd);
                }
                return reproxy(newVal, field);
            case JavaTypes.CALENDAR:
                newVal = (_copy) ? _proxy.copyCalendar((Calendar) curVal) :
                    curVal;
                return reproxy(newVal, field);
            case JavaTypes.DATE:
                newVal = (_copy) ? _proxy.copyDate((Date) curVal) : curVal;
                return reproxy(newVal, field);
            case JavaTypes.OBJECT:
                if (_copy)
                    newVal = _proxy.copyCustom(curVal);
                return reproxy((newVal == null) ? curVal : newVal, field);
            case JavaTypes.PC:
            case JavaTypes.PC_UNTYPED:
                if (cascade) {
                    return detachInternal(curVal);
                }
                return curVal;
            default:
                return curVal;
            }
        }

        /**
         * Make sure all the values in the given array are detached.
         */
        private void detachArray(Object array, FieldMetaData fmd) {
            if (!fmd.getElement().isDeclaredTypePC())
                return;

            int len = Array.getLength(array);
            for (int i = 0; i < len; i++)
                Array.set(array, i, detachInternal(Array.get(array, i)));
        }

        /**
         * Make sure all the values in the given collection are detached.
         */
        private void detachCollection(Collection coll, Collection orig,
            FieldMetaData fmd) {
            // coll can be null if not copyable
            if (_copy && coll == null)
                throw new UserException(_loc.get("not-copyable", fmd));
            if (!fmd.getElement().isDeclaredTypePC())
                return;

            // unfortunately we have to clear the original and re-add to copy
            if (_copy)
                coll.clear();
            Object detached;
            for (Iterator itr = orig.iterator(); itr.hasNext();) {
                detached = detachInternal(itr.next());
                if (_copy)
                    coll.add(detached);
            }
        }

        /**
         * Make sure all the values in the given map are detached.
         */
        private void detachMap(Map map, Map orig, FieldMetaData fmd) {
            // map can be null if not copyable
            if (_copy && map == null)
                throw new UserException(_loc.get("not-copyable", fmd));
            boolean keyPC = fmd.getKey().isDeclaredTypePC();
            boolean valPC = fmd.getElement().isDeclaredTypePC();
            if (!keyPC && !valPC)
                return;

            // if we have to copy keys, just clear and re-add; otherwise
            // we can use the entry set to reset the values only
            Map.Entry entry;
            if (!_copy || keyPC) {
                if (_copy)
                    map.clear();
                Object key, val;
                for (Iterator itr = orig.entrySet().iterator(); itr.hasNext();){
                    entry = (Map.Entry) itr.next();
                    key = entry.getKey();
                    if (keyPC)
                        key = detachInternal(key);
                    val = entry.getValue();
                    if (valPC)
                        val = detachInternal(val);
                    if (_copy)
                        map.put(key, val);
                }
            } else {
                for (Iterator itr = map.entrySet().iterator(); itr.hasNext();) {
                    entry = (Map.Entry) itr.next ();
                    entry.setValue (detachInternal (entry.getValue ()));
				}
			}
		}
	}
}
