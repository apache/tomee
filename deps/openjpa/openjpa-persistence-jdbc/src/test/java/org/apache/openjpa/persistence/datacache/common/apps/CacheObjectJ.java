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
package org.apache.openjpa.persistence.datacache.common.apps;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

/**
 * Used in testing; should be enhanced.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYP")
public class CacheObjectJ {

    private String str = null;
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private CacheObjectE e = null;

    public CacheObjectJ() {
    }

    public CacheObjectJ(String s, CacheObjectE e) {
        this.str = s;
        this.e = e;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String s) {
        this.str = s;
    }

    //@Embedded()
    public CacheObjectE getE() {
        return e;
    }
}
