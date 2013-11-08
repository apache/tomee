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

import java.math.BigInteger;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.openjpa.persistence.FetchAttribute;
import org.apache.openjpa.persistence.FetchGroup;
import org.apache.openjpa.persistence.FetchGroups;
import org.apache.openjpa.persistence.LoadFetchGroup;

@Entity
@Table(name = "FETCH_GRP_TOBJ")
@FetchGroups({
@FetchGroup(name = "g1", attributes = {
@FetchAttribute(name = "b"),
@FetchAttribute(name = "c"),
@FetchAttribute(name = "d")
    }),
@FetchGroup(name = "g2", attributes = {
@FetchAttribute(name = "e"),
@FetchAttribute(name = "f"),
@FetchAttribute(name = "g")
    })
    })
public class FetchGroupTestObject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private int a;

    @Basic(fetch = FetchType.LAZY)
    @LoadFetchGroup("g1")
    private String b;

    @Basic(fetch = FetchType.LAZY)
    @LoadFetchGroup("g1")
    private BigInteger c;

    @Basic(fetch = FetchType.LAZY)
    @LoadFetchGroup("g1")
    @Temporal(TemporalType.DATE)
    private Date d;

    @Basic(fetch = FetchType.LAZY)
    @LoadFetchGroup("g2")
    private String e;

    @Basic(fetch = FetchType.LAZY)
    @LoadFetchGroup("g2")
    private String f;

    @OneToOne(cascade = { CascadeType.PERSIST }, fetch = FetchType.LAZY)
    @LoadFetchGroup("g2")
    private FetchGroupTestObject g;

    @OneToOne(cascade = { CascadeType.PERSIST })
    private FetchGroupTestObject h;

    public int getId() {
        return this.id;
    }

    public void setA(int val) {
        a = val;
    }

    public int getA() {
        return a;
    }

    public void setB(String val) {
        b = val;
    }

    public String getB() {
        return b;
    }

    public void setC(BigInteger val) {
        c = val;
    }

    public BigInteger getC() {
        return c;
    }

    public void setD(Date val) {
        d = val;
    }

    public Date getD() {
        return d;
    }

    public void setE(String val) {
        e = val;
    }

    public String getE() {
        return e;
    }

    public void setF(String val) {
        f = val;
    }

    public String getF() {
        return f;
    }

    public void setG(FetchGroupTestObject val) {
        g = val;
    }

    public FetchGroupTestObject getG() {
        return g;
    }

    public void setH(FetchGroupTestObject val) {
        h = val;
    }

    public FetchGroupTestObject getH() {
        return h;
    }
}
