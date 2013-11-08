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
package org.apache.openjpa.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.Date;

import org.apache.openjpa.enhance.FieldManager;
import org.apache.openjpa.enhance.PCRegistry;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.Reflection;
import org.apache.openjpa.kernel.ObjectIdStateManager;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StateManagerImpl;
import org.apache.openjpa.kernel.StoreManager;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.AccessCode;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.ValueStrategies;

/**
 * Utility class for manipulating application object ids.
 *
 * @author Abe White
 * @nojavadoc
 */
public class ApplicationIds {

    private static final Localizer _loc = Localizer.forPackage
        (ApplicationIds.class);
    private static final Localizer _loc2 = Localizer.forPackage
        (StateManagerImpl.class);

    /**
     * Return the primary key values for the given object id. The values
     * will be returned in the same order as the metadata primary key fields.
     * Values for PC primary key fields will be the primary key value or
     * oid value of the related instance (depending on 
     * {@link FieldMetaData#isObjectIdFieldIdOfPC}).
     */
    public static Object[] toPKValues(Object oid, ClassMetaData meta) {
        if (meta == null)
            return null;

        Object[] pks;
        if (meta.isOpenJPAIdentity()) {
            pks = new Object[1];
            if (oid != null)
                pks[0] = ((OpenJPAId) oid).getIdObject();
            return pks;
        }

        // reset owning 'meta' to the owner of the primary key fields, because
        // the one passed in might be a proxy, like for embedded mappings;
        // since getPrimaryKeyFields is guaranteed to return the primary
        // keys in the order of inheritance, we are guaranteed that
        // the last element will be the most-derived class.
        FieldMetaData[] fmds = meta.getPrimaryKeyFields();
        meta = fmds[fmds.length - 1].getDeclaringMetaData();
        pks = new Object[fmds.length];
        if (oid == null)
            return pks;

        if (!Modifier.isAbstract(meta.getDescribedType().getModifiers())) {
            // copy fields from the oid
            PrimaryKeyFieldManager consumer = new PrimaryKeyFieldManager();
            consumer.setStore(pks);
            oid = wrap(meta, oid);
            PCRegistry.copyKeyFieldsFromObjectId(meta.getDescribedType(),
                consumer, oid);
            return consumer.getStore();
        }

        // default to reflection
        if (meta.isObjectIdTypeShared())
            oid = ((ObjectId) oid).getId();
        Class<?> oidType = oid.getClass();
        for (int i = 0; i < fmds.length; i++) {
            if (AccessCode.isField(meta.getAccessType()))
                pks[i] = Reflection.get(oid, Reflection.findField(oidType, 
                    fmds[i].getName(), true));
            else
                pks[i] = Reflection.get(oid, Reflection.findGetter(oidType,
                    fmds[i].getName(), true));
        }
        return pks;
    }
    
    /**
     * Wraps the given object for the given type into a OpenJPA specific 
     * application identity object wrapper instance (i.e. ObjectId) if all of 
     * the following is true:
     * the given type is not using built-in OpenJPA identity types
     * the given type is using a shared OpenJPA identity type
     * the given object is not already a wrapper identity type
     */
    public static Object wrap(ClassMetaData meta, Object oid) {
        if (!meta.isOpenJPAIdentity() 
         && meta.isObjectIdTypeShared() 
         && !(oid instanceof ObjectId)) {
        	return new ObjectId(meta.getDescribedType(), oid);
        } 
        return oid;
    }
    

