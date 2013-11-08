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

import java.security.AccessController;

import org.apache.openjpa.conf.Compatibility;
import org.apache.openjpa.kernel.DetachedStateManager;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.meta.ClassMetaData;

/**
 * Utility methods for managing proxies.
 *
 * @author Abe White
 */
public class Proxies {

    private static final Localizer _loc = Localizer.forPackage(Proxies.class);

    /**
     * Used by proxy types to check if the given owners and field names
     * are equivalent.
     */
    public static boolean isOwner(Proxy proxy, OpenJPAStateManager sm,
        int field) {
        return proxy.getOwner() == sm && proxy.getOwnerField() == field;
    }

    /**
     * Used by proxy types to check that an attempt to add a new value is legal.
     */
    public static void assertAllowedType(Object value, Class allowed) {
        if (value != null && allowed != null && !allowed.isInstance(value)) {
            throw new UserException(_loc.get("bad-elem-type", new Object[]{
                AccessController.doPrivileged(
                    J2DoPrivHelper.getClassLoaderAction(allowed)),
                allowed,
                AccessController.doPrivileged(
                    J2DoPrivHelper.getClassLoaderAction(value.getClass())),
                value.getClass()
            }));
        }
    }

    /**
     * Used by proxy types to dirty their owner.
     */
    public static void dirty(Proxy proxy, boolean stopTracking) {
        if (proxy.getOwner() != null)
            proxy.getOwner().dirty(proxy.getOwnerField());
        if (stopTracking && proxy.getChangeTracker() != null)
            proxy.getChangeTracker().stopTracking();
    }

    /**
     * Used by proxy types to notify collection owner on element removal.
     */
    public static void removed(Proxy proxy, Object removed, boolean key) {
        if (proxy.getOwner() != null && removed != null)
            proxy.getOwner().removed(proxy.getOwnerField(), removed, key);
    }

    /**
     * Used by proxy types to serialize non-proxy versions.
     */
    public static Object writeReplace(Proxy proxy, boolean detachable) {
        /* OPENJPA-1097 Remove $proxy classes during serialization based on:
         *   1) No Proxy, then return as-is
         *   2) Runtime created proxy (!detachable), then unproxy
         *   3) No StateManager (DetachedStateField==false), then return as-is
         *   Get the new IgnoreDetachedStateFieldForProxySerialization
         *      Compatibility flag from either the metadata/configuration if
         *      this is a normal StateManager, otherwise use the new flag
         *      added to the DetachedStateManager
         *   4) If new 2.0 behavior
         *      4a) If ClassMetaData exists and DetachedStateField == TRUE
         *          then do not remove the proxy and return as-is
         *      4b) Else, using DetachedStateField of transient(default) or
         *          false, so unproxy
         *   5) If 1.0 app or requested old 1.0 behavior
         *      5a) If detached, then do not unproxy and return as-is
         *      5b) Else, unproxy
         * 
         * Original code -
         *   1) Runtime created proxy (!detachable), then unproxy
         *   2) No Proxy, then return as-is
         *   3) No StateManager (DetachedStateField==false), then return as-is
         *   4) If detached, then return as-is <--- ERROR as EM.clear() marks
         *      entity as detached but doesn't remove any $proxy usage
         *   5) Else, unproxy
         * 
         *  if (detachable && (proxy == null || proxy.getOwner() == null 
         *      || proxy.getOwner().isDetached()))
         *      return proxy;
         *
         */
        if (proxy == null) {
            return proxy;
        } else if (!detachable) {
            // OPENJPA-1571 - using our runtime generated proxies, so remove any $proxy
            return proxy.copy(proxy);
        } else if (proxy.getOwner() == null) {
            // no StateManager (DetachedStateField==false), so no $proxy to remove
            return proxy;
        } else {
            // using a StateManager, so determine what DetachedState is being used
            OpenJPAStateManager sm = proxy.getOwner();  // null checked for above
            ClassMetaData meta = null;          // if null, no proxies?
            boolean useDSFForUnproxy = false;   // default to false for old 1.0 behavior

            // Don't rely on sm.isDetached() method because if we are serializing an attached Entity
            // the sm will still be a StateManagerImpl, but isDetached() will return true.

            // Using a DetachedStateManager, so use the new flag since there is no context or
            // metadata
            if (sm instanceof DetachedStateManager) {
                useDSFForUnproxy = ((DetachedStateManager) sm).getUseDSFForUnproxy();
            } else{
                // DetachedStateManager has no context or metadata, so we can't get configuration settings
                Compatibility compat = null;
                meta = sm.getMetaData();
                if (meta != null) {
                    compat = meta.getRepository().getConfiguration().getCompatibilityInstance();
                } else if (sm.getContext() != null && sm.getContext().getConfiguration() != null) {
                    compat = sm.getContext().getConfiguration().getCompatibilityInstance();
                } else {
                    // no-op - using a StateManager, but no Compatibility settings available
                }
                if (compat != null) {
                    // new 2.0 behavior of using DetachedStateField to determine unproxy during serialization
                    useDSFForUnproxy = !compat.getIgnoreDetachedStateFieldForProxySerialization();
                }
            }
            
            if (useDSFForUnproxy) {
                // use new 2.0 behavior
                if ((meta != null) && (Boolean.TRUE.equals(meta.usesDetachedState()))) {
                    // configured to always use and serialize a StateManger, so keep any $proxy
                    return proxy;
                } else {
                    // already detached or using DetachedStateField==false or transient, so remove any $proxy
                    return proxy.copy(proxy);
                }
            } else {
                // use old 1.0 behavior
                if (proxy.getOwner().isDetached())
                    return proxy;
                else
                    return proxy.copy(proxy);
            }
        }
    }
}

