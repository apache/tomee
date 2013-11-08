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
public class AttachB
    extends AttachA {

    private String bstr;
    private int bint;
    private double bdbl;
    private Set ds = new HashSet();
    private Map stringIntMap = new TreeMap();

    public void setBstr(String bstr) {
        this.bstr = bstr;
    }

    public String getBstr() {
        return this.bstr;
    }

    public void setBint(int bint) {
        this.bint = bint;
    }

    public int getBint() {
        return this.bint;
    }

    public void setBdbl(double bdbl) {
        this.bdbl = bdbl;
    }

    public double getBdbl() {
        return this.bdbl;
    }

    public void setDs(Set ds) {
        this.ds = ds;
    }

    public Set getDs() {
        return this.ds;
    }

    public void setStringIntMap(Map stringIntMap) {
        this.stringIntMap = stringIntMap;
    }

    public Map getStringIntMap() {
        return this.stringIntMap;
    }
}
