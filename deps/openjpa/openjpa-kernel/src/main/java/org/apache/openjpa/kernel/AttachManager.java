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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.IdentityMap;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.event.CallbackModes;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.util.CallbackException;
import org.apache.openjpa.util.Exceptions;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.util.OpenJPAException;
import org.apache.openjpa.util.OptimisticException;
import org.apache.openjpa.util.ProxyManager;
import org.apache.openjpa.util.UserException;

/**
 * Handles attaching instances.
 *
 * @author Marc Prud'hommeaux
 */
public class AttachManager {

    private static final Localizer _loc = Localizer.forPackage
        (AttachManager.class);

    private final BrokerImpl _broker;
    private final ProxyManager _proxy;
    private final OpCallbacks _call;
    private final boolean _copyNew;
    private final boolean _failFast;
    private final IdentityMap _attached = new IdentityMap();
    private final Collection<StateManagerImpl> _visitedNodes = new ArrayList();

    // reusable strategies
    private AttachStrategy _version;
    private AttachStrategy _detach;

    /**
     * Constructor. Supply broker attaching to.
     */
    public AttachManager(BrokerImpl broker, boolean copyNew, OpCallbacks call) {
        _broker = broker;
        _proxy = broker.getConfiguration().getProxyManagerInstance();
        _call = call;
        _copyNew = copyNew;
        _failFast = (broker.getConfiguration().getMetaDataRepositoryInstance().
            getMetaDataFactory().getDefaults().getCallbackMode()
            & CallbackModes.CALLBACK_FAIL_FAST) != 0;
    }

    /**
     * Return the behavior supplied on construction.
     */
    public OpCallbacks getBehavior() {
        return _call;
    }

    /**
     * Return whether to copy new instances being persisted.
     */
    public boolean getCopyNew() {
        return _copyNew;
    }

    /**
     * Return an attached version of the given instance.
     */
    public Object attach(Object pc) {
        if (pc == null)
            return null;

        CallbackException excep = null;
        try {
            return attach(pc, null, null, null, true);
        } catch (CallbackException ce) {
            excep = ce;
            return null; // won't be reached as the exceps will be rethrown
        } finally {
            List exceps = null;
            if (excep == null || !_failFast)
                exceps = invokeAfterAttach(null);
            else
                exceps = Collections.singletonList(excep);
            _attached.clear();
            throwExceptions(exceps, null, false);
        }
    }

    /**
     * Return attached versions of the given instances.
     */
    public Object[] attachAll(Collection instances) {
        Object[] attached = new Object[instances.size()];
        List exceps = null;
        List failed = null;
        boolean opt = true;
        boolean failFast = false;
        try {
            int i = 0;
            for (Iterator itr = instances.iterator(); itr.hasNext(); i++) {
                try {
                    attached[i] = attach(itr.next(), null, null, null, true);
                } catch (OpenJPAException ke) {
                    // track exceptions and optimistic failed objects
                    if (opt && !(ke instanceof OptimisticException))
                        opt = false;
                    if (opt && ke.getFailedObject() != null)
                        failed = add(failed, ke.getFailedObject());
                    exceps = add(exceps, ke);

                    if (ke instanceof CallbackException && _failFast) {
                        failFast = true;
                        break;
                    }
                }
                catch (RuntimeException re) {
                    exceps = add(exceps, re);
                }
            }
        } finally {
            // invoke post callbacks unless all failed
            if (!failFast && (exceps == null
                || exceps.size() < instances.size()))
                exceps = invokeAfterAttach(exceps);
            _attached.clear();
        }
        throwExceptions(exceps, failed, opt);
        return attached;
    }

    /**
     * Invoke postAttach() on any attached instances that implement
     * PostAttachCallback. This will be done after the entire graph has
     * been attached.
     */
    private List invokeAfterAttach(List exceps) {
        Set entries = _attached.entrySet();
        for (Iterator i = entries.iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            Object attached = entry.getValue();
            StateManagerImpl sm = _broker.getStateManagerImpl(attached, true);
            if (sm.isNew())
                continue;
            try {
                _broker.fireLifecycleEvent(attached, entry.getKey(),
                    sm.getMetaData(), LifecycleEvent.AFTER_ATTACH);
            } catch (RuntimeException re) {
                exceps = add(exceps, re);
                if (_failFast && re instanceof CallbackException)
                    break;
            }
        }
        return exceps;
    }

    /**
     * Add an object to the list.
     */
    private List add(List list, Object obj) {
        if (list == null)
            list = new LinkedList();
        list.add(obj);
        return list;
    }

    /**
     * Throw exception for failures.
     */
    private void throwExceptions(List exceps, List failed, boolean opt) {
        if (exceps == null)
            return;
        if (exceps.size() == 1)
            throw (RuntimeException) exceps.get(0);

        Throwable[] t = (Throwable[]) exceps.toArray
            (new Throwable[exceps.size()]);
        if (opt && failed != null)
            throw new OptimisticException(failed, t);
        if (opt)
            throw new OptimisticException(t);
        throw new UserException(_loc.get("nested-exceps")).
            setNestedThrowables(t);
    }

