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


/**
 * Distributes the instances uniformly among the available slices
 * based on the integral value of the persistence identifier.
 * <br>
 * Given {@code M} slices and {@code N} instances whose identity
 * value is uniformly distributed, this policy will persist these
 * instances such that
 * <LI>each slice will have N/M instances
 * <LI>the identity of the instances in the {@code i}-th slice 
 * will be divisible by {@code i}.
 * 
 * @author Pinaki Poddar
 *
 */
public class UniformDistributionPolicy implements DistributionPolicy {

	@Override
	public String distribute(Object pc, List<String> slices, Object context) {
		int N = slices.size();
		for (int i = N; i > 0; i--) {
			PObject p = (PObject)pc;
			if (p.getId()%i == 0) return slices.get(i-1);
		}
		return slices.get(0);
	}

}
