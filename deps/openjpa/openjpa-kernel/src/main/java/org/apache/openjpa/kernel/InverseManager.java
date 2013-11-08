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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.datacache.DataCache;
import org.apache.openjpa.datacache.DataCacheManager;
import org.apache.openjpa.lib.conf.Configurable;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.log.Log;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.util.InvalidStateException;

/**
 * Class which manages inverse relations before flushing
 * to the datastore. Ensures that inverse fields are set.
 *  Currently limited to managing PC and Collection-type relations.
 *
 * @author Steve Kim
 */
public class InverseManager implements Configurable {

    private static final Localizer _loc = Localizer.forPackage(InverseManager.class);

    protected static final Object NONE = new Object();
    
    protected DataCacheManager _mgr;

    /**
     * Constant representing the {@link #ACTION_MANAGE} action
     */
    public static final int ACTION_MANAGE = 0;

    /**
     * Constant representing the {@link #ACTION_WARN} action
     */
    public static final int ACTION_WARN = 1;

    /**
     * Constant representing the {@link #ACTION_EXCEPTION} action
     */
    public static final int ACTION_EXCEPTION = 2;

    private boolean _manageLRS = false;
    private int _action = ACTION_MANAGE;
    private Log _log;

    /**
     * Return whether to manage LRS fields.
     */
    public boolean getManageLRS() {
        return _manageLRS;
    }

    /**
     * Set whether to false LRS relations. Defaults to false.
     */
    public void setManageLRS(boolean manage) {
        _manageLRS = manage;
    }

    /**
     * Return the action constant to use during relationship checking.
     * Defaults to {@link #ACTION_MANAGE}.
     */
    public int getAction() {
        return _action;
    }

    /**
     * Set the action constant to use during relationship checking.
     * Defaults to {@link #ACTION_MANAGE}.
     */
    public void setAction(int action) {
        _action = action;
    }

    /**
     * Set the action string to use during relationship checking.
     * Options include <code>manage, exception, warn</code>.
     * This method is primarily for string-based automated configuration.
     */
    public void setAction(String action) {
        if ("exception".equals(action))
            _action = ACTION_EXCEPTION;
        else if ("warn".equals(action))
            _action = ACTION_WARN;
        else if ("manage".equals(action))
            _action = ACTION_MANAGE;
        else
            throw new IllegalArgumentException(action);
    }

    public void startConfiguration() {
    }

    public void endConfiguration() {
    }

    public void setConfiguration(Configuration conf) {
        _log = conf.getLog(OpenJPAConfiguration.LOG_RUNTIME);
        _mgr = ((OpenJPAConfiguration)conf).getDataCacheManagerInstance();
    }

    /**
     * Correct relations from the given dirty field to inverse instances.
     * Field <code>fmd</code> of the instance managed by <code>sm</code> has
     * value <code>value</code>. Ensure that all inverses relations from
     * <code>value</code> are consistent with this.
     */
    public void correctRelations(OpenJPAStateManager sm, FieldMetaData fmd,
        Object value) {
        if (fmd.getDeclaredTypeCode() != JavaTypes.PC &&
            ((fmd.getDeclaredTypeCode() != JavaTypes.COLLECTION  &&
              fmd.getDeclaredTypeCode() != JavaTypes.MAP) ||
                fmd.getElement().getDeclaredTypeCode() != JavaTypes.PC))
            return;

        // ignore LRS fields
        if (!getManageLRS() && fmd.isLRS())
            return;

        FieldMetaData[] inverses = fmd.getInverseMetaDatas();
        if (inverses.length == 0)
            return;

        // clear any restorable relations
        clearInverseRelations(sm, fmd, inverses, value);

        if (value != null) {
            StoreContext ctx = sm.getContext();
            switch (fmd.getDeclaredTypeCode()) {
                case JavaTypes.PC:
                    createInverseRelations(ctx, sm.getManagedInstance(),
                        value, fmd, inverses);
                    break;
                case JavaTypes.COLLECTION:
                    for (Iterator itr = ((Collection) value).iterator();
                        itr.hasNext();)
                        createInverseRelations(ctx, sm.getManagedInstance(),
                            itr.next(), fmd, inverses);
                    break;
            }
        }
    }

