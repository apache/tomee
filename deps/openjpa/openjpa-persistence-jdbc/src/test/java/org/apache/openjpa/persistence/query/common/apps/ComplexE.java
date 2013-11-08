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
package org.apache.openjpa.persistence.query.common.apps;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class ComplexE
    extends ComplexD {

    private String stringE;
    private int intE;
    @Temporal(TemporalType.DATE)
    private Date dateE;
    private Collection fs = new LinkedList();
    @OneToOne(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    private ComplexD d;

    public ComplexE() {

    }

    public ComplexE(String stringE, int intE, Date dateE, ComplexF[] fs,
        ComplexD d) {
        this.stringE = stringE;
        this.intE = intE;
        this.dateE = dateE;
        if (fs != null)
            this.fs.addAll(Arrays.asList(fs));
        this.d = d;
    }

    public void setStringE(String stringE) {
        this.stringE = stringE;
    }

    public String getStringE() {
        return this.stringE;
    }

    public void setIntE(int intE) {
        this.intE = intE;
    }

    public int getIntE() {
        return this.intE;
    }

    public void setDateE(Date dateE) {
        this.dateE = dateE;
    }

    public Date getDateE() {
        return this.dateE;
    }

    public void setFs(Collection fs) {
        this.fs = fs;
    }

    public Collection getFs() {
        return this.fs;
    }

    public void setD(ComplexD d) {
        this.d = d;
    }

    public ComplexD getD() {
        return this.d;
    }
}

