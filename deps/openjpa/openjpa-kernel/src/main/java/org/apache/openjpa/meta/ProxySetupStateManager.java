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
package org.apache.openjpa.meta;

import java.io.ObjectOutput;
import java.util.Calendar;
import java.util.SortedMap;
import java.util.SortedSet;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.StateManager;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.InternalException;

/**
 * Mostly-unimplemented state manager type whose sole purpose is to
 * check the initial value of all SCO container fields for their initialized
 * types and comparators.
 *
 * @author Abe White
 */
class ProxySetupStateManager
    implements StateManager {

	private static final Localizer _loc = Localizer
		.forPackage(ProxySetupStateManager.class);

    private Object _object = null;

    public void setProxyData(PersistenceCapable pc, ClassMetaData meta) {
        FieldMetaData[] fmds = meta.getFields();
        for (int i = 0; i < fmds.length; i++) {
            // This method only gets called for concrete types. We need to do this processing for fields that might 
            // not be owned by pc. 
            
            switch (fmds[i].getDeclaredTypeCode()) {
                case JavaTypes.CALENDAR:
                    pc.pcProvideField(fmds[i].getIndex());
                    if (_object != null) {
                        // specified timezone
                        fmds[i]
                            .setInitializer(((Calendar) _object).getTimeZone());
                    }
                    break;
                case JavaTypes.COLLECTION:
                    pc.pcProvideField(fmds[i].getIndex());
                    if (_object != null) {
                        // more specific type?
                        if (_object.getClass() != fmds[i].getDeclaredType())
                            fmds[i].setProxyType(_object.getClass());

                        // custom comparator?
                        if (_object instanceof SortedSet)
                            fmds[i].setInitializer(((SortedSet) _object).
                                comparator());
                    }
                    break;
                case JavaTypes.MAP:
                    pc.pcProvideField(fmds[i].getIndex());
                    if (_object != null) {
                        // more specific type?
                        if (_object.getClass() != fmds[i].getDeclaredType())
                            fmds[i].setProxyType(_object.getClass());

                        // custom comparator?
                        if (_object instanceof SortedMap)
                            fmds[i].setInitializer(((SortedMap) _object).
                                comparator());
                    }
                    break;
            }
        }
    }

    public Object getPCPrimaryKey(Object oid, int field) {
        throw new UnsupportedOperationException();
    }

    public StateManager replaceStateManager(StateManager sm) {
        throw new InternalException();
    }

    public boolean isDirty() {
        throw new InternalException();
    }

    public boolean isTransactional() {
        throw new InternalException();
    }

    public boolean isPersistent() {
        throw new InternalException();
    }

    public boolean isNew() {
        throw new InternalException();
    }

    public boolean isDeleted() {
        throw new InternalException();
    }

    public boolean isDetached() {
        throw new InternalException();
    }

    public Object getGenericContext() {
        throw new InternalException();
    }

    public void dirty(String s) {
        throw new InternalException();
    }

    public Object fetchObjectId() {
        throw new InternalException();
    }

    public Object getVersion() {
        throw new InternalException();
    }

    public void accessingField(int i) {
        throw new InternalException();
    }

    public boolean serializing() {
        throw new InternalException();
    }

    public boolean writeDetached(ObjectOutput out) {
        throw new InternalException();
    }

    public void proxyDetachedDeserialized(int idx) {
        throw new InternalException();
    }

    public void settingBooleanField(PersistenceCapable pc, int i, boolean b,
        boolean b2, int set) {
        throw new InternalException();
    }

    public void settingCharField(PersistenceCapable pc, int i, char c, char c2,
        int set) {
        throw new InternalException();
    }

    public void settingByteField(PersistenceCapable pc, int i, byte b, byte b2,
        int set) {
        throw new InternalException();
    }

    public void settingShortField(PersistenceCapable pc, int i, short s,
        short s2, int set) {
        throw new InternalException();
    }

    public void settingIntField(PersistenceCapable pc, int i, int i2, int i3,
        int set) {
        throw new InternalException();
    }

    public void settingLongField(PersistenceCapable pc, int i, long l, long l2,
        int set) {
        throw new InternalException();
    }

    public void settingFloatField(PersistenceCapable pc, int i, float f,
        float f2, int set) {
        throw new InternalException();
    }

    public void settingDoubleField(PersistenceCapable pc, int i, double d,
        double d2, int set) {
        throw new InternalException();
    }

    public void settingStringField(PersistenceCapable pc, int i, String s,
        String s2, int set) {
        throw new InternalException();
    }

    public void settingObjectField(PersistenceCapable pc, int i, Object o,
        Object o2, int set) {
        throw new InternalException();
    }

    public void providedBooleanField(PersistenceCapable pc, int i, boolean b) {
        throw new InternalException(_loc.get(
                "unexpected_proxy_sm_attribute_type", pc.getClass().getName(),
				"boolean"));
    }

    public void providedCharField(PersistenceCapable pc, int i, char c) {
        throw new InternalException(_loc.get(
                "unexpected_proxy_sm_attribute_type", pc.getClass().getName(),
				"char"));
    }

    public void providedByteField(PersistenceCapable pc, int i, byte b) {
        throw new InternalException(_loc.get(
                "unexpected_proxy_sm_attribute_type", pc.getClass().getName(),
				"byte"));
    }

    public void providedShortField(PersistenceCapable pc, int i, short s) {
        throw new InternalException(_loc.get(
                "unexpected_proxy_sm_attribute_type", pc.getClass().getName(),
				"short"));
    }

    public void providedIntField(PersistenceCapable pc, int i, int i2) {
        throw new InternalException(_loc.get(
                "unexpected_proxy_sm_attribute_type", pc.getClass().getName(),
				"int"));
    }

    public void providedLongField(PersistenceCapable pc, int i, long l) {
        throw new InternalException(_loc.get(
                "unexpected_proxy_sm_attribute_type", pc.getClass().getName(),
				"long"));
    }

    public void providedFloatField(PersistenceCapable pc, int i, float f) {
        throw new InternalException(_loc.get(
                "unexpected_proxy_sm_attribute_type", pc.getClass().getName(),
				"float"));
    }

    public void providedDoubleField(PersistenceCapable pc, int i, double d) {
        throw new InternalException(_loc.get(
                "unexpected_proxy_sm_attribute_type", pc.getClass().getName(),
				"double"));
    }

    public void providedStringField(PersistenceCapable pc, int i, String s) {
        throw new InternalException(_loc.get(
                "unexpected_proxy_sm_attribute_type", pc.getClass().getName(),
				"String"));
    }

    public void providedObjectField(PersistenceCapable pc, int i, Object o) {
        _object = o;
    }

    public boolean replaceBooleanField(PersistenceCapable pc, int i) {
        throw new InternalException();
    }

    public char replaceCharField(PersistenceCapable pc, int i) {
        throw new InternalException();
    }

    public byte replaceByteField(PersistenceCapable pc, int i) {
        throw new InternalException();
    }

    public short replaceShortField(PersistenceCapable pc, int i) {
        throw new InternalException();
    }

    public int replaceIntField(PersistenceCapable pc, int i) {
        throw new InternalException();
    }

    public long replaceLongField(PersistenceCapable pc, int i) {
        throw new InternalException();
    }

    public float replaceFloatField(PersistenceCapable pc, int i) {
        throw new InternalException();
    }

    public double replaceDoubleField(PersistenceCapable pc, int i) {
        throw new InternalException();
    }

    public String replaceStringField(PersistenceCapable pc, int i) {
        throw new InternalException();
    }

    public Object replaceObjectField(PersistenceCapable pc, int i) {
        throw new InternalException();
    }
}
