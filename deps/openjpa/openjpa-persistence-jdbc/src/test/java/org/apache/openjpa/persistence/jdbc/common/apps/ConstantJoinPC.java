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
import javax.persistence.Entity;

@Entity
public class ConstantJoinPC {

    private int pk1;
    private int pk2;
    private NonPKJoinPC nonPK;
    private NonPKJoinPC nonPK2;
    private Collection nonPKs = new HashSet();

    public int getPk1() {
        return this.pk1;
    }

    public void setPk1(int pk1) {
        this.pk1 = pk1;
    }

    public int getPk2() {
        return this.pk2;
    }

    public void setPk2(int pk2) {
        this.pk2 = pk2;
    }

    public NonPKJoinPC getNonPK() {
        return this.nonPK;
    }

    public void setNonPK(NonPKJoinPC nonPK) {
        this.nonPK = nonPK;
    }

    public NonPKJoinPC getNonPK2() {
        return this.nonPK2;
    }

    public void setNonPK2(NonPKJoinPC nonPK2) {
        this.nonPK2 = nonPK2;
    }

    public Collection getNonPKs() {
        return this.nonPKs;
    }

    public void setNonPKs(Collection nonPKs) {
        this.nonPKs = nonPKs;
    }
}
