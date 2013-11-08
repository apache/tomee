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

import java.util.ArrayList;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="RuntimeTest4k")
public class RuntimeTest4 {

    private String name;

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
    private Collection<RuntimeTest5> runtimeTest5s = new ArrayList();

    protected RuntimeTest4() {
        this("?");
    }
    
    public RuntimeTest4(String str) {
        name = str;
    }

    public void setName(String val) {
        name = val;
    }

    public String getName() {
        return name;
    }

    public Collection getRuntimeTest5s() {
        return runtimeTest5s;
    }

    public void setRuntimeTest5s(Collection c) {
        runtimeTest5s = c;
    }
}
