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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.openjpa.persistence.DataCache;

@Entity
@DiscriminatorValue("CACHE_CHILD2")
@DataCache(enabled = false)
public class CacheObjectAChild2 extends CacheObjectA {

    private String str2 = null;

    protected CacheObjectAChild2() {
    }

    public CacheObjectAChild2(String s, String name, int age) {
        super(name, age);
        this.str2 = s;
    }

    public String getStr2() {
        return str2;
    }

    public void setStr2(String s) {
        this.str2 = s;
    }
}