    /**
     * Create the inverse relations for all the given inverse fields.
     * A relation exists from <code>fromRef</code> to <code>toRef</code>; this
     * method creates the inverses.
     */
    protected void createInverseRelations(StoreContext ctx,
        Object fromRef, Object toRef, FieldMetaData fmd,
        FieldMetaData[] inverses) {
        OpenJPAStateManager other = ctx.getStateManager(toRef);
        if (other == null || other.isDeleted())
            return;

        boolean owned;
        for (int i = 0; i < inverses.length; i++) {
            if (!getManageLRS() && inverses[i].isLRS())
                continue;

            // if this is the owned side of the relation and has not yet been
            // loaded, no point in setting it now, cause it'll have the correct
            // value the next time it is loaded after the flush
            owned = fmd == inverses[i].getMappedByMetaData()
                && _action == ACTION_MANAGE
                && !isLoaded(other, inverses[i].getIndex());

            switch (inverses[i].getDeclaredTypeCode()) {
                case JavaTypes.PC:
                    if (!owned || inverses[i].getCascadeDelete()
                        == ValueMetaData.CASCADE_AUTO)
                        storeField(other, inverses[i], NONE, fromRef);
                    break;
                case JavaTypes.COLLECTION:
                    if (!owned || inverses[i].getElement().getCascadeDelete()
                        == ValueMetaData.CASCADE_AUTO)
                        addToCollection(other, inverses[i], fromRef);
                    break;
            }
        }
    }

    /**
     * Return whether the given field is loaded for the given instance.
     */
    private boolean isLoaded(OpenJPAStateManager sm, int field) {
        if (sm.getLoaded().get(field))
            return true;

        // if the field isn't loaded in the state manager, it still might be
        // loaded in the data cache, in which case we still have to correct
        // it to keep the cache in sync
        DataCache cache = _mgr.selectCache(sm);
        if (cache == null)
            return false;

        // can't retrieve an embedded object directly, so always assume the
        // field is loaded and needs to be corrected
        if (sm.isEmbedded())
            return true;

        PCData pc = cache.get(sm.getObjectId());
        if (pc == null)
            return false;
        return pc.isLoaded(field);
    }

    /**
     * Remove all relations between the initial value of <code>fmd</code> for
     * the instance managed by <code>sm</code> and its inverses. Relations
     * shared with <code>newValue</code> can be left intact.
     */
    protected void clearInverseRelations(OpenJPAStateManager sm,
        FieldMetaData fmd, FieldMetaData[] inverses, Object newValue) {
        // don't bother clearing unflushed new instances
        if (sm.isNew() && !sm.getFlushed().get(fmd.getIndex()))
            return;
        if (fmd.getDeclaredTypeCode() == JavaTypes.PC) {
            Object initial = sm.fetchInitialField(fmd.getIndex());
            clearInverseRelations(sm, initial, fmd, inverses);
        } else {
            Object obj = sm.fetchInitialField(fmd.getIndex());
            Collection initial = null;
            if (obj instanceof Collection)
                initial = (Collection) obj;
            else if (obj instanceof Map)
                initial = ((Map)obj).values();
            
            if (initial == null)
                return;

            // clear all relations not also in the new value
            Collection coll = null;
            if (newValue instanceof Collection)
                coll = (Collection) newValue;
            else if (newValue instanceof Map)
                coll = ((Map)newValue).values();
            Object elem;
            for (Iterator itr = initial.iterator(); itr.hasNext();) {
                elem = itr.next();
                if (coll == null || !coll.contains(elem))
                    clearInverseRelations(sm, elem, fmd, inverses);
            }
        }
    }

