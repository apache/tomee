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
package org.apache.openjpa.lib.conf;

import java.security.AccessController;

import org.apache.commons.lang.ObjectUtils;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.lib.util.ReferenceMap;
import org.apache.openjpa.lib.util.concurrent.ConcurrentReferenceHashMap;

/**
 * An object {@link Value}.
 *
 * @author Abe White
 */
public class ObjectValue extends Value {

    private static final Localizer _loc = Localizer.forPackage
        (ObjectValue.class);

    // cache the types' classloader
    private static ConcurrentReferenceHashMap _classloaderCache =
        new ConcurrentReferenceHashMap(ReferenceMap.HARD, ReferenceMap.WEAK);

    private Object _value = null;

    public ObjectValue(String prop) {
        super(prop);
    }

    /**
     * The internal value.
     */
    public Object get() {
        return _value;
    }

    /**
     * The internal value.
     */
    public void set(Object obj) {
        set(obj, false);
    }

    /**
     * The internal value.
     *
     * @param derived if true, this value was derived from other properties
     */
    public void set(Object obj, boolean derived) {
        if (!derived) assertChangeable();
        Object oldValue = _value;
        _value = obj;
        if (!derived && !ObjectUtils.equals(obj, oldValue)) {
            objectChanged();
            valueChanged();
        }
    }

    /**
     * Instantiate the object as an instance of the given class. Equivalent
     * to <code>instantiate(type, conf, true)</code>.
     */
    public Object instantiate(Class<?> type, Configuration conf) {
        return instantiate(type, conf, true);
    }

    /**
     * Instantiate the object as an instance of the given class.
     */
    public Object instantiate(Class<?> type, Configuration conf, boolean fatal)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Configure the given object.
     */
    public Object configure(Object obj, Configuration conf) {
        return configure(obj, conf, true);
    }

    /**
     * Configure the given object.
     */
    public Object configure(Object obj, Configuration conf, boolean fatal) {
        throw new UnsupportedOperationException();
    }

    /**
     * Allow subclasses to instantiate additional plugins. This method does
     * not perform configuration.
     */
    public Object newInstance(String clsName, Class<?> type, Configuration conf,
            boolean fatal) {
        ClassLoader cl = (ClassLoader) _classloaderCache.get(type);
        if (cl == null) {
            cl = AccessController.doPrivileged(
                J2DoPrivHelper.getClassLoaderAction(type));
            if (cl == null) {  // System classloader is returned as null
                cl = AccessController.doPrivileged(
                    J2DoPrivHelper.getSystemClassLoaderAction()); 
            }
            _classloaderCache.put(type, cl);
        }
        return Configurations.newInstance(clsName, this, conf, cl, fatal);
    }

    public Class<?> getValueType() {
        return Object.class;
    }

    /**
     * Implement this method to synchronize internal data with the new
     * object value.
     */
    protected void objectChanged() {
    }

    protected String getInternalString() {
        return null;
    }

    protected void setInternalString(String str) {
        if (str == null)
            set(null);
        else
            throw new IllegalArgumentException(_loc.get("cant-set-string",
                getProperty()).getMessage());
    }

    protected void setInternalObject(Object obj) {
        set(obj);
    }
}
