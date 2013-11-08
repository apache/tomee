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
 * {@link OpenJPAId} subclass appropriate for boolean fields.
 *
 * @author Dianne Richards
 * @since 2.1.0
 */
public class BooleanId extends OpenJPAId {
    
    private final boolean key;
    
    public BooleanId(Class cls, Boolean key) {
        this(cls, key.booleanValue());
    }

    public BooleanId(Class cls, String key) {
        this(cls, Boolean.parseBoolean(key));
    }

    public BooleanId(Class cls, boolean key) {
        super(cls);
        this.key = key;
    }
    
    public BooleanId(Class cls, boolean key, boolean subs) {
        super(cls, subs);
        this.key = key;
    }
    
    public boolean getId() {
        return key;
    }

    @Override
    public Object getIdObject() {
        return Boolean.valueOf(key);
    }
    
    public String toString() {
        return Boolean.toString(key);
    }

    @Override
    protected boolean idEquals(OpenJPAId other) {
        return key == ((BooleanId) other).key;
    }

    @Override
    protected int idHash() {
        return Boolean.valueOf(key).hashCode();
    }

}
