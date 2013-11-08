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
package org.apache.openjpa.persistence.datacache;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.persistence.GeneratedValue;

import javax.persistence.Table;

@Entity
@Table(name="OPTIMISTIC_LOCK_INSTANCE")
public class OptimisticLockInstance {
    @Id @GeneratedValue 
    private int pk;

    @Version 
    private int oplock;

    private String str;
    private int intField;

    protected OptimisticLockInstance() { }

    public OptimisticLockInstance(String str) {
        this.str = str;
    }

    public int getPK() {
        return pk;
    }

    public int getOpLock() {
        return oplock;
    }

    public String getStr() {
        return str;
    }
}
  
