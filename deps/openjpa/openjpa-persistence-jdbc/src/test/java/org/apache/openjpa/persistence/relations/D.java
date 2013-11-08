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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.openjpa.persistence.jdbc.ForeignKey;


@Entity
@Table(name="D4_REL")
@IdClass(D.CId.class)
public class D {

	@ManyToOne
	@ForeignKey
	private C c;

	@Id
	private String id;
	
	private String a;

	@ManyToOne
	@ForeignKey
	@Id
	private VC vc;

	public D() {
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getA() {
		return a;
	}

	public void setA(String a) {
		this.a = a;
	}

	public C getC() {
		return c;
	}

	public void setC(C c) {
		this.c = c;
	}

	public VC getVc() {
		return vc;
	}

	public void setVc(VC vc) {
		this.vc = vc;
	}
	
	
	public static class CId{
		private String id;
		
		private VC.VCId vc;
		
		public VC.VCId getVc() {
			return vc;
		}
		public void setVc(VC.VCId vc) {
			this.vc = vc;
		}
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null ||  ! (obj instanceof CId))
				return false;
			CId id = (CId) obj;
            return (this.getId() == id.getId() || (this.getId() != null &&
                this.getId().equals(id.getId())))
                && (this.getVc() == id.getVc() || (this.getVc() != null &&
                this.getVc().equals(id.getVc())));
		}
		
		@Override
		public int hashCode() {
            return (this.getId() != null ? this.getId().hashCode():0)
                    ^ (this.getVc() != null ? this.getVc().hashCode():0);
		}
				
	}

}
