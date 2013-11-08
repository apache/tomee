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

import org.apache.openjpa.enhance.FieldManager;
import org.apache.openjpa.util.InternalException;

/**
 * Abstract {@link FieldManager} for easy subclassing. Throws exceptions
 * for all methods.
 *
 * @author Abe White
 */
abstract class AbstractFieldManager
    implements FieldManager {

    public boolean fetchBooleanField(int field) {
        throw new InternalException();
    }

    public byte fetchByteField(int field) {
        throw new InternalException();
    }

    public char fetchCharField(int field) {
        throw new InternalException();
    }

    public double fetchDoubleField(int field) {
        throw new InternalException();
    }

    public float fetchFloatField(int field) {
        throw new InternalException();
    }

    public int fetchIntField(int field) {
        throw new InternalException();
    }

    public long fetchLongField(int field) {
        throw new InternalException();
    }

    public Object fetchObjectField(int field) {
        throw new InternalException();
    }

    public short fetchShortField(int field) {
        throw new InternalException();
    }

    public String fetchStringField(int field) {
        throw new InternalException();
    }

    public void storeBooleanField(int field, boolean curVal) {
        throw new InternalException();
    }

    public void storeByteField(int field, byte curVal) {
        throw new InternalException();
    }

    public void storeCharField(int field, char curVal) {
        throw new InternalException();
    }

    public void storeDoubleField(int field, double curVal) {
        throw new InternalException();
    }

    public void storeFloatField(int field, float curVal) {
        throw new InternalException();
    }

    public void storeIntField(int field, int curVal) {
        throw new InternalException();
    }

    public void storeLongField(int field, long curVal) {
        throw new InternalException();
    }

    public void storeObjectField(int field, Object curVal) {
        throw new InternalException();
    }

    public void storeShortField(int field, short curVal) {
        throw new InternalException();
    }

    public void storeStringField(int field, String curVal) {
        throw new InternalException();
    }
}
