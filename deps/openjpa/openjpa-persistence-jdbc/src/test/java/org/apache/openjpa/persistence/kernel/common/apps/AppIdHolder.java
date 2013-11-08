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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.persistence.Entity;

/**
 * Holder for one-one and one-many relations to app id hierarchy classes.
 *
 * @author <a href="mailto:marc@solarmetric.com">Marc Prud'hommeaux</a>
 */
@Entity
public class AppIdHolder {

    private int id = Math.abs((int) (Math.random() * (Integer.MAX_VALUE)));

    private String someField;

    // one-to-one relations
    private AppIdSuper appIdSuper;
    private AppIdSubA appIdSubA;
    private AppIdSubB appIdSubB;
    private AppIdSubC appIdSubC;
    private AppIdSubD appIdSubD;
    private AppIdSubE appIdSubE;
    private AppIdSubF appIdSubF;

    // one-to-many relations
    private Set appIdSupers;
    private Collection appIdSubAs;
    private List appIdSubBs;
    private Set appIdSubCs;
    private LinkedList appIdSubDs;
    private HashSet appIdSubEs;
    private ArrayList appIdSubFs;

    public void setSomeField(String someField) {
        this.someField = someField;
    }

    public String getSomeField() {
        return this.someField;
    }

    public void setAppIdSuper(AppIdSuper appIdSuper) {
        this.appIdSuper = appIdSuper;
    }

    public AppIdSuper getAppIdSuper() {
        return this.appIdSuper;
    }

    public void setAppIdSubA(AppIdSubA appIdSubA) {
        this.appIdSubA = appIdSubA;
    }

    public AppIdSubA getAppIdSubA() {
        return this.appIdSubA;
    }

    public void setAppIdSubB(AppIdSubB appIdSubB) {
        this.appIdSubB = appIdSubB;
    }

    public AppIdSubB getAppIdSubB() {
        return this.appIdSubB;
    }

    public void setAppIdSubC(AppIdSubC appIdSubC) {
        this.appIdSubC = appIdSubC;
    }

    public AppIdSubC getAppIdSubC() {
        return this.appIdSubC;
    }

    public void setAppIdSubD(AppIdSubD appIdSubD) {
        this.appIdSubD = appIdSubD;
    }

    public AppIdSubD getAppIdSubD() {
        return this.appIdSubD;
    }

    public void setAppIdSubE(AppIdSubE appIdSubE) {
        this.appIdSubE = appIdSubE;
    }

    public AppIdSubE getAppIdSubE() {
        return this.appIdSubE;
    }

    public void setAppIdSubF(AppIdSubF appIdSubF) {
        this.appIdSubF = appIdSubF;
    }

    public AppIdSubF getAppIdSubF() {
        return this.appIdSubF;
    }

    public void setAppIdSubAs(Collection appIdSubAs) {
        this.appIdSubAs = appIdSubAs;
    }

    public Collection getAppIdSubAs() {
        return this.appIdSubAs;
    }

    public void setAppIdSubBs(List appIdSubBs) {
        this.appIdSubBs = appIdSubBs;
    }

    public List getAppIdSubBs() {
        return this.appIdSubBs;
    }

    public void setAppIdSubCs(Set appIdSubCs) {
        this.appIdSubCs = appIdSubCs;
    }

    public Set getAppIdSubCs() {
        return this.appIdSubCs;
    }

    public void setAppIdSubDs(LinkedList appIdSubDs) {
        this.appIdSubDs = appIdSubDs;
    }

    public LinkedList getAppIdSubDs() {
        return this.appIdSubDs;
    }

    public void setAppIdSubEs(HashSet appIdSubEs) {
        this.appIdSubEs = appIdSubEs;
    }

    public HashSet getAppIdSubEs() {
        return this.appIdSubEs;
    }

    public void setAppIdSubFs(ArrayList appIdSubFs) {
        this.appIdSubFs = appIdSubFs;
    }

    public ArrayList getAppIdSubFs() {
        return this.appIdSubFs;
    }

    public void setAppIdSupers(Set appIdSupers) {
        this.appIdSupers = appIdSupers;
    }

    public Set getAppIdSupers() {
        return this.appIdSupers;
    }
}

