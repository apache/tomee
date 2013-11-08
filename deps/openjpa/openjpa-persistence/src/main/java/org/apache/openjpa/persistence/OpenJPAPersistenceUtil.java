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
package org.apache.openjpa.persistence;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import javax.persistence.spi.LoadState;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.StateManager;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StateManagerImpl;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.meta.ValueMetaData;
import org.apache.openjpa.util.ImplHelper;

public class OpenJPAPersistenceUtil {

    /**
     * Returns the identifier of the persistent entity.
     * @param entity
     * @return The identifier of the entity or null if the entity
     * is not persistent.
     */
    public static Object getIdentifier(Object entity) {
        return getIdentifier(null, entity);
    }

    /**
     * Get the object identifier for a persistent entity managed by one
     * of the entity managers of the specified entity manager factory.
     * @return The identifier of the entity or null if the entity does
     * not have an identifier assigned or is not managed by any of the
     * entity managers of the entity manager factory.
     */
    public static Object getIdentifier(OpenJPAEntityManagerFactory emf, 
        Object entity) {

        if (entity instanceof PersistenceCapable) {
            PersistenceCapable pc = (PersistenceCapable)entity;
            // Per contract, if not managed by the owning emf, return null.
            if (emf != null) {
                if (!isManagedBy(emf, pc)) {
                    return null;
                }
            }
            StateManager sm = pc.pcGetStateManager();
            
            if (sm != null && sm instanceof OpenJPAStateManager) {
                OpenJPAStateManager osm = (OpenJPAStateManager)sm;
                return osm.getObjectId();                
            }
        }
        return null;
    }

    /**
     * Determines whether the specified state manager is managed by an open
     * broker within the persistence unit of the provided EMF instance.
     * @param emf OpenJPAEntityManagerFactory
     * @param sm StateManager
     * @return true if this state manager is managed by a broker within
     * this persistence unit.
     */
    public static boolean isManagedBy(OpenJPAEntityManagerFactory emf, Object entity) {
        // Assert a valid emf was provided, it is open, and the entity is PC
        if (emf == null || !emf.isOpen() || !ImplHelper.isManageable(entity)) {
            return false;
        }
        // Assert the context is a broker
        PersistenceCapable pc = (PersistenceCapable)entity;
        if (!(pc.pcGetGenericContext() instanceof Broker)) {
            return false;
        }
        // Assert the broker is available and open
        Broker broker = (Broker)pc.pcGetGenericContext();
        if (broker == null || broker.isClosed()) {
            return false;
        }
        // Assert the emf associated with the PC is the same as the provided emf
        OpenJPAEntityManagerFactory eemf = JPAFacadeHelper.toEntityManagerFactory(broker.getBrokerFactory());
        if (eemf == emf && eemf.isOpen()) {
            return true;
        }
        return false;
    }

    /**
     * Determines whether the attribute on the specified object is loaded and
     * is managed by one of the entity managers.  Use isManagedBy() to
     * determine if an object is managed by a specific entity manager
     * factory.
     * 
     * @return LoadState.LOADED - if the attribute is loaded.
     *         LoadState.NOT_LOADED - if the attribute is not loaded or any
     *         EAGER fetch attributes of the entity are not loaded.
     *         LoadState.UNKNOWN - if the entity is not managed by this
     *         provider or if it does not contain the persistent
     *         attribute.
     */
    public static LoadState isLoaded(Object obj, String attr) {

        if (obj == null) {
            return LoadState.UNKNOWN;
        }
        
        // If the object has a state manager, call it directly.
        if (obj instanceof PersistenceCapable) {
            PersistenceCapable pc = (PersistenceCapable)obj;
            StateManager sm = pc.pcGetStateManager();
            if (sm != null && sm instanceof OpenJPAStateManager) {
                return isLoaded((OpenJPAStateManager)sm, attr, null);
            }
        }        
        return LoadState.UNKNOWN;
    }

    private static LoadState isLoaded(OpenJPAStateManager sm, String attr, 
        HashSet<OpenJPAStateManager> pcs) {
        boolean isLoaded = true;
        try {
            BitSet loadSet = sm.getLoaded();
            if (attr != null) {
                FieldMetaData fmd = sm.getMetaData().getField(attr);
                // Could not find field metadata for the specified attribute.
                if (fmd == null) {
                    return LoadState.UNKNOWN;
                }
                // Otherwise, return the load state
                if(!loadSet.get(fmd.getIndex())) {
                    return LoadState.NOT_LOADED;
                }
            }
            if (pcs != null && pcs.contains(sm)) {
                return LoadState.LOADED;
            }
            FieldMetaData[] fmds = sm.getMetaData().getFields();
            // Check load state of all persistent eager fetch attributes
            if (fmds != null && fmds.length > 0) {
                pcs = addToLoadSet(pcs, sm);
                for (FieldMetaData fmd : fmds) {
                    if (requiresFetch(sm, fmd)) {
                        if (!isLoadedField(sm, fmd, pcs)) {
                            isLoaded = false;
                            break;
                        }
                    }
                }
                pcs.remove(sm);
            }
        } catch (RuntimeException e) {
            // treat any exceptions, like UnsupportedOperationException
            // for detached entities, as LoadState.UNKNOWN
            return LoadState.UNKNOWN;
        }
        return isLoaded ? LoadState.LOADED : LoadState.NOT_LOADED;        
    }
    
