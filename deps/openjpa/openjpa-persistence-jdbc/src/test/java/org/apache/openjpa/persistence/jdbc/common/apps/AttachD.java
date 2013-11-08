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
import java.io.*;

import javax.persistence.Entity;

@Entity
public class AttachD
    implements Serializable {

    private Object version;

    private String dstr;
    private int dint;
    private double ddbl;
    private AttachA a;
    private Set bs = new HashSet(); // DFG
    private Map pcStringMap = new HashMap();
    private TreeMap bigIntegerPCMap = new TreeMap();

    private AttachE embeddedE;
    private AttachA embeddedA;

    public void setDstr(String dstr) {
        this.dstr = dstr;
    }

    public String getDstr() {
        return this.dstr;
    }

    public void setDint(int dint) {
        this.dint = dint;
    }

    public int getDint() {
        return this.dint;
    }

    public void setDdbl(double ddbl) {
        this.ddbl = ddbl;
    }

    public double getDdbl() {
        return this.ddbl;
    }

    public AttachA getA() {
        return a;
    }

    public void setA(AttachA a) {
        this.a = a;
    }

    public void setBs(Set bs) {
        this.bs = bs;
    }

    public Set getBs() {
        return this.bs;
    }

    public void setPcStringMap(Map pcStringMap) {
        this.pcStringMap = pcStringMap;
    }

    public Map getPcStringMap() {
        return this.pcStringMap;
    }

    public void setBigIntegerPCMap(TreeMap bigIntegerPCMap) {
        this.bigIntegerPCMap = bigIntegerPCMap;
    }

    public TreeMap getBigIntegerPCMap() {
        return this.bigIntegerPCMap;
    }

    public void setEmbeddedE(AttachE embeddedE) {
        this.embeddedE = embeddedE;
    }

    public AttachE getEmbeddedE() {
        return this.embeddedE;
    }

    public void setEmbeddedA(AttachA embeddedA) {
        this.embeddedA = embeddedA;
    }

    public AttachA getEmbeddedA() {
        return this.embeddedA;
    }

    public Object getVersion() {
        return this.version;
    }
}
