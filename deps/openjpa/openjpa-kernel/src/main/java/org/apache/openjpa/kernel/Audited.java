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

import java.util.BitSet;

import org.apache.openjpa.audit.AuditableOperation;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.enhance.Reflection;
import org.apache.openjpa.meta.FieldMetaData;

/**
 * Carries immutable information about an audited persistent instance.
 *  
 * @author Pinaki Poddar
 *
 */
public final class Audited {
	private final StateManagerImpl _sm;
	private final PersistenceCapable _original;
	
	/**
	 * Supply a state manager and a transient copy.
	 * @param sm a state manager, must not be null.
	 * @param o the transient copy
	 */
	Audited(StateManagerImpl sm, PersistenceCapable o) {
		if (sm == null || o == null)
			throw new NullPointerException("sm: " + sm + " original: " + o);
		if (o.pcGetStateManager() != null) 
			throw new IllegalArgumentException(o + " is not transient");
		_sm  = sm;
		_original = o;
	}
	
	/**
	 * Gets the current state of the persistent instance.
	 */
	public Object getManagedObject() {
		return _sm.getManagedInstance();
	}
	
	/**
	 * Gets the original state of the persistent instance as a transient instance.
	 */
	public Object getOriginalObject() {
		return _original;
	}
	
	/**
	 * Gets the name of the updated fields.
	 * 
	 * @return persistent property names that are modified.
	 * For deleted instances the array is empty and for newly created instances
	 * the array contains all the fields.
	 */
	public String[] getUpdatedFields() {
		BitSet dirty = _sm.getDirty();
		String[] names = new String[dirty.cardinality()];
		int j = 0;
		for (int pos = dirty.nextSetBit(0); pos != -1; pos = dirty.nextSetBit(pos+1)) {
			names[j++] = _sm.getMetaData().getField(pos).getName();		
		}
		return names;
	}
	
	/**
	 * Gets the value of the given field of the managed object.
	 * 
	 * @param field name of a persistent property
	 * @return value of the given field in the managed instance
	 * @exception IllegalArgumentException if the named field is not a persistent property 
	 */
	public Object getManangedFieldValue(String field) {
		FieldMetaData fmd = _sm.getMetaData().getField(field);
		if (fmd == null) {
			throw new IllegalArgumentException(field + " does not exist in " + _original);
		}
		return _sm.fetch(fmd.getIndex());
	}
	
	/**
	 * Gets the value of the given field of the original state of the object.
	 * 
	 * @param field name of a persistent property
	 * @return value of the given field in the original instance
	 * @exception IllegalArgumentException if the named field is not a persistent property 
	 */
	public Object getOriginalFieldValue(String field) {
		try {
			return Reflection.getValue(_original, field, true);
		} catch (Exception e) {
			throw new IllegalArgumentException(field + " does not exist in " + _original);
		}
	}
	
	/**
	 * Gets the type of this audit.
	 */
	public AuditableOperation getType() {
		PCState state = _sm.getPCState();
		if (state.isNew()) return AuditableOperation.CREATE;
		if (state.isDeleted()) return AuditableOperation.DELETE;
		if (state.isDirty()) return AuditableOperation.UPDATE;
		return null; // should not happen
	}
}
