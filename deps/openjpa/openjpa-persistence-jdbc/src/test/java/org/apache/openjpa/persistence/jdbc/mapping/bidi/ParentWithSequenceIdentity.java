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
package org.apache.openjpa.persistence.jdbc.mapping.bidi;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PostPersist;
import javax.persistence.PreUpdate;

import org.apache.openjpa.persistence.jdbc.ElementJoinColumn;

/**
 * Parent in a logically bidirectional but actually unidirectional parent-child 
 * relationship where Child holds reference to Parent via primary key and not 
 * via object reference.
 * Also database assigns identity for Parent. Hence, Parent sets the children's
 * reference to Parent in PostPersist callback.  
 * 
 * @author Pinaki Poddar
 *
 */
@Entity
public class ParentWithSequenceIdentity  implements IParent {
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private long id;
	
	private String name;
	
	/**
     * This field is <em>not</em> mapped by the child. The child's table will
     * hold an <em>implicit</em> foreign key linking to the primary key of this
	 * Parent's table. 
	 */
	@OneToMany(cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    @ElementJoinColumn(name="FK_PARENT_SEQ_ID", referencedAttributeName="id")
	private Set<Child> children;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setId(long id) {
        throw new RuntimeException(getClass() + ".setId() is not to be " +
            "invoked directly. This class is using SEQ Generation Starategy");
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<Child> getChildren() {
		return children;
	}

	public void addChild(Child child) {
		if (children == null)
			children = new HashSet<Child>();
		children.add(child);
	}
	
	public boolean removeChild(Child child) {
		return children != null && children.remove(child);
	}
	
	/**
	 * This method will be called back after database has assigned identity
	 * to this instance.
	 */
	@PreUpdate
	@PostPersist
	public void postPersist() {
		if (children == null)
			return;
		for (Child child : children) {
			child.setSeqParentId(this.getId());
		}
	}
}
