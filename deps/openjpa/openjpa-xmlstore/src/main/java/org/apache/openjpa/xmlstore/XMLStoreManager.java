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
package org.apache.openjpa.xmlstore;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.openjpa.abstractstore.AbstractStoreManager;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.lib.rop.ListResultObjectProvider;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.util.OptimisticException;
import org.apache.openjpa.util.StoreException;

/**
 * Store manager to a back-end consisting of XML files. This
 * implementation accesses data through the {@link XMLStore} associated with
 * its {@link XMLConfiguration}. Configuration instances are shared by all
 * store managers owned by all brokers created with the same factory.
 *
 * @see AbstractStoreManager
 */
public class XMLStoreManager
    extends AbstractStoreManager {

    private XMLConfiguration _conf;
    private XMLStore _store;

    // changed data within the current transaction
    private Collection _updates;
    private Collection _deletes;

    protected Collection getUnsupportedOptions() {
        Collection c = super.getUnsupportedOptions();

        // remove options we do support but the abstract store doesn't
        c.remove(OpenJPAConfiguration.OPTION_ID_DATASTORE);
        c.remove(OpenJPAConfiguration.OPTION_OPTIMISTIC);

        // and add some that we don't support but the abstract store does
        c.add(OpenJPAConfiguration.OPTION_EMBEDDED_RELATION);
        c.add(OpenJPAConfiguration.OPTION_EMBEDDED_COLLECTION_RELATION);
        c.add(OpenJPAConfiguration.OPTION_EMBEDDED_MAP_RELATION);
        return c;
    }

    protected OpenJPAConfiguration newConfiguration() {
        // override to use our configuration type
        return new XMLConfiguration();
    }

    protected void open() {
        // cache operational state
        _conf = (XMLConfiguration) ctx.getConfiguration();
        _store = _conf.getStore();
    }

    public boolean exists(OpenJPAStateManager sm, Object context) {
        // see if the given object exists in the store
        return _store.getData(sm.getMetaData(), sm.getObjectId()) != null;
    }

    /**
     * Increment the version indicator in the given state manager.
     */
    private static void incrementVersion(OpenJPAStateManager sm) {
        long version = 0;
        if (sm.getVersion() != null)
            version = ((Long) sm.getVersion()).longValue() + 1;
        sm.setNextVersion(version);
    }

    public boolean initialize(OpenJPAStateManager sm, PCState state,
        FetchConfiguration fetch, Object context) {
        // we may already have looked up the backing ObjectData (see our extent
        // implementation below), and passed it through as the context; if
        // not, then look it up in the store
        ObjectData data;
        if (context != null)
            data = (ObjectData) context;
        else
            data = _store.getData(sm.getMetaData(), sm.getObjectId());

        // no matching record?
        if (data == null)
            return false;

        // initialize the state manager with a new instance of the right
        // type and lifecycle state
        sm.initialize(data.getMetaData().getDescribedType(), state);

        // load the data from the ObjectData into the state mgr; note that
        // this store manager doesn't do any locking -- it relies on the
        // system's lock manager to lock after the load is complete
        data.load(sm, fetch);
        return true;
    }

    public boolean load(OpenJPAStateManager sm, BitSet fields,
        FetchConfiguration fetch, int lockLevel, Object context) {
        // we may already have looked up the backing ObjectData (see our extent
        // implementation below), and passed it through as the context; if
        // not, then look it up in the store
        ObjectData data;
        if (context != null)
            data = (ObjectData) context;
        else
            data = _store.getData(sm.getMetaData(), sm.getObjectId());

        // no matching record?
        if (data == null)
            return false;

        // load the data from the ObjectData into the state mgr; note that
        // this store manager doesn't do any locking -- it relies on the
        // system's lock manager to lock after the load is complete
        data.load(sm, fields, fetch);
        return true;
    }

    public boolean syncVersion(OpenJPAStateManager sm, Object context) {
        if (sm.getVersion() == null)
            return false;

        // we may already have looked up the backing ObjectData (see our extent
        // implementation below), and passed it through as the context; if
        // not, then look it up in the store
        ObjectData data;
        if (context != null)
            data = (ObjectData) context;
        else
            data = _store.getData(sm.getMetaData(), sm.getObjectId());

        // no record?
        if (data == null)
            return false;

        // if the version of data held by the state mgr is the same as the
        // version in the datastore, return true, letting the broker know that
        // it doesn't need to load any more data
        if (sm.getVersion().equals(data.getVersion()))
            return true;

        // set the version to be up-to-date, and return false letting
        // the broker know that it needs to load up-to-date data
        sm.setVersion(data.getVersion());
        return false;
    }

    public void begin() {
        _store.beginTransaction();
    }

    public void commit() {
        try {
            _store.endTransaction(_updates, _deletes);
        } finally {
            _updates = null;
            _deletes = null;
        }
    }

    public void rollback() {
        _updates = null;
        _deletes = null;
        _store.endTransaction(null, null);
    }

    protected Collection flush(Collection pNew, Collection pNewUpdated,
        Collection pNewFlushedDeleted, Collection pDirty, Collection pDeleted) {
        // we don't support incremental flushing, so pNewUpdated and
        // pNewFlushedDeleted should be empty; we ignore them here

        // track optimistic violations
        Collection exceps = new LinkedList();

        // convert instances to ObjectDatas
        _updates = new ArrayList(pNew.size() + pDirty.size());
        _deletes = new ArrayList(pDeleted.size());

        // convert additions
        for (Iterator itr = pNew.iterator(); itr.hasNext();) {
            // create new object data for instance
            OpenJPAStateManager sm = (OpenJPAStateManager) itr.next();
            Object oid = sm.getObjectId();
            ObjectData data = _store.getData(sm.getMetaData(), oid);
            if (data != null)
                throw new StoreException("Attempt to insert "
                    + "new object " + sm.getManagedInstance()
                    + "with the same oid as an existing instance: " + oid).
                    setFatal(true);

            data = new ObjectData(oid, sm.getMetaData());
            incrementVersion(sm);
            data.store(sm);
            _updates.add(data);
        }

        // convert updates
        for (Iterator itr = pDirty.iterator(); itr.hasNext();) {
            OpenJPAStateManager sm = (OpenJPAStateManager) itr.next();
            ObjectData data = _store.getData(sm.getMetaData(),
                sm.getObjectId());

            // if data has been deleted or has the wrong version, record
            // opt lock violation
            if (data == null || !data.getVersion().equals(sm.getVersion())) {
                exceps.add(new OptimisticException
                    (sm.getManagedInstance()));
                continue;
            }

            // store changes
            incrementVersion(sm);
            data = (ObjectData) data.clone();
            data.store(sm);
            _updates.add(data);
        }

        // convert deletes
        for (Iterator itr = pDeleted.iterator(); itr.hasNext();) {
            OpenJPAStateManager sm = (OpenJPAStateManager) itr.next();
            ObjectData data = _store.getData(sm.getMetaData(),
                sm.getObjectId());

            // record delete
            if (data != null)
                _deletes.add(data);
        }

        return exceps;
    }

    public ResultObjectProvider executeExtent(ClassMetaData meta,
        boolean subclasses, FetchConfiguration fetch) {
        // ask the store for all ObjectDatas for the given type; this
        // actually gives us all instances of the base class of the type
        ObjectData[] datas = _store.getData(meta);
        Class candidate = meta.getDescribedType();

        // create a list of the corresponding persistent objects that
        // match the type and subclasses criteria
        List pcs = new ArrayList(datas.length);
        for (int i = 0; i < datas.length; i++) {
            // does this instance belong in the extent?
            Class c = datas[i].getMetaData().getDescribedType();
            if (c != candidate && (!subclasses
                || !candidate.isAssignableFrom(c)))
                continue;

            // look up the pc instance for the data, passing in the data
            // as well so that we can take advantage of the fact that we've
            // already looked it up.  note that in the store manager's
            // initialize(), load(), etc methods we check for this data
            // being passed through and save ourselves a trip to the store
            // if it is present; this is particularly important in systems
            // where a trip to the store can be expensive.
            pcs.add(ctx.find(datas[i].getId(), fetch, null, datas[i], 0));
        }
        return new ListResultObjectProvider(pcs);
    }
    public boolean isCached(List<Object> oids, BitSet edata) {
        // XMLStoreManager does not cache oids. 
        return false;
    }
}
