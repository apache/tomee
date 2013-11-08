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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.PCRegistry;
import org.apache.openjpa.enhance.StateManager;
import org.apache.openjpa.enhance.ManagedInstanceProvider;
import org.apache.openjpa.enhance.ReflectingPersistenceCapable;
import org.apache.openjpa.enhance.RuntimeUnenhancedClassesModes;
import org.apache.openjpa.kernel.FetchConfiguration;
import org.apache.openjpa.kernel.LockManager;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.PCState;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.kernel.StoreManager;
import org.apache.openjpa.lib.util.Closeable;
import org.apache.openjpa.lib.util.ReferenceMap;
import org.apache.openjpa.lib.util.UUIDGenerator;
import org.apache.openjpa.lib.util.concurrent.ConcurrentReferenceHashMap;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.SequenceMetaData;
import org.apache.openjpa.meta.ValueStrategies;
import org.apache.openjpa.conf.OpenJPAConfiguration;

/**
 * Helper for OpenJPA back-ends.
 *
 * @since 0.3.0
 * @author Abe White
 * @nojavadoc
 */
public class ImplHelper {

    // Cache for from/to type assignments
    private static final Map _assignableTypes =
        new ConcurrentReferenceHashMap(ReferenceMap.WEAK, ReferenceMap.HARD);

    // map of all new unenhanced instances active in this classloader
    public static final Map _unenhancedInstanceMap =
        new ConcurrentReferenceHashMap(ReferenceMap.WEAK, ReferenceMap.HARD) {

            protected boolean eq(Object x, Object y) {
                // the Entries in ConcurrentReferenceHashMap delegate back to
                // eq() in their equals() impls
                if (x instanceof Map.Entry)
                    return super.eq(x, y);
                else
                    return x == y;
            }

            protected int hc(Object o) {
                // the Entries in ConcurrentReferenceHashMap delegate back to
                // hc() in their hashCode() impls
                if (o instanceof Map.Entry)
                    return super.hc(o);
                else
                    return System.identityHashCode(o);
            }
        };

    /**
     * Helper for store manager implementations. This method simply delegates
     * to the proper singular method for each state manager.
     *
     * @see StoreManager#loadAll
     * @since 0.4.0
     */
    public static Collection loadAll(Collection sms, StoreManager store,
        PCState state, int load, FetchConfiguration fetch, Object context) {
        Collection failed = null;
        OpenJPAStateManager sm;
        LockManager lm;
        for (Iterator itr = sms.iterator(); itr.hasNext();) {
            sm = (OpenJPAStateManager) itr.next();
            if (sm.getManagedInstance() == null) {
                if (!store.initialize(sm, state, fetch, context))
                    failed = addFailedId(sm, failed);
            } else if (load != StoreManager.FORCE_LOAD_NONE
                || sm.getPCState() == PCState.HOLLOW) {
                lm = sm.getContext().getLockManager();
                if (!store.load(sm, sm.getUnloaded(fetch), fetch, 
                    lm.getLockLevel(sm), context))
                    failed = addFailedId(sm, failed);
            } else if (!store.exists(sm, context))
                failed = addFailedId(sm, failed);
        }
        return (failed == null) ? Collections.EMPTY_LIST : failed;
    }

    /**
     * Add identity of given instance to collection.
     */
    private static Collection addFailedId(OpenJPAStateManager sm,
        Collection failed) {
        if (failed == null)
            failed = new ArrayList();
        failed.add(sm.getId());
        return failed;
    }

    /**
     * Generate a value for the given metadata, or return null. Generates
     * values for hte following strategies: {@link ValueStrategies#SEQUENCE},
     * {@link ValueStrategies#UUID_STRING}, {@link ValueStrategies#UUID_HEX}
     */
    public static Object generateIdentityValue(StoreContext ctx,
        ClassMetaData meta, int typeCode) {
        return generateValue(ctx, meta, null, typeCode);
    }

    /**
     * Generate a value for the given metadata, or return null. Generates
     * values for hte following strategies: {@link ValueStrategies#SEQUENCE},
     * {@link ValueStrategies#UUID_STRING}, {@link ValueStrategies#UUID_HEX}
     */
    public static Object generateFieldValue(StoreContext ctx,
        FieldMetaData fmd) {
        return generateValue(ctx, fmd.getDefiningMetaData(), fmd, 
            fmd.getDeclaredTypeCode());
    }

    /**
     * Generate a value for the given metadaa.
     */
    private static Object generateValue(StoreContext ctx,
        ClassMetaData meta, FieldMetaData fmd, int typeCode) {
        int strategy = (fmd == null) ? meta.getIdentityStrategy()
            : fmd.getValueStrategy();
        switch (strategy) {
            case ValueStrategies.SEQUENCE:
                SequenceMetaData smd = (fmd == null)
                    ? meta.getIdentitySequenceMetaData()
                    : fmd.getValueSequenceMetaData();
                return JavaTypes.convert(smd.getInstance(ctx.getClassLoader()).
                    next(ctx, meta), typeCode);
            case ValueStrategies.UUID_STRING:
                return UUIDGenerator.nextString(UUIDGenerator.TYPE1);
            case ValueStrategies.UUID_HEX:
                return UUIDGenerator.nextHex(UUIDGenerator.TYPE1);
            case ValueStrategies.UUID_TYPE4_STRING:
                return UUIDGenerator.nextString(UUIDGenerator.TYPE4);
            case ValueStrategies.UUID_TYPE4_HEX:
                return UUIDGenerator.nextHex(UUIDGenerator.TYPE4);
            default:
                return null;
        }
    }

