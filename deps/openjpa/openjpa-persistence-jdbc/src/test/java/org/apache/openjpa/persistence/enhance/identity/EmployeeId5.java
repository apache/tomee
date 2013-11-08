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

import javax.persistence.Embeddable;

@Embeddable
public class EmployeeId5 implements java.io.Serializable {

    String firstName;
    String lastName;

    public EmployeeId5() {
    }

    public EmployeeId5(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof EmployeeId5))
            return false;
        EmployeeId5 other = (EmployeeId5) o;
        if (firstName.equals(other.firstName) && 
                lastName.equals(other.lastName))
            return true;
        return false;
    }
    
    public int hashCode() {
        int ret = 0;
        ret += firstName.hashCode();
        ret = 31 * ret + lastName.hashCode();
        return ret;
    }
}

