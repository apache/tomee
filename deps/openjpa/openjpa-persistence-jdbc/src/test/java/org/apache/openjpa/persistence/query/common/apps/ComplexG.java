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

import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class ComplexG
    extends ComplexE {

    private String stringG;
    private int intG;
    @Temporal(TemporalType.DATE)
    private Date dateG;
    @OneToOne(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    private ComplexF f;

    public ComplexG() {

    }

    public ComplexG(String stringG, int intG, Date dateG, ComplexF f) {
        this.stringG = stringG;
        this.intG = intG;
        this.dateG = dateG;
        this.f = f;
    }

    public void setStringG(String stringG) {
        this.stringG = stringG;
    }

    public String getStringG() {
        return this.stringG;
    }

    public void setIntG(int intG) {
        this.intG = intG;
    }

    public int getIntG() {
        return this.intG;
    }

    public void setDateG(Date dateG) {
        this.dateG = dateG;
    }

    public Date getDateG() {
        return this.dateG;
    }

    public void setF(ComplexF f) {
        this.f = f;
    }

    public ComplexF getF() {
        return this.f;
    }
}

