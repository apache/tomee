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
package org.apache.openjpa.persistence.access;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Embeddable;

@Access(AccessType.PROPERTY)
@Embeddable
public class EmbedPropAccess {

    String fName;
    String lName;

    public EmbedPropAccess() {        
    }
    
    public EmbedPropAccess(String fn, String ln) {
        setFirstName(fn);
        setLastName(ln);
    }
    
    public String getFirstName() {
        return fName;
    }
    
    public void setFirstName(String fname) {
        fName = fname;        
    }
    
    public String getLastName() {
        return lName;
    }
    
    public void setLastName(String lname) {
        lName = lname;
    }    
    
    public boolean equals(Object obj) {
        if (obj instanceof EmbedPropAccess) {
            EmbedPropAccess ps = (EmbedPropAccess)obj;
            return getFirstName().equals(ps.getFirstName()) &&
                getLastName().equals(ps.getLastName());
        }
        return false;
    }
}