    /**
     * Return a new object id constructed from the given primary key values.
     * Values for PC primary key fields should be the primary key value or
     * oid value of the related instance (depending on 
     * {@link FieldMetaData#isObjectIdFieldIdOfPC}).
     */
    public static Object fromPKValues(Object[] pks, ClassMetaData meta) {
        if (meta == null || pks == null)
            return null;

        boolean convert = !meta.getRepository().getConfiguration().
            getCompatibilityInstance().getStrictIdentityValues();
        if (meta.isOpenJPAIdentity()) {
            int type = meta.getPrimaryKeyFields()[0].getObjectIdFieldTypeCode();
            Object val = (convert) ? JavaTypes.convert(pks[0], type) : pks[0];
            switch (type) {
                case JavaTypes.BYTE:
                case JavaTypes.BYTE_OBJ:
                    if (!convert && !(val instanceof Byte))
                        throw new ClassCastException("!(x instanceof Byte)");
                    return new ByteId(meta.getDescribedType(),
                        val == null ? 0 : ((Number) val).byteValue());
                case JavaTypes.CHAR:
                case JavaTypes.CHAR_OBJ:
                    return new CharId(meta.getDescribedType(),
                        val == null ? 0 : ((Character) val).charValue());
                case JavaTypes.DOUBLE:
                case JavaTypes.DOUBLE_OBJ:
                    if (!convert && !(val instanceof Double))
                        throw new ClassCastException("!(x instanceof Double)");
                    return new DoubleId(meta.getDescribedType(),
                        val == null ? 0 : ((Number) val).doubleValue());
                case JavaTypes.FLOAT:
                case JavaTypes.FLOAT_OBJ:
                    if (!convert && !(val instanceof Float))
                        throw new ClassCastException("!(x instanceof Float)");
                    return new FloatId(meta.getDescribedType(),
                        val == null ? 0 : ((Number) val).floatValue());
                case JavaTypes.INT:
                case JavaTypes.INT_OBJ:
                    if (!convert && !(val instanceof Integer))
                        throw new ClassCastException("!(x instanceof Integer)");
                    return new IntId(meta.getDescribedType(),
                        val == null ? 0 : ((Number) val).intValue());
                case JavaTypes.LONG:
                case JavaTypes.LONG_OBJ:
                    if (!convert && !(val instanceof Long))
                        throw new ClassCastException("!(x instanceof Long)");
                    return new LongId(meta.getDescribedType(),
                        val == null ? 0 : ((Number) val).longValue());
                case JavaTypes.SHORT:
                case JavaTypes.SHORT_OBJ:
                    if (!convert && !(val instanceof Short))
                        throw new ClassCastException("!(x instanceof Short)");
                    return new ShortId(meta.getDescribedType(),
                        val == null ? 0 : ((Number) val).shortValue());
                case JavaTypes.STRING:
                    return new StringId(meta.getDescribedType(), (String) val);
                case JavaTypes.DATE:
                    return new DateId(meta.getDescribedType(), (Date) val);
                case JavaTypes.OID:
                case JavaTypes.OBJECT:
                    return new ObjectId(meta.getDescribedType(), val);
                case JavaTypes.BIGDECIMAL:
                    if (!convert && !(val instanceof BigDecimal))
                        throw new ClassCastException(
                            "!(x instanceof BigDecimal)");
                    return new BigDecimalId(meta.getDescribedType(), 
                        (BigDecimal)val);
                case JavaTypes.BIGINTEGER:
                    if (!convert && !(val instanceof BigInteger))
                        throw new ClassCastException(
                            "!(x instanceof BigInteger)");
                    return new BigIntegerId(meta.getDescribedType(), 
                        (BigInteger)val);
                case JavaTypes.BOOLEAN:
                case JavaTypes.BOOLEAN_OBJ:
                    if (!convert && !(val instanceof Boolean))
                        throw new ClassCastException("!(x instanceof Boolean)");
                    return new BooleanId(meta.getDescribedType(), 
                        val == null ? false : (Boolean)val);
                default:
                    throw new InternalException();
            }
        }

        // copy pks to oid
        if (!Modifier.isAbstract(meta.getDescribedType().getModifiers())) {
            Object oid = PCRegistry.newObjectId(meta.getDescribedType());
            PrimaryKeyFieldManager producer = new PrimaryKeyFieldManager();
            producer.setStore(pks);
            if (convert)
                producer.setMetaData(meta);
            PCRegistry.copyKeyFieldsToObjectId(meta.getDescribedType(),
                producer, oid);
            return ApplicationIds.wrap(meta, oid);
        }

        // default to reflection
        Class<?> oidType = meta.getObjectIdType();
        if (Modifier.isAbstract(oidType.getModifiers()))
            throw new UserException(_loc.get("objectid-abstract", meta));
        Object copy = null;
        try {
            copy = AccessController.doPrivileged(
                J2DoPrivHelper.newInstanceAction(oidType));
        } catch (Throwable t) {
            if (t instanceof PrivilegedActionException)
                t = ((PrivilegedActionException) t).getException();
            throw new GeneralException(t);
        }

        FieldMetaData[] fmds = meta.getPrimaryKeyFields();
        Object val;
        for (int i = 0; i < fmds.length; i++) {
            val = (convert) ? JavaTypes.convert(pks[i],
                fmds[i].getObjectIdFieldTypeCode()) : pks[i];
            if (AccessCode.isField(meta.getAccessType()))
                Reflection.set(copy, Reflection.findField(oidType, 
                    fmds[i].getName(), true), val); 
            else
                Reflection.set(copy, Reflection.findSetter(oidType, 
                    fmds[i].getName(), fmds[i].getDeclaredType(), true), val);
        }

        if (meta.isObjectIdTypeShared())
            copy = new ObjectId(meta.getDescribedType(), copy);
        return copy;
    }

