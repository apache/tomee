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
package org.apache.openjpa.slice.policy;

import java.util.List;

import org.apache.openjpa.slice.DistributionPolicy;

import org.apache.openjpa.slice.PObject;
import org.apache.openjpa.slice.Person;


/**
 * Exemplar {@link DistributionPolicy} that maintains closure and distributes
 * based on attributes of the given instance. 
 * 
 * @author Pinaki Poddar 
 *
 */
public class UserDistributionPolicy implements DistributionPolicy {

	/**
	 * Distribute the given instance.
	 * Assumes that two configured slices are named as <em>One</em> and 
	 * <em>Two</em>.<br>
     * The policy is only implemented for PObject and Person i.e. two of three
     * known classes. No policy is implemented for Address because Address is
     * persisted always by cascade and hence Slice should assign automatically
	 * the same slice as its owner Person. 
	 * 
	 */
    public String distribute(Object pc, List<String> slices, Object context) {
		assertValidSlices(slices);
		if (pc instanceof PObject)
			return distribute((PObject)pc);
		if (pc instanceof Person) {
			return distribute((Person)pc);
		}
		throw new RuntimeException("No policy for " + pc.getClass());
	}
	
	void assertValidSlices(List<String> slices) {
		if (slices.contains("One") && slices.contains("Two"))
			return;
        throw new RuntimeException("This policy assumes two slices named " +
                "One and Two. But configured slices are " + slices);
	}
	
	/**
	 * Distribute PObject based on odd-even value of its id.
	 */
	String distribute(PObject pc) {
		return (pc.getId()%2 == 0) ? "One" : "Two";
	}
	
	/**
	 * Distribute Person based on first character of its name.
	 */
	String distribute(Person pc) {
		return (pc.getName().startsWith("A")) ? "One" : "Two";
	}
}
