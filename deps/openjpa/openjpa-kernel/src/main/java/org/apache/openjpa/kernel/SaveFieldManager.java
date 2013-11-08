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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.ProxyManager;

/**
 * FieldManager type used to store information for rollback.
 *
 * @author Abe White
 */
public class SaveFieldManager
    extends ClearFieldManager
    implements Serializable {

    private final StateManagerImpl _sm;
    private final BitSet _unloaded;
    private BitSet _saved = null;
    private int[] _copyField = null;
    private transient PersistenceCapable _state = null;

    // used to track field value during store/fetch cycle
    private Object _field = null;

    /**
     * Constructor. Provide {@link StateManagerImpl} of instance to save.
     */
    SaveFieldManager(StateManagerImpl sm, PersistenceCapable pc, BitSet dirty) {
        _sm = sm;
        _state = pc;

        // if instance is new or transient all fields will be marked dirty even
        // though they have their original values, so we can restore them;
        // otherwise, we need to record already-dirty persistent fields as
        // ones we won't be able to restore
        FieldMetaData[] fields = _sm.getMetaData().getFields();
        if (_sm.isNew() || !_sm.isPersistent() || dirty == null)
            _unloaded = new BitSet(fields.length);
        else {
            _unloaded = (BitSet) dirty.clone();
            for (int i = 0; i < fields.length; i++)
                if (fields[i].getManagement() != fields[i].MANAGE_PERSISTENT)
                    _unloaded.clear(i);
        }
    }

    /**
     * Return the persistence capable copy holding the rollback field values.
     */
    public PersistenceCapable getState() {
        return _state;
    }

    /**
     * Return the currently-loaded fields that will be unloaded after rollback.
     */
    public BitSet getUnloaded() {
        return _unloaded;
    }

    /**
     * Save the given field. If this method returns true, then you need
     * to use this field manager to replace the given field in the instance
     * returned by {@link #getState}.
     */
    public boolean saveField(int field) {
        // if not loaded we can't save orig value; mark as unloaded on rollback
        if (_sm.getLoaded() != null && !_sm.getLoaded().get(field)) {
            _unloaded.set(field);
            return false;
        }

        // already saved?
        if (_saved != null && _saved.get(field))
            return false;

        FieldMetaData fmd = _sm.getMetaData().getField(field);
        boolean mutable = false;
        switch (fmd.getDeclaredTypeCode()) {
            case JavaTypes.DATE:
            case JavaTypes.ARRAY:
            case JavaTypes.COLLECTION:
            case JavaTypes.MAP:
            case JavaTypes.OBJECT:
                mutable = true;
        }

        // if this is not an inverse field and the proper restore flag is
        // not set, skip it

        if (_sm.getBroker().getInverseManager() == null
            || fmd.getInverseMetaDatas().length == 0) {
            // use sm's restore directive, not broker's
            int restore = _sm.getBroker().getRestoreState();
            if (restore == RestoreState.RESTORE_NONE
                || (mutable && restore == RestoreState.RESTORE_IMMUTABLE)) {
                _unloaded.set(field);
                return false;
            }
        }

        // prepare to save the field
        if (_state == null)
            _state = _sm.getPersistenceCapable().pcNewInstance(_sm, true);
        if (_saved == null)
            _saved = new BitSet(_sm.getMetaData().getFields().length);

        _saved.set(field);

        // if mutable, return true to indicate that the field needs to be
        // copied by providing and replacing it using this field manager
        if (mutable)
            return true;

        // immutable fields can just be copied over
        if (_copyField == null)
            _copyField = new int[1];
        _copyField[0] = field;
        getState().pcCopyFields(_sm.getPersistenceCapable(), _copyField);
        return false;
    }

    /**
     * Restore the given field. If this method returns true, then you need
     * to use this field manager to replace the given field in the state
     * manager's instance.
     */
    public boolean restoreField(int field) {
        // if the given field needs to be unloaded, return true so that it gets
        // replaced with a default value
        if (_unloaded.get(field))
            return true;

        // if the field was not saved, it must not have gotten dirty; just
        // return false so that the current value is kept
        if (_saved == null || !_saved.get(field))
            return false;

        // copy the saved field over
        if (_copyField == null)
            _copyField = new int[1];
        _copyField[0] = field;
        _sm.getPersistenceCapable().pcCopyFields(getState(), _copyField);
        return false;
    }

    /**
     * Compare the given field.
     * @return <code>true</code> if the field is the same in the current
     * state and in the saved state; otherwise, <code>false</code>.
     */
    public boolean isFieldEqual(int field, Object current) {
        // if the field is not available, assume that it has changed.
        if (_saved == null || !_saved.get(field))
            return false;
        if (!(getState().pcGetStateManager() instanceof StateManagerImpl))
            return false;

        StateManagerImpl sm = (StateManagerImpl) getState().pcGetStateManager();
        SingleFieldManager single = new SingleFieldManager(sm, sm.getBroker());
        sm.provideField(getState(), single, field);
        Object old = single.fetchObjectField(field);
        return current == old || current != null && current.equals(old);
    }

    public Object fetchObjectField(int field) {
        // return the copied field during save, or a null value during restore
        Object val = _field;
        _field = null;
        return val;
    }

    public void storeObjectField(int field, Object curVal) {
        // copy mutable fields
        ProxyManager proxy = _sm.getBroker().getConfiguration().
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
        if (curVal != null && _field == null) {
            _unloaded.set(field);
            _saved.clear(field);
		}
	}

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        _sm.writePC(oos, _state);
    }

    private void readObject(ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        _state = _sm.readPC(ois);
    }
}
