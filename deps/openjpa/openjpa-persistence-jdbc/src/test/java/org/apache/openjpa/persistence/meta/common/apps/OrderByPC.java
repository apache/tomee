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
package org.apache.openjpa.persistence.meta.common.apps;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;

import org.apache.openjpa.persistence.PersistentCollection;

@Entity
public class OrderByPC {

    private long id;

    private List stringListAsc = new ArrayList();
    @PersistentCollection
    private int[] intArrayDesc;
    private List orderByPKAsc = new ArrayList();
    private List orderByStringDesc = new ArrayList();
    private List orderByStringAndPKDesc = new ArrayList();

    private List oneToManyAsc = new ArrayList();

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List getStringListAsc() {
        return this.stringListAsc;
    }

    public int[] getIntArrayDesc() {
        return this.intArrayDesc;
    }

    public void setIntArrayDesc(int[] intArrayDesc) {
        this.intArrayDesc = intArrayDesc;
    }

    public List getOrderByPKAsc() {
        return this.orderByPKAsc;
    }

    public List getOrderByStringDesc() {
        return this.orderByStringDesc;
    }

    public List getOrderByStringAndPKDesc() {
        return this.orderByStringAndPKDesc;
    }

    public List getOneToManyAsc() {
        return this.oneToManyAsc;
    }
}
