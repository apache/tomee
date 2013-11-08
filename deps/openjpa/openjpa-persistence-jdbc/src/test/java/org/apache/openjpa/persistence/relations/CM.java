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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.openjpa.persistence.jdbc.ForeignKey;

@Entity
@IdClass(CM.CMId.class)
@Table(name="CM4_REL")
public class CM {
	
	@Id
    private String cmId;

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="cm")
    private Set<C> cs = new HashSet<C>();

    @ManyToOne
    @ForeignKey
    @Id
    private E e;
    
    public CM() {
    }

    public String getCmId() {
        return cmId;
    }

    public void setCmId(String cmId) {
        this.cmId = cmId;
    }

	public Set<C> getCs() {
		return cs;
	}

	public void setCs(Set<C> cs) {
		this.cs = cs;
	}
	
	public void addC(C c){
		cs.add(c);
		c.setCm(this);
	}

	public E getE() {
		return e;
	}

	public void setE(E e) {
		this.e = e;
	}

	public static class CMId{
		private String cmId;
		private String e;
		
		public String getCmId() {
			return cmId;
		}
		public void setCmId(String id) {
			cmId = id;
		}
		
		public String getE() {
			return e;
		}
		public void setE(String e) {
			this.e = e;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;			
			if (obj == null && ! (obj instanceof CMId))
				return false;
			CMId id = (CMId) obj;
			return ( this.getCmId() == id.getCmId() ||
                (this.getCmId() != null && this.getCmId().equals(id.getCmId())))
                && ( this.getE() == id.getE() || (this.getE() != null &&
                this.getE().equals(id.getE())));
		}
		
		@Override
		public int hashCode() {
            return (this.getCmId() != null? this.getCmId().hashCode():0)
                    ^ (this.getE()!= null ? this.getE().hashCode():0);
		}
	}

}
