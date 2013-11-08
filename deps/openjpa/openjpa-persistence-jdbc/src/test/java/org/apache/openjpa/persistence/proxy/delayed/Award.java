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
package org.apache.openjpa.persistence.proxy.delayed;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class Award implements Serializable, Comparable<Award>{

    private static final long serialVersionUID = -1110613520812966568L;

    private String awdName;
    
    private String awdType;

    public void setAwdName(String awdName) {
        this.awdName = awdName;
    }

    public String getAwdName() {
        return awdName;
    }

    public void setAwdType(String awdType) {
        this.awdType = awdType;
    }

    public String getAwdType() {
        return awdType;
    }

    @Override
    public int compareTo(Award o) {
        String nameType = awdName+awdType;
        String nameType2 = o.getAwdName()+o.getAwdType();
        return nameType.compareTo(nameType2);
    }
}
