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

import org.apache.openjpa.slice.Car;
import org.apache.openjpa.slice.FinderTargetPolicy;
import org.apache.openjpa.slice.Manufacturer;
import org.apache.openjpa.slice.PObject;
import org.apache.openjpa.slice.Person;

/**
 * A sample policy to target slices based on a persistent type and a given identifier.
 * 
 * @author Pinaki Poddar
 *
 */
public class SampleFinderPolicy implements FinderTargetPolicy {

	@Override
	public String[] getTargets(Class<?> cls, Object oid, List<String> slices, Object context) {
		
        if (cls == PObject.class || cls == Person.class) {
            int id = ((Long)oid).intValue();
            return new String[]{slices.get(id%2)};
        }
        
        if (cls == Car.class) {
        	String vin = (String)oid;
        	char firstChar = Character.toLowerCase(vin.charAt(0));
        	return new String[]{slices.get((firstChar >= 'a' && firstChar <='m') ? 0 : 1)};
        }
        if (cls == Manufacturer.class) {
        	String name = (String)oid;
            return new String[] {
                    slices.get("BMW".equalsIgnoreCase(name) ? 0 : "HONDA".equalsIgnoreCase(name) ? 1 : 2)
                };
        }
		return null; // look in all slices
	}

}
