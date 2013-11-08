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
package org.apache.openjpa.persistence.identity;

/**
 * Compound id containing a boolean value
 *
 * @author Dianne Richards
 * @since 2.1.0
 */
public class CompoundBooleanId {
    public String stringId;
    public boolean booleanId;
    
    @Override
    public int hashCode() {
        int result = 1;
        result = result + ((stringId == null) ? 0 : stringId.hashCode());
        result = result + ((booleanId == false) ? 0 : 1);
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CompoundBooleanId other = (CompoundBooleanId) obj;
        if (stringId == null && other.stringId != null)
            return false;
        else if (!stringId.equals(other.stringId))
            return false;
        if (booleanId != other.booleanId)
            return false;
        
        
        return true;
    }

    public String getStringId() {
        return stringId;
    }

    public void setStringId(String stringId) {
        this.stringId = stringId;
    }

    public boolean isBooleanId() {
        return booleanId;
    }

    public void setBooleanId(boolean booleanId) {
        this.booleanId = booleanId;
    }

}
