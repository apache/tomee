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

/**
 * Identity type appropriate for object primary key fields and shared
 * id classes.
 *
 * @author Abe White
 */
public final class ObjectId
    extends OpenJPAId {

    private Object _key;

    public ObjectId(Class cls, Object key) {
        super(cls);
        _key = key;
    }

    public ObjectId(Class cls, Object key, boolean subs) {
        super(cls, subs);
        _key = key;
    }

    public Object getId() {
        return _key;
    }

    /**
     * Allow utilities in this package to mutate id.
     */
    void setId(Object id) {
        _key = id;
    }

    public Object getIdObject() {
        return _key;
    }

    protected int idHash() {
        return (_key == null) ? 0 : _key.hashCode();
    }

    protected boolean idEquals(OpenJPAId o) {
        Object key = ((ObjectId) o)._key;
        return (_key == null) ? key == null : _key.equals(key);
    }
}
