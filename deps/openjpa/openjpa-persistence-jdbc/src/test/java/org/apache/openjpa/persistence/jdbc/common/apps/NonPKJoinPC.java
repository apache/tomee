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
package org.apache.openjpa.persistence.jdbc.common.apps;

import java.util.*;

public class NonPKJoinPC {

    private int id1;
    private int id2;
    private PartialJoinPC partial;
    private Set partials = new HashSet();
    private ConstantJoinPC constant;

    public PartialJoinPC getPartial() {
        return this.partial;
    }

    public void setPartial(PartialJoinPC partial) {
        this.partial = partial;
    }

    public int getId1() {
        return this.id1;
    }

    public void setId1(int id1) {
        this.id1 = id1;
    }

    public int getId2() {
        return this.id2;
    }

    public void setId2(int id2) {
        this.id2 = id2;
    }

    public Set getPartials() {
        return this.partials;
    }

    public void setPartials(Set partials) {
        this.partials = partials;
    }

    public ConstantJoinPC getConstant() {
        return this.constant;
    }

    public void setConstant(ConstantJoinPC constant) {
        this.constant = constant;
    }
}
