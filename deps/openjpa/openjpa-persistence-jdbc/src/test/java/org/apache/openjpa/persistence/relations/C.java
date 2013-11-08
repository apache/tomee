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
package org.apache.openjpa.persistence.relations;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.openjpa.persistence.jdbc.ForeignKey;

@Entity
@IdClass(C.CId.class)
@Table(name="C4_REL")
public class C {
	
	@Id
	private String cId;

	@ManyToOne
	@ForeignKey
	@Id
    private CM cm;

	@OneToMany(mappedBy="c")
	private Set<D> ds = new HashSet<D>();

    public C() {
    }
    
    public String getCId() {
        return cId;
    }

    public void setCId(String cId) {
        this.cId = cId;
    }

    public CM getCm() {
		return cm;
	}

	public void setCm(CM cm) {
		this.cm = cm;
	}

	public Set<D> getDs() {
		return ds;
	}

	public void setDs(Set<D> ds) {
		this.ds = ds;
	}
    
    public static class CId{
    	String cId;
    	CM.CMId  cm;
    	
		public String getCId() {
			return cId;
		}
		public void setCId(String id) {
			cId = id;
		}
    	
		
		public CM.CMId getCm() {
			return cm;
		}
		public void setCm(CM.CMId cm) {
			this.cm = cm;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null || !(obj instanceof CId))
				return false;
			
			CId id = (CId) obj;
			
            return (this.getCId() == id.getCId() || (this.getCId() != null &&
                this.getCId().equals(id.getCId())))
                && (this.getCm() == id.getCm() || (this.getCm() != null &&
                this.getCm().equals(id.getCm())));
		}
		
		@Override
		public int hashCode() {
            return ((this.getCId() != null) ? this.getCId().hashCode():0)
                    ^ ((this.getCm() != null)? this.getCm().hashCode():0);
		}
    }
}
