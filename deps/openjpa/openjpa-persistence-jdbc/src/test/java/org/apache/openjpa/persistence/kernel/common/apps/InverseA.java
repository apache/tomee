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

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "inverseA")
public class InverseA {

    @Column(length = 35)
    private String stringField;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int intField;
    @Column(name = "oneone")
    private InverseA oneOne;
    @Column(name = "oneowner")
    private InverseA oneOneOwner;
    @Column(name = "onemany")
    private InverseA oneMany;
    private Set manyOne = new HashSet();
    private Set manyMany = new HashSet();
    private Set manyManyOwner = new HashSet();
    private Set nullSet = null;
    private InverseA nullOwner;

    public String getStringField() {
        return stringField;
    }

    public void setStringField(String s) {
        stringField = s;
    }

    public int getIntField() {
        return intField;
    }

    public void setIntField(int i) {
        intField = i;
    }

    public InverseA getOneOne() {
        return oneOne;
    }

    public void setOneOne(InverseA a) {
        oneOne = a;
    }

    public InverseA getOneOneOwner() {
        return oneOneOwner;
    }

    public void setOneOneOwner(InverseA a) {
        oneOneOwner = a;
    }

    public InverseA getOneMany() {
        return oneMany;
    }

    public void setOneMany(InverseA a) {
        oneMany = a;
    }

    public Set getManyOne() {
        return manyOne;
    }

    public Set getManyMany() {
        return manyMany;
    }

    public Set getManyManyOwner() {
        return manyManyOwner;
    }

    public Set getNullSet() {
        return nullSet;
    }

    public void setNullSet(Set s) {
        nullSet = s;
    }

    public InverseA getNullOwner() {
        return nullOwner;
    }

    public void setNullOwner(InverseA a) {
        nullOwner = a;
    }
}
