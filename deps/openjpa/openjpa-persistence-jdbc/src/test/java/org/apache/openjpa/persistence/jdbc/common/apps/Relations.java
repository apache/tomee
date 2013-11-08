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

public class Relations {

    private Base base;
    private Base nullBase;
    private BaseSub1 baseSub1;
    private BaseSub1Sub2 baseSub1Sub2;
    private List baseList = new ArrayList();
    private List emptyBaseList = new ArrayList();
    private List baseSub1List = new ArrayList();
    private List baseSub1Sub2List = new ArrayList();

    public Base getBase() {
        return this.base;
    }

    public void setBase(Base base) {
        this.base = base;
    }

    public BaseSub1 getBaseSub1() {
        return this.baseSub1;
    }

    public void setBaseSub1(BaseSub1 baseSub1) {
        this.baseSub1 = baseSub1;
    }

    public BaseSub1Sub2 getBaseSub1Sub2() {
        return this.baseSub1Sub2;
    }

    public void setBaseSub1Sub2(BaseSub1Sub2 baseSub1Sub2) {
        this.baseSub1Sub2 = baseSub1Sub2;
    }

    public List getBaseList() {
        return this.baseList;
    }

    public void setBaseList(List baseList) {
        this.baseList = baseList;
    }

    public List getBaseSub1List() {
        return this.baseSub1List;
    }

    public void setBaseSub1List(List baseSub1List) {
        this.baseSub1List = baseSub1List;
    }

    public List getBaseSub1Sub2List() {
        return this.baseSub1Sub2List;
    }

    public void setBaseSub1Sub2List(List baseSub1Sub2List) {
        this.baseSub1Sub2List = baseSub1Sub2List;
    }

    public Base getNullBase() {
        return this.nullBase;
    }

    public void setNullBase(Base nullBase) {
        this.nullBase = nullBase;
    }

    public List getEmptyBaseList() {
        return this.emptyBaseList;
    }

    public void setEmptyBaseList(List emptyBaseList) {
        this.emptyBaseList = emptyBaseList;
    }
}
