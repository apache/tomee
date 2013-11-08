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

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.InvalidStateException;

/**
 * Base class for all lifecycle states. Each instance is managed by
 * a {@link OpenJPAStateManager}, which in turn keeps a reference to its current
 * state.
 *  The state governs the behavior of the instance during all lifecycle
 * events. The class itself is stateless, and is provided its context
 * on each method call. This base class provides no-op implementations
 * of all methods and maintains static singelton shared references to each
 * possible state.
 *
 * @author Abe White
 */
@SuppressWarnings("serial")
public class PCState
    implements Serializable {

    /**
     * Persistent-New
     */
    public static final PCState PNEW = new PNewState();

    /**
     * Persistent-Clean
     */
    public static final PCState PCLEAN = new PCleanState();

    /**
     * Persistent-Dirty
     */
    public static final PCState PDIRTY = new PDirtyState();

    /**
     * Persistent-Deleted
     */
    public static final PCState PDELETED = new PDeletedState();

    /**
     * Persistent-New-Deleted
     */
    public static final PCState PNEWDELETED = new PNewDeletedState();

    /**
     * Persistent-New-Provisional
     */
    public static final PCState PNEWPROVISIONAL = new PNewProvisionalState();

    /**
     * Persistent-Nontransactinoal
     */
    public static final PCState PNONTRANS = new PNonTransState();

    /**
     * Persistent-Dirty-Nontransactinoal
     */
    public static final PCState PNONTRANSDIRTY = new PNonTransDirtyState();

    /**
     * Persistent-New-Nontransactional
     */
    public static final PCState PNONTRANSNEW = new PNonTransNewState();

    /**
     * Persistent-Deleted-Nontransactional
     */
    public static final PCState PNONTRANSDELETED = new PNonTransDeletedState();

    /**
     * Hollow; exists in data store
     */
    public static final PCState HOLLOW = new HollowState();

    /**
     * Transient; unmanaged instance
     */
    public static final PCState TRANSIENT = new TransientState();

    /**
     * Transient-Clean
     */
    public static final PCState TCLEAN = new TCleanState();

    /**
     * Transient-Dirty
     */
    public static final PCState TDIRTY = new TDirtyState();

    /**
     * Transient-Loaded
     */
    public static final PCState TLOADED = new TLoadedState();

    /**
     * Embedded-Copy
     */
    public static final PCState ECOPY = new ECopyState();

    /**
     * Embedded-Clean
     */
    public static final PCState ECLEAN = new ECleanState();

    /**
     * Embedded-Dirty
     */
    public static final PCState EDIRTY = new EDirtyState();

    /**
     * Embedded-Deleted
     */
    public static final PCState EDELETED = new EDeletedState();

    /**
     * Embedded-Nontransactional
     */
    public static final PCState ENONTRANS = new ENonTransState();

    /**
     * Persistent-New-Flushed-Deleted
     */
    public static final PCState PNEWFLUSHEDDELETED = new PNewFlushedDeletedState();

    /**
     * Persistent-New-Flushed-Deleted-Flushed
     */
    public static final PCState PNEWFLUSHEDDELETEDFLUSHED = new PNewFlushedDeletedFlushedState();

    /**
     * Persistent-Deleted-Flushed
     */
    public static final PCState PDELETEDFLUSHED = new PDeletedFlushedState();

    private static Localizer _loc = Localizer.forPackage(PCState.class);

    /**
     * Called when this state is first assigned to the given state manager.
     */
    void initialize(StateManagerImpl context, PCState previousState) {
    }

    /**
     * Called before the state is flushed.
     */
    void beforeFlush(StateManagerImpl context, boolean logical,
        OpCallbacks call) {
    }

    /**
     * Perform any actions necessary and return the proper lifecycle
     * state on fush. Returns the <code>this</code> pointer by default.
     */
    PCState flush(StateManagerImpl context) {
        return this;
    }

    /**
     * Perform any actions necesssary and return the proper lifecycle state
     * on transaction commit. Returns the <code>this</code> pointer by default.
     */
    PCState commit(StateManagerImpl context) {
        return this;
    }

    /**
     * Perform any actions necesssary and return the proper lifecycle state
     * on transaction commit with the retainValues flag set.
     * Returns the <code>this</code> pointer by default.
     */
    PCState commitRetain(StateManagerImpl context) {
        return this;
    }

    /**
     * Perform any actions necesssary and return the proper lifecycle state
     * on transaction rollback.
     * Returns the <code>this</code> pointer by default.
     */
    PCState rollback(StateManagerImpl context) {
        return this;
    }

    /**
     * Perform any actions necesssary and return the proper lifecycle state
     * on transaction rollback with the restoreValues flag set.
     * Returns the <code>this</code> pointer by default.
     */
    PCState rollbackRestore(StateManagerImpl context) {
        return this;
    }

    /**
     * Perform any actions necesssary and return the proper lifecycle state
     * on a call to {@link Broker#persist} with the given instance.
     * Returns the <code>this</code> pointer by default. Note: this method
     * is <b>not</b> called for embedded states, and is only called when an
     * existing managed instance is the target of a persist call.
     */
    PCState persist(StateManagerImpl context) {
        return this;
    }

    /**
     * Perform any actions necesssary and return the proper lifecycle state
     * on a call to {@link Broker#delete} with the given instance.
     * Returns the <code>this</code> pointer by default.
     */
    PCState delete(StateManagerImpl context) {
        return this;
    }

    /**
     * Return the state to transition to after making no longer provisional. 
     * Returns the <code>this</code> pointer by default.
     */
    PCState nonprovisional(StateManagerImpl context, boolean logical, 
        OpCallbacks call) {
        return this;
    }

    /**
     * Perform any actions necesssary and return the proper lifecycle state
     * on a call to {@link StoreContext#nontransactional} with the given
     * instance. Returns the <code>this</code> pointer by default.
     */
    PCState nontransactional(StateManagerImpl context) {
        return this;
    }

    /**
     * Perform any actions necesssary and return the proper lifecycle state
     * on a call to {@link StoreContext#nontransactional} with the given
     * instance. Returns the <code>this</code> pointer by default.
     */
    PCState transactional(StateManagerImpl context) {
        return this;
    }

    /**
     * Perform any actions necesssary and return the proper lifecycle state
     * on a call to {@link Broker#makeTransient} with the given instance.
     * Returns the <code>this</code> pointer by default.
     */
    PCState release(StateManagerImpl context) {
        return this;
    }

    /**
     * Perform any actions necesssary and return the proper lifecycle state
     * on a call to {@link Broker#evict} with the given instance.
     * Returns the <code>this</code> pointer by default.
     */
    PCState evict(StateManagerImpl context) {
        return this;
    }

    /**
     * Return the state to transition to after refresh. The context is
     * not given because no actions should be taken.
     */
    PCState afterRefresh() {
        return this;
    }

    /**
     * Return the state to transition to after refresh. The context is
     * not given because no actions should be taken.
     */
    PCState afterOptimisticRefresh() {
        return this;
    }

    /**
     * Return the state to transition to after refresh. The context is
     * not given because no actions should be taken.
     */
    PCState afterNontransactionalRefresh() {
        return this;
    }

    /**
     * Perform any actions necesssary and return the proper lifecycle state
     * prior to the state of the given instance being read within
     * an active transaction. The given field number can be -1 if it is
     * a general object read. Returns the <code>this</code> pointer by default.
     */
    PCState beforeRead(StateManagerImpl context, int field) {
        return this;
    }

    /**
     * Perform any actions necesssary and return the proper lifecycle state
     * prior to the state of the given instance being read outside of
     * an active transaction. The given field number can be -1 if it is
     * a general object read. Returns the <code>this</code> pointer by default.
     */
    PCState beforeNontransactionalRead(StateManagerImpl context, int field) {
        return this;
    }

    /**
     * Perform any actions necesssary and return the proper lifecycle state
     * prior to the state of the given instance being read in an optimistic
     * transaction. The given field number can be -1 if it is
     * a general object read. Returns the <code>this</code> pointer by default.
     */
    PCState beforeOptimisticRead(StateManagerImpl context, int field) {
        return this;
    }

    /**
     * Perform any actions necesssary and return the proper lifecycle state
     * prior to the state of the given instance being written within
     * an active transaction. The mutate parameter tells if it is a
     * direct mutation on an SCO field.
     * Returns the <code>this</code> pointer by default.
     */
    PCState beforeWrite(StateManagerImpl context, int field, boolean mutate) {
        return this;
    }

    /**
     * Perform any actions necesssary and return the proper lifecycle state
     * prior to the state of the given instance being written within
     * an optimistic transaction. The mutate parameter tells if it is a
     * direct mutation on an SCO field.
     * Returns the <code>this</code> pointer by default.
     */
    PCState beforeOptimisticWrite(StateManagerImpl context, int field,
        boolean mutate) {
        return this;
    }

    /**
     * Perform any actions necesssary and return the proper lifecycle state
     * prior to the state of the given instance being written outside of
     * an active transaction. The mutate parameter tells if it is a
     * direct mutation on an SCO field.
     * Returns the <code>this</code> pointer by default.
     */
    PCState beforeNontransactionalWrite(StateManagerImpl context, int field,
        boolean mutate) {
        return this;
    }

    /**
     * Return whether this is a transactional state.
     * Returns <code>false</code> by default.
     */
    boolean isTransactional() {
        return false;
    }

    /**
     * Return whether this is a persistent state.
     * Returns <code>false</code> by default.
     */
    boolean isPersistent() {
        return false;
    }

    /**
     * Return whether this is a new state.
     * Returns <code>false</code> by default.
     */
    boolean isNew() {
        return false;
    }

    /**
     * Return whether this is a deleted state.
     * Returns <code>false</code> by default.
     */
    boolean isDeleted() {
        return false;
    }

    /**
     * Return whether this is a dirty state.
     * Returns <code>false</code> by default.
     */
    boolean isDirty() {
        return false;
    }

    /**
     * Return whether this is a state that will become transactional
     * upon the begin of the next transaction.
     * Returns <code>false</code> by default.
     */
    boolean isPendingTransactional() {
        return false;
    }

    /**
     * Return whether this is a state that will become transient
     * at the end of the next transaction.
     * Returns <code>false</code> by default.
     */
    boolean isProvisional() {
        return false;
    }

    /**
     * Whether this state requires a version check when being flushed, 
     * assuming the system is configured for version checks.
     */
    boolean isVersionCheckRequired(StateManagerImpl context) {
        return false;
    }

    /**
     * Throw an error with a localized message identified by the given key.
     */
    PCState error(String key, StateManagerImpl context) {
        throw new InvalidStateException(_loc.get(key)).
            setFailedObject(context.getManagedInstance());
    }

    protected Object readResolve()
        throws ObjectStreamException {
        if (this instanceof PNewState)
            return PNEW;
        if (this instanceof PCleanState)
            return PCLEAN;
        if (this instanceof PDirtyState)
            return PDIRTY;
        if (this instanceof PDeletedState)
            return PDELETED;
        if (this instanceof PNewDeletedState)
            return PNEWDELETED;
        if (this instanceof PNewProvisionalState)
            return PNEWPROVISIONAL;
        if (this instanceof PNonTransState)
            return PNONTRANS;
        if (this instanceof PNonTransDirtyState)
            return PNONTRANSDIRTY;
        if (this instanceof PNonTransNewState)
            return PNONTRANSNEW;
        if (this instanceof PNonTransDeletedState)
            return PNONTRANSDELETED;
        if (this instanceof HollowState)
            return HOLLOW;
        if (this instanceof TransientState)
            return TRANSIENT;
        if (this instanceof TCleanState)
            return TCLEAN;
        if (this instanceof TDirtyState)
            return TDIRTY;
        if (this instanceof ECopyState)
            return ECOPY;
        if (this instanceof ECleanState)
            return ECLEAN;
        if (this instanceof EDirtyState)
            return EDIRTY;
        if (this instanceof EDeletedState)
            return EDELETED;
        if (this instanceof ENonTransState)
            return ENONTRANS;
        if (this instanceof PNewFlushedDeletedState)
            return PNEWFLUSHEDDELETED;
        if (this instanceof PNewFlushedDeletedFlushedState)
			return PNEWFLUSHEDDELETEDFLUSHED;
		if (this instanceof PDeletedFlushedState)
			return PDELETEDFLUSHED;
		throw new InternalException ();
	}
}

