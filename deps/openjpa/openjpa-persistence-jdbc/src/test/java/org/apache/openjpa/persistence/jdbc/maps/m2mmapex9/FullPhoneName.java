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
package org.apache.openjpa.persistence.jdbc.maps.m2mmapex9;

import javax.persistence.Embeddable;

@Embeddable
public class FullPhoneName {

    String fName;
    String lName;

    public FullPhoneName() {}

    public FullPhoneName(String fName, String lName) {
        this.fName = fName;
        this.lName = lName;
    }

    public String getFName() {
        return fName;
    }

    public void setFName(String fName) {
        this.fName = fName;
    }

    public String getLName() {
        return lName;
    }

    public void setLName(String lName) {
        this.lName = lName;
    }

    public boolean equals(Object o) {
        if (!(o instanceof FullPhoneName)) return false;
        FullPhoneName other = (FullPhoneName) o;
        if (fName.equals(other.fName) &&
                lName.equals(other.lName))
            return true;
        return false;
    }

    public int hashCode() {
        int ret = 0;
        ret += lName.hashCode();
        ret = 31 * ret + fName.hashCode();
        return ret;
    }
}
