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
package org.apache.openjpa.persistence.criteria.results;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * A simple class for testing select projections.
 * 
 * @author Pinaki Poddar
 *
 */

@Entity
public class Foo {
    @Id
    private long fid;
    private String fstring;
    private long flong;
    private int fint;
    @OneToOne
    private Bar bar;
    
    protected Foo() {
        
    }
    
    public Foo(long fid, long flong, int fint) {
        super();
        this.fid = fid;
        this.flong = flong;
        this.fint = fint;
    }

    public Foo(long fid, String fstring) {
        super();
        this.fid = fid;
        this.fstring = fstring;
    }

    public Foo(long fid, int fint) {
        super();
        this.fid = fid;
        this.fint = fint;
    }
    
    public long getFid() {
        return fid;
    }

    public void setFid(long fid) {
        this.fid = fid;
    }

    public String getFstring() {
        return fstring;
    }

    public void setFstring(String fstring) {
        this.fstring = fstring;
    }

    public long getFlong() {
        return flong;
    }

    public void setFlong(long flong) {
        this.flong = flong;
    }

    public int getFint() {
        return fint;
    }

    public void setFint(int fint) {
        this.fint = fint;
    }

    public Bar getBar() {
        return bar;
    }

    public void setBar(Bar bar) {
        this.bar = bar;
    }

}
