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
package org.apache.openjpa.persistence.embed.attrOverrides;
import javax.persistence.*;

@Embeddable
public class PropertyOwner {
	@Embedded
	protected Address addr;
	@Column(length = 10)
	protected String ssn;
	
	public Address getAddress() {
		return addr;
	}
	
	public void setAddress(Address addr) {
		this.addr = addr;
	}
	
	public String getSsn() {
		return ssn;
	}

	public void setSsn(String ssn) {
		this.ssn = ssn;
	}
	
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof PropertyOwner)) return false;
		PropertyOwner p = (PropertyOwner) o;
		if (!ssn.equals(p.ssn)) return false;
		if (!addr.equals(p.addr)) return false;
		return true;
	}
	
	public int hashCode() {
		int ret = 0;
		ret = ret + 31 * ssn.hashCode();
		ret = ret + 31 * addr.hashCode();
		return ret;
	}
}