    private static boolean requiresFetch(OpenJPAStateManager sm, FieldMetaData fmd) {
        if (sm instanceof StateManagerImpl)
            return ((StateManagerImpl)sm).requiresFetch(fmd);
        return fmd.isInDefaultFetchGroup();
    }

    private static HashSet<OpenJPAStateManager> addToLoadSet(
        HashSet<OpenJPAStateManager> pcs, OpenJPAStateManager sm) {
        if (pcs == null) {
            pcs = new HashSet<OpenJPAStateManager>();
        }
        pcs.add(sm);
        return pcs;
    }

    private static boolean isLoadedField(OpenJPAStateManager sm,
        FieldMetaData fmd, HashSet<OpenJPAStateManager> pcs) {
        BitSet loadSet = sm.getLoaded();
                
        // Simple load state check for the field
        if (!loadSet.get(fmd.getIndex()))
            return false;

        Object field = sm.fetchField(fmd.getIndex(), false);

        // Get the state manager for the field, if it is a PC
        OpenJPAStateManager ofsm = getStateManager(field);

        // Prevent circular load state evaluation for this sm.
        if (ofsm != null && pcs.contains(ofsm))
            return true;
        
        // If a collection type, determine if it is loaded
        switch (fmd.getDeclaredTypeCode()) {
            case JavaTypes.COLLECTION:   
                return isLoadedCollection(sm, fmd.getElement(), 
                    (Collection<?>)field, pcs);
            case JavaTypes.MAP:
                return isLoadedMap(sm, fmd, 
                    (Map<?,?>)field, pcs);
            case JavaTypes.ARRAY:
                return isLoadedArray(sm, fmd.getElement(), 
                    (Object[])field, pcs);
        }
        // If other PC type, determine if it is loaded
        if (ofsm != null && fmd.isDeclaredTypePC()) {
            return isLoaded(ofsm, null, pcs) ==
                LoadState.LOADED;
        }

        return true;
    } 
    
    private static boolean isLoadedCollection(OpenJPAStateManager sm, 
        ValueMetaData vmd, Collection<?> coll, HashSet<OpenJPAStateManager> pcs) {
        
        // This field passed the load state check in isLoadedField, so
        // if any of these conditions are true the collection is loaded.
        if (sm == null || coll == null || coll.size() == 0) {
            return true;
        }
        
        // Convert to array to prevent concurrency issues
        Object[] arr = coll.toArray();

        return isLoadedArray(sm, vmd, arr, pcs);
    }

    private static boolean isLoadedArray(OpenJPAStateManager sm, 
        ValueMetaData vmd, Object[] arr, 
        HashSet<OpenJPAStateManager> pcs) {

        // This field passed the load state check in isLoadedField, so
        // if any of these conditions are true the array is loaded.
        if (sm == null || arr == null || arr.length == 0) {
            return true;
        }

        // Not a collection of PC's 
        if (!vmd.isDeclaredTypePC()) {
          return true;
        }
        
        for (Object pc : arr) {
            OpenJPAStateManager esm = getStateManager(pc);
            if (esm == null) {
                return true;
            }
            if (!(isLoaded(esm, null, pcs) == LoadState.LOADED))
                return false;
        }
        return true;
    }

    private static boolean isLoadedMap(OpenJPAStateManager sm, 
        FieldMetaData fmd, Map<?,?> map, HashSet<OpenJPAStateManager> pcs) {
                
        // This field passed the load state check in isLoadedField, so
        // if any of these conditions are true the map is loaded.
        if (sm == null || map == null || map.size() == 0) {
            return true;
        }

        boolean keyIsPC = fmd.getKey().isDeclaredTypePC();
        boolean valIsPC = fmd.getElement().isDeclaredTypePC();

        // Map is does not contain PCs in either keys or values
        if (!(keyIsPC || valIsPC)) {
          return true;
        }
        
        Object[] arr = map.keySet().toArray();

        for (Object key : arr) {
            if (keyIsPC) {
                OpenJPAStateManager ksm = getStateManager(key);
                if (ksm == null) {
                    return true;
                }                        
                if (!(isLoaded(ksm, null, pcs) == LoadState.LOADED))
                    return false;
            }
            if (valIsPC) {
                Object value = map.get(key);
                OpenJPAStateManager vsm = getStateManager(value);
                if (vsm == null) {
                    return true;
                }                        
                if (!(isLoaded(vsm, null, pcs) == LoadState.LOADED))
                    return false;                    
            }
        }
        return true;
    }

    private static OpenJPAStateManager getStateManager(Object obj) {        
        if (obj == null || !(obj instanceof PersistenceCapable)) {
            return null;
        }
        
        PersistenceCapable pc = (PersistenceCapable)obj;
        StateManager sm = pc.pcGetStateManager();
        if (sm == null || !(sm instanceof OpenJPAStateManager)) {
            return null;
        }
        return (OpenJPAStateManager)sm;
    }
}
