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

import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.ProxyManager;

/**
 * FieldManager type used to store information for savepoint rollback.
 *
 * @author Steve Kim
 * @since 0.3.4
 */
class SavepointFieldManager
    extends ClearFieldManager
    implements Serializable {

    private static final Localizer _loc = Localizer.forPackage
        (SavepointFieldManager.class);

    private final StateManagerImpl _sm;
    private final BitSet _loaded;
    private final BitSet _dirty;
    private final BitSet _flush;
    private final PCState _state;
    private transient PersistenceCapable _copy;

    private final Object _version;
    private final Object _loadVersion;

    // used to track field value during store/fetch cycle
    private Object _field = null;
    private int[] _copyField = null;
    private BitSet _mutable;

    /**
     * Constructor. Provide instance to save and indicate whether
     * to copy persistent fields. Transactional fields will be
     * copied regardless of copy setting.
     */
    public SavepointFieldManager(StateManagerImpl sm, boolean copy) {
        _sm = sm;
        _state = _sm.getPCState();

        _dirty = (BitSet) _sm.getDirty().clone();
        _flush = (BitSet) _sm.getFlushed().clone();
        _loaded = (BitSet) _sm.getLoaded().clone();

        FieldMetaData[] fields = _sm.getMetaData().getFields();
        for (int i = 0; i < _loaded.length(); i++) {
            if (!_loaded.get(i))
                continue;
            if (copy || fields[i].getManagement() ==
                FieldMetaData.MANAGE_TRANSACTIONAL) {
                if (_copy == null)
                    _copy = _sm.getPersistenceCapable().pcNewInstance
                        (_sm, true);
                storeField(fields[i]);
            } else
                _loaded.clear(i);
        }

        // we need to proxy the fields so that we can track future changes
        // from this savepoint forward for PNew instances' mutable fields
        _sm.proxyFields(false, false);

        _version = _sm.getVersion ();
        _loadVersion = _sm.getLoadVersion ();
    }

    /**
     * Return the state manager that this manager is associated with.
     */
    public StateManagerImpl getStateManager() {
        return _sm;
    }

    public Object getVersion() {
        return _version;
    }

    public Object getLoadVersion() {
        return _loadVersion;
    }

    /**
     * Return the persistence capable copy holding the savepoint field values.
     */
    public PersistenceCapable getCopy() {
        return _copy;
    }

    /**
     * Return the saved {@link PCState}
     */
    public PCState getPCState() {
        return _state;
    }

    /**
     * Return the fields stored in this manager.
     */
    public BitSet getLoaded() {
        return _loaded;
    }

    /**
     * Return the dirty fields during the saved state.
     */
    public BitSet getDirty() {
        return _dirty;
    }

    /**
     * Return the flushed fields during the saved state.
     */
    public BitSet getFlushed() {
        return _flush;
    }

    /**
     * Store the data for the given field.
     */
    public void storeField(FieldMetaData field) {
        switch (field.getDeclaredTypeCode()) {
            case JavaTypes.DATE:
            case JavaTypes.ARRAY:
            case JavaTypes.COLLECTION:
            case JavaTypes.MAP:
            case JavaTypes.OBJECT:
                if (_mutable == null)
                    _mutable = new BitSet(_sm.getMetaData().getFields().length);
                _mutable.set(field.getIndex());
        }
        if (_mutable == null || !_mutable.get(field.getIndex())) {
            // immutable fields can just be copied over
            if (_copyField == null)
                _copyField = new int[1];
            _copyField[0] = field.getIndex();
            _copy.pcCopyFields(_sm.getPersistenceCapable(), _copyField);
        } else {
            _sm.provideField(_sm.getPersistenceCapable(), this,
                field.getIndex());
            _sm.replaceField(_copy, this, field.getIndex());
        }
    }

    /**
     * Restore the given field. If this method returns true, then you need
     * to use this field manager to replace the given field in the state
     * manager's instance.
     */
    public boolean restoreField(int field) {
        if (!_loaded.get(field))
            return false;
        if (_mutable != null && _mutable.get(field))
            return true;

        // copy the saved field over
        if (_copyField == null)
            _copyField = new int[1];
        _copyField[0] = field;
        _sm.getPersistenceCapable().pcCopyFields(_copy, _copyField);
        return false;
    }

    public Object fetchObjectField(int field) {
        // return the copied field during save, or a null value during restore
        Object val = _field;
        _field = null;
        return val;
    }

    public void storeObjectField(int field, Object curVal) {
        // copy mutable fields
        ProxyManager proxy = _sm.getContext().getConfiguration().
            getProxyManagerInstance();
        FieldMetaData fmd = _sm.getMetaData().getField(field);
        switch (fmd.getDeclaredTypeCode()) {
            case JavaTypes.ARRAY:
                _field = proxy.copyArray(curVal);
                break;
            case JavaTypes.COLLECTION:
                _field = proxy.copyCollection((Collection) curVal);
                break;
            case JavaTypes.MAP:
                _field = proxy.copyMap((Map) curVal);
                break;
            case JavaTypes.DATE:
                _field = proxy.copyDate((Date) curVal);
                break;
            case JavaTypes.OBJECT:
                _field = proxy.copyCustom(curVal);
                if (_field == null)
                    _field = curVal;
                break;
            default:
                _field = curVal;
        }

        // if we couldn't get a copy of the sco, act like it wasn't saved
        // should throw an exception
        if (curVal != null && _field == null)
            throw new InternalException(_loc.get("no-savepoint-copy", fmd));
	}

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        _sm.writePC(oos, _copy);
    }

    private void readObject(ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        _copy = _sm.readPC(ois);
    }
}
