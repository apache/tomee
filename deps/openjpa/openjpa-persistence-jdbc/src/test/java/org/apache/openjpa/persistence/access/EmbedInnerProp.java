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
import javax.persistence.Embedded;

@Access(AccessType.PROPERTY)
@Embeddable
public class EmbedInnerProp {

    private String inName;
    
    private EmbedOuterField eof;
    
    public String getInnerName() {
        return inName;
    }
    
    public void setInnerName(String innerName) {
        inName = innerName;
    }
    
    @Embedded
    public EmbedOuterField getOuterField() {
        return eof;
    }
    
    public void setOuterField(EmbedOuterField eo) {
        eof = eo;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof EmbedInnerProp) {
            EmbedInnerProp ps = (EmbedInnerProp)obj;
            
            return getInnerName().equals(ps.getInnerName()) &&
                getOuterField().equals(ps.getOuterField());
        }
        return false;
    }
}
