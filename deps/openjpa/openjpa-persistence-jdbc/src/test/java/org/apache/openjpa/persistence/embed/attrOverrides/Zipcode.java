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
public class Zipcode {
    @Column(length = 20)
	protected String zip;
    @Column(length = 20)
	protected String plusFour;
	
	public String getZip() {
		return zip;
	}
	
	public void setZip(String zip) {
		this.zip = zip;
	}
	
	public String getPlusFour() {
		return plusFour;
	}
	
	public void setPlusFour(String plusFour) {
		this.plusFour = plusFour;
	}
	
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Zipcode)) return false;
		Zipcode z = (Zipcode) o;
		if (!zip.equals(z.zip)) return false;
		if (!plusFour.equals(z.plusFour)) return false;
		return true;
	}
	
	public int hashCode() {
		int ret = 0;
		ret = ret + 31 * zip.hashCode();
		ret = ret + 31 * plusFour.hashCode();
		return ret;
	}
}
