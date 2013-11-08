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
 * {@link OpenJPAId} subclass appropriate for float fields.
 *
 * @author Abe White
 */
public final class FloatId 
    extends OpenJPAId {

    private final float key;

    public FloatId(Class cls, Float key) {
        this(cls, (key == null) ? 0F : key.floatValue());
    }

    public FloatId(Class cls, String key) {
        this(cls, (key == null) ? 0F : Float.parseFloat(key));
    }

    public FloatId(Class cls, float key) {
        super(cls);
        this.key = key;
    }

    public FloatId(Class cls, float key, boolean subs) {
        super(cls, subs);
        this.key = key;
    }

    public float getId() {
        return key;
    }

    public Object getIdObject() {
        return Float.valueOf(key);
    }

    public String toString() {
        return Float.toString(key);
    }

    protected int idHash() {
        return Float.floatToIntBits(key);
    }

    protected boolean idEquals(OpenJPAId o) {
        return key == ((FloatId) o).key;
    }
}