    /**
     * Returns the fields of the state that require an update.
     *
     * @param  sm  the state to check
     * @return the BitSet of fields that need update, or null if none
     */
    public static BitSet getUpdateFields(OpenJPAStateManager sm) {
        if ((sm.getPCState() == PCState.PDIRTY
            && (!sm.isFlushed() || sm.isFlushedDirty()))
            || (sm.getPCState() == PCState.PNEW && sm.isFlushedDirty())) {
            BitSet dirty = sm.getDirty();
            if (sm.isFlushed()) {
                dirty = (BitSet) dirty.clone();
                dirty.andNot(sm.getFlushed());
            }
            if (dirty.length() > 0)
                return dirty;
        }
        return null;
    }

    /**
     * Close the given resource. The resource can be an extent iterator,
     * query result, large result set relation, or any closeable OpenJPA
     * component.
     */
    public static void close(Object o) {
        try {
            if (o instanceof Closeable)
                ((Closeable) o).close();
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new GeneralException(e);
        }
    }

    /**
     * Returns true if the specified class is a type that can be managed by
     * OpenJPA.
     *
     * @param type the class to test
     * @return true if the class is manageable.
     *
     * @since 1.0.0
     */
    public static boolean isManagedType(OpenJPAConfiguration conf, Class type) {
        return (PersistenceCapable.class.isAssignableFrom(type)
            || (type != null
                && (conf == null || conf.getRuntimeUnenhancedClassesConstant()
                    == RuntimeUnenhancedClassesModes.SUPPORTED)
                && PCRegistry.isRegistered(type)));
    }

    /**
     * Returns true if the specified instance is manageable.
     *
     * @param instance the object to check
     * @return true if the instance is a persistent type, false otherwise
     */
    public static boolean isManageable(Object instance) {
        return instance instanceof PersistenceCapable
            || instance != null && PCRegistry.isRegistered(instance.getClass());
    }

    /**
     * Returns true if the referenced "to" class is assignable to the "from"
     * class.  This helper method utilizes a cache to help avoid the overhead
     * of the Class.isAssignableFrom() method.
     *
     * @param from target class instance to be checked for assignability
     * @param to second class instance to be checked for assignability
     * @return true if the "to" class is assignable to the "from" class
     */
    public static boolean isAssignable(Class from, Class to) {
        if (from == null || to == null)
            return false;

        Boolean isAssignable = null;
        Map assignableTo = (Map) _assignableTypes.get(from);
        if (assignableTo == null) { // "to" cache doesn't exist, so create it...
            assignableTo = new ConcurrentReferenceHashMap(ReferenceMap.WEAK,
                    ReferenceMap.HARD);
            _assignableTypes.put(from, assignableTo);
        } else { // "to" cache exists...
            isAssignable = (Boolean) assignableTo.get(to);
        }

        if (isAssignable == null) {// we don't have a record of this pair...
            isAssignable = Boolean.valueOf(from.isAssignableFrom(to));
            assignableTo.put(to, isAssignable);
        }

        return isAssignable.booleanValue();
    }

    /**
     * @return the persistence-capable instance responsible for managing
     * <code>o</code>, or <code>null</code> if <code>o</code> is not manageable.
     * @since 1.0.0
     */
    public static PersistenceCapable toPersistenceCapable(Object o, Object ctx){
        if (o instanceof PersistenceCapable)
            return (PersistenceCapable) o;

        OpenJPAConfiguration conf = null;
        if (ctx instanceof OpenJPAConfiguration)
            conf = (OpenJPAConfiguration) ctx;
        else if (ctx instanceof StateManager
            && ((StateManager) ctx).getGenericContext() instanceof StoreContext)
            conf = ((StoreContext) ((StateManager) ctx).getGenericContext())
                .getConfiguration();

        if (!isManageable(o))
            return null;

        // if we had a putIfAbsent() method, we wouldn't need to sync here
        synchronized (o) {
            PersistenceCapable pc = (PersistenceCapable)
                _unenhancedInstanceMap.get(o);

            if (pc != null)
                return pc;

            // if we don't have a conf passed in, then we can't create a new
            // ReflectingPC; this will only be the case when invoked from a
            // context outside of OpenJPA.
            if (conf == null)
                return null;

            pc = new ReflectingPersistenceCapable(o, conf);
            _unenhancedInstanceMap.put(o, pc);
            return pc;
        }
    }

    public static void registerPersistenceCapable(
        ReflectingPersistenceCapable pc) {
        _unenhancedInstanceMap.put(pc.getManagedInstance(), pc);
    }

    /**
     * @return the user-visible representation of <code>o</code>.
     * @since 1.0.0
     */
    public static Object getManagedInstance(Object o) {
        if (o instanceof ManagedInstanceProvider)
            return ((ManagedInstanceProvider) o).getManagedInstance();
        else
            return o;
    }
}
