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
package org.apache.openjpa.persistence.proxy.entities;

import java.util.HashMap;
import java.util.Map;

public class StringParameterizable implements Parameterizable<String, String> {
	private static final long serialVersionUID = -4289064323865338447L;
	private Map<String, String> params = new HashMap<String, String>();
	
	public void addParameter(String key, String value) {
		this.params.put(key, value);
	}

	public Map<String, String> getParameters() {
		return this.params;
	}

	public void removeParameter(String key) {
		this.params.remove(key);
	}

	public void clearAllParameters() {
		this.params.clear();
	}

	public String getParameterValue(String key) {
		return this.params.get(key);
	}
	
	public void addAllParams(Map<String, String> newParams) {
		if (newParams != null) {
			params.putAll(newParams);
		}
	}
}
