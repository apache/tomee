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
package org.apache.openjpa.audit;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.kernel.Audited;
import org.apache.openjpa.persistence.Type;

/**
 * An example of an immutable persistent entity that holds a reference to the entity being audited.
 * <br>
 * This entity holds the reference to the entity being audited in a <em>generic</em>
 * sense i.e. it does not know the exact type of the audited entity, but merely that
 * it is a {@link PersistenceCapable} instance.
 * <br>
 * OpenJPA supports such reference by annotating with the {@link #audited reference field} as 
 * <tt>@Type(PersistenceCapable.class)</tt>.
 * <br>
 * The audit operation is also represented as a {@link #operation enumerated field}.
 *    
 * @author Pinaki Poddar
 *
 */
@Entity
public class AuditedEntry {
	@Id
	@GeneratedValue
	private long id;
	
	@ManyToOne(cascade=CascadeType.MERGE)
	@Type(PersistenceCapable.class)
	private Object audited;
	
	@Enumerated(EnumType.STRING)
	private AuditableOperation operation;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Timestamp ts;
	
	@ElementCollection
	private List<String> updatedFields;
	
	/**
	 * Constructs using an {@link Audited audited} instance.
	 * <br>
	 * An audited instances are supplied to the {@link Auditor#audit(Broker, Collection, Collection, Collection)
	 * auditor} by OpenJPA runtime within the transaction just before it is going to commit.
	 * <br>
	 * An audited instance carries the managed instance being audited in two <em>separate</em> references.
	 * The {link {@link Audited#getManagedObject()} first reference} is to the actual persistence instance
	 * being audited. The {link {@link Audited#getOriginalObject() second reference} is a <em>transient</em>
	 * copy of the actual persistence instance but in a state as it were when it entered the managed context
	 * i.e. when it was persisted or loaded from the database.
	 * <br>
	 * The {@link Audited} instance also knows the fields that were updated.    
	 * @param a an audited instance.
	 */
	public AuditedEntry(Audited a) {
		audited = a.getManagedObject();
		ts = new Timestamp(new Date().getTime());
		operation = a.getType();
		if (operation == AuditableOperation.UPDATE) {
			updatedFields = Arrays.asList(a.getUpdatedFields());
		}
		
	}

	public Object getAudited() {
		return audited;
	}

	public AuditableOperation getOperation() {
		return operation;
	}

	public Timestamp getTimestamp() {
		return ts;
	}

	public long getId() {
		return id;
	}
	
	public List<String> getUpdatedFields() {
		return updatedFields;
	}
}
