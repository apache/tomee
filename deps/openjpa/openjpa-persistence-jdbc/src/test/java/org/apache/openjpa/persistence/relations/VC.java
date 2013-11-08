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

import org.apache.openjpa.persistence.relations.VCS.VCSId;

import org.apache.openjpa.persistence.jdbc.ForeignKey;

@Entity
@Table(name="VC4_REL")
@IdClass(VC.VCId.class)

public class VC {

	@Id
	private String vcId;

	// @ManyToOne
	// @ForeignKey
	// private I i;

	@ManyToOne
	@ForeignKey
	@Id
	private VCS vcs;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY,
            mappedBy = "vc")
	private Set<D> ds = new HashSet<D>();

	public VC() {
	}

	public String getVcId() {
		return vcId;
	}

	public void setVcId(String vcId) {
		this.vcId = vcId;
	}

	public Set<D> getDs() {
		return ds;
	}

	public void setDs(Set<D> ds) {
		this.ds = ds;
	}

	public VCS getVcs() {
		return vcs;
	}

	public void setVcs(VCS vcs) {
		this.vcs = vcs;
	}

	public static class VCId {
		private String vcId;

		private VCSId vcs;

		public String getVcId() {
			return vcId;
		}

		public void setVcId(String vcId) {
			this.vcId = vcId;
		}

		public VCSId getVcs() {
			return vcs;
		}

		public void setVcs(VCSId vcs) {
			this.vcs = vcs;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null || !(obj instanceof VCId))
				return false;
			VCId id = (VCId) obj;
			return (this.getVcId() == id.getVcId() ||
                (this.getVcId() == null &&
                this.getVcId().equals(id.getVcId()))) && 
			    (this.getVcs() == id.getVcs() ||
                (this.getVcs() == null && this
				.getVcs().equals(id.getVcs())));
		}

		@Override
		public int hashCode() {
            return (this.getVcId() != null ? this.getVcId().hashCode() : 0)
                    ^ (this.getVcs() != null ? this.getVcs().hashCode() : 0);
		}
	}
}
