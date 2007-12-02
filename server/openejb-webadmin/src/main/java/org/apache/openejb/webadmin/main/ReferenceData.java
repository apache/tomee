/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.webadmin.main;

import java.io.Serializable;

/**
 * A simple data object for ejb and resource reference data
 * 
 * @author <a href="mailto:tim_urberg@yahoo.com">Tim Urberg</a>
 */
public class ReferenceData implements Serializable {
	public static final String RESOURCE_REFERENCE = "resource_reference";
	public static final String EJB_REFERENCE = "ejb_reference";
	
	private String referenceType;
	private String referenceIdName;
	private String referenceIdValue;
	private String referenceName;
	private String referenceValue;

	public ReferenceData() {
		super();
	}

	public String getReferenceIdName() {
		return referenceIdName;
	}

	public String getReferenceIdValue() {
		return referenceIdValue;
	}

	public String getReferenceName() {
		return referenceName;
	}

	public String getReferenceValue() {
		return referenceValue;
	}

	public String getReferenceType() {
		return referenceType;
	}

	public void setReferenceIdName(String string) {
		referenceIdName = string;
	}

	public void setReferenceIdValue(String string) {
		referenceIdValue = string;
	}

	public void setReferenceName(String string) {
		referenceName = string;
	}

	public void setReferenceValue(String string) {
		referenceValue = string;
	}

	public void setReferenceType(String string) {
		referenceType = string;
	}

}
