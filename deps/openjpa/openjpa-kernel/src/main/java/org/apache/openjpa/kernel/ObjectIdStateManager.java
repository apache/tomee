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
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.BitSet;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.StateManager;
import org.apache.openjpa.enhance.Reflection;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.meta.AccessCode;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.util.GeneralException;
import org.apache.openjpa.util.ImplHelper;

/**
 * State manager used to access state of embedded object id primary key fields.
 *
 * @author Abe White
 * @nojavadoc
 */
public class ObjectIdStateManager
    implements OpenJPAStateManager {

    private static final Byte ZERO_BYTE = Byte.valueOf((byte)0);
    private static final Character ZERO_CHAR = Character.valueOf((char)0);
    private static final Double ZERO_DOUBLE = Double.valueOf(0);
    private static final Float ZERO_FLOAT = Float.valueOf(0);
    private static final Short ZERO_SHORT = Short.valueOf((short)0);

    private Object _oid;
    private final OpenJPAStateManager _owner;
    private final ValueMetaData _vmd;

    /**
     * Constructor; supply embedded object id and its owner.
     *
     * @param owner may be null
     */
    public ObjectIdStateManager(Object oid, OpenJPAStateManager owner,
        ValueMetaData ownerVal) {
        _oid = oid;
        _owner = owner;
        _vmd = ownerVal;
    }

    public Object getGenericContext() {
        return (_owner == null) ? null : _owner.getGenericContext();
    }

    public Object getPCPrimaryKey(Object oid, int field) {
        throw new UnsupportedOperationException();
    }

    public StateManager replaceStateManager(StateManager sm) {
        throw new UnsupportedOperationException();
    }

    public Object getVersion() {
        return null;
    }

    public void setVersion(Object version) {
        throw new UnsupportedOperationException();
    }

    public boolean isDirty() {
        return false;
    }

    public boolean isTransactional() {
        return false;
    }

    public boolean isPersistent() {
        return false;
    }

    public boolean isNew() {
        return false;
    }

    public boolean isDeleted() {
        return false;
    }

    public boolean isDetached() {
        return true;
    }

    public boolean isVersionUpdateRequired() {
        return false;
    }

    public boolean isVersionCheckRequired() {
        return false;
    }

    public void dirty(String field) {
        throw new UnsupportedOperationException();
    }

    public Object fetchObjectId() {
        return null;
    }

    public void accessingField(int idx) {
        throw new UnsupportedOperationException();
    }

    public boolean serializing() {
        throw new UnsupportedOperationException();
    }

    public boolean writeDetached(ObjectOutput out)
        throws IOException {
        throw new UnsupportedOperationException();
    }

    public void proxyDetachedDeserialized(int idx) {
        throw new UnsupportedOperationException();
    }

    public void settingBooleanField(PersistenceCapable pc, int idx,
        boolean cur, boolean next, int set) {
        throw new UnsupportedOperationException();
    }

    public void settingCharField(PersistenceCapable pc, int idx, char cur,
        char next, int set) {
        throw new UnsupportedOperationException();
    }

    public void settingByteField(PersistenceCapable pc, int idx, byte cur,
        byte next, int set) {
        throw new UnsupportedOperationException();
    }

    public void settingShortField(PersistenceCapable pc, int idx, short cur,
        short next, int set) {
        throw new UnsupportedOperationException();
    }

    public void settingIntField(PersistenceCapable pc, int idx, int cur,
        int next, int set) {
        throw new UnsupportedOperationException();
    }

    public void settingLongField(PersistenceCapable pc, int idx, long cur,
        long next, int set) {
        throw new UnsupportedOperationException();
    }

    public void settingFloatField(PersistenceCapable pc, int idx, float cur,
        float next, int set) {
        throw new UnsupportedOperationException();
    }

    public void settingDoubleField(PersistenceCapable pc, int idx, double cur,
        double next, int set) {
        throw new UnsupportedOperationException();
    }

    public void settingStringField(PersistenceCapable pc, int idx, String cur,
        String next, int set) {
        throw new UnsupportedOperationException();
    }

    public void settingObjectField(PersistenceCapable pc, int idx, Object cur,
        Object next, int set) {
        throw new UnsupportedOperationException();
    }

    public void providedBooleanField(PersistenceCapable pc, int idx,
        boolean cur) {
        throw new UnsupportedOperationException();
    }

    public void providedCharField(PersistenceCapable pc, int idx, char cur) {
        throw new UnsupportedOperationException();
    }

    public void providedByteField(PersistenceCapable pc, int idx, byte cur) {
        throw new UnsupportedOperationException();
    }

    public void providedShortField(PersistenceCapable pc, int idx, short cur) {
        throw new UnsupportedOperationException();
    }

    public void providedIntField(PersistenceCapable pc, int idx, int cur) {
        throw new UnsupportedOperationException();
    }

    public void providedLongField(PersistenceCapable pc, int idx, long cur) {
        throw new UnsupportedOperationException();
    }

    public void providedFloatField(PersistenceCapable pc, int idx, float cur) {
        throw new UnsupportedOperationException();
    }

    public void providedDoubleField(PersistenceCapable pc, int idx,
        double cur) {
        throw new UnsupportedOperationException();
    }

    public void providedStringField(PersistenceCapable pc, int idx,
        String cur) {
        throw new UnsupportedOperationException();
    }

    public void providedObjectField(PersistenceCapable pc, int idx,
        Object cur) {
        throw new UnsupportedOperationException();
    }

    public boolean replaceBooleanField(PersistenceCapable pc, int idx) {
        throw new UnsupportedOperationException();
    }

    public char replaceCharField(PersistenceCapable pc, int idx) {
        throw new UnsupportedOperationException();
    }

    public byte replaceByteField(PersistenceCapable pc, int idx) {
        throw new UnsupportedOperationException();
    }

    public short replaceShortField(PersistenceCapable pc, int idx) {
        throw new UnsupportedOperationException();
    }

    public int replaceIntField(PersistenceCapable pc, int idx) {
        throw new UnsupportedOperationException();
    }

    public long replaceLongField(PersistenceCapable pc, int idx) {
        throw new UnsupportedOperationException();
    }

    public float replaceFloatField(PersistenceCapable pc, int idx) {
        throw new UnsupportedOperationException();
    }

    public double replaceDoubleField(PersistenceCapable pc, int idx) {
        throw new UnsupportedOperationException();
    }

    public String replaceStringField(PersistenceCapable pc, int idx) {
        throw new UnsupportedOperationException();
    }

    public Object replaceObjectField(PersistenceCapable pc, int idx) {
        throw new UnsupportedOperationException();
    }

    ///////////////////////////////////
    // OpenJPAStateManager implementation
    ///////////////////////////////////

    public void initialize(Class forType, PCState state) {
        throw new UnsupportedOperationException();
    }

    public void load(FetchConfiguration fetch) {
        throw new UnsupportedOperationException();
    }

    public Object getManagedInstance() {
        return _oid;
    }

    public PersistenceCapable getPersistenceCapable() {
        return ImplHelper.toPersistenceCapable(_oid,
            _vmd.getRepository().getConfiguration());
    }

    public ClassMetaData getMetaData() {
        return _vmd.getEmbeddedMetaData();
    }

    public OpenJPAStateManager getOwner() {
        return _owner;
    }

    public int getOwnerIndex() {
        return _vmd.getFieldMetaData().getIndex();
    }

    public boolean isEmbedded() {
        return true;
    }

    public boolean isFlushed() {
        return false;
    }

    public boolean isFlushedDirty() {
        return false;
    }

    public boolean isProvisional() {
        return false;
    }

    public BitSet getLoaded() {
        throw new UnsupportedOperationException();
    }

    public BitSet getDirty() {
        throw new UnsupportedOperationException();
    }

    public BitSet getFlushed() {
        throw new UnsupportedOperationException();
    }

    public BitSet getUnloaded(FetchConfiguration fetch) {
        throw new UnsupportedOperationException();
    }

    public Object newProxy(int field) {
        throw new UnsupportedOperationException();
    }

    public Object newFieldProxy(int field) {
        throw new UnsupportedOperationException();
    }

    public boolean isDefaultValue(int field) {
        Object val = getValue(field);
        if (val == null)
            return true;

        FieldMetaData fmd = getMetaData().getField(field);
        switch (fmd.getDeclaredTypeCode()) {
            case JavaTypes.BOOLEAN:
                return Boolean.FALSE.equals(val);
            case JavaTypes.CHAR:
                return ((Character) val).charValue() == 0;
            case JavaTypes.BYTE:
            case JavaTypes.DOUBLE:
            case JavaTypes.FLOAT:
            case JavaTypes.INT:
            case JavaTypes.LONG:
            case JavaTypes.SHORT:
                return ((Number) val).intValue() == 0;
            case JavaTypes.STRING:
                return "".equals(val);
            default:
                return false;
        }
    }

    public StoreContext getContext() {
        return (_owner == null) ? null : _owner.getContext();
    }

    public PCState getPCState() {
        throw new UnsupportedOperationException();
    }

    public Object getObjectId() {
        return null;
    }

    public void setObjectId(Object oid) {
        throw new UnsupportedOperationException();
    }

    public boolean assignObjectId(boolean flush) {
        throw new UnsupportedOperationException();
    }

    public Object getId() {
        return null;
    }

    public Object getLock() {
        return null;
    }

    public void setLock(Object lock) {
        throw new UnsupportedOperationException();
    }

    public void setNextVersion(Object version) {
        throw new UnsupportedOperationException();
    }

    public Object getImplData() {
        return null;
    }

    public Object setImplData(Object data, boolean cacheable) {
        throw new UnsupportedOperationException();
    }

    public boolean isImplDataCacheable() {
        return false;
    }

    public Object getImplData(int field) {
        return null;
    }

    public Object setImplData(int field, Object data) {
        throw new UnsupportedOperationException();
    }

    public boolean isImplDataCacheable(int field) {
        return false;
    }

    public Object getIntermediate(int field) {
        return null;
    }

    public void setIntermediate(int field, Object data) {
        throw new UnsupportedOperationException();
    }

    public void removed(int field, Object removed, boolean key) {
        throw new UnsupportedOperationException();
    }

    public boolean beforeRefresh(boolean all) {
        throw new UnsupportedOperationException();
    }

    public void dirty(int field) {
        throw new UnsupportedOperationException();
    }

    public void storeBoolean(int field, boolean extVal) {
        setValue(field, (extVal) ? Boolean.TRUE : Boolean.FALSE, true);
    }

    public void storeByte(int field, byte extVal) {
        setValue(field, Byte.valueOf(extVal), true);
    }

    public void storeChar(int field, char extVal) {
        setValue(field, Character.valueOf(extVal), true);
    }

    public void storeInt(int field, int extVal) {
        setValue(field, extVal, true);
    }

    public void storeShort(int field, short extVal) {
        setValue(field, Short.valueOf(extVal), true);
    }

    public void storeLong(int field, long extVal) {
        setValue(field, extVal, true);
    }

    public void storeFloat(int field, float extVal) {
        setValue(field, Float.valueOf(extVal), true);
    }

    public void storeDouble(int field, double extVal) {
        setValue(field, Double.valueOf(extVal), true);
    }

    public void storeString(int field, String extVal) {
        setValue(field, extVal, extVal != null);
    }

    public void storeObject(int field, Object extVal) {
        setValue(field, extVal, extVal != null);
    }

    public void store(int field, Object extVal) {
        boolean forceInst = true;
        if (extVal == null) {
            extVal = getDefaultValue(field);
            forceInst = false;
        }
        setValue(field, extVal, forceInst);
    }

    public void storeBooleanField(int field, boolean extVal) {
        storeBoolean(field, extVal);
    }

    public void storeByteField(int field, byte extVal) {
        storeByte(field, extVal);
    }

    public void storeCharField(int field, char extVal) {
        storeChar(field, extVal);
    }

    public void storeIntField(int field, int extVal) {
        storeInt(field, extVal);
    }

    public void storeShortField(int field, short extVal) {
        storeShort(field, extVal);
    }

    public void storeLongField(int field, long extVal) {
        storeLong(field, extVal);
    }

    public void storeFloatField(int field, float extVal) {
        storeFloat(field, extVal);
    }

    public void storeDoubleField(int field, double extVal) {
        storeDouble(field, extVal);
    }

    public void storeStringField(int field, String extVal) {
        storeString(field, extVal);
    }

    public void storeObjectField(int field, Object extVal) {
        storeObject(field, extVal);
    }

    public void storeField(int field, Object value) {
        store(field, value);
    }

    public boolean fetchBoolean(int field) {
        return ((Boolean) getValue(field)).booleanValue();
    }

    public byte fetchByte(int field) {
        return ((Number) getValue(field)).byteValue();
    }

    public char fetchChar(int field) {
        return ((Character) getValue(field)).charValue();
    }

    public short fetchShort(int field) {
        return ((Number) getValue(field)).shortValue();
    }

    public int fetchInt(int field) {
        return ((Number) getValue(field)).intValue();
    }

    public long fetchLong(int field) {
        return ((Number) getValue(field)).longValue();
    }

    public float fetchFloat(int field) {
        return ((Number) getValue(field)).floatValue();
    }

    public double fetchDouble(int field) {
        return ((Number) getValue(field)).doubleValue();
    }

    public String fetchString(int field) {
        return (String) getValue(field);
    }

    public Object fetchObject(int field) {
        return getValue(field);
    }

    public Object fetch(int field) {
        Object ret = getValue(field);
        if (ret == null)
            ret = getDefaultValue(field);
        return ret;
    }

    public boolean fetchBooleanField(int field) {
        return fetchBoolean(field);
    }

    public byte fetchByteField(int field) {
        return fetchByte(field);
    }

    public char fetchCharField(int field) {
        return fetchChar(field);
    }

    public short fetchShortField(int field) {
        return fetchShort(field);
    }

    public int fetchIntField(int field) {
        return fetchInt(field);
    }

    public long fetchLongField(int field) {
        return fetchLong(field);
    }

    public float fetchFloatField(int field) {
        return fetchFloat(field);
    }

    public double fetchDoubleField(int field) {
        return fetchDouble(field);
    }

    public String fetchStringField(int field) {
        return fetchString(field);
    }

    public Object fetchObjectField(int field) {
        return fetch(field);
    }

    public Object fetchField(int field, boolean transitions) {
        return fetch(field);
    }

    public Object fetchInitialField(int field) {
        throw new UnsupportedOperationException();
    }

    public void setRemote(int field, Object value) {
        store(field, value);
    }

    public void lock() {
    }

    public void unlock() {
    }

    /**
     * Return the default value of the given field based on its type.
     */
    private Object getDefaultValue(int field) {
        FieldMetaData fmd = getMetaData().getField(field);
        switch (fmd.getDeclaredTypeCode()) {
            case JavaTypes.BOOLEAN:
                return Boolean.FALSE;
            case JavaTypes.BYTE:
                return ZERO_BYTE;
            case JavaTypes.CHAR:
                return ZERO_CHAR;
            case JavaTypes.DOUBLE:
                return ZERO_DOUBLE;
            case JavaTypes.FLOAT:
                return ZERO_FLOAT;
            case JavaTypes.INT:
                return 0;
            case JavaTypes.LONG:
                return 0L;
            case JavaTypes.SHORT:
                return ZERO_SHORT;
            default:
                return null;
        }
    }

    /**
     * Return the value of the given field using reflection.
     * Relies on the fact that all oid fields/properties are made public
     * during enhancement.
     */
    private Object getValue(int field) {
        if (_oid == null)
            return null;

        FieldMetaData fmd = getMetaData().getField(field);
        Object val = null;
        if (fmd.getBackingMember() instanceof Field) 
            val = Reflection.get(_oid, (Field) fmd.getBackingMember());
        else if (fmd.getBackingMember() instanceof Method) 
            val = Reflection.get(_oid, (Method) fmd.getBackingMember());
        else if (AccessCode.isField(fmd.getDefiningMetaData().getAccessType()))
            val = Reflection.get(_oid, Reflection.findField(_oid.getClass(), 
                fmd.getName(), true));
        else 
            val = Reflection.get(_oid, Reflection.findGetter(_oid.getClass(),
            fmd.getName(), true));

        if (fmd.getValue().getEmbeddedMetaData() != null) 
            return new ObjectIdStateManager(val, null, fmd);
        return val;
    }

    /**
     * Set the value of the given field using reflection.
     * Relies on the fact that all oid fields/properties are made public
     * during enhancement.
     */
    private void setValue(int field, Object val, boolean forceInst) {
        if (_oid == null && forceInst) {
            try {
                _oid = AccessController.doPrivileged(
                    J2DoPrivHelper.newInstanceAction(
                        getMetaData().getDescribedType()));
            } catch (Exception e) {
                if (e instanceof PrivilegedActionException)
                    e = ((PrivilegedActionException) e).getException();
                throw new GeneralException(e);
            }
        } else if (_oid == null)
            return;

        FieldMetaData fmd = getMetaData().getField(field);
        if (fmd.getBackingMember() instanceof Field)
            Reflection.set(_oid, (Field) fmd.getBackingMember(), val);
        else if (AccessCode.isField(fmd.getDefiningMetaData().getAccessType()))
            Reflection.set(_oid, Reflection.findField(_oid.getClass(), 
                fmd.getName(), true), val);
        else
            Reflection.set(_oid, Reflection.findSetter(_oid.getClass(),
                fmd.getName(), fmd.getDeclaredType(), true), val);
	}
    
    @Override
    public boolean isDelayed(int field) {
        return false;
    }

    @Override
    public void setDelayed(int field, boolean delay) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadDelayedField(int field) {
        throw new UnsupportedOperationException();
    }
}
