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
package org.apache.openjpa.enhance;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.kernel.StateManagerImpl;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;
import org.apache.openjpa.util.ImplHelper;

/**
 * Helper methods for managed types that use method redefinition for field
 * tracking.
 *
 * @since 1.0.0
 */
public class RedefinitionHelper {

    /**
     * Call {@link StateManagerImpl#dirtyCheck} if the argument is a
     * {@link StateManagerImpl}.
     */
    public static void dirtyCheck(StateManager sm) {
        if (sm instanceof StateManagerImpl)
            ((StateManagerImpl) sm).dirtyCheck();
    }

    /**
     * Notify the state manager for <code>o</code> (if any) that a field
     * is about to be accessed.
     */
    public static void accessingField(Object o, int absoluteIndex) {
        PersistenceCapable pc = ImplHelper.toPersistenceCapable(o, null);
        if (pc == null)
            return;
        StateManager sm = pc.pcGetStateManager();
        if (sm != null)
            sm.accessingField(absoluteIndex);
    }

    /**
     * Setting state callback.
     */
    public static void settingField(Object o, int idx, boolean cur,
        boolean next) {
        PersistenceCapable pc = ImplHelper.toPersistenceCapable(o, null);
        if (pc == null)
            return;
        StateManager sm = pc.pcGetStateManager();
        if (sm != null)
            sm.settingBooleanField(pc, idx, cur, next,
                OpenJPAStateManager.SET_USER);
    }

    /**
     * Setting state callback.
     */
    public static void settingField(Object o, int idx, char cur, char next) {
        PersistenceCapable pc = ImplHelper.toPersistenceCapable(o, null);
        if (pc == null)
            return;
        StateManager sm = pc.pcGetStateManager();
        if (sm != null)
            sm.settingCharField(pc, idx, cur, next,
                OpenJPAStateManager.SET_USER);
    }

    /**
     * Setting state callback.
     */
    public static void settingField(Object o, int idx, byte cur, byte next) {
        PersistenceCapable pc = ImplHelper.toPersistenceCapable(o, null);
        if (pc == null)
            return;
        StateManager sm = pc.pcGetStateManager();
        if (sm != null)
            sm.settingByteField(pc, idx, cur, next,
                OpenJPAStateManager.SET_USER);
    }

    /**
     * Setting state callback.
     */
    public static void settingField(Object o, int idx, short cur, short next) {
        PersistenceCapable pc = ImplHelper.toPersistenceCapable(o, null);
        if (pc == null)
            return;
        StateManager sm = pc.pcGetStateManager();
        if (sm != null)
            sm.settingShortField(pc, idx, cur, next,
                OpenJPAStateManager.SET_USER);
    }

    /**
     * Setting state callback.
     */
    public static void settingField(Object o, int idx, int cur, int next) {
        PersistenceCapable pc = ImplHelper.toPersistenceCapable(o, null);
        if (pc == null)
            return;
        StateManager sm = pc.pcGetStateManager();
        if (sm != null)
            sm.settingIntField(pc, idx, cur, next,
                OpenJPAStateManager.SET_USER);
    }

    /**
     * Setting state callback.
     */
    public static void settingField(Object o, int idx, long cur, long next) {
        PersistenceCapable pc = ImplHelper.toPersistenceCapable(o, null);
        if (pc == null)
            return;
        StateManager sm = pc.pcGetStateManager();
        if (sm != null)
            sm.settingLongField(pc, idx, cur, next,
                OpenJPAStateManager.SET_USER);
    }

    /**
     * Setting state callback.
     */
    public static void settingField(Object o, int idx, float cur, float next) {
        PersistenceCapable pc = ImplHelper.toPersistenceCapable(o, null);
        if (pc == null)
            return;
        StateManager sm = pc.pcGetStateManager();
        if (sm != null)
            sm.settingFloatField(pc, idx, cur, next,
                OpenJPAStateManager.SET_USER);
    }

    /**
     * Setting state callback.
     */
    public static void settingField(Object o, int idx, double cur,
        double next) {
        PersistenceCapable pc = ImplHelper.toPersistenceCapable(o, null);
        if (pc == null)
            return;
        StateManager sm = pc.pcGetStateManager();
        if (sm != null)
            sm.settingDoubleField(pc, idx, cur, next,
                OpenJPAStateManager.SET_USER);
    }

    /**
     * Setting state callback.
     */
    public static void settingField(Object o, int idx, String cur,
        String next) {
        PersistenceCapable pc = ImplHelper.toPersistenceCapable(o, null);
        if (pc == null)
            return;
        StateManager sm = pc.pcGetStateManager();
        if (sm != null)
            sm.settingStringField(pc, idx, cur, next,
                OpenJPAStateManager.SET_USER);
    }

    /**
     * Setting state callback.
     */
    public static void settingField(Object o, int idx, Object cur,
        Object next) {
        PersistenceCapable pc = ImplHelper.toPersistenceCapable(o, null);
        if (pc == null)
            return;
        StateManager sm = pc.pcGetStateManager();
        if (sm != null)
            sm.settingObjectField(pc, idx, cur, next,
                OpenJPAStateManager.SET_USER);
    }

    /**
     * Create a container instance that will delegate back to the state
     * manager to emulate lazy loading. This is used by PC subclasses for
     * unenhanced types that could not be redefined, and thus do not have
     * field-interception capabilities. Do this for all collection and
     * map field types, even if they are in the dfg, in case the fetch
     * groups are reset at runtime.
     *
     * @since 1.1.0
     */
    public static void assignLazyLoadProxies(StateManagerImpl sm) {
        FieldMetaData[] fmds = sm.getMetaData().getFields();
        for (int i = 0; i < fmds.length; i++) {
            switch (fmds[i].getTypeCode()) {
                case JavaTypes.COLLECTION:
                case JavaTypes.MAP:
                    PersistenceCapable pc = sm.getPersistenceCapable();
                    Field field = (Field) fmds[i].getBackingMember();
                    Reflection.set(pc, field,
                        newLazyLoadingProxy(fmds[i].getDeclaredType(), i, sm));
                    break;
            }
        }
    }

    private static Object newLazyLoadingProxy(Class type, final int idx,
        final StateManagerImpl sm) {
        InvocationHandler handler = new InvocationHandler() {

            public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
                // this will replace the field in the instance, so the dynamic
                // proxy should only be called the first time a
                // lazy-load-proxied field is used in normal usage.
                Object delegate = sm.fetch(idx);
                return method.invoke(delegate, args);
            }
        };
        return Proxy.newProxyInstance(type.getClassLoader(),
            new Class[] { type }, handler);
    }
}
