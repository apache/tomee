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
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@IdClass(ParentPK.class)
@Table(name = "MRG_PARENT")
public class Parent implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "KEY_1")
  private String key1 = "00000000000000000000000000000000";

  @Id
  @Column(name = "KEY_2")
  private Integer key2;

  @OneToMany(mappedBy = "parent", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private Collection<Child> childs = new ArrayList<Child>();
  public Parent() {}

  public String getKey1() { return key1;  }
  public void setKey1(String key1) { this.key1 = key1;  }
  public Integer getKey2() { return key2;  }
  public void setKey2(Integer key2) { this.key2 = key2;  }
  public Collection<Child> getChilds() { return childs;  }
  public void setChilds(Collection<Child> childs) { this.childs = childs;  }
@Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((childs == null) ? 0 : childs.hashCode());
	result = prime * result + ((key1 == null) ? 0 : key1.hashCode());
	result = prime * result + ((key2 == null) ? 0 : key2.hashCode());
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
	Parent other = (Parent) obj;
	if (childs == null) {
		if (other.childs != null)
			return false;
	} else if (!childs.equals(other.childs))
		return false;
	if (key1 == null) {
		if (other.key1 != null)
			return false;
	} else if (!key1.equals(other.key1))
		return false;
	if (key2 == null) {
		if (other.key2 != null)
			return false;
	} else if (!key2.equals(other.key2))
		return false;
	return true;
}
  
  
}
