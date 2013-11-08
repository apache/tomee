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

/**
 * FieldManager that responds to all fetch methods with the default value
 * for that field; used to clear the state of managed instances.
 *
 * @author Abe White
 */
class ClearFieldManager
    extends AbstractFieldManager {

    private static final ClearFieldManager _single = new ClearFieldManager();

    public static ClearFieldManager getInstance() {
        return _single;
    }

    protected ClearFieldManager() {
    }

    public boolean fetchBooleanField(int field) {
        return false;
    }

    public byte fetchByteField(int field) {
        return 0;
    }

    public char fetchCharField(int field) {
        return 0;
    }

    public double fetchDoubleField(int field) {
        return 0D;
    }

    public float fetchFloatField(int field) {
        return 0F;
    }

    public int fetchIntField(int field) {
        return 0;
    }

    public long fetchLongField(int field) {
        return 0L;
    }

    public Object fetchObjectField(int field) {
        return null;
    }

    public short fetchShortField(int field) {
        return 0;
    }

    public String fetchStringField(int field) {
        return null;
    }
}
