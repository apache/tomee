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

import java.util.BitSet;

import org.apache.openjpa.enhance.FieldManager;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.StateManager;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.util.Proxy;

/**
 * Interface implemented by OpenJPA state managers. Each state manager
 * manages the state of a single persistence capable instance. The
 * state manager is also responsible for all communications about the
 * instance to the {@link StoreManager}.
 *  The <code>fetchXXXField</code> and <code>storeXXXField</code>
 * methods can be used to get and set fields of the managed persistent object.
 * Most back-end code, however, should use the similar
 * <code>fetchXXX</code> and <code>storeXXX</code> methods in place of
 * the field methods. These methods function just like the field methods, but
 * also pass the value through the externalizer and factory the field may have.
 *
 * @author Abe White
 */
public interface OpenJPAStateManager
    extends StateManager, FieldManager {

    /**
     * A user is setting the field.
     */
    public static final int SET_USER = 0;

    /**
     * The remote broker framework is setting the field on the server.
     */
    public static final int SET_REMOTE = 1;

    /**
     * The field is being attached by a detached state manager; the value is
     * from the detached instance.
     */
    public static final int SET_ATTACH = 2;

    /**
     * Initialize the state manager with a new instance of the given
     * persistence capable type and the proper lifecycle state.
     * Invoking this method may change the object id and metadata for the
     * state manager, as the concrete type specified in the parameter may be
     * a subclass of the expected type.
     *
     * @param forType the type for which to create a new instance
     * @param state the initial state to which to set the instance
     * @since 0.3.1.2
     */
    public void initialize(Class forType, PCState state);

    /**
     * Load fetch group fields.
     */
    public void load(FetchConfiguration fetch);

    /**
     * Return the managed instance.
     */
    public Object getManagedInstance();

    /**
     * Return the {@link PersistenceCapable} instance that provides access to
     * the instance managed by this state manager. May be a proxy around
     * the actual managed instance.
     */
    public PersistenceCapable getPersistenceCapable();

    /**
     * Get the metadata for this instance.
     */
    public ClassMetaData getMetaData();

    /**
     * Return the owning state if this is an embedded instance.
     */
    public OpenJPAStateManager getOwner();

    /**
     * Return the owning value's field index
     *
     * @since 1.1.0
     */
    public int getOwnerIndex();

    /**
     * Return true if this instance has an owner, meaning it is an embedded
     * value.
     */
    public boolean isEmbedded();

    /**
     * Return whether this object has been flushed to the datastore in this
     * transaction.
     */
    public boolean isFlushed();

    /**
     * Return whether this object has been flushed, then dirtied again.
     */
    public boolean isFlushedDirty();

    /**
     * Return whether this object is provisionally persistent.
     */
    public boolean isProvisional();

    /**
     * Return a read-only mask of the indexes of all loaded fields.
     */
    public BitSet getLoaded();

    /**
     * Return a read-only mask of the indexes of all dirty fields.
     */
    public BitSet getDirty();

    /**
     * Return a read-only mask of the indexes of all fields that have been
     * flushed since they were last changed.
     */
    public BitSet getFlushed();

    /**
     * Return a mutable mask of the unloaded fields that need loading based
     * on the given fetch configuration. Pass in null to retrieve all
     * unloaded fields.
     */
    public BitSet getUnloaded(FetchConfiguration fetch);

    /**
     * Create a new hollow proxy instance for the given field. In cases where
     * the field externalizes to an SCO but is declared something else, the
     * returned object may not implement {@link Proxy}. In all other cases,
     * this method delegates to the system 
     * {@link org.apache.openjpa.util.ProxyManager}
     * with the correct field information. The returned proxy's owner is
     * unset so that modifications to the proxy will not be tracked while its
     * state is initialized. Calling {@link #storeField} or {@link #store}
     * will set the proxy's owner automatically.
     */
    public Object newProxy(int field);

    /**
     * Create a new hollow proxy instance for the given field. This method
     * differs from {@link #newProxy} in that it returns a proxy for the
     * field's declared type, not its externalized type.
     *
     * @see #newProxy
     */
    public Object newFieldProxy(int field);

    /**
     * Return true if the given field has a default value.
     */
    public boolean isDefaultValue(int field);

    /**
     * Return the managing context.
     */
    public StoreContext getContext();

    /**
     * Return the state that this object is in.
     */
    public PCState getPCState();

    /**
     * Return the identifier for this state manager. This may return a
     * temporary identifier for new unflushed instances that have not been
     * assigned an object id, or for non-persistent or embedded instances.
     * For all other instances this method is the same as {@link #getObjectId}.
     */
    public Object getId();

    /**
     * Return the instance's object id. This method will return null if no
     * oid has been assigned. Oids are assigned to newly-persisted instances
     * when the user first asks for it, or on flush.
     */
    public Object getObjectId();

    /**
     * Set the object id for the managed instance. Some back ends may not be
     * able to assign a permanent oid until flush. Do not call this method on
     * application identity instances; changing the primary key fields of
     * application identity objects through the <code>storeXXXField</code>
     * methods will automatically change the oid.
     */
    public void setObjectId(Object oid);

    /**
     * Ask the store manager to assign a permanent oid to this new instance.
     *
     * @param flush if true, flush if necessary to get a permanent oid; if
     * false, the oid may be left unassigned
     * @return true if an oid assigned, false otherwise
     */
    public boolean assignObjectId(boolean flush);

    /**
     * The lock object set for this instance. This object is generally
     * managed by the system lock manager.
     */
    public Object getLock();

    /**
     * The lock object set for this instance. This object is generally
     * managed by the system lock manager.
     */
    public void setLock(Object lock);

    /**
     * Return the current version indicator for this instance.
     */
    public Object getVersion();

    /**
     * Set the version indicator for this instance, as loaded from the
     * data store. This method is used by the {@link StoreManager} when
     * loading instance data. On rollback, the version will be rolled back
     * to this value. Version objects should be serializable and should not
     * require vendor-specific classes, because they are transferred to
     * detached objects.
     */
    public void setVersion(Object version);

    /**
     * Set the next version indicator in the datastore pending a successful
     * flush. The {@link StoreManager} uses this method during flush.
     */
    public void setNextVersion(Object version);

    /**
     * Returns true if this state needs to issue a version update, possibly
     * as a result of being locked.
     */
    public boolean isVersionUpdateRequired();

    /**
     * Returns true if this state needs to issue a version check at flush time.
     */
    public boolean isVersionCheckRequired();

    /**
     * An object that concrete back ends can associate with each instance.
     * This object is not used or modified in any way by the generic
     * persistence layer.
     */
    public Object getImplData();

    /**
     * An object that concrete back ends can associate with each instance.
     * This object is not used or modified in any way by the generic
     * persistence layer.
     *
     * @param cacheable whether the impl data can be shared among instances
     * in different contexts if L2 caching is enabled
     * @return the previous impl data value, if any
     */
    public Object setImplData(Object data, boolean cacheable);

    /**
     * Whether the instance-level impl data can be shared among instances
     * in different contexts if L2 caching is enabled.
     */
    public boolean isImplDataCacheable();

    /**
     * Field-level impl data. Field-level data only applies to loaded fields,
     * and is cleared when the field is cleared.
     */
    public Object getImplData(int field);

    /**
     * Field-level impl data. Field-level data only applies to loaded fields,
     * and is cleared when the field is cleared. Whether the data is cached
     * across instances depends on the corresponding field metadata's response
     * to {@link FieldMetaData#usesImplData}.
     *
     * @return the previous impl data value, if any
     */
    public Object setImplData(int field, Object data);

    /**
     * Whether the field's impl data is loaded and can be shared among
     * instances in different contexts if L2 caching is enabled.
     */
    public boolean isImplDataCacheable(int field);

    /**
     * Use intermediate field data to store intermediate information that
     * might be available before the field is fully loaded. The system
     * will automatically clear this data when the field gets loaded.
     * This data should be cacheable; the datastore cache will attempt to
     * cache it if the field value is not available.
     */
    public Object getIntermediate(int field);

    /**
     * Use intermediate field data to store intermediate information that
     * might be available before the field is fully loaded. The system
     * will automatically clear this data when the field gets loaded.
     * This data should be cacheable; the datastore cache will attempt to
     * cache it if the field value is not available.
     */
    public void setIntermediate(int field, Object value);

    /**
     * Return the external value of the given field.
     */
    public boolean fetchBoolean(int field);

    /**
     * Return the external value of the given field.
     */
    public byte fetchByte(int field);

    /**
     * Return the external value of the given field.
     */
    public char fetchChar(int field);

    /**
     * Return the external value of the given field.
     */
    public double fetchDouble(int field);

    /**
     * Return the external value of the given field.
     */
    public float fetchFloat(int field);

    /**
     * Return the external value of the given field.
     */
    public int fetchInt(int field);

    /**
     * Return the external value of the given field.
     */
    public long fetchLong(int field);

    /**
     * Return the external value of the given field.
     */
    public Object fetchObject(int field);

    /**
     * Return the external value of the given field.
     */
    public short fetchShort(int field);

    /**
     * Return the external value of the given field.
     */
    public String fetchString(int field);

    /**
     * Return the externalized value of the field with the given index as an
     * object. If there is no externalizer, this is equivalent to
     * {@link #fetchField}.
     */
    public Object fetch(int field);

    /**
     * Return the value of the field with the given index as an object.
     *
     * @param transitions if true, this method will cause state transitions
     * to occur as if the field were accessed normally
     */
    public Object fetchField(int field, boolean transitions);

    /**
     * Return the value of the field at the specified index as of the
     * beginning of the transaction.
     *
     * @since 0.3.1.1
     */
    public Object fetchInitialField(int field);

    /**
     * Set the given external value back into the given field.
     */
    public void storeBoolean(int field, boolean externalVal);

    /**
     * Set the given external value back into the given field.
     */
    public void storeByte(int field, byte externalVal);

    /**
     * Set the given external value back into the given field.
     */
    public void storeChar(int field, char externalVal);

    /**
     * Set the given external value back into the given field.
     */
    public void storeDouble(int field, double externalVal);

    /**
     * Set the given external value back into the given field.
     */
    public void storeFloat(int field, float externalVal);

    /**
     * Set the given external value back into the given field.
     */
    public void storeInt(int field, int externalVal);

    /**
     * Set the given external value back into the given field.
     */
    public void storeLong(int field, long externalVal);

    /**
     * Set the given external value back into the given field.
     */
    public void storeObject(int field, Object externalVal);

    /**
     * Set the given external value back into the given field.
     */
    public void storeShort(int field, short externalVal);

    /**
     * Set the given external value back into the given field.
     */
    public void storeString(int field, String externalVal);

    /**
     * Set the value of the field with the given index as from the external
     * object value. If there is no externalizer, this is equivalent to
     * {@link #storeField}.
     */
    public void store(int field, Object value);

    /**
     * Set the value of the field with the given index as an object.
     */
    public void storeField(int field, Object value);

    /**
     * Mark the given field as dirty.
     */
    public void dirty(int field);

    /**
     * Notification that an element has been removed from the given field.
     */
    public void removed(int field, Object removed, boolean key);

    /**
     * Prepare the instance for refresh
     *
     * @param refreshAll true if this instance is one of a collection of
     * objects being refreshed
     * @return true if the object needs a refresh, false otherwise
     * @see Broker#refresh
     */
    public boolean beforeRefresh(boolean refreshAll);

    /**
     * Set the given field to the given value. Make the field dirty as
     * if user code set it. Do not delete dependent objects in the field's
     * current value. This method is invoked by the remote package to
     * synch a server-side state manager with remote changes. We do not
     * need to delete dependent instances because they will have been
     * deleted when the field changed on the client side, and those
     * client-side deletes will be transmitted independently.
     *
     * @since 0.3.1
     */
    public void setRemote (int field, Object value);
    
    /**
     * Some field types (collection proxies) support delayed loading.  Delayed loading
     * is a step beyond lazy loading.  Delayed load allows an instance of a field to be 
     * returned without actually loading it.
     * 
     * @param field
     * @return true if the field is setup for delayed access
     */
    public boolean isDelayed(int field);
    
    /**
     * Some field types (collection proxies) support delayed loading.  Delayed loading
     * is a step beyond lazy loading.  Delayed load allows an instance of a field to be 
     * returned without actually loading it.
     * 
     * @param field
     */
    public void setDelayed(int field, boolean delay);
    
    /**
     * If a field was marked delayed in a previous load operation this method can be
     * used to load the field.
     * @param field
     */
    public void loadDelayedField(int field);
    
    /**
     * Fetch an object field by index.
     * @param field
     */
    public Object fetchObjectField(int field);
}

