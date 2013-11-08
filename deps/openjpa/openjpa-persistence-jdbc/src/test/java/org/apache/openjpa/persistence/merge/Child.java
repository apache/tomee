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
package org.apache.openjpa.persistence.merge;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@IdClass(ChildPK.class)
@Table(name = "MRG_CHILD")
public class Child implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @JoinColumns({ @JoinColumn(name = "KEY_1", referencedColumnName = "KEY_1"),
      @JoinColumn(name = "KEY_2", referencedColumnName = "KEY_2") })
  @ManyToOne
  private Parent parent;

  @Id
  @Column(name = "KEY_3")
  private Integer childKey;

  @OneToMany(mappedBy = "child", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Collection<GrandChild> grandChilds = new ArrayList<GrandChild>();

  public Parent getParent() { return parent;  }
  public void setParent(Parent parent) { this.parent = parent;  }
  public Integer getChildKey() { return childKey; }
  public void setChildKey(Integer childKey) { this.childKey = childKey; }
  public Collection<GrandChild> getGrandChilds() {  return grandChilds;  }
  public void setGrandChilds(Collection<GrandChild> grandChilds) {   this.grandChilds = grandChilds; }
@Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((childKey == null) ? 0 : childKey.hashCode());
	result = prime * result
			+ ((grandChilds == null) ? 0 : grandChilds.hashCode());
	result = prime * result + ((parent == null) ? 0 : parent.hashCode());
	return result;
}
@Override
public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (obj == null)
		return false;
	if (getClass() != obj.getClass())
		return false;
	Child other = (Child) obj;
	if (childKey == null) {
		if (other.childKey != null)
			return false;
	} else if (!childKey.equals(other.childKey))
		return false;
	if (grandChilds == null) {
		if (other.grandChilds != null)
			return false;
	} else if (!grandChilds.equals(other.grandChilds))
		return false;
	if (parent == null) {
		if (other.parent != null)
			return false;
	} else if (!parent.equals(other.parent))
		return false;
	return true;
}  
}
