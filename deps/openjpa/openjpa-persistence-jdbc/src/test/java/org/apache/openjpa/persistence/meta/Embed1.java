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
import javax.persistence.Embeddable;
import javax.persistence.OneToOne;

/**
 * Domain class used by meta-model testing.
 * 
 * Uses explicit field based access.
 *
 * @author Pinaki Poddar
 *
 */
@Embeddable
@Access(AccessType.PROPERTY)
public class Embed1 {
	private String f1;
	private ImplicitFieldAccessBase entity0;
	
	
	public String getF1() {
		return f1;
	}
	public void setF1(String f1) {
		this.f1 = f1;
	}
	
	@OneToOne
	public ImplicitFieldAccessBase getEntity0() {
		return entity0;
	}
	
	public void setEntity0(ImplicitFieldAccessBase entity0) {
		this.entity0 = entity0;
	}
}
