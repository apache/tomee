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
package org.apache.openjpa.persistence.kernel.common.apps;

import javax.persistence.Basic;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;

import org.apache.openjpa.persistence.FetchAttribute;
import org.apache.openjpa.persistence.FetchGroup;
import org.apache.openjpa.persistence.FetchGroups;
import org.apache.openjpa.persistence.LoadFetchGroup;

@Entity
@DiscriminatorValue("FETCH_GRP_TOBJCHILD")
@FetchGroups({
@FetchGroup(name = "g1", attributes = {
@FetchAttribute(name = "childB")
    }),
@FetchGroup(name = "g2", attributes = {
@FetchAttribute(name = "childC")
    }),
@FetchGroup(name = "g3", attributes = {
@FetchAttribute(name = "childD")
    })
    })
public class FetchGroupTestObjectChild extends FetchGroupTestObject {

    private int childA;

    @Basic(fetch = FetchType.LAZY)
    @LoadFetchGroup("g1")
    private int childB;

    @Basic(fetch = FetchType.LAZY)
    @LoadFetchGroup("g2")
    private int childC;

    @Basic(fetch = FetchType.LAZY)
    @LoadFetchGroup("g3")
    private int childD;

    public void setChildA(int val) {
        childA = val;
    }

    public int getChildA() {
        return childA;
    }

    public void setChildB(int val) {
        childB = val;
    }

    public int getChildB() {
        return childB;
    }

    public void setChildC(int val) {
        childC = val;
    }

    public int getChildC() {
        return childC;
    }

    public void setChildD(int val) {
        childD = val;
    }

    public int getChildD() {
        return childD;
    }
}
