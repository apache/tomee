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

import org.apache.openjpa.slice.*;

public class EvenOddDistributionPolicy implements DistributionPolicy {
    public String distribute(Object pc, List<String> slices, Object context) {
        if (pc instanceof PObject) {
            int v = ((PObject)pc).getValue();
            return (v%2 == 0) ? "Even" : "Odd";
        }
        if (pc instanceof Person) {
        	String name = ((Person)pc).getName();
        	char firstChar = Character.toLowerCase(name.charAt(0));
        	return (firstChar >= 'a' && firstChar <='m') ? "Even" : "Odd";
        }
        if (pc instanceof Car)
            return distribute((Car)pc);
        if (pc instanceof Manufacturer)
            return distribute((Manufacturer)pc);
        
        return null;
    }
    
    String distribute(Car car) {
        return "Even";
    }
    
    String distribute(Manufacturer maker) {
        return "Odd";
    }

}
