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
package org.apache.openjpa.enhance.ids;

public class SoftwareId {
    
    public static boolean[] usedConstructor = new boolean[3];

    private Integer idInteger;
    
    private int idInt;
    
    private String idString;
    
    public SoftwareId() {
        
    }
    
    public SoftwareId(int idint) {
        usedConstructor[0] = true;
        idInt = idint;
    }

    public SoftwareId(Integer idinteger, int idint) {
        usedConstructor[1] = true;
        idInteger = idinteger;
        idInt = idint;
    }

    public SoftwareId(Integer idinteger, int idint, String idstring) {
        usedConstructor[2] = true;
        idInteger = idinteger;
        idInt = idint;
        idString =idstring;
    }
    public Integer getIdInteger() {
        return idInteger;
    }

    public int getIdInt() {
        return idInt;
    }

    public String getIdString() {
        return idString;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SoftwareId) {
            SoftwareId swid = (SoftwareId)obj;
            return swid.getIdInt() == getIdInt() &&
                swid.getIdInteger().equals(getIdInteger()) &&
                swid.getIdString().equals(getIdString());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return getIdInt() + getIdInteger().hashCode() + getIdString().hashCode();
    }

}
