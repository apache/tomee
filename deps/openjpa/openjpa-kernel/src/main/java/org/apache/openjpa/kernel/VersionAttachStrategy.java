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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.Reflection;
import org.apache.openjpa.enhance.StateManager;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.meta.ValueStrategies;
import org.apache.openjpa.util.ApplicationIds;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.util.ObjectNotFoundException;
import org.apache.openjpa.util.OptimisticException;

/**
 * Handles attaching instances using version and primary key fields.
 *
 * @nojavadoc
 * @author Steve Kim
 */
class VersionAttachStrategy
    extends AttachStrategy
    implements DetachState {

    private static final Localizer _loc = Localizer.forPackage
        (VersionAttachStrategy.class);

    protected Object getDetachedObjectId(AttachManager manager,
        Object toAttach) {
        Broker broker = manager.getBroker();
        ClassMetaData meta = broker.getConfiguration().
            getMetaDataRepositoryInstance().getMetaData(
                ImplHelper.getManagedInstance(toAttach).getClass(),
                broker.getClassLoader(), true);
        return ApplicationIds.create(ImplHelper.toPersistenceCapable(toAttach,
            broker.getConfiguration()),
            meta);
    }

    protected void provideField(Object toAttach, StateManagerImpl sm,
        int field) {
        sm.provideField(ImplHelper.toPersistenceCapable(toAttach,
            sm.getContext().getConfiguration()), this, field);
    }

    public Object attach(AttachManager manager, Object toAttach,
        ClassMetaData meta, PersistenceCapable into, OpenJPAStateManager owner,
        ValueMetaData ownerMeta, boolean explicit) {
        BrokerImpl broker = manager.getBroker();
        PersistenceCapable pc = ImplHelper.toPersistenceCapable(toAttach,
            meta.getRepository().getConfiguration());

        boolean embedded = ownerMeta != null && ownerMeta.isEmbeddedPC();
        
        // OJ-2405: If toAttach has a StateManagerImpl, then it is important to check if it
        // is being managed by different broker.  If it is, then it should not be
        // considered "new".
        boolean isNew = !broker.isDetached(pc) && !isManagedByAnotherPCtx(pc, broker);
        Object version = null;
        StateManagerImpl sm;

        // if the state manager for the embedded instance is null, then
        // it should be treated as a new instance (since the
        // newly persisted owner may create a new embedded instance
        // in the constructor); fixed bug #1075.
        // also, if the user has attached a detached obj from somewhere
        // else in the graph to an embedded field that was previously null,
        // copy into a new embedded instance
        if (embedded && (isNew || into == null
            || broker.getStateManager(into) == null)) {
            if (into == null)
                into = pc.pcNewInstance(null, false);
            sm = (StateManagerImpl) broker.embed(into, null, owner, ownerMeta);
            into = sm.getPersistenceCapable();
        } else if (isNew) {
            Object oid = null;
            if (!isPrimaryKeysGenerated(meta))
                oid = ApplicationIds.create(pc, meta);

            sm = persist(manager, pc, meta, oid, explicit);
            into = sm.getPersistenceCapable();
        } else if (!embedded && into == null) {
            Object id = getDetachedObjectId(manager, toAttach);
            if (id != null)
                into =
                    ImplHelper.toPersistenceCapable(broker.find(id, true, null),
                        broker.getConfiguration());
            if (into == null)
                throw new OptimisticException(_loc.get("attach-version-del",
                    ImplHelper.getManagedInstance(pc).getClass(), id, version))
                    .setFailedObject(toAttach);

            sm = manager.assertManaged(into);
            if (meta.getDescribedType()
                != sm.getMetaData().getDescribedType()) {
                throw new ObjectNotFoundException(_loc.get
                    ("attach-wrongclass", id, toAttach.getClass(),
                        sm.getMetaData().getDescribedType())).
                    setFailedObject(toAttach);
            }
        } else
            sm = manager.assertManaged(into);

        // mark that we attached the instance *before* we
        // fill in values to avoid endless recursion
        manager.setAttachedCopy(toAttach, into);

        // if persisting in place, just attach field values
        if (pc == into) {
            attachFieldsInPlace(manager, sm);
            return into;
        }

        if (isNew) {
            broker.fireLifecycleEvent(toAttach, null, meta,
                LifecycleEvent.BEFORE_PERSIST);
        } else {
            // invoke any preAttach on the detached instance
            manager.fireBeforeAttach(toAttach, meta);
        }

        // assign the detached pc the same state manager as the object we're
        // copying into during the attach process
        StateManager smBefore = pc.pcGetStateManager();
        pc.pcReplaceStateManager(sm);
        int detach = (isNew) ? DETACH_ALL : broker.getDetachState();
        FetchConfiguration fetch = broker.getFetchConfiguration();
        try {
            FieldMetaData[] fmds = sm.getMetaData().getFields(); 
            for (int i = 0; i < fmds.length; i++) {
                switch (detach) {
                    case DETACH_ALL:
                        attachField(manager, toAttach, sm, fmds[i], true);
                        break;
                    case DETACH_FETCH_GROUPS:
                        if (fetch.requiresFetch(fmds[i]) 
                            != FetchConfiguration.FETCH_NONE)
                            attachField(manager, toAttach, sm, fmds[i], true);
                        break;
                    case DETACH_LOADED:
                        attachField(manager, toAttach, sm, fmds[i], false);
                        break;
                }
            }
        } finally {
            pc.pcReplaceStateManager(smBefore);
        }
        if (!embedded && !isNew)
            compareVersion(sm, pc);
        return ImplHelper.getManagedInstance(into);
    }

    /**
     * Make sure the version information is correct in the detached object.
     */
    private void compareVersion(StateManagerImpl sm, PersistenceCapable pc) {
        Object version = pc.pcGetVersion();
        // In the event that the version field is a primitive and it is the types default value, we can't differentiate
        // between a value that was set to be the default, and one that defaulted to that value.
        if (version != null 
                && JavaTypes.isPrimitiveDefault(version, sm.getMetaData().getVersionField().getTypeCode())) {
            Field pcVersionInitField = null;
            try {
                pcVersionInitField = pc.getClass().getDeclaredField("pcVersionInit");
                Object pcField = Reflection.get(pc, pcVersionInitField);
                if (pcField != null) {
                    boolean bool = (Boolean) pcField;
                    if (bool == false) {
                        // If this field if false, that means that the pcGetVersion returned a default value rather than
                        // and actual value.
                        version = null;
                    }
                }
            } catch (Exception e) {
                // Perhaps this is an Entity that was enhanced before the pcVersionInit field was added. 
            }
        }
        if (version == null) {
            return;
        }
        // don't need to load unloaded fields since its implicitly
        // a single field value
        StoreManager store = sm.getBroker().getStoreManager();
        switch (store.compareVersion(sm, version, sm.getVersion())) {
            case StoreManager.VERSION_LATER:
                // we have a later version: set it into the object.
                // lock validation will occur at commit time
                sm.setVersion(version);
                break;
            case StoreManager.VERSION_EARLIER:
            case StoreManager.VERSION_DIFFERENT:
                sm.setVersion(version);
                throw new OptimisticException(sm.getManagedInstance());
            case StoreManager.VERSION_SAME:
                // no action required
                break;
        }
    }

    /**
     * Attach the fields of an in-place persisted instance.
     */
    private void attachFieldsInPlace(AttachManager manager,
        StateManagerImpl sm) {
        FieldMetaData[] fmds = sm.getMetaData().getFields();
        for (int i = 0; i < fmds.length; i++) {
            if (fmds[i].getManagement() != FieldMetaData.MANAGE_PERSISTENT)
                continue;

            Object cur, attached;
            switch (fmds[i].getDeclaredTypeCode()) {
                case JavaTypes.PC:
                case JavaTypes.PC_UNTYPED:
                    cur = sm.fetchObjectField(i);
                    attached = attachInPlace(manager, sm, fmds[i], cur);
                    break;
                case JavaTypes.ARRAY:
                    if (!fmds[i].getElement().isDeclaredTypePC())
                        continue;
                    cur = sm.fetchObjectField(i);
                    attached =
                        attachInPlace(manager, sm, fmds[i], (Object[]) cur);
                    break;
                case JavaTypes.COLLECTION:
                    if (!fmds[i].getElement().isDeclaredTypePC())
                        continue;
                    cur = sm.fetchObjectField(i);
                    attached = attachInPlace(manager, sm, fmds[i],
                        (Collection) cur);
                    break;
                case JavaTypes.MAP:
                    if (!fmds[i].getElement().isDeclaredTypePC()
                        && !fmds[i].getKey().isDeclaredTypePC())
                        continue;
                    cur = sm.fetchObjectField(i);
                    attached = attachInPlace(manager, sm, fmds[i], (Map) cur);
                    break;
                default:
                    continue;
            }

            if (cur != attached)
                sm.settingObjectField(sm.getPersistenceCapable(), i,
                    cur, attached, StateManager.SET_REMOTE);
        }
    }

    /**
     * Attach the given pc.
     */
    private Object attachInPlace(AttachManager manager, StateManagerImpl sm,
        ValueMetaData vmd, Object pc) {
        if (pc == null)
            return null;
        Object attached = manager.getAttachedCopy(pc);
        if (attached != null)
            return attached;

        OpenJPAStateManager into = manager.getBroker().getStateManager(pc);
        PersistenceCapable intoPC = (into == null) ? null
            : into.getPersistenceCapable();
        if (vmd.isEmbedded())
            return manager.attach(pc, intoPC, sm, vmd, false);
        return manager.attach(pc, intoPC, null, null, false);
    }

    /**
     * Attach the given array.
     */
    private Object[] attachInPlace(AttachManager manager, StateManagerImpl sm,
        FieldMetaData fmd, Object[] arr) {
        if (arr == null)
            return null;

        for (int i = 0; i < arr.length; i++)
            arr[i] = attachInPlace(manager, sm, fmd.getElement(), arr[i]);
        return arr;
    }

    /**
     * Attach the given collection.
     */
    private Collection attachInPlace(AttachManager manager,
        StateManagerImpl sm, FieldMetaData fmd, Collection coll) {
        if (coll == null || coll.isEmpty())
            return coll;

        // copy if elements embedded or contains detached, which will mean
        // we'll have to copy the existing elements
        Collection copy = null;
        if (fmd.getElement().isEmbedded())
            copy = (Collection) sm.newFieldProxy(fmd.getIndex());
        else {
            for (Iterator itr = coll.iterator(); itr.hasNext();) {
                if (manager.getBroker().isDetached(itr.next())) {
                    copy = (Collection) sm.newFieldProxy(fmd.getIndex());
                    break;
                }
            }
        }

        Object attached;
        for (Iterator itr = coll.iterator(); itr.hasNext();) {
            attached = attachInPlace(manager, sm, fmd.getElement(),
                itr.next());
            if (copy != null)
                copy.add(attached);
        }
        return (copy == null) ? coll : copy;
    }

    /**
     * Attach the given map.
     */
    private Map attachInPlace(AttachManager manager, StateManagerImpl sm,
        FieldMetaData fmd, Map map) {
        if (map == null || map.isEmpty())
            return map;

        Map copy = null;
        Map.Entry entry;
        boolean keyPC = fmd.getKey().isDeclaredTypePC();
        boolean valPC = fmd.getElement().isDeclaredTypePC();

        // copy if embedded pcs or detached pcs, which will require us to
        // copy elements
        if (fmd.getKey().isEmbeddedPC() || fmd.getElement().isEmbeddedPC())
            copy = (Map) sm.newFieldProxy(fmd.getIndex());
        else {
            for (Iterator itr = map.entrySet().iterator(); itr.hasNext();) {
                entry = (Map.Entry) itr.next();
                if ((keyPC && manager.getBroker().isDetached(entry.getKey()))
                    || (valPC && manager.getBroker().isDetached
                    (entry.getValue()))) {
                    copy = (Map) sm.newFieldProxy(fmd.getIndex());
                    break;
                }
            }
        }

        Object key, val;
        for (Iterator itr = map.entrySet().iterator(); itr.hasNext();) {
            entry = (Map.Entry) itr.next();
            key = entry.getKey();
            if (keyPC)
                key = attachInPlace(manager, sm, fmd.getKey(), key);
            val = entry.getValue();
            if (valPC)
                val = attachInPlace(manager, sm, fmd.getElement(), val);
            if (copy != null)
                copy.put(key, val);
        }
        return (copy == null) ? map : copy;
	}

    /**
     * Find a PersistenceCapable instance of an Object if it exists in the
     * database. If the object is null or can't be found in the database.
     *
     * @param pc An object which will be attached into the current context. The
     * object may or may not correspond to a row in the database.
     *
     * @return If the object is null or can't be found in the database this
     * method returns null. Otherwise a PersistenceCapable representation of the
     * object is returned.
     */
    protected PersistenceCapable findFromDatabase(AttachManager manager,
        Object pc) {
        Object oid = manager.getBroker().newObjectId(pc.getClass(),
            manager.getDetachedObjectId(pc));

        if (oid != null) {
            return ImplHelper.toPersistenceCapable(
                manager.getBroker().find(oid, true, null),
                manager.getBroker().getConfiguration());
        } else {
            return null;
        }
    }

    private boolean isPrimaryKeysGenerated(ClassMetaData meta) {
        FieldMetaData[] pks = meta.getPrimaryKeyFields();
        for (int i = 0; i < pks.length; i++) {
            if (pks[i].getValueStrategy() != ValueStrategies.NONE)
                return true;
        }
        return false;
    }
    
    private static boolean isManagedByAnotherPCtx(PersistenceCapable pc, BrokerImpl broker) {        
        StateManager sm = pc.pcGetStateManager();
        if (sm != null && sm instanceof StateManagerImpl) {
            StateManagerImpl smi = (StateManagerImpl) sm;
            Broker associatedBroker = smi.getBroker(); 
            
            if (broker != associatedBroker) {
                return true;
            }
        }
        
        return false;
    }
}
