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

import org.apache.openjpa.datacache.DataCache;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;

/**
 * Default {@link PCData} implementation.
 *
 * @author Patrick Linskey
 * @author Abe White
 * @nojavadoc
 */
public class PCDataImpl
    extends AbstractPCData {

    private final Object _oid;
    private final Class<?> _type;
    private final String _cache;
    private final Object[] _data;
    private final BitSet _loaded;
    private Object _version = null;
    private Object _impl = null;
    private Object[] _fieldImpl = null;

    public PCDataImpl(Object oid, ClassMetaData meta) {
        this(oid, meta, DataCache.NAME_DEFAULT);
    }
    
    /**
     * Constructor.
     */
    public PCDataImpl(Object oid, ClassMetaData meta, String name) {
        _oid = oid;
        _type = meta.getDescribedType();
        _cache = name;

        int len = meta.getFields().length;
        _data = new Object[len];
        _loaded = new BitSet(len);
    }

    public Object getId() {
        return _oid;
    }

    public Class<?> getType() {
        return _type;
    }

    public BitSet getLoaded() {
        return _loaded;
    }

    public Object getData(int index) {
        // make sure index is actually loaded to avoid returning an
        // intermediate value
        return (_loaded.get(index)) ? _data[index] : null;
    }

    public void setData(int index, Object val) {
        _loaded.set(index);
        _data[index] = val;
    }

    public void clearData(int index) {
        _loaded.clear(index);
        _data[index] = null;
    }

    public Object getImplData() {
        return _impl;
    }

    public void setImplData(Object val) {
        _impl = val;
    }

    public Object getImplData(int index) {
        return (_fieldImpl != null) ? _fieldImpl[index] : null;
    }

    public void setImplData(int index, Object val) {
        if (val != null) {
            if (_fieldImpl == null)
                _fieldImpl = new Object[_data.length];
            _fieldImpl[index] = val;
        } else if (_fieldImpl != null)
            _fieldImpl[index] = null;
    }

    public Object getIntermediate(int index) {
        return (!_loaded.get(index)) ? _data[index] : null;
    }

    public void setIntermediate(int index, Object val) {
        _loaded.clear(index);
        _data[index] = val;
    }

    public boolean isLoaded(int index) {
        return _loaded.get(index);
    }

    public void setLoaded(int index, boolean loaded) {
        if (loaded)
            _loaded.set(index);
        else
            _loaded.clear(index);
    }

    public Object getVersion() {
        return _version;
    }

    public void setVersion(Object version) {
        _version = version;
    }

    public void load(OpenJPAStateManager sm, FetchConfiguration fetch,
        Object context) {
        loadVersion(sm);
        loadImplData(sm);

        FieldMetaData[] fmds = sm.getMetaData().getFields();
        ((StateManagerImpl)sm).setLoading(true);
        for (int i = 0; i < fmds.length; i++) {
            // load intermediate data for all unloaded fields and data for
            // fields in configured fetch groups
            if (!isLoaded(i))
                loadIntermediate(sm, fmds[i]);
            else if (!sm.getLoaded().get(i) && fetch.requiresFetch(fmds[i]) 
                != FetchConfiguration.FETCH_NONE)
                loadField(sm, fmds[i], fetch, context);
        }
    }

    public void load(OpenJPAStateManager sm, BitSet fields,
        FetchConfiguration fetch, Object context) {
        loadVersion(sm);
        loadImplData(sm);

        // attempt to load given fields
        int len = (fields == null) ? 0 : fields.length();
        FieldMetaData fmd;
        for (int i = 0; i < len; i++) {
            if (!fields.get(i))
                continue;

            fmd = sm.getMetaData().getField(i);
            boolean loading = false; 
            if(sm.getContext() != null && sm.getContext() instanceof BrokerImpl) { 
                loading = ((BrokerImpl) sm.getContext()).isLoading(sm.getObjectId());
            }
            if (!isLoaded(i) || loading) { // prevent reentrant calls. 
                loadIntermediate(sm, fmd);
            }
            else {
                loadField(sm, fmd, fetch, context);
                loadImplData(sm, fmd);
                fields.clear(i);
            }
        }
    }

    /**
     * Set version information into the given state manager.
     */
    protected void loadVersion(OpenJPAStateManager sm) {
        if (sm.getVersion() == null)
            sm.setVersion(getVersion());
    }

    /**
     * Set impl data information into the given state manager.
     */
    protected void loadImplData(OpenJPAStateManager sm) {
        Object impl = getImplData();
        if (sm.getImplData() == null && impl != null)
            sm.setImplData(impl, true);
    }

    /**
     * Set field-level information into the given state manager.
     */
    protected void loadField(OpenJPAStateManager sm, FieldMetaData fmd, FetchConfiguration fetch, Object context) {
        int index = fmd.getIndex();
        Object val = toField(sm, fmd, getData(index), fetch, context);
        
        // If val is null, make sure that we don't send back a null Embeddable or ElementCollection...perhaps others?
        // Probably should think about trying to shove this data back into the cache at this point so we don't
        // continually run through this code.
        if (val == null && fmd.isEmbeddedPC()) {
            val = sm.getContext().embed(null, null, sm, fmd).getManagedInstance();
        } else if (val == null && fmd.isElementCollection()) {
            val = sm.newProxy(index);
        }
        sm.storeField(index, val);
    }

    /**
     * Set field-level impl data into the given state manager.
     */
    protected void loadImplData(OpenJPAStateManager sm, FieldMetaData fmd) {
        int index = fmd.getIndex();
        Object impl = getImplData(index);
        if (impl != null)
            sm.setImplData(index, impl);
    }

    /**
     * Set intermediate information for the given field into the state manager.
     */
    protected void loadIntermediate(OpenJPAStateManager sm, FieldMetaData fmd) {
        int index = fmd.getIndex();
        Object inter = getIntermediate(index);
        if (inter != null && !sm.getLoaded().get(index))
            sm.setIntermediate(index, inter);
    }

    public void store(OpenJPAStateManager sm) {
        storeVersion(sm);
        storeImplData(sm);

        FieldMetaData[] fmds = sm.getMetaData().getFields();
        for (int i = 0; i < fmds.length; i++) {
            if (sm.getLoaded().get(i)) {
                storeField(sm, fmds[i]);
                storeImplData(sm, fmds[i], isLoaded(i));
            } else if (!isLoaded(i))
                storeIntermediate(sm, fmds[i]);
        }
    }

    public void store(OpenJPAStateManager sm, BitSet fields) {
        storeVersion(sm);
        storeImplData(sm);

        FieldMetaData[] fmds = sm.getMetaData().getFields();
        for (int i = 0; i < fmds.length; i++) {
            if (fields != null && fields.get(i)) {
                storeField(sm, fmds[i]);
                storeImplData(sm, fmds[i], isLoaded(i));
            } else if (!isLoaded(i))
                storeIntermediate(sm, fmds[i]);
        }
    }

    /**
     * Store version information from the given state manager.
     */
    protected void storeVersion(OpenJPAStateManager sm) {
        setVersion(sm.getVersion());
    }

    /**
     * Store impl data from the given state manager.
     */
    protected void storeImplData(OpenJPAStateManager sm) {
        if (sm.isImplDataCacheable())
            setImplData(sm.getImplData());
    }

    /**
     * Store field-level information from the given state manager.
     */
    protected void storeField(OpenJPAStateManager sm, FieldMetaData fmd) {
        if (fmd.getManagement() != fmd.MANAGE_PERSISTENT)
            return;

        int index = fmd.getIndex();
        OpenJPAStateManager dsm = null;
        if (sm.getPersistenceCapable().pcIsDetached()) {
            dsm = (DetachedStateManager) sm.getPersistenceCapable().
                pcGetStateManager();
            sm.getPersistenceCapable().pcReplaceStateManager(sm);
        }

        Object val = toData(fmd, sm.fetchField(index, false),
            sm.getContext());
        if (dsm != null)
            sm.getPersistenceCapable().pcReplaceStateManager(dsm);
        if (val != NULL)
            setData(index, val);
        else // unable to store field value; clear out any old values
            clearData(index);
    }

    /**
     * Store the intermediate field value for the given field.
     */
    protected void storeIntermediate(OpenJPAStateManager sm,
        FieldMetaData fmd) {
        int index = fmd.getIndex();
        Object val = sm.getIntermediate(index);
        if (val != null)
            setIntermediate(index, val);
    }

    /**
     * Store impl data for the given field.
     */
    protected void storeImplData(OpenJPAStateManager sm, FieldMetaData fmd,
        boolean fieldLoaded) {
        int index = fmd.getIndex();
        if (fieldLoaded) {
            // is there impl data to store?
            Object impl = sm.getImplData(index);
            if (impl != null && sm.isImplDataCacheable(index))
                setImplData(index, impl);
        } else
            setImplData(index, null);
    }

    /**
     * Return a new {@link PCData} implementation of the right type for
     * embedded instances. Returns a {@link PCDataImpl} by default.
     */
    public AbstractPCData newEmbeddedPCData(OpenJPAStateManager sm) {
        return new PCDataImpl(sm.getId (), sm.getMetaData (), _cache);
	}

    public String getCache() {
        return _cache;
    }
}
