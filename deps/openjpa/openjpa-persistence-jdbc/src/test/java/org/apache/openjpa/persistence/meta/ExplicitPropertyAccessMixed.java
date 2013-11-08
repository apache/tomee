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

package org.apache.openjpa.persistence.meta;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

/**
 * Domain class used by meta-model testing.
 * 
 * Uses explicit property access mixed with field access.
 *  
 * @author Pinaki Poddar
 *
 */
@Entity
@Access(AccessType.PROPERTY)
public class ExplicitPropertyAccessMixed {
	private String f1;
	private long f2;
	
	@Access(AccessType.FIELD)
	@OneToOne
	private ImplicitFieldAccessBase f3;
	private int f4;
	private short f5;
	private ImplicitFieldAccessBase f6;
	
	public String getF1() {
		return f1;
	}
	public void setF1(String f1) {
		this.f1 = f1;
	}
	@Transient
	public long getF2() {
		return f2;
	}
	public void setF2(long f2) {
		this.f2 = f2;
	}
	
	public ImplicitFieldAccessBase getF3() {
		return f3;
	}
	public void setF3(ImplicitFieldAccessBase f3) {
		this.f3 = f3;
	}
	
	public int getF4() {
		return f4;
	}
	public void setF4(int f4) {
		this.f4 = f4;
	}
	
	public short getF5() {
		return f5;
	}
	public void setF5(short f5) {
		this.f5 = f5;
	}
	
	@OneToOne
	public ImplicitFieldAccessBase getF6() {
		return f6;
	}
	public void setF6(ImplicitFieldAccessBase f6) {
		this.f6 = f6;
	}
}
