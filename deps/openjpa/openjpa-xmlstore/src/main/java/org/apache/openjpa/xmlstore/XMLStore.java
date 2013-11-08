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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.meta.ClassMetaData;

/**
 * Represents a store of object data encoded in XML. This store only allows
 * one datastore transaction to proceed at a time. File I/O errors can put
 * this store into an invalid state.
 */
public class XMLStore {

    private final XMLConfiguration _conf;

    // each key in the map is a least-derived class metadata object, and each
    // value is a map of oids to object datas representing the instances of
    // that class, including subclasses
    private final Map _metaOidMaps = new HashMap();

    // store gets locked during transactions
    private boolean _locked;

    /**
     * Constructor; supply configuration.
     */
    public XMLStore(XMLConfiguration conf) {
        _conf = conf;
    }

    /**
     * Return the data for the given oid, or null if it does not exist.
     */
    public synchronized ObjectData getData(ClassMetaData meta, Object oid) {
        meta = getLeastDerived(meta);
        return (ObjectData) getMap(meta).get(oid);
    }

    /**
     * Return all datas for the base class of the given type.
     */
    public synchronized ObjectData[] getData(ClassMetaData meta) {
        meta = getLeastDerived(meta);
        Collection vals = getMap(meta).values();
        return (ObjectData[]) vals.toArray(new ObjectData[vals.size()]);
    }

    /**
     * Returns the map of oids to object datas for the given least-derived type.
     */
    private Map getMap(ClassMetaData meta) {
        Map m = (Map) _metaOidMaps.get(meta);
        if (m != null)
            return m;

        // load datas from file and cache them
        Collection datas = _conf.getFileHandler().load(meta);
        m = new HashMap(datas.size());
        for (Iterator itr = datas.iterator(); itr.hasNext();) {
            ObjectData data = (ObjectData) itr.next();
            m.put(data.getId(), data);
        }
        _metaOidMaps.put(meta, m);
        return m;
    }

    /**
     * Return the least-derived metadata in the inheritance chain
     * above <code>meta</code>, or <code>meta</code> if it is a
     * least-derived metadata.
     */
    private static ClassMetaData getLeastDerived(ClassMetaData meta) {
        while (meta.getPCSuperclass() != null)
            meta = meta.getPCSuperclassMetaData();
        return meta;
    }

    /**
     * Begin a datastore transaction. Obtains an exclusive write lock on the
     * store.
     */
    public synchronized void beginTransaction() {
        // lock store
        while (_locked)
            try {
                wait();
            } catch (InterruptedException ie) {
            }
        _locked = true;
    }

    /**
     * End the datastore transaction.
     *
     * @param updates {@link ObjectData} instances to insert or update
     * @param deletes {@link ObjectData} instances to delete
     */
    public synchronized void endTransaction(Collection updates,
        Collection deletes) {
        // track dirty types
        Set dirty = new HashSet();
        try {
            // commit updates
            if (updates != null) {
                for (Iterator itr = updates.iterator(); itr.hasNext();) {
                    ObjectData data = (ObjectData) itr.next();
                    ClassMetaData meta = getLeastDerived(data.getMetaData());
                    getMap(meta).put(data.getId(), data);
                    dirty.add(meta);
                }
            }

            // commit deletes
            if (deletes != null) {
                for (Iterator itr = deletes.iterator(); itr.hasNext();) {
                    ObjectData data = (ObjectData) itr.next();
                    ClassMetaData meta = getLeastDerived(data.getMetaData());
                    getMap(meta).remove(data.getId());
                    dirty.add(meta);
                }
            }

            // write changes to dirty extents back to file
            XMLFileHandler fh = _conf.getFileHandler();
            for (Iterator itr = dirty.iterator(); itr.hasNext();) {
                ClassMetaData meta = (ClassMetaData) itr.next();
                fh.store(meta, getMap(meta).values());
            }
        }
        finally {
            // unlock store
            notify();
            _locked = false;
		}
	}
}
