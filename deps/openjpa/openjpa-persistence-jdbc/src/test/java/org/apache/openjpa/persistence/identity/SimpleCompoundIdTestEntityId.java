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
package org.apache.openjpa.persistence.identity;


/**
 * JPA Id for SimpleCompoundIdTestEntity.
 *
 * @author Michael Vorburger
 */
public class SimpleCompoundIdTestEntityId {

	public Long firstId;

	public Long secondId;

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((firstId == null) ? 0 : firstId.hashCode());
		result = prime * result + ((secondId == null) ? 0 : secondId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleCompoundIdTestEntityId other = (SimpleCompoundIdTestEntityId) obj;
		if (firstId == null) {
			if (other.firstId != null)
				return false;
		} else if (!firstId.equals(other.firstId))
			return false;
		if (secondId == null) {
			if (other.secondId != null)
				return false;
		} else if (!secondId.equals(other.secondId))
			return false;
		return true;
	}
}
