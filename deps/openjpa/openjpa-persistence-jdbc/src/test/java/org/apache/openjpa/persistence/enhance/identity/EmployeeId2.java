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
package org.apache.openjpa.persistence.enhance.identity;

import javax.persistence.*;

@Embeddable
public class EmployeeId2 {
    String firstName;
    String lastName;
    
    public EmployeeId2() {}
    
    public EmployeeId2(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof EmployeeId2)) return false;
        EmployeeId2 eid = (EmployeeId2)o;
        String firstName0 = eid.getFirstName();
        String lastName0 = eid.getLastName();
        if (firstName != null && !firstName.equals(firstName0)) return false;
        if (firstName == null && firstName0 != null) return false;
        if (lastName != null && !lastName.equals(lastName0)) return false;
        if (lastName == null && lastName0 != null) return false;
        return true;
    }
    
    public int hashCode() {
        int ret = 0;
        ret = ret * 31 + firstName.hashCode();
        ret = ret * 31 + lastName.hashCode();
        return ret;
    }
    
}
