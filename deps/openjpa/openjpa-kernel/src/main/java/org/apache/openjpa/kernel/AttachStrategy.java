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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.ObjectUtils;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.StateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.util.InternalException;
import org.apache.openjpa.util.UserException;

/**
 * Strategy for attaching objects.
 *
 * @author Marc Prud'hommeaux
 * @author Steve Kim
 * @nojavadoc
 */
abstract class AttachStrategy
    extends TransferFieldManager {

    private static final Localizer _loc = Localizer.forPackage
        (AttachStrategy.class);

    /**
     * Attach.
     *
     * @param manager manager holding cache of attached instances
     * @param toAttach detached instance
     * @param meta metadata for the instance being attached
     * @param into instance we're attaching into
     * @param owner state manager for <code>into</code>
     * @param ownerMeta field we traversed to find <code>toAttach</code>
     * @param explicit whether to make new instances explicitly persistent
     */
    public abstract Object attach(AttachManager manager,
        Object toAttach, ClassMetaData meta, PersistenceCapable into,
        OpenJPAStateManager owner, ValueMetaData ownerMeta, boolean explicit);

    /**
     * Return the identity of the given detached instance.
     */
    protected abstract Object getDetachedObjectId(AttachManager manager,
        Object toAttach);

    /**
     * Provide the given field into this field manager.
     */
    protected abstract void provideField(Object toAttach, StateManagerImpl sm,
        int field);

    /**
     * Return a PNew/PNewProvisional managed object for the given detached 
     * instance.
     */
    protected StateManagerImpl persist(AttachManager manager,
        PersistenceCapable pc, ClassMetaData meta, Object appId, 
        boolean explicit) {
        PersistenceCapable newInstance;
        if (!manager.getCopyNew())
            newInstance = pc;
        else if (appId == null)
            // datastore identity or application identity with generated keys
            newInstance = pc.pcNewInstance(null, false);
        else // application identity: use existing fields
            newInstance = pc.pcNewInstance(null, appId, false);

        StateManagerImpl sm = (StateManagerImpl) manager.getBroker().persist
            (newInstance, appId, explicit, manager.getBehavior(), !manager.getCopyNew());
        
        attachPCKeyFields(pc, sm, meta, manager);
        
        return sm;
    }
    
    private void attachPCKeyFields(PersistenceCapable fromPC, 
        StateManagerImpl sm, ClassMetaData meta, AttachManager manager) {
        
        
        if (fromPC.pcGetStateManager() == null) {
            fromPC.pcReplaceStateManager(sm);
        
            FieldMetaData[] fmds = meta.getDefinedFields();
            for (FieldMetaData fmd : fmds) {
                if (fmd.isPrimaryKey() && fmd.getDeclaredTypeCode() == JavaTypes.PC) {
                    attachField(manager, fromPC, sm, fmd, true);
                }
            }
        
            fromPC.pcReplaceStateManager(null);
        }
    }

    /**
     * Attach the given field into the given instance.
     *
     * @param toAttach the detached persistent instance
     * @param sm state manager for the managed instance we're copying
     * into; <code>toAttach</code> also uses this state manager
     * @param fmd metadata on the field we're copying
     * @param nullLoaded if false, nulls will be considered unloaded and will
     * not be attached
     */
    protected boolean attachField(AttachManager manager, Object toAttach,
        StateManagerImpl sm, FieldMetaData fmd, boolean nullLoaded) {
        if (fmd.isVersion()
            || fmd.getManagement() != FieldMetaData.MANAGE_PERSISTENT)
            return false;

        PersistenceCapable into = sm.getPersistenceCapable();
        int i = fmd.getIndex();
        provideField(toAttach, sm, i);

        int set = StateManager.SET_ATTACH;
        Object val;
        switch (fmd.getDeclaredTypeCode()) {
            case JavaTypes.BOOLEAN:
                sm.settingBooleanField(into, i, sm.fetchBooleanField(i),
                    fetchBooleanField(i), set);
                break;
            case JavaTypes.BYTE:
                sm.settingByteField(into, i, sm.fetchByteField(i),
                    fetchByteField(i), set);
                break;
            case JavaTypes.CHAR:
                sm.settingCharField(into, i, sm.fetchCharField(i),
                    fetchCharField(i), set);
                break;
            case JavaTypes.DOUBLE:
                sm.settingDoubleField(into, i, sm.fetchDoubleField(i),
                    fetchDoubleField(i), set);
                break;
            case JavaTypes.FLOAT:
                sm.settingFloatField(into, i, sm.fetchFloatField(i),
                    fetchFloatField(i), set);
                break;
            case JavaTypes.INT:
                sm.settingIntField(into, i, sm.fetchIntField(i),
                    fetchIntField(i), set);
                break;
            case JavaTypes.LONG:
                sm.settingLongField(into, i, sm.fetchLongField(i),
                    fetchLongField(i), set);
                break;
            case JavaTypes.SHORT:
                sm.settingShortField(into, i, sm.fetchShortField(i),
                    fetchShortField(i), set);
                break;
            case JavaTypes.STRING:
                String sval = fetchStringField(i);
                if (sval == null && !nullLoaded)
                    return false;
                sm.settingStringField(into, i, sm.fetchStringField(i), sval,
                    set);
                break;
            case JavaTypes.DATE:
            case JavaTypes.CALENDAR:
            case JavaTypes.NUMBER:
            case JavaTypes.BOOLEAN_OBJ:
            case JavaTypes.BYTE_OBJ:
            case JavaTypes.CHAR_OBJ:
            case JavaTypes.DOUBLE_OBJ:
            case JavaTypes.FLOAT_OBJ:
            case JavaTypes.INT_OBJ:
            case JavaTypes.LONG_OBJ:
            case JavaTypes.SHORT_OBJ:
            case JavaTypes.BIGDECIMAL:
            case JavaTypes.BIGINTEGER:
            case JavaTypes.LOCALE:
            case JavaTypes.OBJECT:
            case JavaTypes.OID:
            case JavaTypes.ENUM:
                val = fetchObjectField(i);
                if (val == null && !nullLoaded)
                    return false;
                sm.settingObjectField(into, i, sm.fetchObjectField(i), val,
                    set);
                break;
            case JavaTypes.PC:
            case JavaTypes.PC_UNTYPED:
                Object frmpc = fetchObjectField(i);
                if (frmpc == null && !nullLoaded)
                    return false;

                OpenJPAStateManager tosm = manager.getBroker().getStateManager
                    (sm.fetchObjectField(i));
                PersistenceCapable topc = (tosm == null) ? null
                    : tosm.getPersistenceCapable();
                if (frmpc != null || topc != null) {
                    if (fmd.getCascadeAttach() == ValueMetaData.CASCADE_NONE) {
                        // Use the attached copy of the object, if available
                        PersistenceCapable cpy = manager.getAttachedCopy(frmpc);
                        if (cpy != null) {
                            frmpc = cpy;
                        } else {
                        	frmpc = getReference(manager, frmpc, sm, fmd);
                        }
                    }
                    else {
                        PersistenceCapable intopc = topc;
                        if (!fmd.isEmbeddedPC() && frmpc != null && topc != null
                            && !ObjectUtils.equals(topc.pcFetchObjectId(),
                            manager.getDetachedObjectId(frmpc))) {
                            intopc = null;
                        }
                        frmpc = manager.attach(frmpc, intopc, sm, fmd, false);
                    }
                    if (frmpc != topc)
                        sm.settingObjectField(into, i, topc, frmpc, set);
                }
                break;
            case JavaTypes.COLLECTION:
                Collection frmc = (Collection) fetchObjectField(i);
                if (frmc == null && !nullLoaded)
                    return false;
                Collection toc = (Collection) sm.fetchObjectField(i);
                if ((toc != null && !toc.isEmpty())
                    || frmc != null && !frmc.isEmpty()) {
                    if (frmc == null)
                        sm.settingObjectField(into, i, toc, null, set);
                    else if (toc == null) {
                        sm.settingObjectField(into, i, null,
                            attachCollection(manager, frmc, sm, fmd), set);
                    } else if (toc instanceof Set && frmc instanceof Set)
                        replaceCollection(manager, frmc, toc, sm, fmd);
                    else {
                        sm.settingObjectField(into, i, toc,
                            replaceList(manager, frmc, toc, sm, fmd), set);
                    }
                }
                break;
            case JavaTypes.MAP:
                Map frmm = (Map) fetchObjectField(i);
                if (frmm == null && !nullLoaded)
                    return false;
                Map tom = (Map) sm.fetchObjectField(i);
                if ((tom != null && !tom.isEmpty())
                    || (frmm != null && !frmm.isEmpty())) {
                    if (frmm == null)
                        sm.settingObjectField(into, i, tom, null, set);
                    else if (tom == null)
                        sm.settingObjectField(into, i, null,
                            attachMap(manager, frmm, sm, fmd), set);
                    else
                        replaceMap(manager, frmm, tom, sm, fmd);
                }
                break;
            case JavaTypes.ARRAY:
                Object frma = fetchObjectField(i);
                if (frma == null && !nullLoaded)
                    return false;
                Object toa = sm.fetchObjectField(i);
                if ((toa != null && Array.getLength(toa) > 0)
                    || (frma != null && Array.getLength(frma) > 0)) {
                    if (frma == null)
                        sm.settingObjectField(into, i, toa, null, set);
                    else
                        sm.settingObjectField(into, i, toa,
                            replaceArray(manager, frma, toa, sm, fmd), set);
                }
                break;
            default:
                throw new InternalException(fmd.toString());
        }
        return true;
    }

    /**
     * Return a managed, possibly hollow reference for the given detached
     * object.
     */
    protected Object getReference(AttachManager manager, Object toAttach, OpenJPAStateManager sm, ValueMetaData vmd) {
        if (toAttach == null)
            return null;

        if (manager.getBroker().isNew(toAttach)) {
            // Check if toAttach is already mapped to a managed instance
            PersistenceCapable pc = manager.getAttachedCopy(toAttach);
            if (pc != null) {
                return pc;
            } else {
                return toAttach;
            }
        } else if (manager.getBroker().isPersistent(toAttach)) {
            return toAttach;
        } else if (manager.getBroker().isDetached(toAttach)) {
            Object oid = manager.getDetachedObjectId(toAttach);
            if (oid != null) {
                return manager.getBroker().find(oid, false, null);
            }
        }
        throw new UserException(_loc.get("cant-cascade-attach", vmd)).setFailedObject(toAttach);
    }

    /**
     * Replace the contents of <code>toc</code> with the contents of
     * <code>frmc</code>. Neither collection is null.
     */
    private void replaceCollection(AttachManager manager, Collection frmc,
        Collection toc, OpenJPAStateManager sm, FieldMetaData fmd) {
        // if frmc collection is empty, just clear toc
        if (frmc.isEmpty()) {
            if (!toc.isEmpty())
                toc.clear();
            return;
        }

        // if this is a pc collection, attach all instances
        boolean pc = fmd.getElement().isDeclaredTypePC();
        if (pc)
            frmc = attachCollection(manager, frmc, sm, fmd);

        // remove all elements from the toc collection that aren't in frmc
        toc.retainAll(frmc);

        // now add all elements that are in frmc but not toc
        if (frmc.size() != toc.size()) {
            for (Iterator i = frmc.iterator(); i.hasNext();) {
                Object ob = i.next();
                if (!toc.contains(ob))
                    toc.add(ob);
            }
        }
    }

    /**
     * Return a new collection with the attached contents of the given one.
     */
    protected Collection attachCollection(AttachManager manager,
        Collection orig, OpenJPAStateManager sm, FieldMetaData fmd) {
        Collection coll = copyCollection(manager, orig, fmd, sm);
        ValueMetaData vmd = fmd.getElement();
        if (!vmd.isDeclaredTypePC())
            return coll;

        // unfortunately we have to clear the original and re-add
        coll.clear();
        Object elem;
        for (Iterator itr = orig.iterator(); itr.hasNext();) {
            if (vmd.getCascadeAttach() == ValueMetaData.CASCADE_NONE)
                elem = getReference(manager, itr.next(), sm, vmd);
            else
                elem = manager.attach(itr.next(), null, sm, vmd, false);
            coll.add(elem);
        }
        return coll;
    }

    /**
     * Copies the given collection.
     */
    private Collection copyCollection(AttachManager manager, Collection orig,
        FieldMetaData fmd) {
        Collection coll = manager.getProxyManager().copyCollection(orig);
        if (coll == null)
            throw new UserException(_loc.get("not-copyable", fmd));
        return coll;
    }
    
    /**
     * Copies the given collection.
     */
    private Collection copyCollection(AttachManager manager, Collection orig,
        FieldMetaData fmd, OpenJPAStateManager sm) {
        if (orig == null)
            throw new UserException(_loc.get("not-copyable", fmd));
        try {
            return copyCollection(manager, orig, fmd);
        } catch (Exception e) {
            Collection coll = (Collection) sm.newFieldProxy(fmd.getIndex());
            coll.addAll(orig);
            return coll;
        }
    }

    /**
     * Copies the given map.
     */
    private Map copyMap(AttachManager manager, Map orig,
        FieldMetaData fmd, OpenJPAStateManager sm) {
        if (orig == null)
            throw new UserException(_loc.get("not-copyable", fmd));
        try {
            return manager.getProxyManager().copyMap(orig);
        } catch (Exception e) {
            Map<Object, Object> map = (Map<Object, Object>) sm.newFieldProxy(fmd.getIndex());
            
            for (Entry<Object, Object> entry : ((Map<Object, Object>) orig).entrySet()) {
                map.put(entry.getKey(), entry.getValue());
            }
            return map;
        }
    }

    /**
     * Returns an attached version of the <code>frml</code>
     * list if it is different than <code>tol</code>. If the lists
     * will be identical, returns <code>tol</code>. Neither list is null.
     */
    private Collection replaceList(AttachManager manager, Collection frml,
        Collection tol, OpenJPAStateManager sm, FieldMetaData fmd) {
        boolean pc = fmd.getElement().isDeclaredTypePC();
        if (pc)
            frml = attachCollection(manager, frml, sm, fmd);

        // if the only diff between frml and tol is some added elements at
        // the end, make the changes directly in tol
        if (frml.size() >= tol.size()) {
            Iterator frmi = frml.iterator();
            for (Iterator toi = tol.iterator(); toi.hasNext();) {
                // if there's an incompatibility, just return a copy of frml
                // (it's already copied if we attached it)
                if (!equals(frmi.next(), toi.next(), pc))
                    return (pc) ? frml : copyCollection(manager, frml, fmd, sm);
            }

            // just add the extra elements in frml to tol and return tol
            while (frmi.hasNext())
                tol.add(frmi.next());
            return tol;
        }

        // the lists are different; just make sure frml is copied and return it
        return (pc) ? frml : copyCollection(manager, frml, fmd, sm);
    }

    /**
     * Replace the contents of <code>tom</code> with the contents of
     * <code>frmm</code>. Neither map is null.
     */
    private void replaceMap(AttachManager manager, Map frmm, Map tom,
        OpenJPAStateManager sm, FieldMetaData fmd) {
        if (frmm.isEmpty()) {
            if (!tom.isEmpty())
                tom.clear();
            return;
        }

        // if this is a pc map, attach all instances
        boolean keyPC = fmd.getKey().isDeclaredTypePC();
        boolean valPC = fmd.getElement().isDeclaredTypePC();
        if (keyPC || valPC)
            frmm = attachMap(manager, frmm, sm, fmd);

        // make sure all the keys in the from map are in the two map, and
        // that they have the same values
        for (Iterator i = frmm.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            if (!tom.containsKey(entry.getKey())
                || !equals(tom.get(entry.getKey()), entry.getValue(), valPC)) {
                tom.put(entry.getKey(), entry.getValue());
            }
        }

        // remove any keys in the to map that aren't in the from map
        if (tom.size() != frmm.size()) {
            for (Iterator i = tom.keySet().iterator(); i.hasNext();) {
                if (!(frmm.containsKey(i.next())))
                    i.remove();
            }
        }
    }

    /**
     * Make sure all the values in the given map are attached.
     */
    protected Map attachMap(AttachManager manager, Map orig,
        OpenJPAStateManager sm, FieldMetaData fmd) {
        Map map = copyMap(manager, orig, fmd, sm);
        if (map == null)
            throw new UserException(_loc.get("not-copyable", fmd));

        ValueMetaData keymd = fmd.getKey();
        ValueMetaData valmd = fmd.getElement();
        if (!keymd.isDeclaredTypePC() && !valmd.isDeclaredTypePC())
            return map;

        // if we have to replace keys, just clear and re-add; otherwise
        // we can use the entry set to reset the values only
        Map.Entry entry;
        if (keymd.isDeclaredTypePC()) {
            map.clear();
            Object key, val;
            for (Iterator itr = orig.entrySet().iterator(); itr.hasNext();) {
                entry = (Map.Entry) itr.next();
                key = entry.getKey();
                if (keymd.getCascadeAttach() == ValueMetaData.CASCADE_NONE)
                    key = getReference(manager, key, sm, keymd);
                else
                    key = manager.attach(key, null, sm, keymd, false);
                val = entry.getValue();
                if (valmd.isDeclaredTypePC()) {
                    if (valmd.getCascadeAttach() == ValueMetaData.CASCADE_NONE)
                        val = getReference(manager, val, sm, valmd);
                    else
                        val = manager.attach(val, null, sm, valmd, false);
                }
                map.put(key, val);
            }
        } else {
            Object val;
            for (Iterator itr = map.entrySet().iterator(); itr.hasNext();) {
                entry = (Map.Entry) itr.next();
                if (valmd.getCascadeAttach() == ValueMetaData.CASCADE_NONE)
                    val = getReference(manager, entry.getValue(), sm, valmd);
                else
                    val = manager.attach(entry.getValue(), null, sm, valmd, 
                        false);
                entry.setValue(val);
            }
        }
        return map;
    }

    /**
     * Returns an attached version of the <code>frma</code>
     * array if it is different than <code>toa</code>. If the arrays
     * will be identical, returns <code>toa</code>.
     */
    private Object replaceArray(AttachManager manager, Object frma,
        Object toa, OpenJPAStateManager sm, FieldMetaData fmd) {
        int len = Array.getLength(frma);
        boolean diff = toa == null || len != Array.getLength(toa);

        // populate an array copy on the initial assumption that the array
        // is dirty
        Object newa = Array.newInstance(fmd.getElement().getDeclaredType(),
            len);
        ValueMetaData vmd = fmd.getElement();
        boolean pc = vmd.isDeclaredTypePC();
        Object elem;
        for (int i = 0; i < len; i++) {
            elem = Array.get(frma, i);
            if (pc) {
                if (vmd.getCascadeAttach() == ValueMetaData.CASCADE_NONE)
                    elem = getReference(manager, elem, sm, vmd);
                else
                    elem = manager.attach(elem, null, sm, vmd, false);
            }
            diff = diff || !equals(elem, Array.get(toa, i), pc);
            Array.set(newa, i, elem);
        }
        return (diff) ? newa : toa;
    }

    /**
     * Return true if the given objects are equal. PCs are compared for
     * on JVM identity.
     */
    private static boolean equals(Object a, Object b, boolean pc) {
        if (a == b)
            return true;
        if (pc || a == null || b == null)
            return false;
		return a.equals (b);
	}
}
