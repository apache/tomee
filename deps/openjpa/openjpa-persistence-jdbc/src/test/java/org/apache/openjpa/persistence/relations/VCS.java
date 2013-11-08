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

import javax.persistence.Basic;
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
@Table(name="VCS4_REL")
@IdClass(VCS.VCSId.class)
public class VCS {

	@Id
	private String vcsId;

	@ManyToOne
	@ForeignKey
	@Id
	private E e;
	
	@Basic
	private String name;

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy = "vcs")
	private Set<VC> vcs = new HashSet<VC>();

	public VCS() {
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getVcsId() {
		return vcsId;
	}


	public void setVcsId(String vcsId) {
		this.vcsId = vcsId;
	}


	public E getE() {
		return e;
	}


	public void setE(E e) {
		this.e = e;
	}


	public Set<VC> getVcs() {
		return vcs;
	}


	public void setVcs(Set<VC> vcs) {
		this.vcs = vcs;
	}
	
	public void addVC(VC vc){
		vcs.add(vc);
		vc.setVcs(this);
	}
	public static class VCSId{
		private String vcsId;
		private String e;
	
		
		public String getE() {
			return e;
		}

		public void setE(String e) {
			this.e = e;
		}

		public String getVcsId() {
			return vcsId;
		}

		public void setVcsId(String vcsId) {
			this.vcsId = vcsId;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null ||  ! (obj instanceof VCSId))
				return false;
			VCSId id = (VCSId) obj;
			return (this.getVcsId() == id.getVcsId() || 
                (this.getVcsId() != null && 
                this.getVcsId().equals(id.getVcsId())))
                && (this.getE() == id.getE() || (this.getE() != null
                && this.getE().equals(id.getE())));
		}
		
		@Override
		public int hashCode() {
            return (this.getVcsId() != null ?this.getVcsId().hashCode():0)
                    ^ (this.getE() != null ? this.getE().hashCode():0);
		}
	}

}
