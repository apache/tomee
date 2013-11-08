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
 * {@link OpenJPAId} subclass appropriate for short fields.
 *
 * @author Steve Kim
 */
public final class ShortId extends OpenJPAId {

    private final short key;

    public ShortId(Class cls, Short key) {
        this(cls, (key == null) ? (short) 0 : key.shortValue());
    }

    public ShortId(Class cls, String key) {
        this(cls, (key == null) ? (short) 0 : Short.parseShort(key));
    }

    public ShortId(Class cls, short key) {
        super(cls);
        this.key = key;
    }

    public ShortId(Class cls, short key, boolean subs) {
        super(cls, subs);
        this.key = key;
    }

    public short getId() {
        return key;
    }

    public Object getIdObject() {
        return Short.valueOf(key);
    }

    public String toString() {
        return Short.toString(key);
    }

    protected int idHash() {
        return key;
    }

    protected boolean idEquals(OpenJPAId o) {
        return key == ((ShortId) o).key;
    }
}