    /**
     * Clear all inverse the relations from <code>val</code> to the instance
     * managed by <code>sm</code>.
     */
    protected void clearInverseRelations(OpenJPAStateManager sm, Object val,
        FieldMetaData fmd, FieldMetaData[] inverses) {
        if (val == null)
            return;
        OpenJPAStateManager other = sm.getContext().getStateManager(val);
        if (other == null || other.isDeleted())
            return;

        boolean owned;
        for (int i = 0; i < inverses.length; i++) {
            if (!getManageLRS() && inverses[i].isLRS())
                continue;

            // if this is the owned side of the relation and has not yet been
            // loaded, no point in setting it now, cause it'll have the correct
            // value the next time it is loaded after the flush
            owned = fmd == inverses[i].getMappedByMetaData()
                && _action == ACTION_MANAGE
                && !isLoaded(other, inverses[i].getIndex());

            switch (inverses[i].getDeclaredTypeCode()) {
                case JavaTypes.PC:
                    if (!owned || inverses[i].getCascadeDelete()
                        == ValueMetaData.CASCADE_AUTO)
                        storeNull(other, inverses[i], sm.getManagedInstance());
                    break;
                case JavaTypes.COLLECTION:
                    if (!owned || inverses[i].getElement().getCascadeDelete()
                        == ValueMetaData.CASCADE_AUTO)
                        removeFromCollection(other, inverses[i],
                            sm.getManagedInstance());
                    break;
            }
        }
    }

    /**
     * Store null value at the given field. Verify that the given compare
     * value is the value being nulled. Pass NONE for no comparison.
     */
    protected void storeNull(OpenJPAStateManager sm, FieldMetaData fmd,
        Object compare) {
        storeField(sm, fmd, compare, null);
    }

    /**
     * Store a given value at the given field. Compare the given
     * argument if not NONE.
     */
    protected void storeField(OpenJPAStateManager sm, FieldMetaData fmd,
        Object compare, Object val) {
        Object oldValue = sm.fetchObjectField(fmd.getIndex());
        if (oldValue == val)
            return;
        if (compare != NONE && oldValue != compare)
            return;

        switch (_action) {
            case ACTION_MANAGE:
                sm.settingObjectField(sm.getPersistenceCapable(),
                    fmd.getIndex(),
                    oldValue, val, OpenJPAStateManager.SET_USER);
                break;
            case ACTION_WARN:
                warnConsistency(sm, fmd);
                break;
            case ACTION_EXCEPTION:
                throwException(sm, fmd);
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Remove the given instance from the collection.
     */
    protected void removeFromCollection(OpenJPAStateManager sm,
        FieldMetaData fmd,
        Object val) {
        Collection coll = (Collection) sm.fetchObjectField(fmd.getIndex());
        if (coll != null) {
            switch (_action) {
                case ACTION_MANAGE:
                    remove:
                    for (int i = 0; coll.remove(val); i++)
                        if (i == 0 && coll instanceof Set)
                            break remove;
                    break;
                case ACTION_WARN:
                    if (coll.contains(val))
                        warnConsistency(sm, fmd);
                    break;
                case ACTION_EXCEPTION:
                    if (coll.contains(val))
                        throwException(sm, fmd);
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    /**
     * Add the given value to the collection at the selected field.
     */
    protected void addToCollection(OpenJPAStateManager sm, FieldMetaData fmd,
        Object val) {
        Collection coll = (Collection) sm.fetchObjectField(fmd.getIndex());
        if (coll == null) {
            coll = (Collection) sm.newFieldProxy(fmd.getIndex());
            sm.storeObjectField(fmd.getIndex(), coll);
        }
        if (!coll.contains(val)) {
            switch (_action) {
                case ACTION_MANAGE:
                    coll.add(val);
                    break;
                case ACTION_WARN:
                    warnConsistency(sm, fmd);
                    break;
                case ACTION_EXCEPTION:
                    throwException(sm, fmd);
                default:
                    throw new IllegalStateException();
            }
        }
    }

    /**
     * Log an inconsistency warning
     */
    protected void warnConsistency(OpenJPAStateManager sm, FieldMetaData fmd) {
        if (_log.isWarnEnabled())
            _log.warn(_loc.get("inverse-consistency", fmd, sm.getId(),
                sm.getContext()));
    }

    /**
     * Throw an inconsistency exception
     */
    protected void throwException(OpenJPAStateManager sm, FieldMetaData fmd) {
        throw new InvalidStateException(_loc.get("inverse-consistency",
            fmd, sm.getId(), sm.getContext())).setFailedObject
            (sm.getManagedInstance()).setFatal(true);
	}
}