    /**
     * Copy the given oid value.
     */
    public static Object copy(Object oid, ClassMetaData meta) {
        if (meta == null || oid == null)
            return null;

        if (meta.isOpenJPAIdentity()) {
            // use meta type instead of oid type in case it's a subclass
            Class<?> cls = meta.getDescribedType();
            OpenJPAId koid = (OpenJPAId) oid;
            FieldMetaData pk = meta.getPrimaryKeyFields()[0];
            switch (pk.getObjectIdFieldTypeCode()) {
                case JavaTypes.BYTE:
                case JavaTypes.BYTE_OBJ:
                    return new ByteId(cls, ((ByteId) oid).getId(),
                        koid.hasSubclasses());
                case JavaTypes.CHAR:
                case JavaTypes.CHAR_OBJ:
                    return new CharId(cls, ((CharId) oid).getId(),
                        koid.hasSubclasses());
                case JavaTypes.DOUBLE:
                case JavaTypes.DOUBLE_OBJ:
                    return new DoubleId(cls, ((DoubleId) oid).getId(),
                        koid.hasSubclasses());
                case JavaTypes.FLOAT:
                case JavaTypes.FLOAT_OBJ:
                    return new FloatId(cls, ((FloatId) oid).getId(),
                        koid.hasSubclasses());
                case JavaTypes.INT:
                case JavaTypes.INT_OBJ:
                    return new IntId(cls, ((IntId) oid).getId(),
                        koid.hasSubclasses());
                case JavaTypes.LONG:
                case JavaTypes.LONG_OBJ:
                    return new LongId(cls, ((LongId) oid).getId(),
                        koid.hasSubclasses());
                case JavaTypes.SHORT:
                case JavaTypes.SHORT_OBJ:
                    return new ShortId(cls, ((ShortId) oid).getId(),
                        koid.hasSubclasses());
                case JavaTypes.STRING:
                    return new StringId(cls, oid.toString(),
                        koid.hasSubclasses());
                case JavaTypes.OID:
                    ClassMetaData embed = pk.getEmbeddedMetaData();
                    Object inner = koid.getIdObject();
                    if (embed != null)
                        inner = copy(inner, embed, embed.getFields());
                    return new ObjectId(cls, inner, koid.hasSubclasses());
                case JavaTypes.OBJECT:
                    return new ObjectId(cls, koid.getIdObject(), 
                        koid.hasSubclasses());
                case JavaTypes.DATE:
                    return new DateId(cls, ((DateId) oid).getId(),
                        koid.hasSubclasses());
                case JavaTypes.BIGDECIMAL:
                    return new BigDecimalId(cls, ((BigDecimalId) oid).getId(),
                        koid.hasSubclasses());
                case JavaTypes.BIGINTEGER:
                    return new BigIntegerId(cls, ((BigIntegerId) oid).getId(),
                        koid.hasSubclasses());
                default:
                    throw new InternalException();
            }
        }

        // create a new pc instance of the right type, set its key fields
        // to the original oid values, then copy its key fields to a new
        // oid instance
        if (!Modifier.isAbstract(meta.getDescribedType().getModifiers())
            && !hasPCPrimaryKeyFields(meta)) {
            Class<?> type = meta.getDescribedType();
            PersistenceCapable pc = PCRegistry.newInstance(type, null, oid, 
                 false);
            Object copy = pc.pcNewObjectIdInstance();
            pc.pcCopyKeyFieldsToObjectId(copy);
            return copy;
        }

        Object copy = (!meta.isObjectIdTypeShared()) ? oid
            : ((ObjectId) oid).getId();
        copy = copy(copy, meta, meta.getPrimaryKeyFields());
        if (meta.isObjectIdTypeShared())
            copy = new ObjectId(meta.getDescribedType(), copy,
                ((OpenJPAId) oid).hasSubclasses());
        return copy;
    }