    /**
     * Attach.
     *
     * @param toAttach the detached object
     * @param into the instance we're attaching into
     * @param owner state manager for <code>into</code>
     * @param ownerMeta the field we traversed to find <code>toAttach</code>
     * @param explicit whether to make new instances explicitly persistent
     */
    Object attach(Object toAttach, PersistenceCapable into,
        OpenJPAStateManager owner, ValueMetaData ownerMeta, boolean explicit) {
        if (toAttach == null)
            return null;

        // check if already attached
        Object attached = _attached.get(toAttach);
        if (attached != null)
            return attached;

        //### need to handle ACT_CASCADE
        int action = processArgument(toAttach);
        if ((action & OpCallbacks.ACT_RUN) == 0 &&
            (action & OpCallbacks.ACT_CASCADE) != 0) {
            if(_visitedNodes.contains(_broker.getStateManager(toAttach)))
                return toAttach;
            return handleCascade(toAttach,owner);
        }

        if ((action & OpCallbacks.ACT_RUN) == 0)
            return toAttach;

        //### need to handle ACT_RUN without also ACT_CASCADE
        ClassMetaData meta = _broker.getConfiguration().
            getMetaDataRepositoryInstance().getMetaData(
                ImplHelper.getManagedInstance(toAttach).getClass(),
                _broker.getClassLoader(), true);
        return getStrategy(toAttach).attach(this, toAttach, meta, into,
            owner, ownerMeta, explicit);
    }

    private Object handleCascade(Object toAttach, OpenJPAStateManager owner) {
        StateManagerImpl sm = _broker.getStateManagerImpl(toAttach, true);
        BitSet loaded = sm.getLoaded();
        FieldMetaData[] fmds = sm.getMetaData().getDefinedFields();
        for (FieldMetaData fmd : fmds) {
            if (fmd.getElement().getCascadeAttach() == ValueMetaData.CASCADE_IMMEDIATE) {
                FieldMetaData[] inverseFieldMappings = fmd.getInverseMetaDatas();
                if (inverseFieldMappings.length != 0) {
                    _visitedNodes.add(sm);
                    // Only try to attach this field is it is loaded
                    if (loaded.get(fmd.getIndex())) {
                        getStrategy(toAttach).attachField(this, toAttach, sm, fmd, true);
                    }
                }
            }
        }
        return toAttach;
    }
    
    /**
     * Determine the action to take on the given argument.
     */
    private int processArgument(Object obj) {
        if (_call == null)
            return OpCallbacks.ACT_RUN;
        return _call.processArgument(OpCallbacks.OP_ATTACH, obj,
            _broker.getStateManager(obj));
    }

    /**
     * Calculate proper attach strategy for instance.
     */
    private AttachStrategy getStrategy(Object toAttach) {
        PersistenceCapable pc = ImplHelper.toPersistenceCapable(toAttach,
            getBroker().getConfiguration());
        if (pc.pcGetStateManager() instanceof AttachStrategy)
            return (AttachStrategy) pc.pcGetStateManager();

        Object obj = pc.pcGetDetachedState();
        if (obj instanceof AttachStrategy)
            return (AttachStrategy) obj;
        if (obj == null || obj == PersistenceCapable.DESERIALIZED) {
            // new or detached without state
            if (_version == null)
                _version = new VersionAttachStrategy();
            return _version;
        }

        // detached state
        if (_detach == null)
            _detach = new DetachedStateAttachStrategy();
        return _detach;
    }

    /**
     * Owning broker.
     */
    BrokerImpl getBroker() {
        return _broker;
    }

    /**
     * System proxy manager.
     */
    ProxyManager getProxyManager() {
        return _proxy;
    }

    /**
     * If the passed in argument has already been attached, return
     * the (cached) attached copy.
     */
    PersistenceCapable getAttachedCopy(Object pc) {
        return ImplHelper.toPersistenceCapable(_attached.get(pc),
            getBroker().getConfiguration());
    }

    /**
     * Record the attached copy in the cache.
     */
    void setAttachedCopy(Object from, PersistenceCapable into) {
        _attached.put(from, into);
    }

    /**
     * Fire before-attach event.
     */
    void fireBeforeAttach(Object pc, ClassMetaData meta) {
        _broker.fireLifecycleEvent(pc, null, meta,
            LifecycleEvent.BEFORE_ATTACH);
    }

    /**
     * Return the detached oid of the given instance.
     */
    Object getDetachedObjectId(Object pc) {
        if (pc == null)
            return null;
        return getStrategy(pc).getDetachedObjectId(this, pc);
    }

    /**
     * Throw an exception if the given object is not managed; otherwise
     * return its state manager.
     */
    StateManagerImpl assertManaged(Object obj) {
        StateManagerImpl sm = _broker.getStateManagerImpl(obj, true);
        if (sm == null)
            throw new UserException(_loc.get("not-managed",
                Exceptions.toString(obj))).setFailedObject (obj);
		return sm;
	}
}
