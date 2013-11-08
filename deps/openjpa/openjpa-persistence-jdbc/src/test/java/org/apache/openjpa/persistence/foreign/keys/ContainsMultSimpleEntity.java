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
package org.apache.openjpa.persistence.foreign.keys;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class ContainsMultSimpleEntity implements Serializable {
    
    private static final long serialVersionUID = -8576236113079133657L;

    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(nullable=false)
    private SimpleEntity simpleEntity1;
    
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(nullable=false)
    private SimpleEntity simpleEntity2;
    
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(nullable=false)
    private SimpleEntity simpleEntity3;

    public SimpleEntity getSimpleEntity1() {
        return simpleEntity1;
    }

    public void setSimpleEntity1(SimpleEntity simpleEntity1) {
        this.simpleEntity1 = simpleEntity1;
    }

    public SimpleEntity getSimpleEntity2() {
        return simpleEntity2;
    }

    public void setSimpleEntity2(SimpleEntity simpleEntity2) {
        this.simpleEntity2 = simpleEntity2;
    }

    public SimpleEntity getSimpleEntity3() {
        return simpleEntity3;
    }

    public void setSimpleEntity3(SimpleEntity simpleEntity3) {
        this.simpleEntity3 = simpleEntity3;
    }

}