    /**
     * Return true if any of the given type's primary key fields are 
     * persistent objects.
     */
    private static boolean hasPCPrimaryKeyFields(ClassMetaData meta) {
        FieldMetaData[] fmds = meta.getPrimaryKeyFields();
        for (int i = 0; i < fmds.length; i++)
            if (fmds[i].getDeclaredTypeCode() == JavaTypes.PC)
                return true;
        return false;
    }

    /**
     * Copy the given identity object using reflection.
     */
    private static Object copy(Object oid, ClassMetaData meta,
        FieldMetaData[] fmds) {
        if (oid == null)
            return null;

        Class<?> oidType = oid.getClass();
        Object copy = null;
        try {
            copy = AccessController.doPrivileged(
                J2DoPrivHelper.newInstanceAction(oidType));
        } catch (Throwable t) {
            if (t instanceof PrivilegedActionException)
                t = ((PrivilegedActionException) t).getException();
            throw new GeneralException(t);
        }

        Field field;
        Object val;
        for (int i = 0; i < fmds.length; i++) {
            if (fmds[i].getManagement() != FieldMetaData.MANAGE_PERSISTENT)
                continue;

            if (AccessCode.isField(meta.getAccessType())) {
                    field = Reflection.findField(oidType, fmds[i].getName(),
                        true);
                    Reflection.set(copy, field, Reflection.get(oid, field));
                } else { // property
                    val = Reflection.get(oid, Reflection.findGetter(oidType,
                        fmds[i].getName(), true));
                    Reflection.set(copy, Reflection.findSetter(oidType, fmds[i].
                        getName(), fmds[i].getObjectIdFieldType(), true), val);
                }
            }
            return copy;
    }

    /**
     * Return the given primary key field value from the given oid.
     */
    public static Object get(Object oid, FieldMetaData fmd) {
        if (oid == null)
            return null;
        if (oid instanceof OpenJPAId)
            return ((OpenJPAId) oid).getIdObject();

        ClassMetaData meta = fmd.getDefiningMetaData();
        Class<?> oidType = oid.getClass();
        if (AccessCode.isField(meta.getAccessType()))
            return Reflection.get(oid, Reflection.findField(oidType, 
                fmd.getName(), true));
        return Reflection.get(oid, Reflection.findGetter(oidType, fmd.getName(),
            true));
    }

    /**
     * Generate an application id based on the current primary key field state
     * of the given instance.
     */
    public static Object create(PersistenceCapable pc, ClassMetaData meta) {
        if (pc == null)
            return null;

        Object oid = pc.pcNewObjectIdInstance();
        if (oid == null)
            return null;

        if (!meta.isOpenJPAIdentity()) {
            pc.pcCopyKeyFieldsToObjectId(oid);
            return oid;
        }

        FieldMetaData pk = meta.getPrimaryKeyFields()[0];
        if (pk.getDeclaredTypeCode() != JavaTypes.OID)
            return oid;

        // always copy oid object in case field value mutates or becomes
        // managed
        ObjectId objid = (ObjectId) oid;
        ClassMetaData embed = pk.getEmbeddedMetaData();
        objid.setId(copy(objid.getId(), embed, embed.getFields()));
        return objid;
    }

