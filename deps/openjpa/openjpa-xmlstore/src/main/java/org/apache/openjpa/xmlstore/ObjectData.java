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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.openjpa.event.OrphanedKeyAction;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.util.Proxy;
import org.apache.openjpa.util.UnsupportedException;

/**
 * In-memory form of data in datastore backing a single persistent object.
 */
public final class ObjectData
    implements Cloneable {

    private Object _oid;
    private Object[] _data;
    private Long _version;
    private ClassMetaData _meta;

    /**
     * Create the object without underlying data. Just pass in type specific
     * metadata and the oid.
     */
    public ObjectData(Object oid, ClassMetaData meta) {
        _oid = oid;
        _meta = meta;
        _data = new Object[meta.getFields().length];
    }

    /**
     * Getter for oid.
     */
    public Object getId() {
        return _oid;
    }

    /**
     * Get the data for the field with the given index.
     */
    public Object getField(int num) {
        return _data[num];
    }

    /**
     * Set the data for the field with the given index.
     */
    public void setField(int num, Object val) {
        _data[num] = val;
    }

    /**
     * Set the version number of the object.
     */
    public void setVersion(Long version) {
        _version = version;
    }

    /**
     * Get the version number of the object.
     */
    public Long getVersion() {
        return _version;
    }

    /**
     * Get the metadata associated with the type of persistent object for
     * which this data applies.
     */
    public ClassMetaData getMetaData() {
        return _meta;
    }

    /**
     * Load the data and version information for this object into the
     * given state manager. Only fields in the given fetch configuration are
     * loaded.
     */
    public void load(OpenJPAStateManager sm, FetchConfiguration fetch) {
        if (sm.getVersion() == null)
            sm.setVersion(_version);

        FieldMetaData[] fmds = _meta.getFields();
        for (int i = 0; i < fmds.length; i++)
            if (!sm.getLoaded().get(i) && fetch.requiresFetch(fmds[i]) 
                != FetchConfiguration.FETCH_NONE)
                sm.store(i, toLoadable(sm, fmds[i], _data[i], fetch));
    }

    /**
     * Load the data and version information for this object into the
     * given state manager. Only fields in the given bit set will be loaded.
     */
    public void load(OpenJPAStateManager sm, BitSet fields,
        FetchConfiguration fetch) {
        if (sm.getVersion() == null)
            sm.setVersion(_version);

        FieldMetaData[] fmds = _meta.getFields();
        for (int i = 0; i < fmds.length; i++)
            if (fields.get(i))
                sm.store(i, toLoadable(sm, fmds[i], _data[i], fetch));
    }

    /**
     * Convert the stored value <code>val</code> into a value for loading
     * into a state manager.
     */
    private static Object toLoadable(OpenJPAStateManager sm,
        FieldMetaData fmd, Object val, FetchConfiguration fetch) {
        if (val == null)
            return null;

        Collection c;
        switch (fmd.getTypeCode()) {
            case JavaTypes.COLLECTION:
                // the stored value must be a collection
                c = (Collection) val;

                // the state manager will create a proxy collection of the
                // needed type depending on the declared type of the user's
                // field; the proxy will perform dirty tracking, etc
                Collection c2 = (Collection) sm.newFieldProxy(fmd.getIndex());

                // populate the proxy collection with our stored data,
                // converting it to the right type from its stored form
                for (Iterator itr = c.iterator(); itr.hasNext();)
                    c2.add(toNestedLoadable(sm, fmd.getElement(), itr.next(),
                        fetch));
                return c2;

            case JavaTypes.ARRAY:
                // the stored value must be a collection; we put arrays into
                // collections for storage
                c = (Collection) val;

                // create a new array of the right type; unlike collections in
                // the case above, arrays cannot be proxied
                Object a = Array.newInstance(fmd.getElement().getType(),
                    c.size());

                // populate the array with our stored data, converting it to the
                // right type from its stored form
                int idx = 0;
                for (Iterator itr = c.iterator(); itr.hasNext(); idx++)
                    Array.set(a, idx, toNestedLoadable(sm, fmd.getElement(),
                        itr.next(), fetch));
                return a;

            case JavaTypes.MAP:
                // the stored value must be a map
                Map m = (Map) val;

                // the state manager will create a proxy map of the needed
                // type depending on the declared type of the user's field; the
                // proxy will perform dirty tracking, etc
                Map m2 = (Map) sm.newFieldProxy(fmd.getIndex());

                // populate the proxy map with our stored data, converting
                // it to the right type from its stored form
                for (Iterator itr = m.entrySet().iterator(); itr.hasNext();) {
                    Map.Entry e = (Map.Entry) itr.next();
                    m2.put(toNestedLoadable(sm, fmd.getKey(), e.getKey(),fetch),
                        toNestedLoadable(sm, fmd.getElement(), e.getValue(),
                            fetch));
                }
                return m2;

            default:
                // just convert the stored value into its loadable equivalent.
                return toNestedLoadable(sm, fmd, val, fetch);
        }
    }

    /**
     * Convert the given stored value <code>val</code> to a value for loading
     * into a state manager. The value <code>val</code> must be a singular
     * value; it cannot be a container.
     */
    private static Object toNestedLoadable(OpenJPAStateManager sm,
        ValueMetaData vmd, Object val, FetchConfiguration fetch) {
        if (val == null)
            return null;

        switch (vmd.getTypeCode()) {
            // clone the date to prevent direct modification of our stored value
            case JavaTypes.DATE:
                return ((Date) val).clone();

            case JavaTypes.PC:
            case JavaTypes.PC_UNTYPED:
                // for relations to other persistent objects, we store the
                // related object's oid -- convert it back into a persistent
                // instance
                StoreContext ctx = sm.getContext();
                Object pc = ctx.find(val, fetch, null, null, 0);
                if (pc != null)
                    return pc;
                OrphanedKeyAction action = ctx.getConfiguration().
                    getOrphanedKeyActionInstance();
                return action.orphan(val, sm, vmd);
            default:
                return val;
        }
    }

    /**
     * Store the data and version information for this object from the
     * given state manager. Only dirty fields will be stored.
     */
    public void store(OpenJPAStateManager sm) {
        _version = (Long) sm.getVersion();

        // if the version has not been set in the state manager (only true
        // when the object is new), set the version number to 0
        if (_version == null)
            _version = 0L;

        // run through each persistent field in the state manager and store it
        FieldMetaData[] fmds = _meta.getFields();
        for (int i = 0; i < fmds.length; i++) {
            if (sm.getDirty().get(i)
                && fmds[i].getManagement() == fmds[i].MANAGE_PERSISTENT)
                _data[i] = toStorable(fmds[i], sm.fetch(i), sm.getContext());
        }
    }

    /**
     * Convert the given field value <code>val</code> to a form we can store.
     */
    private static Object toStorable(FieldMetaData fmd, Object val,
        StoreContext ctx) {
        if (val == null)
            return null;

        Collection c;
        switch (fmd.getTypeCode()) {
            case JavaTypes.COLLECTION:
                c = (Collection) val;

                // create a collection to copy the elements into for storage,
                // and populate it with converted element values
                Collection c2 = new ArrayList();
                for (Iterator itr = c.iterator(); itr.hasNext();)
                    c2.add(toNestedStorable(fmd.getElement(), itr.next(), ctx));
                return c2;

            case JavaTypes.ARRAY:
                // create a collection to copy the elements into for storage,
                // and populate it with converted element values
                c = new ArrayList();
                for (int i = 0, len = Array.getLength(val); i < len; i++)
                    c.add(toNestedStorable(fmd.getElement(), Array.get(val, i),
                        ctx));
                return c;

            case JavaTypes.MAP:
                Map m = (Map) val;

                // create a map to copy the entries into for storage, and
                // populate it with converted entry values
                Map m2 = new HashMap();
                for (Iterator itr = m.entrySet().iterator(); itr.hasNext();) {
                    Map.Entry e = (Map.Entry) itr.next();
                    m2.put(toNestedStorable(fmd.getKey(), e.getKey(), ctx),
                        toNestedStorable(fmd.getElement(), e.getValue(), ctx));
                }
                return m2;

            default:
                // just convert the loaded value into its storable equivalent
                return toNestedStorable(fmd, val, ctx);
        }
    }

    /**
     * Convert the given loaded value <code>val</code> to a value for storing.
     * The value <code>val</code> must be a singular value; it cannot be a
     * container.
     */
    private static Object toNestedStorable(ValueMetaData vmd, Object val,
        StoreContext ctx) {
        if (val == null)
            return null;

        switch (vmd.getTypeCode()) {
            case JavaTypes.DATE:
                // if the date is a proxy (since Dates are second class
                // objects (SCOs) they can be proxied for dirty tracking,
                // etc) then copy the value out of it for storage
                if (val instanceof Proxy)
                    return ((Proxy) val).copy(val);
                return ((Date) val).clone();

            case JavaTypes.PC:
            case JavaTypes.PC_UNTYPED:
                return ctx.getObjectId(val);

            case JavaTypes.COLLECTION:
            case JavaTypes.ARRAY:
            case JavaTypes.MAP:
                // nested relation types (e.g. collections of collections)
                // are not currently supported
                throw new UnsupportedException("This store does not support "
                    + "nested containers (e.g. collections of collections).");

            default:
                return val;
        }
    }

    /**
     * Clone this data.
     */
    public Object clone() {
        ObjectData data = new ObjectData(_oid, _meta);
        data.setVersion(_version);

        // copy each field
        FieldMetaData[] fmds = _meta.getFields();
        for (int i = 0; i < fmds.length; i++) {
            Object val = _data[i];
            if (val == null) {
                data.setField(i, null);
                continue;
            }

            switch (fmds[i].getTypeCode()) {
                case JavaTypes.COLLECTION:
                case JavaTypes.ARRAY:
                    data.setField(i, new ArrayList((Collection) val));
                    break;
                case JavaTypes.MAP:
                    data.setField(i, new HashMap((Map) val));
                    break;
                default:
                    data.setField(i, val);
            }
        }
        return data;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Class: (" + _meta.getDescribedType().getName() + ")\n");
        buf.append("Object Id: (" + _oid + ")\n");
        buf.append("Version: (" + _version + ")\n");
        FieldMetaData[] fmds = _meta.getFields();
        for (int i = 0; i < fmds.length; i++) {
            buf.append("  Field: (" + i + ")\n");
            buf.append("  Name: (" + fmds[i].getName() + ")\n");
            buf.append("  Value: (" + _data[i] + ")\n");
		}
		return buf.toString ();
	}
}
