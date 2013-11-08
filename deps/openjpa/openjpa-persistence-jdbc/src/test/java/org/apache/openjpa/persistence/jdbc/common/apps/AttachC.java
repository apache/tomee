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
public class AttachC
    extends AttachB {

    private String cstr;
    private int cint;
    private double cdbl;
    private List es = new LinkedList(); // non-DFG

    public void setCstr(String cstr) {
        this.cstr = cstr;
    }

    public String getCstr() {
        return this.cstr;
    }

    public void setCint(int cint) {
        this.cint = cint;
    }

    public int getCint() {
        return this.cint;
    }

    public void setCdbl(double cdbl) {
        this.cdbl = cdbl;
    }

    public double getCdbl() {
        return this.cdbl;
    }

    public void setEs(List es) {
        this.es = es;
    }

    public List getEs() {
        return this.es;
    }
}