    /**
     * Assign an application identity object to the given state, or return
     * false if determining the application identity requires a flush.
     */
    public static boolean assign(OpenJPAStateManager sm, StoreManager store,
        boolean preFlush) {
        ClassMetaData meta = sm.getMetaData();
        if (meta.getIdentityType() != ClassMetaData.ID_APPLICATION)
            throw new InternalException();

        boolean ret;
        FieldMetaData[] pks = meta.getPrimaryKeyFields();
        if (meta.isOpenJPAIdentity()
            && pks[0].getDeclaredTypeCode() == JavaTypes.OID) {
            OpenJPAStateManager oidsm = new ObjectIdStateManager
                (sm.fetchObjectField(pks[0].getIndex()), sm, pks[0]);
            ret = assign(oidsm, store, pks[0].getEmbeddedMetaData().
                getFields(), preFlush);
            sm.storeObjectField(pks[0].getIndex(),
                oidsm.getManagedInstance());
        } else
            ret = assign(sm, store, meta.getPrimaryKeyFields(), preFlush);
        if (!ret)
            return false;

        // base oid on field values
        sm.setObjectId(create(sm.getPersistenceCapable(), meta));
        return true;
    }

    /**
     * Assign generated values to given primary key fields.
     */
    private static boolean assign(OpenJPAStateManager sm, StoreManager store,
        FieldMetaData[] pks, boolean preFlush) {
        for (int i = 0; i < pks.length; i++)
            // If we are generating values...
            if (pks[i].getValueStrategy() != ValueStrategies.NONE) {
                // If a value already exists on this field, throw exception.
                // This is considered an application coding error.
                if (!sm.isDefaultValue(pks[i].getIndex()))
                    throw new InvalidStateException(_loc2.get("existing-value-override-excep", 
                            pks[i].getFullName(false), Exceptions.toString(sm.getPersistenceCapable()),
                            sm.getPCState().getClass().getSimpleName()));
                // Assign the generated value
                if (store.assignField(sm, pks[i].getIndex(), preFlush))
                    pks[i].setValueGenerated(true);
                else
                    return false;
            }
        return true;
    }

    /**
     * Check if object id is set or not. 
     */
    public static boolean isIdSet(Object id, ClassMetaData meta, 
        String mappedByIdFieldName) {
        Object key = null;
        if (meta.isOpenJPAIdentity())
            key = ApplicationIds.getKey(id, meta);
        else
            key = ((ObjectId)id).getId();
        Object val = null;
        if (mappedByIdFieldName.length() != 0) {
            if (((ObjectId)id).getId() == null)
                return false;        	
            Class<?> idClass = ((ObjectId)id).getId().getClass();
            val = Reflection.get(key, 
                    Reflection.findField(idClass, mappedByIdFieldName, true)); 
        } else
            val = key;
        
        boolean notSet = (val == null  
                || (val instanceof String && ((String)val).length() == 0)
                || (val instanceof Number && ((Number)val).longValue() == 0));
        return !notSet;
    }
    
