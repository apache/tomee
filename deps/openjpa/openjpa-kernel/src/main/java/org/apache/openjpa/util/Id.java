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

import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.lib.util.J2DoPrivHelper;
import org.apache.openjpa.lib.util.Localizer;

/**
 * Datastore identity type. Implementations may choose to use this type,
 * or choose to use their own datastore identity values.
 *
 * @author Abe White
 */
public final class Id
    extends OpenJPAId {

    private static final Localizer _loc = Localizer.forPackage(Id.class);

    private final long _id;

    /**
     * Create an id from the given type and value; the value might be an
     * id instance, a stringified id, or a primary key value.
     */
    public static Id newInstance(Class cls, Object val) {
        if (val instanceof Id)
            return (Id) val;
        if (val instanceof String)
            return new Id(cls, (String) val);
        if (val instanceof Number)
            return new Id(cls, ((Number) val).longValue());
        if (val == null)
            return new Id(cls, 0L);
        throw new UserException(_loc.get("unknown-oid", cls, val,
            val.getClass()));
    }

    /**
     * Create an id from the result of a {@link #toString} call on another
     * instance.
     */
    public Id(String str) {
        this(str, (ClassLoader) null);
    }

    /**
     * Create an id from the result of an {@link #toString} call on another
     * instance.
     */
    public Id(String str, OpenJPAConfiguration conf, ClassLoader brokerLoader) {
        this(str, (conf == null) ? brokerLoader : conf.
            getClassResolverInstance().getClassLoader(Id.class, brokerLoader));
    }

    /**
     * Create an id from the result of an {@link #toString} call on another
     * instance.
     */
    public Id(String str, ClassLoader loader) {
        if (loader == null)
            loader = AccessController.doPrivileged(J2DoPrivHelper.getContextClassLoaderAction());

        if (str == null)
            _id = 0L;
        else {
            int dash = str.indexOf(TYPE_VALUE_SEP);
            try {
                type = Class.forName(str.substring(0, dash), true, loader);
            } catch (Throwable t) {
                throw new UserException(_loc.get("string-id", str), t);
            }
            _id = Long.parseLong(str.substring(dash + 1));
        }
    }

    /**
     * Construct from the result of a {@link #toString} call on another
     * instance.
     */
    public Id(Class cls, String key) {
        super(cls);

        if (key == null)
            _id = 0L;
        else {
            // allow either stringified long or result of Id.toString
            int dash = key.indexOf(TYPE_VALUE_SEP);
            if (dash > 0) // don't check for -1; might be negative number
                key = key.substring(dash + 1);
            _id = Long.parseLong(key);
        }
    }

    /**
     * Construct from key value.
     */
    public Id(Class cls, Long key) {
        this(cls, (key == null) ? 0L : key.longValue());
    }

    /**
     * Construct from key value.
     */
    public Id(Class cls, long key) {
        super(cls);
        _id = key;
    }

    /**
     * Construct from key value.
     */
    public Id(Class cls, long key, boolean subs) {
        super(cls, subs);
        _id = key;
    }

    /**
     * Primary key.
     */
    public long getId() {
        return _id;
    }

    public Object getIdObject() {
        return _id;
    }

    protected int idHash() {
        return (int) (_id ^ (_id >>> 32));
    }

    protected boolean idEquals(OpenJPAId other) {
        return _id == ((Id) other)._id;
	}
}
