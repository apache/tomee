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
package org.apache.openjpa.persistence.jdbc.maps.m2mmapex10;

import java.io.Serializable;

import javax.persistence.*;

@Embeddable
public class PhonePK implements Serializable {
    String areaCode;
    String phoneNum;

    public PhonePK() {}
    public PhonePK(String areaCode, String phoneNum) {
        this.areaCode = areaCode;
        this.phoneNum = phoneNum;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PhonePK))
            return false;
        PhonePK pk = (PhonePK) o;
        if (pk.areaCode.equals(areaCode) &&
            pk.phoneNum.equals(phoneNum))
            return true;    
        return false;
    }

    public int hashCode() {
        int code = 0;
        code = code * 31 + areaCode.hashCode();
        code = code * 31 + phoneNum.hashCode();
        return code;
    }
}