    /**
     * Return the key from the given id. 
     */
    public static Object getKey(Object id, ClassMetaData meta) {
        if (meta == null || id == null)
            return null;

        if (meta.isOpenJPAIdentity()) {
            int type = meta.getPrimaryKeyFields()[0].getObjectIdFieldTypeCode();
            switch (type) {
                case JavaTypes.BYTE:
                case JavaTypes.BYTE_OBJ:
                    return ((ByteId)id).getId();
                case JavaTypes.CHAR:
                case JavaTypes.CHAR_OBJ:
                    return ((CharId)id).getId();
                case JavaTypes.DOUBLE:
                case JavaTypes.DOUBLE_OBJ:
                    return ((DoubleId)id).getId();
                case JavaTypes.FLOAT:
                case JavaTypes.FLOAT_OBJ:
                    return ((FloatId)id).getId();
                case JavaTypes.INT:
                case JavaTypes.INT_OBJ:
                    return ((IntId)id).getId();
                case JavaTypes.LONG:
                case JavaTypes.LONG_OBJ:
                    return ((LongId)id).getId();
                case JavaTypes.SHORT:
                case JavaTypes.SHORT_OBJ:
                    return ((ShortId)id).getId();
                case JavaTypes.STRING:
                    return ((StringId)id).getId();
                case JavaTypes.DATE:
                    return ((DateId)id).getId();
                case JavaTypes.OID:
                case JavaTypes.OBJECT:
                    return ((ObjectId)id).getId();
                case JavaTypes.BIGDECIMAL:
                    return ((BigDecimalId)id).getId(); 
                case JavaTypes.BIGINTEGER:
                    return ((BigIntegerId)id).getId();
                default:
                    throw new InternalException();
            }
        } else { // IdClass
            return ((ObjectId)id).getId();
        }

    }

    /**
     * Sets the underlying id of an ObjectId.  Should only
     * be used with simple (idclass) types.
     */
    public static void setAppId(ObjectId id, Object newId) {
        id.setId(newId);
    }

    /**
     * Helper class used to transfer pk values to/from application oids.
     */
    private static class PrimaryKeyFieldManager
        implements FieldManager {

        private Object[] _store = null;
        private int _index = 0;
        private ClassMetaData _meta = null;

        public void setMetaData(ClassMetaData meta) {
            _meta = meta;
        }

        public Object[] getStore() {
            return _store;
        }

        public void setStore(Object[] store) {
            _store = store;
        }

        public void storeBooleanField(int field, boolean val) {
            store((val) ? Boolean.TRUE : Boolean.FALSE);
        }

        public void storeByteField(int field, byte val) {
            store(Byte.valueOf(val));
        }

        public void storeCharField(int field, char val) {
            store(Character.valueOf(val));
        }

        public void storeShortField(int field, short val) {
            store(Short.valueOf(val));
        }

        public void storeIntField(int field, int val) {
            store(val);
        }

        public void storeLongField(int field, long val) {
            store(val);
        }

        public void storeFloatField(int field, float val) {
            store(Float.valueOf(val));
        }

        public void storeDoubleField(int field, double val) {
            store(Double.valueOf(val));
        }

        public void storeStringField(int field, String val) {
            store(val);
        }

        public void storeObjectField(int field, Object val) {
            store(val);
        }

        public boolean fetchBooleanField(int field) {
            return (retrieve(field) == Boolean.TRUE) ? true : false;
        }

        public char fetchCharField(int field) {
            return ((Character) retrieve(field)).charValue();
        }

        public byte fetchByteField(int field) {
            return ((Number) retrieve(field)).byteValue();
        }

        public short fetchShortField(int field) {
            return ((Number) retrieve(field)).shortValue();
        }

        public int fetchIntField(int field) {
            return ((Number) retrieve(field)).intValue();
        }

        public long fetchLongField(int field) {
            return ((Number) retrieve(field)).longValue();
        }

        public float fetchFloatField(int field) {
            return ((Number) retrieve(field)).floatValue();
        }

        public double fetchDoubleField(int field) {
            return ((Number) retrieve(field)).doubleValue();
        }

        public String fetchStringField(int field) {
            return (String) retrieve(field);
        }

        public Object fetchObjectField(int field) {
            return retrieve(field);
        }

        private void store(Object val) {
            _store[_index++] = val;
        }

        private Object retrieve(int field) {
            Object val = _store[_index++];
            if (_meta != null) {
                FieldMetaData fmd = _meta.getField(field);
                if (fmd.getDeclaredTypeCode() != JavaTypes.PC)
                    val = JavaTypes.convert(val, fmd.getDeclaredTypeCode());
                else
                    val = JavaTypes.convert(val, JavaTypes.getTypeCode(fmd.
                        getObjectIdFieldType()));
            }
            return val;
		}
	}
}
