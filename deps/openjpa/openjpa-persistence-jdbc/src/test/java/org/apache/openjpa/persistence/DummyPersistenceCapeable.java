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
package org.apache.openjpa.persistence;

import org.apache.openjpa.enhance.FieldConsumer;
import org.apache.openjpa.enhance.FieldSupplier;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.enhance.PCRegistry;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.StateManager;

/**
 * This Object is here for the sole purpose of testing pcGetEnhancementContractVersion. This object isn't a tested
 * PersistenceCapable implementation so it shouldn't be used unless you are fully aware of what you are doing.
 */
public class DummyPersistenceCapeable implements PersistenceCapable {
    private static int pcInheritedFieldCount;
    private static String pcFieldNames[] = {};
    private static Class pcFieldTypes[];
    private static byte pcFieldFlags[] = {};
    private static Class pcPCSuperclass;
    protected transient boolean pcVersionInit;
    protected transient StateManager pcStateManager;
    private transient Object pcDetachedState;

    static {
        Class aclass[] = new Class[0];
        pcFieldTypes = aclass;
        PCRegistry.register(DummyPersistenceCapeable.class, pcFieldNames, pcFieldTypes, pcFieldFlags, pcPCSuperclass,
            "DummyPersistenceCapeable", new DummyPersistenceCapeable());
    }

    public int pcGetEnhancementContractVersion() {
        return PCEnhancer.ENHANCER_VERSION - 1;
    }

    public PersistenceCapable pcNewInstance(StateManager sm, boolean clear) {
        return new DummyPersistenceCapeable();
    }

    public void pcCopyFields(Object fromObject, int[] fields) {
        // TODO Auto-generated method stub

    }

    public void pcCopyKeyFieldsFromObjectId(FieldConsumer consumer, Object obj) {
        // TODO Auto-generated method stub

    }

    public void pcCopyKeyFieldsToObjectId(FieldSupplier supplier, Object obj) {
        // TODO Auto-generated method stub

    }

    public void pcCopyKeyFieldsToObjectId(Object obj) {
        // TODO Auto-generated method stub

    }

    public void pcDirty(String fieldName) {
        // TODO Auto-generated method stub

    }

    public Object pcFetchObjectId() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object pcGetDetachedState() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object pcGetGenericContext() {
        // TODO Auto-generated method stub
        return null;
    }

    public StateManager pcGetStateManager() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object pcGetVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean pcIsDeleted() {
        // TODO Auto-generated method stub
        return false;
    }

    public Boolean pcIsDetached() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean pcIsDirty() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean pcIsNew() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean pcIsPersistent() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean pcIsTransactional() {
        // TODO Auto-generated method stub
        return false;
    }

    public PersistenceCapable pcNewInstance(StateManager sm, Object obj, boolean clear) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object pcNewObjectIdInstance() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object pcNewObjectIdInstance(Object obj) {
        // TODO Auto-generated method stub
        return null;
    }

    public void pcProvideField(int fieldIndex) {
        // TODO Auto-generated method stub

    }

    public void pcProvideFields(int[] fieldIndices) {
        // TODO Auto-generated method stub

    }

    public void pcReplaceField(int fieldIndex) {
        // TODO Auto-generated method stub

    }

    public void pcReplaceFields(int[] fieldIndex) {
        // TODO Auto-generated method stub

    }

    public void pcReplaceStateManager(StateManager sm) {
        // TODO Auto-generated method stub

    }

    public void pcSetDetachedState(Object state) {
        // TODO Auto-generated method stub

    }

    public DummyPersistenceCapeable() {
        // TODO Auto-generated constructor stub
    }
}
